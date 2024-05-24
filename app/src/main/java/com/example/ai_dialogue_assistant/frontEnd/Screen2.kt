package com.example.ai_dialogue_assistant.frontEnd

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import me.xdrop.fuzzywuzzy.FuzzySearch

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
        "Greeting",
        "Food", "Finance", "Travel", "Daily", "Introduction", "Shopping",
        "Socializing", "Health", "Work", "Weather", "Technology", "Education"
    )
}

fun getLanguages(): List<String> {
    return listOf(
        "Afrikaans",
        "Albanian",
        "Amharic",
        "Arabic",
        "Armenian",
        "Assamese",
        "Aymara",
        "Azerbaijani",
        "Bambara",
        "Basque",
        "Belarusian",
        "Bengali",
        "Bhojpuri",
        "Bosnian",
        "Bulgarian",
        "Catalan",
        "Cebuano",
        "Chinese (Simplified)",
        "Chinese (Traditional)",
        "Corsican",
        "Croatian",
        "Czech",
        "Danish",
        "Dhivehi",
        "Dogri",
        "Dutch",
        "English",
        "Esperanto",
        "Estonian",
        "Ewe",
        "Filipino (Tagalog)",
        "Finnish",
        "French",
        "Frisian",
        "Galician",
        "Georgian",
        "German",
        "Greek",
        "Guarani",
        "Gujarati",
        "Haitian Creole",
        "Hausa",
        "Hawaiian",
        "Hebrew",
        "Hindi",
        "Hmong",
        "Hungarian",
        "Icelandic",
        "Igbo",
        "Ilocano",
        "Indonesian",
        "Irish",
        "Italian",
        "Japanese",
        "Javanese",
        "Kannada",
        "Kazakh",
        "Khmer",
        "Kinyarwanda",
        "Konkani",
        "Korean",
        "Krio",
        "Kurdish",
        "Kurdish (Sorani)",
        "Kyrgyz",
        "Lao",
        "Latin",
        "Latvian",
        "Lingala",
        "Lithuanian",
        "Luganda",
        "Luxembourgish",
        "Macedonian",
        "Maithili",
        "Malagasy",
        "Malay",
        "Malayalam",
        "Maltese",
        "Maori",
        "Marathi",
        "Meiteilon (Manipuri)",
        "Mizo",
        "Mongolian",
        "Myanmar (Burmese)",
        "Nepali",
        "Norwegian",
        "Nyanja (Chichewa)",
        "Odia (Oriya)",
        "Oromo",
        "Pashto",
        "Persian",
        "Polish",
        "Portuguese (Portugal, Brazil)",
        "Punjabi",
        "Quechua",
        "Romanian",
        "Russian",
        "Samoan",
        "Sanskrit",
        "Scots Gaelic",
        "Sepedi",
        "Serbian",
        "Sesotho",
        "Shona",
        "Sindhi",
        "Sinhala (Sinhalese)",
        "Slovak",
        "Slovenian",
        "Somali",
        "Spanish",
        "Sundanese",
        "Swahili",
        "Swedish",
        "Tagalog (Filipino)",
        "Tajik",
        "Tamil",
        "Tatar",
        "Telugu",
        "Thai",
        "Tigrinya",
        "Tsonga",
        "Turkish",
        "Turkmen",
        "Twi (Akan)",
        "Ukrainian",
        "Urdu",
        "Uyghur",
        "Uzbek",
        "Vietnamese",
        "Welsh",
        "Xhosa",
        "Yiddish",
        "Yoruba",
        "Zulu"
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
        ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class,
        ExperimentalComposeUiApi::class
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

        @Composable
        fun TopicSelection(modifier: Modifier = Modifier, topics: List<String>) {
            FlowRow(modifier = modifier.padding(16.dp)) {
                topics.forEach { topic ->
                    Card(
                        modifier = modifier
                            .padding(8.dp)
                            .background(if (topic == selectedTopic) Color.Blue else Color.LightGray),
                        onClick = { selectedTopic = topic}
                    ) {
                        Text(
                            text = topic,
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp,
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
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Serif)
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
                                languages.filter { FuzzySearch.ratio(it, searchQuery) >= 50
                                }
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

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxSize()
                .background(Color.LightGray)
                .clickable { keyboardController?.hide() }
        ) {
            Text(
                "Choose a conversation topic:",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                modifier = modifier.align(Alignment.CenterHorizontally)
            )
            TopicSelection(topics = topics)
            TextField(
                value = newTopic,
                onValueChange = { newTopic = it },
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                label = { Text("Add a new topic", fontFamily = FontFamily.Serif) },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            Button(
                onClick = {
                    if (newTopic.isNotBlank()) {
                        topics.add(newTopic)
                        saveTopics(context, topics)
//                        deleteTopic(context, "Gyn"). ONLY USE WHEN YOU WANT TO DELETE A TOPIC
                        newTopic = ""
                    }
                },
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Add Topic", fontFamily = FontFamily.Serif)
            }

            Text(
                "Choose a language:",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                modifier = modifier.align(Alignment.CenterHorizontally)
            )
            LanguageDropdownMenu()
            Spacer(modifier = modifier.weight(1f))
            Button(
                onClick = {
                    if (selectedLanguage.isNotBlank() && selectedTopic.isNotBlank()) {
                        navigator?.push(ChatScreen(selectedLanguage, selectedTopic))
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
                    .fillMaxWidth()
            ) {
                Text(
                    "Continue",
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp
                )
            }
        }
    }
}