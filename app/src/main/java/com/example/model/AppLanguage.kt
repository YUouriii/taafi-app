package com.example.model

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val isRtl: Boolean) {
    ENGLISH("en", "English", "English", false),
    ARABIC("ar", "Arabic", "العربية", true)
}
