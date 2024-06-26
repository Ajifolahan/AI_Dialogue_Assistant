package com.example.ai_dialogue_assistant.backEnd

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String?) : AuthState()
    data class Error(val message: String) : AuthState()
}
