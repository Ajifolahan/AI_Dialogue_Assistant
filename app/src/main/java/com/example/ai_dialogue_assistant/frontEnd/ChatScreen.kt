package com.example.ai_dialogue_assistant.frontEnd

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cafe.adriel.voyager.core.screen.Screen
import coil.compose.rememberAsyncImagePainter
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
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MultipartBody
import coil.compose.rememberImagePainter
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Date

data class ChatScreen(
    val language: String,
    val topic: String,
    val conversationId: String,
    val userId: String
) : Screen {

    @Composable
    @OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
        ExperimentalCoroutinesApi::class
    )
    override fun Content() {
        val modifier = Modifier
        var userInput by remember { mutableStateOf("") }
        val conversationHistory = remember { mutableStateListOf<Message>() }
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        val context = LocalContext.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val speakingService = SpeakingService(context)
        val apiService = API_Interface.create()
        var suppressInitialMessage by remember { mutableStateOf(false) }
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

        // Mutable state to check if the RECORD_AUDIO permission is granted
        val hasRecordAudioPermission = remember { mutableStateOf(false) }
        val requestCode = 200

        // adds messages to the database
        fun addMessageToConversation(message: Message, imageFile: File? = null) {
            if (message.message.isBlank() && imageFile == null) {
                Log.e("API_Interface", "Message is blank and no image file provided")
                return
            }
            if (message.sender.isBlank()) {
                Log.e("API_Interface", "Sender is blank")
                return
            }
            CoroutineScope(Dispatchers.IO).launch {
//                val messageToSend = if (imageFile != null) {
//                    message.copy(message = message.message.ifBlank { "Image attached" })
//                } else {
//                    message
//                }
                val messageJson = Gson().toJson(message)
                Log.d("API_Interface", "Momore- Sending message: $messageJson")
                val messagePart = MultipartBody.Part.createFormData("message", messageJson)

                val imagePart = imageFile?.let { file ->
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image", file.name, requestFile)
                }
                apiService.addMessageWithImage(userId, conversationId, messagePart, imagePart)
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

        // gets the conversation history if the user has already started a conversation based on selected language and topic
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

        suspend fun checkLastMessageFromAI(userId: String, conversationId: String): Boolean {
            return suspendCancellableCoroutine { continuation ->
                apiService.checkLastMessageFromAI(userId, conversationId).enqueue(object : Callback<Map<String, Boolean>> {
                    override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                        if (response.isSuccessful) {
                            suppressInitialMessage = response.body()?.get("suppressInitialMessage") ?: false
                            continuation.resume(suppressInitialMessage) {}
                        } else {
                            Log.e("ChatScreen", "Failed to check last message: ${response.errorBody()?.string()}")
                            continuation.resume(false) {}
                        }
                    }

                    override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                        Log.e("ChatScreen", "API call failed: ${t.message}")
                        continuation.resume(false) {}
                    }
                })
            }
        }


        //conversation with the initial prompt
        LaunchedEffect(Unit) {
            fetchConversationHistory()

            val suppressInitialMessage = checkLastMessageFromAI(userId, conversationId)
            if (!suppressInitialMessage) {
                val initialPrompt =
                    "Language: $language Topic: $topic. Begin a dialogue, staying on the topic $topic and using only this language $language. Just start the dialogue and allow me to respond. Don't carry on the conversation with yourself, let the conversation flow between us."
                // Send the initial prompt to the AI
                sendToAI(initialPrompt, conversationHistory, scope, listState)
            }

            // Notify users that they can change the keyboard language in settings
            Toast.makeText(
                context,
                "You can change the keyboard language in settings",
                Toast.LENGTH_LONG
            ).show()
        }



        Column(
            modifier = modifier
                .fillMaxSize()
                .clickable { keyboardController?.hide() },
            ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
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
                                containerColor = if (message.sender == "user") Color(0xFF00785D) else Color(0xFF539BA9)
                            ),
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(0.9f)
                        ) {
                            // Check if the message has an image URI
                            if (message.imageUri != null) {
                                val imageUrl = BuildConfig.VERCEL_URL + message.imageUri
                                // Display the image
                                Image(
                                    painter = rememberAsyncImagePainter(model = imageUrl),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
//                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            if (message.message.isNotBlank()) {
                                Text(
                                    text = message.message,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 17.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
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
                    message.imageUri?.let { uri ->
                        Image(painter = rememberImagePainter(data = uri), contentDescription = null)
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
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Serif, fontSize = 15.sp),
                    leadingIcon = {
                        // Display the selected image as the leading icon
                        selectedImageUri?.let { uri ->
                            Image(painter = rememberImagePainter(data = uri), contentDescription = null)
                        }
                    }
                )
                Spacer(modifier = modifier.width(8.dp))

                Button(
                    onClick = {
                        if (userInput.isNotBlank() || selectedImageUri != null) {
                            val userMessage = Message("user", userInput, Date().toString(), selectedImageUri)
                            conversationHistory.add(userMessage)
                            // If an image is selected, create a File object from the URI
                            val imageFile = selectedImageUri?.let { uri ->
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val file = File(context.cacheDir, "tempImage")
                                file.outputStream().use { fileOut ->
                                    inputStream?.copyTo(fileOut)
                                }
                                file
                            }
                            addMessageToConversation(userMessage,imageFile)
                            val prompt =
                                "$userInput. Continue this dialogue based on the listed response and only in this $language. Don't carry on the conversation with yourself, let the conversation flow between us."
                            userInput = ""
                            selectedImageUri = null
                            // Scroll to the bottom after adding user message
                            scope.launch {
                                listState.scrollToItem(conversationHistory.size - 1)
                                sendToAI(prompt, conversationHistory, scope, listState)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFFFDC323))
                ) {
                    Text(
                        text ="Send",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                val speechHandler = remember {
                    SpeechHandler(context) { result ->
                        userInput = result
                    }
                }
                IconButton(
                    onClick = {
                        //if the permission is not granted, request it. This is needed because using just the android manifest file isn't working, permission needs to be requested in runtime
                        //and not before the app is installed
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
                val pickImageLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let { imageUri ->
                        selectedImageUri = imageUri
                        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                            val file = File(context.cacheDir, "tempImage")
                            file.outputStream().use { fileOut ->
                                inputStream.copyTo(fileOut)
                            }
                        }
                    }
                }
                IconButton(
                    onClick = {
                        pickImageLauncher.launch("image/*")
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.image),
                        contentDescription = "Pick Image"
                    )
                }
            }
        }
    }
}
