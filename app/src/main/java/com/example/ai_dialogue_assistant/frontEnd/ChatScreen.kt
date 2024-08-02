package com.example.ai_dialogue_assistant.frontEnd

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cafe.adriel.voyager.core.screen.Screen
import com.example.ai_dialogue_assistant.BuildConfig
import com.example.ai_dialogue_assistant.R
import com.example.ai_dialogue_assistant.backEnd.API_Interface
import com.example.ai_dialogue_assistant.backEnd.Conversation
import com.example.ai_dialogue_assistant.backEnd.Message
import com.example.ai_dialogue_assistant.backEnd.SpeakingService
import com.example.ai_dialogue_assistant.backEnd.SpeechHandler
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.ResponseStoppedException
import com.google.ai.client.generativeai.type.SafetySetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.util.Date

data class ChatScreen(
    val language: String,
    val topic: String,
    val conversationId: String,
    val userId: String
) : Screen {

    @Composable
    override fun Content() {
        val modifier = Modifier
        var userInput by remember { mutableStateOf(TextFieldValue("")) }
        val conversationHistory = remember { mutableStateListOf<Message>() }
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        val context = LocalContext.current
        val speakingService = SpeakingService(context)
        val apiService = API_Interface.create()

        // Mutable state to check if the RECORD_AUDIO permission is granted
        val hasRecordAudioPermission = remember { mutableStateOf(false) }
        val requestCode = 200

        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

        val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                selectedImageBitmap = BitmapFactory.decodeStream(inputStream)
            }
        }

        // Adds messages to the database
        fun addMessageToConversation(message: Message) {
            CoroutineScope(Dispatchers.IO).launch {
                apiService.addMessage(userId, conversationId, message)
                    .enqueue(object : Callback<Conversation> {
                        override fun onResponse(
                            call: Call<Conversation>,
                            response: Response<Conversation>
                        ) {
                            if (!response.isSuccessful) {
                                Log.e(
                                    "API_Interface",
                                    "Failed to add message: ${response.errorBody()?.string()}"
                                ) // sanity check
                            }
                        }

                        override fun onFailure(call: Call<Conversation>, t: Throwable) {
                            Log.e("API_Interface", "API call failed: ${t.message}")
                        }
                    })
            }
        }

        // Gets the conversation history if the user has already started a conversation based on selected language and topic
        fun fetchConversationHistory() {
            CoroutineScope(Dispatchers.IO).launch {
                apiService.getConversation(userId, conversationId)
                    .enqueue(object : Callback<Conversation> {
                        override fun onResponse(
                            call: Call<Conversation>,
                            response: Response<Conversation>
                        ) {
                            if (response.isSuccessful) {
                                val conversation = response.body()
                                conversation?.messages?.let {
                                    conversationHistory.addAll(it)
                                }
                            } else {
                                Log.e(
                                    "API_Interface",
                                    "Failed to fetch conversation: ${
                                        response.errorBody()?.string()
                                    }"
                                )
                            }
                        }

                        override fun onFailure(call: Call<Conversation>, t: Throwable) {
                            Log.e("API_Interface", "API call failed: ${t.message}")
                        }
                    })
            }
        }

        // Function to send message to AI and update UI
        suspend fun sendToAI(
            message: String,
            conversationHistory: MutableList<Message>,
            scope: CoroutineScope,
            listState: LazyListState
        ) {
            val harassmentSafety = SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH)
            val hateSpeechSafety = SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.ONLY_HIGH)
            val sexualSafety =
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.ONLY_HIGH)
            val dangerousSafety =
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH)

            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-pro",
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
                    response.text?.let {
                        val aiMessage = Message("ai", it, Date().toString())
                        conversationHistory.add(aiMessage)
                        scope.launch {
                            listState.scrollToItem(conversationHistory.size - 1)
                        }
                        addMessageToConversation(aiMessage)
                    }
                    break
                } catch (e: ResponseStoppedException) {
                    attempt++
                    Log.e(
                        "ChatScreen",
                        "Attempt $attempt: Content generation stopped: ${e.message}"
                    )
                    if (attempt >= maxAttempts) {
                        Toast.makeText(
                            context,
                            "Content generation stopped after $attempt attempts: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        break
                    }
                } catch (e: Exception) {
                    Log.e("ChatScreen", "Error generating content: ${e.message}")
                    Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                    break
                }
            }
        }

        // Conversation with the initial prompt
        LaunchedEffect(Unit) {
            fetchConversationHistory()
            if (conversationHistory.isEmpty()) {
                val initialPrompt =
                    "Language: $language Topic: $topic. Begin a dialogue, staying on the topic $topic and using only this language $language. Just start the dialogue and allow me to respond. Don't carry on the conversation with yourself, let the conversation flow between us."
                // Send the initial prompt to the AI
                sendToAI(initialPrompt, conversationHistory, scope, listState)
            }
            // Notify users that they can change their keyboard language in settings
            Toast.makeText(
                context,
                "You can change the keyboard language in settings",
                Toast.LENGTH_LONG
            ).show()
        }

        Column(
            modifier = modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(conversationHistory) { message ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (message.sender == "user") Color(0xFF00785D) else Color(0xFF539BA9)
                            ),
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(0.9f)
                        ) {
                            Text(
                                text = message.message,
                                fontFamily = FontFamily.Serif,
                                fontSize = 17.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        if (message.sender == "ai" && listOf(
                                "Arabic",
                                "Chinese (Traditional)",
                                "Danish",
                                "English",
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
                                    speakingService.speakAmazonPolly(
                                        message.message,
                                        language
                                    )
                                },
                                modifier = modifier
                                    .size(48.dp)
                                    .weight(0.1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play sound",
                                    modifier = modifier.size(32.dp)
                                )
                            }
                        } else if (message.sender == "ai" && listOf(
                                "Basque",
                                "Bengali",
                                "Bulgarian",
                                "Catalan",
                                "Czech",
                                "Dutch",
                                "Finnish",
                                "Galician",
                                "Greek",
                                "Gujarati",
                                "Hungarian",
                                "Indonesian",
                                "Latvian",
                                "Lithuanian",
                                "Malay",
                                "Malayalam",
                                "Marathi",
                                "Serbian",
                                "Slovak",
                                "Telugu",
                                "Thai",
                                "Ukrainian",
                                "Vietnamese"
                            ).any { it.equals(language, ignoreCase = true) }
                        ) {
                            IconButton(
                                onClick = {
                                    speakingService.speakGoogleTTS(message.message, language)
                                },
                                modifier = modifier
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

            Column(
                modifier = modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text("Pick Image")
                }

                Spacer(modifier = Modifier.height(16.dp))

                selectedImageBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Serif, fontSize = 15.sp)
                    )
                    Spacer(modifier = modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (userInput.text.isNotBlank()) {
                                val userMessage = Message("user", userInput.text, Date().toString())
                                conversationHistory.add(userMessage)
                                addMessageToConversation(userMessage)
                                val prompt =
                                    "${userInput.text}. Continue this dialogue based on the listed response and only in this $language. Don't carry on the conversation with yourself, let the conversation flow between us."
                                userInput = TextFieldValue("")
                                // Scroll to the bottom after adding user message
                                scope.launch {
                                    listState.scrollToItem(conversationHistory.size - 1)
                                }
                                scope.launch {
                                    sendToAI(prompt, conversationHistory, scope, listState)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFFFDC323))
                    ) {
                        Text(
                            text = "Send",
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                val speechHandler = remember {
                    SpeechHandler(context) { result ->
                        userInput = TextFieldValue(result)
                    }
                }
                IconButton(
                    onClick = {
                        // If the permission is not granted, request it. This is needed because using just the android manifest file isn't working, permission needs to be requested in runtime
                        // and not before the app is installed
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                context as Activity,
                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                requestCode
                            )
                        } else {
                            // If permission, update the state
                            hasRecordAudioPermission.value = true
                        }

                        // If permission, start the speech recognizer
                        if (hasRecordAudioPermission.value) {
                            speechHandler.startListening()
                        } else {
                            Toast.makeText(
                                context,
                                "Please grant the RECORD_AUDIO permission to use this feature",
                                Toast.LENGTH_LONG
                            ).show()
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
