package com.example.ai_dialogue_assistant.frontEnd

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        var passwordVisible by remember { mutableStateOf(false) }


        LaunchedEffect(authState) {
            if (authState is AuthState.Success && (authState as AuthState.Success).message == "Authentication Successful") {
                navigator?.push(Screen2())
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.languagesss),
                contentDescription = "Background Image",
                modifier = Modifier
                    .fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sign In",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = Color(0xFF32BFDB),
                    textDecoration = TextDecoration.Underline
                )


                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = {
                        Text(
                            text = "Email",
                            fontFamily = FontFamily.Serif,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = FontFamily.Serif),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Magenta,
                        unfocusedBorderColor = Color(0xFF00785D)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(
                            text = "Password",
                            fontFamily = FontFamily.Serif,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = FontFamily.Serif),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Magenta,
                        unfocusedBorderColor = Color(0xFF00785D)
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = {
                            passwordVisible = !passwordVisible
                        }) {
                            val painter =
                                painterResource(id = if (passwordVisible) R.drawable.eye_on else R.drawable.eye_off)
                            Image(
                                painter = painter,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { signInViewModel.signIn(email, password) },
                    colors = ButtonDefaults.buttonColors(Color(0xFFFDC323))
                ) {
                    Text(
                        text = "Login",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Don't have an account? Click here to Sign Up!",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF539BA9),
                    modifier = Modifier.clickable {
                        navigator?.push(SignUpScreen())
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Reset Password",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF539BA9),
                    modifier = Modifier.clickable {
                        signInViewModel.resetPassword(email)
                    }
                )

                when (authState) {
                    is AuthState.Loading -> Text(
                        text = "Loading...",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraLight,
                        color = Color(0xFF539BA9)
                    )

                    is AuthState.Success -> Text(
                        text = (authState as AuthState.Success).message ?: "Success",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraLight,
                        color = Color(0xFF539BA9)
                    )

                    is AuthState.Error -> Text(
                        text = "Authentication Failed: ${(authState as AuthState.Error).message}",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraLight,
                        color = Color(0xFF539BA9)
                    )

                    else -> {}
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GPreview() {
        Content()
    }

}
