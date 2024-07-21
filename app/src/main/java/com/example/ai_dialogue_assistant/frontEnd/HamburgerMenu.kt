package com.example.ai_dialogue_assistant.frontEnd

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai_dialogue_assistant.backEnd.API_Interface
import com.example.ai_dialogue_assistant.backEnd.Conversation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun HamburgerMenu(
    onConversationClick: (Conversation) -> Unit
) {
    var conversations by remember { mutableStateOf(listOf<Conversation>()) }

    fun fetchConversations() {
        val apiService = API_Interface.create()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                apiService.getAllConversations(userId).enqueue(object : Callback<List<Conversation>> {
                    override fun onResponse(
                        call: Call<List<Conversation>>,
                        response: Response<List<Conversation>>
                    ) {
                        if (response.isSuccessful) {
                            conversations = response.body() ?: listOf()
                        } else {
                            Log.e("HamburgerMenu", "Failed to fetch conversations")
                        }
                    }

                    override fun onFailure(call: Call<List<Conversation>>, t: Throwable) {
                        Log.e("HamburgerMenu", "API call failed: ${t.message}")
                    }
                })
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchConversations()
    }

    ModalDrawerSheet {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Text("Conversations", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = FontFamily.Serif, modifier = Modifier.padding(16.dp))
            conversations.forEach { conversation ->
                Text(
                    text = "${conversation.topic} - ${conversation.language}",
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .clickable { onConversationClick(conversation) }
                        .padding(8.dp),
                    color = Color.Black
                )
            }
        }
    }
}
