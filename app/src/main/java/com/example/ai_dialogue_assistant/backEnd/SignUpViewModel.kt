package com.example.ai_dialogue_assistant.backEnd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> get() = _authState

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun signUp(email: String, password: String, confirmPassword: String) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }

        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        _authState.value = AuthState.Success(user?.email)
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Sign Up Failed")
                    }
                    clearMessageAfterDelay()
                }
        }
    }

    private fun clearMessageAfterDelay() {
        viewModelScope.launch {
            delay(5000)
            _authState.value = AuthState.Idle
        }
    }
}