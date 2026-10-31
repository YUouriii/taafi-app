package com.aistudio.breakfree.recovr.ai

import android.util.Log
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.DailyCheckIn
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/** One counselor answer, and whether it actually came from the model. */
data class CounselorReply(
    val text: String,
    val isFallback: Boolean
)

/**
 * Gemini-backed recovery counselor.
 *
 * Uses the cheapest, fastest free-tier Gemini models (gemini-1.5-flash / gemini-2.0-flash).
 *
 * Supports dual-engine execution:
 *  1. Direct Google Gemini Developer API (via personal API key) for instant, quota-free responses.
 *  2. Firebase AI Logic client (as cloud fallback).
 *  3. Neuroscience-backed offline counselor (only if offline or all engines fail).
 */
class GeminiRecoveryCounselor(
    private val getRecentCheckIns: () -> List<DailyCheckIn>,
    private val getApiKey: () -> String? = { null }
) {

    private val modelCandidates = listOf(
        PRIMARY_MODEL,
        FALLBACK_MODEL_FLASH_8B,
        FALLBACK_MODEL_2_0,
        FALLBACK_MODEL_2_5
    )

    /** Remembered once a model answers, so we stop probing the alternatives. */
    private var workingModel: String? = null

    // Dual history formats
    private val textHistory = ArrayDeque<Pair<String, String>>()
    private val firebaseHistory = ArrayDeque<Content>()

    suspend fun sendChatMessage(
        userMessage: String,
        cleanDays: Long,
        language: AppLanguage
    ): CounselorReply {
        // Safety first: never route a crisis message through a model or a canned tip.
        if (CrisisSupport.isCrisisMessage(userMessage)) {
            return CounselorReply(CrisisSupport.response(language), isFallback = false)
        }

        val systemPrompt = buildSystemPrompt(cleanDays, language)

        return try {
            val text = withTimeout(REQUEST_TIMEOUT_MS) {
                generate(userMessage, systemPrompt)
            }
            if (text.isNullOrBlank()) {
                Log.w(TAG, "Gemini returned an empty answer; using offline guidance")
                CounselorReply(offlineReply(userMessage, cleanDays, language), isFallback = true)
            } else {
                rememberTurn(userMessage, text)
                CounselorReply(text, isFallback = false)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Gemini generation failed: ${e.message}", e)
            CounselorReply(offlineReply(userMessage, cleanDays, language), isFallback = true)
        }
    }

    private suspend fun offlineReply(userMessage: String, cleanDays: Long, language: AppLanguage): String =
        OfflineCounselor.generateCounselorResponse(userMessage, cleanDays, language)

    /**
     * Tries the direct Gemini REST API if an API key is available,
     * otherwise tries Firebase AI Logic with the cheapest free models.
     */
    private suspend fun generate(userMessage: String, systemPrompt: String): String {
        val apiKey = getApiKey()?.trim()

        // 1. Direct Google Generative Language REST API (Google AI Studio free tier)
        if (!apiKey.isNullOrBlank()) {
            val candidates = workingModel
                ?.let { known -> listOf(known) + modelCandidates.filter { it != known } }
                ?: modelCandidates

            for (modelName in candidates) {
                try {
                    val result = callGeminiRestApi(
                        apiKey = apiKey,
                        modelName = modelName,
                        systemPrompt = systemPrompt,
                        userMessage = userMessage,
                        history = textHistory.toList()
                    )
                    if (result.isNotBlank()) {
                        workingModel = modelName
                        Log.i(TAG, "Successfully generated reply using Gemini REST API ($modelName)")
                        return result
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "REST API model '$modelName' failed: ${e.message}")
                    if (workingModel == modelName) workingModel = null
                }
            }
        }

        // 2. Firebase AI Logic fallback
        val candidates = workingModel
            ?.let { known -> listOf(known) + modelCandidates.filter { it != known } }
            ?: modelCandidates
        var lastError: Exception? = null

        for (name in candidates) {
            try {
                val model = Firebase.ai.generativeModel(
                    modelName = name,
                    systemInstruction = content { text(systemPrompt) }
                )
                val chat = model.startChat(history = firebaseHistory.toList())
                val text = chat.sendMessage(userMessage).text
                if (!text.isNullOrBlank()) {
                    workingModel = name
                    Log.i(TAG, "Successfully generated reply using Firebase AI Logic ($name)")
                    return text
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Firebase AI model '$name' failed: ${e.message}")
                lastError = e
                if (workingModel == name) workingModel = null
            }
        }

        throw lastError ?: IllegalStateException("No Gemini model available. Please configure a valid Gemini API key.")
    }

    /**
     * Direct HTTPS REST call to Google's Generative Language API.
     * Uses zero external libraries and connects directly to Gemini free tier.
     */
    private suspend fun callGeminiRestApi(
        apiKey: String,
        modelName: String,
        systemPrompt: String,
        userMessage: String,
        history: List<Pair<String, String>>
    ): String = withContext(Dispatchers.IO) {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-goog-api-key", apiKey)
            connectTimeout = 30_000
            readTimeout = 30_000
            doOutput = true
        }

        val requestJson = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })

            val contentsArray = JSONArray()
            for ((role, text) in history) {
                contentsArray.put(JSONObject().apply {
                    put("role", if (role == "user") "user" else "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    })
                })
            }
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", userMessage) })
                })
            })
            put("contents", contentsArray)

            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 1200)
            })
        }

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(requestJson.toString()) }

        val responseCode = conn.responseCode
        val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
        val responseText = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }

        if (responseCode !in 200..299) {
            val errorMsg = try {
                JSONObject(responseText).getJSONObject("error").getString("message")
            } catch (_: Exception) {
                responseText
            }
            throw RuntimeException("Gemini API error ($responseCode): $errorMsg")
        }

        val responseJson = JSONObject(responseText)
        val candidates = responseJson.optJSONArray("candidates")
            ?: throw RuntimeException("No candidates returned by Gemini")
        if (candidates.length() == 0) {
            throw RuntimeException("Empty candidates list from Gemini")
        }

        val candidate = candidates.getJSONObject(0)
        val content = candidate.getJSONObject("content")
        val parts = content.getJSONArray("parts")
        val textBuilder = StringBuilder()
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            if (part.has("text")) {
                textBuilder.append(part.getString("text"))
            }
        }
        textBuilder.toString()
    }

    private fun rememberTurn(userMessage: String, modelText: String) {
        textHistory.addLast("user" to userMessage)
        textHistory.addLast("model" to modelText)
        firebaseHistory.addLast(content("user") { text(userMessage) })
        firebaseHistory.addLast(content("model") { text(modelText) })

        while (textHistory.size > MAX_HISTORY_TURNS * 2) {
            textHistory.removeFirst()
            textHistory.removeFirst()
        }
        while (firebaseHistory.size > MAX_HISTORY_TURNS * 2) {
            firebaseHistory.removeFirst()
            firebaseHistory.removeFirst()
        }
    }

    private fun buildSystemPrompt(cleanDays: Long, language: AppLanguage): String {
        val isAr = language == AppLanguage.ARABIC
        val recentCheckIns = getRecentCheckIns()

        val checkInContext = if (recentCheckIns.isNotEmpty()) {
            val last3 = recentCheckIns.take(3)
            val summary = last3.joinToString("\n") {
                "- Date: ${it.dateString}, Mood (1-5): ${it.moodScore}, Urge Intensity (0-10): ${it.urgeIntensity}, Emotions: ${it.primaryEmotions.joinToString()}, Triggers: ${it.triggersEncountered.joinToString()}, Reflection: ${it.reflectionNotes}"
            }
            "\n[User's recent daily mood & urge log]:\n$summary\n"
        } else {
            "\n[User's daily log]: no check-ins submitted yet.\n"
        }

        return """
You are the BreakFree / تعافي resilience counselor. You specialise in Cognitive Behavioral Therapy (CBT), Acceptance and Commitment Therapy (ACT), and neuroscience-backed urge surfing for people overcoming compulsive pornography use.

Client profile:
- Current clean streak: $cleanDays days
- Preferred language: ${if (isAr) "Arabic (العربية)" else "English"}
$checkInContext
Guidelines:
1. Deep empathy and neuroscience: frame urges as a natural dopamine readjustment wave, remove shame, and reinforce prefrontal neuroplasticity.
2. Use the mood/urge data above. If urge was high or mood was low recently, point out the pattern gently and offer coping mechanisms that fit it.
3. Be concrete and actionable (4-7-8 breathing, sensory grounding, changing rooms, cold water).
4. Reply only in ${if (isAr) "warm, supportive Arabic" else "motivating, plain English"}.
5. You are a supportive companion, not a clinician. Never diagnose, never prescribe, and never claim to be a human or a therapist.
6. If the user mentions self-harm, suicide or being in danger, stop counseling and tell them to contact local emergency services or a crisis line immediately.
        """.trimIndent()
    }

    companion object {
        private const val TAG = "GeminiCounselor"

        // The cheapest, fastest official free models from Google
        private const val PRIMARY_MODEL = "gemini-1.5-flash"
        private const val FALLBACK_MODEL_FLASH_8B = "gemini-1.5-flash-8b"
        private const val FALLBACK_MODEL_2_0 = "gemini-2.0-flash"
        private const val FALLBACK_MODEL_2_5 = "gemini-2.5-flash"

        private const val REQUEST_TIMEOUT_MS = 45_000L
        private const val MAX_HISTORY_TURNS = 10
    }
}
