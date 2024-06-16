package com.example.ai_dialogue_assistant.frontEnd

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.ai_dialogue_assistant.R
import com.example.ai_dialogue_assistant.backEnd.AuthState
import com.example.ai_dialogue_assistant.backEnd.SignUpViewModel

class SignUpScreen() : Screen {

    @Composable
    override fun Content() {
        val signUpViewModel: SignUpViewModel = viewModel()
        val authState by signUpViewModel.authState.collectAsState()
        val navigator = LocalNavigator.current
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var confirmpasswordVisible by remember { mutableStateOf(false) }


        LaunchedEffect(authState) {
            if (authState is AuthState.Success) {
                navigator?.push(Screen2())
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.email),
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.password),
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = {
                        passwordVisible = !passwordVisible
                    }) {
                        val painter = painterResource(id = if (passwordVisible) R.drawable.eye_on else R.drawable.eye_off)
                        Image(
                            painter = painter,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.password),
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmpasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = {
                        confirmpasswordVisible = !confirmpasswordVisible
                    }) {
                        val painter = painterResource(id = if (confirmpasswordVisible) R.drawable.eye_on else R.drawable.eye_off)
                        Image(
                            painter = painter,
                            contentDescription = if (confirmpasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    signUpViewModel.signUp(email, password, confirmPassword)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Up")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (authState) {
                is AuthState.Loading -> Text("Loading...")
                is AuthState.Success -> Text("Sign Up Successful")
                is AuthState.Error -> Text("Sign Up Failed: ${(authState as AuthState.Error).message}")
                else -> {}
            }
        }
    }
}
