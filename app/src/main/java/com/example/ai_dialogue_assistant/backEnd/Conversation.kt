package com.example.ai_dialogue_assistant.backEnd



data class Conversation(
    val userId: String,
    val topic: String,
    val language: String,
    val conversationId: String,
    val messages: List<Message>,
    val createdAt: String,
    val updatedAt: String
)