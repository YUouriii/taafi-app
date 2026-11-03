package com.aistudio.breakfree.recovr.model

/**
 * One of the 30 Rewire micro-lessons: a short, practical piece of recovery
 * education. Each lesson is a 60-second read, not a lecture.
 */
data class RewireLesson(
    val id: Int,
    val title: Triple<String, String, String>,
    val body: Triple<String, String, String>
) {
    fun titleFor(language: AppLanguage): String = when (language) {
        AppLanguage.ARABIC -> title.second
        AppLanguage.FRENCH -> title.third
        AppLanguage.ENGLISH -> title.first
    }

    fun bodyFor(language: AppLanguage): String = when (language) {
        AppLanguage.ARABIC -> body.second
        AppLanguage.FRENCH -> body.third
        AppLanguage.ENGLISH -> body.first
    }
}
