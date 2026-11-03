package com.aistudio.breakfree.recovr.ai

import com.aistudio.breakfree.recovr.model.AppLanguage

/**
 * The shape of the endpoint the counselor talks to. Only Google Gemini is supported now:
 * users sign in with Google (Firebase AI Logic) or paste an AI Studio key; the offline
 * neuroscience counselor answers if neither works.
 */
enum class AiProviderKind(
    val key: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val keyPageUrl: String,
    val titleEn: String,
    val titleAr: String,
    val titleFr: String = titleEn
) {
    GEMINI(
        key = "gemini",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta",
        defaultModel = "gemini-3.5-flash-lite",
        keyPageUrl = "https://aistudio.google.com/app/apikey",
        titleEn = "Google Gemini",
        titleAr = "Google Gemini"
    );

    fun title(language: AppLanguage): String = when (language) {
        AppLanguage.ARABIC -> titleAr
        AppLanguage.FRENCH -> titleFr
        else -> titleEn
    }

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

    /** Shown under the chat header, e.g. "Google Gemini · gemini-3.5-flash-lite". */
    fun label(language: AppLanguage): String = "${kind.title(language)} · $effectiveModel"
}
