package com.example.ai_dialogue_assistant.backEnd

import android.net.Uri

data class Message(
    val sender: String,
    val message: String,
    val timestamp: String,
    val imageUri: Uri? = null
)


