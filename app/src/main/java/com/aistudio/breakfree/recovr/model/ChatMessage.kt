package com.aistudio.breakfree.recovr.model

data class ChatMessage(
    val id: String,
    val isFromUser: Boolean,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val quickPrompts: List<String> = emptyList(),
    /**
     * True when the text is pre-written offline guidance rather than a model answer.
     * The UI labels these messages so they are never mistaken for personalised advice.
     */
    val isFallback: Boolean = false
)
