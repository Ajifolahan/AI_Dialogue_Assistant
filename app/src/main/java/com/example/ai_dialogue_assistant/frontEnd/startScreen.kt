package com.example.ai_dialogue_assistant.frontEnd

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxSize().background(Color.LightGray)
        ) {
            Image(
                painter = painterResource(id = R.drawable.languagesss),
                contentDescription = "languages_image",
                modifier = modifier.padding(20.dp).fillMaxWidth().height(500.dp)
            )
            Text(
                text = "LangAI",
                fontFamily = FontFamily.Serif,
                fontSize = 40.sp
            )
            Text(
                text = "Improve language skills with realistic dialogues",
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp
            )
            Spacer(modifier = modifier.height(20.dp))
            Button(
                onClick = { navigator?.push(Screen2())},
                modifier = modifier
                    .clip(RoundedCornerShape(50))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Get Started",
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }

    }

@Preview(showBackground = true)
@Composable
fun GPreview() {
    Content()
}
}