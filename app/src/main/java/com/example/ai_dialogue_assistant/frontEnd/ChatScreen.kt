package com.example.ai_dialogue_assistant.frontEnd

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cafe.adriel.voyager.core.screen.Screen
import com.example.ai_dialogue_assistant.BuildConfig
import com.example.ai_dialogue_assistant.R
import com.example.ai_dialogue_assistant.backEnd.AmazonPollyService
import com.example.ai_dialogue_assistant.backEnd.GoogleTTSService
import com.example.ai_dialogue_assistant.backEnd.SpeechHandler
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.ResponseStoppedException
import com.google.ai.client.generativeai.type.SafetySetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class ChatScreen(val language: String, val topic: String) : Screen {

    @Composable
    override fun Content() {
        val modifier = Modifier
        var userInput by remember { mutableStateOf("") }
        val conversationHistory = remember { mutableStateListOf<Message>() }
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        val context = LocalContext.current
        val amazonPollyService = AmazonPollyService(context)
        val googleTTSService = GoogleTTSService(context)
        val hasRecordAudioPermission = remember { mutableStateOf(false) }
        val requestCode = 200

        LaunchedEffect(topic) {
            // Retrieve existing messages if necessary (this part depends on your app's logic)

        }

        suspend fun sendToAI(
            message: String,
            conversationHistory: MutableList<Message>,
            scope: CoroutineScope,
            listState: LazyListState
        ) {
            val harassmentSafety = SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH)
            val hateSpeechSafety = SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.ONLY_HIGH)
            val sexualSafety = SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.ONLY_HIGH)
            val dangerousSafety = SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH)

            val generativeModel = GenerativeModel(
                modelName = "gemini-pro",
                apiKey = BuildConfig.GEMINI_KEY,
                safetySettings = listOf(
                    harassmentSafety,
                    hateSpeechSafety,
                    sexualSafety,
                    dangerousSafety
                )
            )

            var attempt = 0
            val maxAttempts = 3
            while (attempt < maxAttempts) {
                try {
                    val response = generativeModel.generateContent(message)
                    response.text?.let { Message(it, "ai", topic, language) }?.let {
                        conversationHistory.add(it)
                        scope.launch {
                            listState.scrollToItem(conversationHistory.size - 1)
                        }
                    }
                    break
                } catch (e: ResponseStoppedException) {
                    attempt++
                    Log.e("ChatScreen", "Attempt $attempt: Content generation stopped: ${e.message}")
                    if (attempt >= maxAttempts) {
                        Toast.makeText(context, "Content generation stopped after $attempt attempts: ${e.message}", Toast.LENGTH_LONG).show()
                        break
                    }
                } catch (e: Exception) {
                    Log.e("ChatScreen", "Error generating content: ${e.message}")
                    Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                    break
                }
            }
        }

        LaunchedEffect(Unit) {
            if (conversationHistory.isEmpty()) {
                val initialPrompt =
                    "Language: $language Topic: $topic. Begin a dialogue, staying on the topic $topic and using only this language $language. Just start the dialogue and allow me to respond. Don't carry on the conversation with yourself, let the conversation flow between us."
                sendToAI(initialPrompt, conversationHistory, scope, listState)
            }
            Toast.makeText(context, "You can change the keyboard language in settings", Toast.LENGTH_LONG).show()
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ) {
            LazyColumn(
                state = listState, modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(conversationHistory) { message ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (message.type == "user") Color.Cyan else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(0.9f)
                        ) {
                            Text(
                                text = message.text,
                                fontFamily = FontFamily.Serif,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        if (message.type == "ai" && listOf(
                                "Arabic",
                                "Dutch",
                                "Catalan",
                                "Chinese",
                                "Danish",
                                "English",
                                "Finnish",
                                "French",
                                "German",
                                "Hindi",
                                "Icelandic",
                                "Italian",
                                "Japanese",
                                "Korean",
                                "Norwegian",
                                "Polish",
                                "Portuguese (Portugal, Brazil)",
                                "Romanian",
                                "Russian",
                                "Spanish",
                                "Swedish",
                                "Turkish",
                                "Welsh"
                            ).any { it.equals(language, ignoreCase = true) }
                        ) {
                            IconButton(
                                onClick = {
                                    amazonPollyService.speak(
                                        message.text,
                                        language
                                    )
                                }, modifier = modifier
                                    .size(48.dp)
                                    .weight(0.1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play sound",
                                    modifier = modifier.size(32.dp)
                                )
                            }
                        } else if (message.type == "ai") {
                            IconButton(
                                onClick = {
                                    googleTTSService.speak(message.text, language)
                                }, modifier = modifier
                                    .size(48.dp)
                                    .weight(0.1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play sound",
                                    modifier = modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Serif)
                )
                Spacer(modifier = modifier.width(8.dp))
                Button(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            conversationHistory.add(Message(userInput, "user", topic, language))
                            val prompt =
                                "$userInput. Continue this dialogue based on the listed response and only in this $language. Don't carry on the conversation with yourself, let the conversation flow between us."
                            userInput = ""
                            scope.launch {
                                listState.scrollToItem(conversationHistory.size - 1)
                            }
                            scope.launch {
                                sendToAI(prompt, conversationHistory, scope, listState)
                            }
                        }
                    },
                    modifier = modifier
                ) {
                    Text("Send")
                }
                val speechHandler = remember {
                    SpeechHandler(context) { result ->
                        userInput = result
                    }
                }
                IconButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.RECORD_AUDIO), requestCode)
                        } else {
                            hasRecordAudioPermission.value = true
                        }

                        if (hasRecordAudioPermission.value) {
                            speechHandler.startListening()
                        } else {
                            Toast.makeText(context, "Please grant the RECORD_AUDIO permission to use this feature", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.voice),
                        contentDescription = "Listen to audio"
                    )
                }
            }
        }
    }
}

// To hold information about a message in the chat. text: content of the message, type: who sent the message- "user" or "AI", topic: the topic of the conversation, language: the language used.
data class Message(val text: String, val type: String, val topic: String, val language: String)
