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
 * Verifies that a configured endpoint/key/model actually answers, so the user's key is
 * checked before it is saved.
 */
object AiModelFetcher {

    private const val TIMEOUT_MS = 15_000

    /**
     * Makes a tiny generation request to verify the configured endpoint/key/model actually
     * work. Returns a short success message, or throws with the provider's error.
     */
    suspend fun testConnection(config: AiProviderConfig): String = withContext(Dispatchers.IO) {
        withTimeout(TIMEOUT_MS.toLong()) {
            testGemini(config)
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
