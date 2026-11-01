package com.aistudio.breakfree.recovr.ai

import android.util.Log
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.DailyCheckIn
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
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
    val isFallback: Boolean,
    val error: String? = null
)

/**
 * The recovery counselor, backed by whichever model the user configured.
 *
 * Supports three request shapes (see [AiProviderKind]):
 *  1. Gemini `generateContent` (x-goog-api-key).
 *  2. Any OpenAI-compatible `/chat/completions` endpoint (Bearer key).
 *  3. Anthropic `/messages` (x-api-key).
 *
 * If no key is configured, or the call fails, the offline neuroscience-backed counselor
 * answers instead — the chat never dies and never invents a model response.
 */
class AiRecoveryCounselor(
    private val getRecentCheckIns: () -> List<DailyCheckIn>,
    private val getConfig: () -> AiProviderConfig
) {

    /** Remembered once a Gemini model answers, so we stop probing the alternatives. */
    private var workingGeminiModel: String? = null

    /** Unified conversation history: "user" / "assistant". */
    private val history = ArrayDeque<Pair<String, String>>()

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
                Log.w(TAG, "The model returned an empty answer; using offline guidance")
                CounselorReply(
                    offlineReply(userMessage, cleanDays, language),
                    isFallback = true,
                    error = "The model returned an empty answer."
                )
            } else {
                rememberTurn(userMessage, text)
                CounselorReply(text, isFallback = false)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Model generation failed: ${e.message}", e)
            CounselorReply(
                offlineReply(userMessage, cleanDays, language),
                isFallback = true,
                error = e.message ?: e::class.simpleName
            )
        }
    }

    private suspend fun offlineReply(
        userMessage: String,
        cleanDays: Long,
        language: AppLanguage
    ): String = OfflineCounselor.generateCounselorResponse(userMessage, cleanDays, language)

    private suspend fun generate(userMessage: String, systemPrompt: String): String {
        val config = getConfig()

        if (!config.isConfigured) {
            // Gemini has a second, key-less route through Firebase AI Logic (App Check
            // protected). The other providers do not, so they simply stay offline.
            if (config.kind == AiProviderKind.GEMINI) {
                return generateWithFirebase(userMessage, systemPrompt)
            }
            throw IllegalStateException("No API key configured for ${config.kind.titleEn}")
        }

        return when (config.kind) {
            AiProviderKind.GEMINI -> generateWithGemini(config, systemPrompt, userMessage)
            AiProviderKind.OPENAI_COMPATIBLE -> generateWithOpenAiCompatible(config, systemPrompt, userMessage)
            AiProviderKind.ANTHROPIC -> generateWithAnthropic(config, systemPrompt, userMessage)
        }
    }

    // ---------------------------------------------------------------------------------
    // Gemini
    // ---------------------------------------------------------------------------------

    private suspend fun generateWithGemini(
        config: AiProviderConfig,
        systemPrompt: String,
        userMessage: String
    ): String {
        val candidates = if (config.model.isBlank()) {
            val preferred = workingGeminiModel
            if (preferred != null) {
                listOf(preferred) + GEMINI_MODEL_CANDIDATES.filter { it != preferred }
            } else {
                GEMINI_MODEL_CANDIDATES
            }
        } else {
            listOf(config.effectiveModel)
        }

        var lastError: Exception? = null
        for (modelName in candidates) {
            try {
                val result = callGemini(
                    config = config,
                    modelName = modelName,
                    systemPrompt = systemPrompt,
                    userMessage = userMessage
                )
                if (result.isNotBlank()) {
                    workingGeminiModel = modelName
                    Log.i(TAG, "Reply generated by Gemini ($modelName)")
                    return result
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Gemini model '$modelName' failed: ${e.message}")
                lastError = e
                if (workingGeminiModel == modelName) workingGeminiModel = null
            }
        }

        // Last resort for Google users: the App Check protected Firebase route.
        return try {
            generateWithFirebase(userMessage, systemPrompt)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw lastError ?: e
        }
    }

    private suspend fun callGemini(
        config: AiProviderConfig,
        modelName: String,
        systemPrompt: String,
        userMessage: String
    ): String {
        val url = "${config.effectiveBaseUrl}/models/$modelName:generateContent"

        val contents = JSONArray()
        for ((role, text) in history) {
            contents.put(
                JSONObject().apply {
                    put("role", if (role == "user") "user" else "model")
                    put("parts", JSONArray().apply { put(JSONObject().put("text", text)) })
                }
            )
        }
        contents.put(
            JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply { put(JSONObject().put("text", userMessage)) })
            }
        )

        val body = JSONObject().apply {
            put(
                "system_instruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", systemPrompt))
                )
            )
            put("contents", contents)
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", MAX_OUTPUT_TOKENS)
                }
            )
        }

        val response = postJson(
            url = url,
            headers = mapOf("x-goog-api-key" to config.apiKey),
            body = body.toString()
        )

        val candidates = JSONObject(response).optJSONArray("candidates")
            ?: throw RuntimeException("No candidates returned by Gemini")
        if (candidates.length() == 0) {
            throw RuntimeException("Empty candidates list from Gemini")
        }

        val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
        val text = StringBuilder()
        for (i in 0 until parts.length()) {
            val part = parts.optJSONObject(i) ?: continue
            if (part.has("text")) text.append(part.getString("text"))
        }
        return text.toString()
    }

    /** Firebase AI Logic route: only used when Gemini is selected and no key is set. */
    private suspend fun generateWithFirebase(userMessage: String, systemPrompt: String): String {
        val firebaseHistory = history.map { (role, text) ->
            if (role == "user") content("user") { text(text) } else content("model") { text(text) }
        }

        var lastError: Exception? = null
        for (name in GEMINI_MODEL_CANDIDATES) {
            try {
                val model = Firebase.ai.generativeModel(
                    modelName = name,
                    systemInstruction = content { text(systemPrompt) }
                )
                val chat = model.startChat(history = firebaseHistory)
                val text = chat.sendMessage(userMessage).text
                if (!text.isNullOrBlank()) {
                    Log.i(TAG, "Reply generated by Firebase AI Logic ($name)")
                    return text
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Firebase AI model '$name' failed: ${e.message}")
                lastError = e
            }
        }
        throw lastError ?: IllegalStateException("No model available")
    }

    // ---------------------------------------------------------------------------------
    // OpenAI-compatible (OpenAI, OpenRouter, Groq, DeepSeek, Mistral, Ollama, …)
    // ---------------------------------------------------------------------------------

    private suspend fun generateWithOpenAiCompatible(
        config: AiProviderConfig,
        systemPrompt: String,
        userMessage: String
    ): String {
        val url = if (config.effectiveBaseUrl.endsWith("/chat/completions")) {
            config.effectiveBaseUrl
        } else {
            "${config.effectiveBaseUrl}/chat/completions"
        }

        val messages = JSONArray()
        messages.put(JSONObject().apply { put("role", "system"); put("content", systemPrompt) })
        for ((role, text) in history) {
            messages.put(
                JSONObject().apply {
                    put("role", if (role == "user") "user" else "assistant")
                    put("content", text)
                }
            )
        }
        messages.put(JSONObject().apply { put("role", "user"); put("content", userMessage) })

        val body = JSONObject().apply {
            put("model", config.effectiveModel)
            put("messages", messages)
            put("temperature", 0.7)
            put("max_tokens", MAX_OUTPUT_TOKENS)
            put("stream", false)
        }

        val response = postJson(
            url = url,
            headers = mapOf("Authorization" to "Bearer ${config.apiKey}"),
            body = body.toString()
        )

        val choices = JSONObject(response).optJSONArray("choices")
            ?: throw RuntimeException("No choices returned by the provider")
        if (choices.length() == 0) throw RuntimeException("Empty choices list")

        val message = choices.getJSONObject(0).optJSONObject("message")
            ?: throw RuntimeException("No message in the provider response")

        // Some compatible servers answer with a parts array instead of a string.
        val contentValue = message.opt("content")
        return when (contentValue) {
            is String -> contentValue
            is JSONArray -> buildString {
                for (i in 0 until contentValue.length()) {
                    val part = contentValue.optJSONObject(i) ?: continue
                    part.optString("text").takeIf { it.isNotBlank() }?.let(::append)
                }
            }
            else -> message.optString("reasoning_content").ifBlank { "" }
        }.also {
            if (it.isBlank()) throw RuntimeException("The provider returned an empty message")
        }
    }

    // ---------------------------------------------------------------------------------
    // Anthropic Claude
    // ---------------------------------------------------------------------------------

    private suspend fun generateWithAnthropic(
        config: AiProviderConfig,
        systemPrompt: String,
        userMessage: String
    ): String {
        val url = if (config.effectiveBaseUrl.endsWith("/messages")) {
            config.effectiveBaseUrl
        } else {
            "${config.effectiveBaseUrl}/messages"
        }

        val messages = JSONArray()
        for ((role, text) in history) {
            messages.put(
                JSONObject().apply {
                    put("role", if (role == "user") "user" else "assistant")
                    put("content", text)
                }
            )
        }
        messages.put(JSONObject().apply { put("role", "user"); put("content", userMessage) })

        val body = JSONObject().apply {
            put("model", config.effectiveModel)
            put("max_tokens", MAX_OUTPUT_TOKENS)
            put("system", systemPrompt)
            put("messages", messages)
            put("temperature", 0.7)
        }

        val response = postJson(
            url = url,
            headers = mapOf(
                "x-api-key" to config.apiKey,
                "anthropic-version" to ANTHROPIC_VERSION
            ),
            body = body.toString()
        )

        val content = JSONObject(response).optJSONArray("content")
            ?: throw RuntimeException("No content returned by Claude")

        val text = StringBuilder()
        for (i in 0 until content.length()) {
            val block = content.optJSONObject(i) ?: continue
            if (block.optString("type") == "text") text.append(block.optString("text"))
        }
        return text.toString()
    }

    // ---------------------------------------------------------------------------------
    // Transport
    // ---------------------------------------------------------------------------------

    /** POSTs JSON and returns the body, or throws with the provider's own error message. */
    private suspend fun postJson(
        url: String,
        headers: Map<String, String>,
        body: String
    ): String = withContext(Dispatchers.IO) {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            headers.forEach { (name, value) -> setRequestProperty(name, value) }
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
        }

        try {
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()

            if (code !in 200..299) {
                val providerMessage = try {
                    JSONObject(responseText).optJSONObject("error")?.optString("message")
                } catch (_: Exception) {
                    null
                }
                throw RuntimeException(
                    "Provider error ($code): ${providerMessage ?: responseText.take(300)}"
                )
            }

            responseText
        } finally {
            conn.disconnect()
        }
    }

    private fun rememberTurn(userMessage: String, modelText: String) {
        history.addLast("user" to userMessage)
        history.addLast("assistant" to modelText)
        while (history.size > MAX_HISTORY_TURNS * 2) {
            history.removeFirst()
            history.removeFirst()
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
        private const val TAG = "AiCounselor"

        /** Only used when the user leaves the Gemini model field blank. */
        private val GEMINI_MODEL_CANDIDATES = listOf(
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-2.5-flash",
            "gemini-1.5-flash-8b"
        )

        private const val ANTHROPIC_VERSION = "2023-06-01"
        private const val MAX_OUTPUT_TOKENS = 1200
        private const val MAX_HISTORY_TURNS = 10
        private const val CONNECT_TIMEOUT_MS = 20_000
        private const val READ_TIMEOUT_MS = 60_000
        private const val REQUEST_TIMEOUT_MS = 75_000L
    }
}
