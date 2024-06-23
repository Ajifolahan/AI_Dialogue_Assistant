package com.example.ai_dialogue_assistant.backEnd

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.ai_dialogue_assistant.models.ChatMessages
class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val chatCollection = db.collection("chats")

    fun saveChatMessage(message: ChatMessages, onComplete: (Boolean) -> Unit) {
        chatCollection.document(message.id).set(message)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getChatMessages(userId: String, topic: String, onComplete: (List<ChatMessages>) -> Unit) {
        chatCollection.whereEqualTo("userId", userId)
            .whereEqualTo("topic", topic)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val messages = documents.map { it.toObject(ChatMessages::class.java) }
                onComplete(messages)
            }
            .addOnFailureListener { onComplete(emptyList()) }
    }
}
