package com.example.model

data class ChatMessage(
    val id: String,
    val isFromUser: Boolean,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val quickPrompts: List<String> = emptyList()
)
