package com.example.ai_dialogue_assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cafe.adriel.voyager.navigator.Navigator
import com.example.ai_dialogue_assistant.frontEnd.startScreen
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import dagger.hilt.android.AndroidEntryPoint
import com.example.ai_dialogue_assistant.BuildConfig

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // add firebase authentication/ set up here
        configureFirebaseServices()
        setContent {
            Navigator(startScreen())
        }
    }

    // sets up api needed to configure services that are used in firebase
    private fun configureFirebaseServices() {
        if (BuildConfig.DEBUG) {
            Firebase.auth.useEmulator(BuildConfig.LOCALHOST, BuildConfig.AUTH_PORT.toInt())
            Firebase.firestore.useEmulator(BuildConfig.LOCALHOST, BuildConfig.FIRESTORE_PORT.toInt())
        }
        }
}

