package com.example.ai_dialogue_assistant.models

data class ChatMessages (
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val language: String = "",
    val type: String = "", // "user" or "ai"
    val topic: String = ""
)