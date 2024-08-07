package com.example.ai_dialogue_assistant.frontEnd

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.ai_dialogue_assistant.backEnd.API_Interface
import com.example.ai_dialogue_assistant.backEnd.Conversation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xdrop.fuzzywuzzy.FuzzySearch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.UUID

fun saveTopics(context: Context, topics: List<String>) {
    val sharedPreferences = context.getSharedPreferences("topics", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putStringSet("topicList", topics.toSet())
    editor.apply()
}

fun loadTopics(context: Context): MutableList<String> {
    val sharedPreferences = context.getSharedPreferences("topics", Context.MODE_PRIVATE)
    val topicSet = sharedPreferences.getStringSet("topicList", null)
    return topicSet?.toMutableList() ?: mutableStateListOf(
        "Greeting", "Food", "Finance", "Travel", "Daily", "Introduction", "Shopping",
        "Socializing", "Health", "Work", "Weather", "Technology", "Education"
    )
}

fun getLanguages(): List<String> {
    return listOf(
        "Albanian", "Arabic", "Armenian", "Assamese", "Aymara", "Basque", "Belarusian",
        "Bengali", "Bhojpuri", "Bosnian", "Bulgarian", "Catalan", "Cebuano", "Chinese (Simplified)",
        "Chinese (Traditional)", "Corsican", "Croatian", "Czech", "Danish", "Dhivehi", "Dogri",
        "Dutch", "English", "Esperanto", "Estonian", "Ewe", "Filipino (Tagalog)", "Finnish",
        "French", "Galician", "Georgian", "German", "Greek", "Guarani", "Gujarati", "Haitian Creole",
        "Hausa", "Hawaiian", "Hebrew", "Hindi", "Hmong", "Hungarian", "Icelandic", "Igbo", "Ilocano",
        "Indonesian", "Italian", "Japanese", "Kinyarwanda", "Konkani", "Korean", "Krio", "Kurdish",
        "Kurdish (Sorani)", "Kyrgyz", "Lao", "Latin", "Latvian", "Lingala", "Lithuanian", "Luganda",
        "Maithili", "Malagasy", "Malay", "Malayalam", "Maori", "Marathi", "Meiteilon (Manipuri)",
        "Mizo", "Mongolian", "Nepali", "Norwegian", "Nyanja (Chichewa)", "Oromo", "Pashto", "Polish",
        "Portuguese (Portugal, Brazil)", "Quechua", "Romanian", "Russian", "Samoan", "Sanskrit",
        "Scots Gaelic", "Sepedi", "Serbian", "Sesotho", "Shona", "Sindhi", "Slovak", "Slovenian",
        "Somali", "Spanish", "Sundanese", "Swahili", "Swedish", "Tagalog (Filipino)", "Tatar",
        "Telugu", "Thai", "Tsonga", "Turkish", "Turkmen", "Ukrainian", "Vietnamese", "Welsh",
        "Xhosa", "Yiddish", "Yoruba", "Zulu"
    )
}

fun deleteTopic(context: Context, topic: String) {
    val sharedPreferences = context.getSharedPreferences("topics", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val topicsSet = sharedPreferences.getStringSet("topicList", null)
    topicsSet?.remove(topic)
    editor.putStringSet("topicList", topicsSet)
    editor.apply()
}

class Screen2 : Screen {

    @OptIn(
        ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class
    )
    @Composable
    override fun Content() {
        val modifier = Modifier
        //stateholders. recompose composable functions that are reading the state
        var newTopic by remember { mutableStateOf("") }
        //so that clicking outside the textfield will hide the keyboard
        val keyboardController = LocalSoftwareKeyboardController.current
        val navigator = LocalNavigator.current
        val context = LocalContext.current
        var selectedLanguage by remember { mutableStateOf("") }
        var selectedTopic by remember { mutableStateOf("") }
        var topics by remember { mutableStateOf(loadTopics(context)) }
        val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        @Composable
        fun TopicSelection(modifier: Modifier = Modifier, topics: List<String>) {
            FlowRow(modifier = modifier.padding(16.dp)) {
                topics.forEach { topic ->
                    Card(
                        modifier = modifier
                            .padding(8.dp)
                            .background(
                                if (topic == selectedTopic) Color.Magenta else Color.Gray)
                            .clickable { selectedTopic = topic },
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = topic,
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp,
                            color = if (topic == selectedTopic) Color.Magenta else Color.Black,
                            modifier = modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        @Composable
        fun LanguageDropdownMenu(modifier: Modifier = Modifier) {
            var expanded by remember { mutableStateOf(false) }
            var searchQuery by remember { mutableStateOf("") }
            val languages = getLanguages()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    TextField(
                        modifier = modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .clickable(onClick = { expanded = true }),
                        value = selectedLanguage,
                        onValueChange = { selectedLanguage = it },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        label = { Text("Select a language", fontFamily = FontFamily.Serif) },
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Serif),
                    )
                    MaterialTheme(
                        shapes = MaterialTheme.shapes.copy(
                            extraSmall = RoundedCornerShape(
                                16.dp
                            )
                        )
                    ) {
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Search", fontFamily = FontFamily.Serif) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            val filteredLanguageNames = if (searchQuery.isBlank()) {
                                languages
                            } else {
                                languages.filter { FuzzySearch.ratio(it, searchQuery) >= 50 }
                            }
                            filteredLanguageNames.forEach { languageName ->
                                DropdownMenuItem(
                                    {
                                        Text(
                                            text = languageName,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily.Serif
                                        )
                                    },
                                    onClick = {
                                        selectedLanguage = languageName
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                HamburgerMenu(
                    onConversationClick = { conversation ->
                        navigator?.push(
                            ChatScreen(
                                conversation.language,
                                conversation.topic,
                                conversation.conversationId,
                                auth.currentUser!!.uid
                            )
                        )
                        scope.launch { drawerState.close() }
                    }
                )
            },
            content = {
                Column(
                    verticalArrangement = Arrangement.Top,
                    modifier = modifier
                        .fillMaxSize()
                        .clickable { keyboardController?.hide() }
                ) {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Hamburger Menu"
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    //Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        "Choose a conversation topic:",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        modifier = modifier.align(Alignment.CenterHorizontally)
                    )
                    TopicSelection(topics = topics)
                    TextField(
                        value = newTopic,
                        onValueChange = { newTopic = it },
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                        label = { Text("Add a new topic", fontFamily = FontFamily.Serif) },
                        textStyle = TextStyle(fontFamily = FontFamily.Serif),
                        modifier = modifier.fillMaxWidth().padding(16.dp)
                    )
                    Button(
                        onClick = {
                            if (newTopic.isNotBlank()) {
                                topics.add(newTopic)
                                saveTopics(context, topics)
                                // deleteTopic(context, "Gyn"). ONLY USE WHEN YOU WANT TO DELETE A TOPIC
                                newTopic = ""
                            }
                        },
                        modifier = modifier
                            .padding(horizontal = 16.dp)
                            .align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(Color(0xFFFDC323))
                    ) {
                        Text(
                            "Add Topic",
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    //Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        "Choose a language:",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        modifier = modifier.align(Alignment.CenterHorizontally)
                    )
                    LanguageDropdownMenu()
                    //Spacer(modifier = Modifier.height(2.dp))
                    Button(
                        onClick = {
                            if (selectedLanguage.isNotBlank() && selectedTopic.isNotBlank()) {
                                val apiService = API_Interface.create()
                                val userId = auth.currentUser!!.uid
                                val conversationId =
                                    UUID.nameUUIDFromBytes("$userId-$selectedTopic-$selectedLanguage".toByteArray())
                                        .toString() // I could probably be doing this better another way.

                                CoroutineScope(Dispatchers.IO).launch {
                                    apiService.getConversation(userId, conversationId)
                                        .enqueue(object : Callback<Conversation> {
                                            override fun onResponse(
                                                call: Call<Conversation>,
                                                response: Response<Conversation>
                                            ) {
                                                if (response.isSuccessful) {
                                                    val existingConversation = response.body()
                                                    if (existingConversation != null) {
                                                        // then conversation exists, carry on with it.
                                                        navigator?.push(
                                                            ChatScreen(
                                                                existingConversation.language,
                                                                existingConversation.topic,
                                                                existingConversation.conversationId,
                                                                userId
                                                            )
                                                        )
                                                    }
                                                } else {
                                                    // Conversation does not exist, create a new one
                                                    val newConversation = Conversation(
                                                        userId = userId,
                                                        topic = selectedTopic,
                                                        language = selectedLanguage,
                                                        conversationId = conversationId,
                                                        messages = listOf(),
                                                        createdAt = Date().toString(),
                                                        updatedAt = Date().toString()
                                                    )
                                                    apiService.createConversation(newConversation)
                                                        .enqueue(object : Callback<Conversation> {
                                                            override fun onResponse(
                                                                call: Call<Conversation>,
                                                                response: Response<Conversation>
                                                            ) {
                                                                if (response.isSuccessful) {
                                                                    response.body()?.let { conv ->
                                                                        navigator?.push(
                                                                            ChatScreen(
                                                                                conv.language,
                                                                                conv.topic,
                                                                                conv.conversationId,
                                                                                userId
                                                                            )
                                                                        )
                                                                    }
                                                                } else {
                                                                    val errorMessage =
                                                                        response.errorBody()?.string()
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Failed to create conversation: $errorMessage",
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                }
                                                            }

                                                            override fun onFailure(
                                                                call: Call<Conversation>,
                                                                t: Throwable
                                                            ) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "API call failed: ${t.message}",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                                Log.d(
                                                                    "Screen2",
                                                                    "API_Service_Create, API call failed: ${t.message})"
                                                                )
                                                            }
                                                        })
                                                }
                                            }

                                            override fun onFailure(call: Call<Conversation>, t: Throwable) {
                                                Toast.makeText(
                                                    context,
                                                    "API call failed: ${t.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                Log.d(
                                                    "Screen2",
                                                    "API_Service_GET, API call failed: ${t.message})"
                                                )
                                            }
                                        })
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please select a language and a conversation topic",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = modifier
                            .clip(RoundedCornerShape(50))
                            .padding(16.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color(0xFFFDC323))
                    )
                    {
                        Text(
                            "Continue",
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.weight(6f))
                }
            }
        )
    }
}
