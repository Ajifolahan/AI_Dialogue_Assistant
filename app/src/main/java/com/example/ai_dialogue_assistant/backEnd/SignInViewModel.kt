package com.example.ai_dialogue_assistant.backEnd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SignInViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> get() = _authState

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun signIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Success("Authentication Successful")
                    } else {
                        val errorMessage = when (task.exception) {
                            is FirebaseAuthInvalidUserException -> "No account found with this email."
                            is FirebaseAuthInvalidCredentialsException -> "Invalid password."
                            else -> "Authentication failed: ${task.exception?.message}"
                        }
                        _authState.value = AuthState.Error(errorMessage)
                    }
                    clearMessageAfterDelay()
                }
        }
    }

    fun resetPassword(email: String) {
        if (email.isEmpty()) {
            _authState.value = AuthState.Error("Email cannot be empty")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Success("Reset link sent to your email. If you don't see it, you do not have an account with us.")
                    } else {
                        val errorMessage = when (task.exception) {
                            is FirebaseAuthInvalidUserException -> "No account found with this email."
                            else -> "Failed to send reset link: ${task.exception?.message}"
                        }
                        _authState.value = AuthState.Error(errorMessage)
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
