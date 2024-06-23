package com.example.ai_dialogue_assistant.frontEnd

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.ai_dialogue_assistant.R

class startScreen: Screen {
    @Composable
    override fun Content() {
        val modifier = Modifier
        val navigator = LocalNavigator.current

        val animationState = remember {mutableFloatStateOf(0f) }
        LaunchedEffect(key1 = true) {
            animationState.floatValue = 1f
        }
        val animationValue by animateFloatAsState(
            targetValue = animationState.floatValue,
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
        )

        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            Column(modifier = modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "LangAI",
                        fontFamily = FontFamily.Serif,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = modifier.paddingFromBaseline(0.dp).scale(animationValue)
                    )
                    Spacer(modifier = modifier.height(10.dp))
                    Text(
                        text = "Unlock Fluency with AI-driven Realistic Dialogues",
                        fontFamily = FontFamily.Serif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraLight,
                        color = Color.Gray
                    )
                    Spacer(modifier = modifier.height(45.dp))
                    Button(
                        onClick = { navigator?.push(SignInScreen())},
                        colors = ButtonDefaults.buttonColors(Color(0xFFFDC323))
                        ){
                        Text(
                            text = "Get Started",
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
                Box(
                    modifier = modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.languages),
                        contentDescription = "languages_image",
                        modifier = modifier.fillMaxSize()
                    )
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