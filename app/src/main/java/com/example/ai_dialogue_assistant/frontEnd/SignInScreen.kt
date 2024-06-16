package com.example.ai_dialogue_assistant.frontEnd

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import com.example.ai_dialogue_assistant.R
import com.example.ai_dialogue_assistant.backEnd.AuthState
import com.example.ai_dialogue_assistant.backEnd.SignInViewModel
import cafe.adriel.voyager.navigator.LocalNavigator


class SignInScreen() : Screen {

    @Composable
    override fun Content() {
        val signInViewModel: SignInViewModel = viewModel()
        val authState by signInViewModel.authState.collectAsState()
        val navigator = LocalNavigator.current
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.languagesss),
                contentDescription = null,
                modifier = Modifier.size(128.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    signInViewModel.signIn(email, password)
                    if (authState is AuthState.Success) {
                        navigator?.push(startScreen())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign In")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Don't have an account? Click here to Sign Up!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    navigator?.push(SignUpScreen())
                }
            )

            when (authState) {
                is AuthState.Loading -> Text("Loading...")
                is AuthState.Success -> Text("Authentication Successful")
                is AuthState.Error -> Text("Authentication Failed: ${(authState as AuthState.Error).message}")
                else -> {}
            }
        }
    }
}
