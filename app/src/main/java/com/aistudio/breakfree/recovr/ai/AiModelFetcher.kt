package com.aistudio.breakfree.recovr.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Discovers the models a provider exposes, so the user never has to type a model id
 * by hand. Each provider family has a different endpoint/shape, but all of them answer
 * a simple GET with the configured credentials.
 *
 * Returns the model ids sorted, or throws if the endpoint is unreachable / unsupported.
 * Some providers (local servers, very old endpoints) simply do not implement /models:
 * callers should fall back to the manual text field in that case.
 */
object AiModelFetcher {

    private const val TAG = "AiModelFetcher"
    private const val TIMEOUT_MS = 15_000

    suspend fun fetchModels(config: AiProviderConfig): List<String> = withContext(Dispatchers.IO) {
        withTimeout(TIMEOUT_MS.toLong()) {
            when (config.kind) {
                AiProviderKind.GEMINI -> fetchGemini(config)
                AiProviderKind.OPENAI_COMPATIBLE -> fetchOpenAiCompatible(config)
                AiProviderKind.ANTHROPIC -> fetchAnthropic(config)
            }
        }
    }

    private fun fetchGemini(config: AiProviderConfig): List<String> {
        val url = "${config.effectiveBaseUrl}/models?key=${config.apiKey}"
        val response = get(url, emptyMap())
        val models = JSONObject(response).optJSONArray("models") ?: JSONArray()
        val ids = mutableListOf<String>()
        for (i in 0 until models.length()) {
            val name = models.optJSONObject(i)?.optString("name").orEmpty()
            // "models/gemini-2.0-flash" -> "gemini-2.0-flash"
            val id = name.substringAfter("models/", name).trim()
            if (id.isNotBlank()) ids.add(id)
        }
        return ids.sorted()
    }

    private fun fetchOpenAiCompatible(config: AiProviderConfig): List<String> {
        val base = config.effectiveBaseUrl
        val url = if (base.endsWith("/models")) base else "$base/models"
        val response = get(url, mapOf("Authorization" to "Bearer ${config.apiKey}"))
        val data = JSONObject(response).optJSONArray("data") ?: JSONArray()
        val ids = mutableListOf<String>()
        for (i in 0 until data.length()) {
            val id = data.optJSONObject(i)?.optString("id").orEmpty()
            if (id.isNotBlank()) ids.add(id)
        }
        // Some compatible servers return { models: [ { id } ] } instead.
        if (ids.isEmpty()) {
            val models = JSONObject(response).optJSONArray("models") ?: JSONArray()
            for (i in 0 until models.length()) {
                val obj = models.optJSONObject(i)
                val id = obj?.optString("id").orEmpty().ifBlank { obj?.optString("name").orEmpty() }
                if (id.isNotBlank()) ids.add(id)
            }
        }
        return ids.sorted()
    }

    private fun fetchAnthropic(config: AiProviderConfig): List<String> {
        val base = config.effectiveBaseUrl
        val url = if (base.endsWith("/models")) base else "$base/models"
        val response = get(
            url,
            mapOf(
                "x-api-key" to config.apiKey,
                "anthropic-version" to "2023-06-01"
            )
        )
        val data = JSONObject(response).optJSONArray("data") ?: JSONArray()
        val ids = mutableListOf<String>()
        for (i in 0 until data.length()) {
            val id = data.optJSONObject(i)?.optString("id").orEmpty()
            if (id.isNotBlank()) ids.add(id)
        }
        return ids.sorted()
    }

    /**
     * Makes a tiny generation request to verify the configured endpoint/key/model actually
     * work. Returns a short success message, or throws with the provider's error.
     */
    suspend fun testConnection(config: AiProviderConfig): String = withContext(Dispatchers.IO) {
        withTimeout(TIMEOUT_MS.toLong()) {
            when (config.kind) {
                AiProviderKind.GEMINI -> testGemini(config)
                AiProviderKind.OPENAI_COMPATIBLE -> testOpenAiCompatible(config)
                AiProviderKind.ANTHROPIC -> testAnthropic(config)
            }
        }
    }

    private fun testGemini(config: AiProviderConfig): String {
        val url = "${config.effectiveBaseUrl}/models/${config.effectiveModel}:generateContent?key=${config.apiKey}"
        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply { put(JSONObject().put("text", "Reply with OK")) })
                })
            })
            put("generationConfig", JSONObject().apply { put("maxOutputTokens", 8) })
        }
        val response = post(url, emptyMap(), body.toString())
        val text = JSONObject(response)
            .optJSONArray("candidates")?.optJSONObject(0)
            ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)
            ?.optString("text").orEmpty()
        if (text.isBlank()) throw RuntimeException("Empty response from the model.")
        return "OK — ${config.effectiveModel}"
    }

    private fun testOpenAiCompatible(config: AiProviderConfig): String {
        val base = config.effectiveBaseUrl
        val url = if (base.endsWith("/chat/completions")) base else "$base/chat/completions"
        val body = JSONObject().apply {
            put("model", config.effectiveModel)
            put("messages", JSONArray().apply {
                put(JSONObject().apply { put("role", "user"); put("content", "Reply with OK") })
            })
            put("max_tokens", 8)
            put("temperature", 0.0)
        }
        val response = post(url, mapOf("Authorization" to "Bearer ${config.apiKey}"), body.toString())
        val text = JSONObject(response)
            .optJSONArray("choices")?.optJSONObject(0)
            ?.optJSONObject("message")?.optString("content").orEmpty()
        if (text.isBlank()) throw RuntimeException("Empty response from the model.")
        return "OK — ${config.effectiveModel}"
    }

    private fun testAnthropic(config: AiProviderConfig): String {
        val base = config.effectiveBaseUrl
        val url = if (base.endsWith("/messages")) base else "$base/messages"
        val body = JSONObject().apply {
            put("model", config.effectiveModel)
            put("max_tokens", 8)
            put("messages", JSONArray().apply {
                put(JSONObject().apply { put("role", "user"); put("content", "Reply with OK") })
            })
        }
        val response = post(
            url,
            mapOf("x-api-key" to config.apiKey, "anthropic-version" to "2023-06-01"),
            body.toString()
        )
        val content = JSONObject(response).optJSONArray("content")
        val text = (0 until (content?.length() ?: 0))
            .mapNotNull { content?.optJSONObject(it)?.optString("text") }
            .firstOrNull().orEmpty()
        if (text.isBlank()) throw RuntimeException("Empty response from the model.")
        return "OK — ${config.effectiveModel}"
    }

    private fun post(url: String, headers: Map<String, String>, body: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            headers.forEach { (name, value) -> setRequestProperty(name, value) }
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            doOutput = true
        }
        try {
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val providerMessage = try {
                    JSONObject(text).optJSONObject("error")?.optString("message")
                } catch (_: Exception) {
                    null
                }
                throw RuntimeException("Provider error ($code): ${providerMessage ?: text.take(200)}")
            }
            return text
        } finally {
            conn.disconnect()
        }
    }
    private fun get(url: String, headers: Map<String, String>): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            headers.forEach { (name, value) -> setRequestProperty(name, value) }
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
        }
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val providerMessage = try {
                    JSONObject(text).optJSONObject("error")?.optString("message")
                } catch (_: Exception) {
                    null
                }
                throw RuntimeException(
                    "Provider error ($code): ${providerMessage ?: text.take(200)}"
                )
            }
            return text
        } finally {
            conn.disconnect()
        }
    }
}
