package com.aistudio.breakfree.recovr.ai

import com.aistudio.breakfree.recovr.model.AppLanguage

/**
 * The shape of the endpoint the counselor talks to. Three families cover almost every
 * hosted and local model:
 *
 *  - [GEMINI] Google AI Studio / Vertex keys.
 *  - [OPENAI_COMPATIBLE] anything that speaks `/chat/completions`: OpenAI, OpenRouter,
 *    Groq, DeepSeek, Mistral, Together, xAI, Ollama, LM Studio, vLLM …
 *  - [ANTHROPIC] Claude's `/messages` API.
 *
 * Every field except the key has a working default, so "pick a provider, paste a key"
 * is enough — but all of them stay editable for self-hosted endpoints.
 */
enum class AiProviderKind(
    val key: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val keyPageUrl: String,
    val titleEn: String,
    val titleAr: String
) {
    GEMINI(
        key = "gemini",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta",
        defaultModel = "gemini-2.0-flash",
        keyPageUrl = "https://aistudio.google.com/app/apikey",
        titleEn = "Google Gemini",
        titleAr = "Google Gemini"
    ),
    OPENAI_COMPATIBLE(
        key = "openai",
        defaultBaseUrl = "https://api.openai.com/v1",
        defaultModel = "gpt-4o-mini",
        keyPageUrl = "https://platform.openai.com/api-keys",
        titleEn = "OpenAI-compatible",
        titleAr = "متوافق مع OpenAI"
    ),
    ANTHROPIC(
        key = "anthropic",
        defaultBaseUrl = "https://api.anthropic.com/v1",
        defaultModel = "claude-3-5-haiku-latest",
        keyPageUrl = "https://console.anthropic.com/settings/keys",
        titleEn = "Anthropic Claude",
        titleAr = "Anthropic Claude"
    );

    fun title(language: AppLanguage): String =
        if (language == AppLanguage.ARABIC) titleAr else titleEn

    companion object {
        fun fromKey(key: String?): AiProviderKind =
            entries.firstOrNull { it.key == key } ?: GEMINI
    }
}

/**
 * What the user configured. A blank key is a valid state: it means "no model", and the
 * counselor answers from its offline guidance instead of pretending to call anything.
 */
data class AiProviderConfig(
    val kind: AiProviderKind = AiProviderKind.GEMINI,
    val baseUrl: String = "",
    val model: String = "",
    val apiKey: String = ""
) {
    val effectiveBaseUrl: String
        get() = baseUrl.trim().ifBlank { kind.defaultBaseUrl }.trimEnd('/')

    val effectiveModel: String
        get() = model.trim().ifBlank { kind.defaultModel }

    val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    /** Shown under the chat header, e.g. "Google Gemini · gemini-2.0-flash". */
    fun label(language: AppLanguage): String = "${kind.title(language)} · $effectiveModel"
}
