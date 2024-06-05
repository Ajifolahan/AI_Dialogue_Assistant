package com.example.ai_dialogue_assistant.backEnd

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.ai_dialogue_assistant.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class GoogleTTSService(private val context: Context) {

    private val client = OkHttpClient()
    private val apiKey = BuildConfig.GOOGLE_TEXT_TO_SPEECH

    private fun getLanguageCode(language: String): String {
        return when (language) {
            "Afrikaans" -> "af-ZA"
            "Amharic" -> "am-ET"
            "Bulgarian" -> "bg-BG"
            "Bengali" -> "bn-IN"
            "Catalan" -> "ca-ES"
            "Valencian-catalan" -> "ca-ES"
            "Czech" -> "cs-CZ"
            "Greek" -> "el-GR"
            "Basque" -> "eu-ES"
            "Galician" -> "gl-ES"
            "Gujarati" -> "gu-IN"
            "Hungarian" -> "hu-HU"
            "Indonesian" -> "id-ID"
            "Kannada" -> "kn-IN"
            "Lithuanian" -> "lt-LT"
            "Latvia" -> "lv-LV"
            "Malayalam" -> "ml-IN"
            "Marathi" -> "mr-IN"
            "Malay" -> "ms-MY"
            "Slovak" -> "sk-SK"
            "Serbian" -> "sr-RS"
            "Tamil" -> "ta-IN"
            "Telugu" -> "te-IN"
            "Thai" -> "th-TH"
            "Ukrainian" -> "uk-UA"
            "Urdu" -> "ur-IN"
            "Vietnamese" -> "vi-VN"
            else -> language
        }
    }

    fun speak(text: String, language: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val correctedLanguageCode = getLanguageCode(language)
                val url = "https://texttospeech.googleapis.com/v1/text:synthesize?key=$apiKey"
                val json = """
                    {
                      "input": {
                        "text": "$text"
                      },
                      "voice": {
                        "languageCode": "$correctedLanguageCode",
                        "ssmlGender": "NEUTRAL"
                      },
                      "audioConfig": {
                        "audioEncoding": "MP3"
                      }
                    }
                """.trimIndent()

                val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                Log.d("GoogleTTSService", "Request: $json")

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e("GoogleTTSService", "Error in speak: ${response.code} - ${response.message}")
                    Log.e("GoogleTTSService", "Error body: $errorBody")
                    throw IOException("Unexpected code $response")
                }

                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody!!)
                val audioContent = jsonResponse.getString("audioContent")

                val audioData = android.util.Base64.decode(audioContent, android.util.Base64.DEFAULT)
                val audioUri = "data:audio/mp3;base64,$audioContent"
                withContext(Dispatchers.Main) {
                    playAudio(audioUri)
                }
            } catch (e: Exception) {
                Log.e("GoogleTTSService", "Error in speak: ${e.message}")
            }
        }
    }

    private fun playAudio(audioUri: String) {
        Log.d("GoogleTTSService", "Playing audio from URI")
        val exoPlayer = ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(audioUri)
            setMediaItem(mediaItem)
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("GoogleTTSService", "Error occurred while playing audio. Error: ${error.message}")
                }
            })
            prepare()
        }
    }
}
