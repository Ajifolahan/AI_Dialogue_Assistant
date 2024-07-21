package com.example.ai_dialogue_assistant.frontEnd

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.ai_dialogue_assistant.R
import com.example.ai_dialogue_assistant.backEnd.AuthState
import com.example.ai_dialogue_assistant.backEnd.SignUpViewModel

class SignUpScreen() : Screen {

    @Composable
    @OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    override fun Content() {
        val signUpViewModel: SignUpViewModel = viewModel()
        val authState by signUpViewModel.authState.collectAsState()
        val navigator = LocalNavigator.current
        val keyboardController = LocalSoftwareKeyboardController.current
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


        Box(
            modifier = Modifier.fillMaxSize().clickable { keyboardController?.hide() },

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
                    text = "Sign Up",
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
                    label = {
                        Text(
                            text = "Confirm Password",
                            fontFamily = FontFamily.Serif,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = FontFamily.Serif),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Magenta,
                        unfocusedBorderColor = Color(0xFF00785D)
                    ),
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
                    colors = ButtonDefaults.buttonColors(Color(0xFFFDC323))
                ) {
                    Text(
                        text = "Create an Account",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (authState) {
                    is AuthState.Loading -> Text(
                        text = "Loading...",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraLight,
                        color = Color(0xFF539BA9)
                    )
                    is AuthState.Success -> Text(
                        text =  "Sign Up Successful",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraLight,
                        color = Color(0xFF539BA9)
                    )
                    is AuthState.Error -> Text(
                        text = "Sign Up Failed: ${(authState as AuthState.Error).message}",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraLight,
                        color = Color(0xFF539BA9))
                    else -> {}
                }
            }
        }
    }
}
