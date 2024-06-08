package com.example.ai_dialogue_assistant.backEnd

import android.content.Context
import android.util.Log
import android.util.LruCache
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SpeakingService(private val context: Context) {
    private var exoPlayer: ExoPlayer? = null
    private val cache = LruCache<String, String>(50)

    private fun getVoiceForLanguageAmazonPolly(language: String): String {
        // mapping between languages and voice
        return when (language) {
            "Arabic" -> "Zeina"
            "Chinese (Traditional)" -> "Zhiyu"
            "Danish" -> "Naja"
            "English" -> "Joanna"
            "French" -> "Celine"
            "German" -> "Vicki"
            "Hindi" -> "Aditi"
            "Icelandic" -> "Dora"
            "Italian" -> "Carla"
            "Japanese" -> "Mizuki"
            "Korean" -> "Seoyeon"
            "Norwegian" -> "Liv"
            "Polish" -> "Ewa"
            "Portuguese (Portugal, Brazil)" -> "Ines"
            "Romanian" -> "Carmen"
            "Russian" -> "Tatyana"
            "Spanish" -> "Conchita"
            "Swedish" -> "Astrid"
            "Turkish" -> "Filiz"
            "Welsh" -> "Gwyneth"
            else -> ""
        }
    }

    private fun getLanguageCodeGoogleTTS(language: String): String {
        return when (language) {
            "Basque" -> "eu-ES"
            "Bengali" -> "bn-IN"
            "Bulgarian" -> "bg-BG"
            "Catalan" -> "ca-ES"
            "Czech" -> "cs-CZ"
            "Dutch" -> "nl-NL"
            "Finnish" -> "fi-FI"
            "Galician" -> "gl-ES"
            "Greek" -> "el-GR"
            "Gujarati" -> "gu-IN"
            "Hungarian" -> "hu-HU"
            "Indonesian" -> "id-ID"
            "Latvian" -> "lv-LV"
            "Lithuanian" -> "lt-LT"
            "Malay" -> "ms-MY"
            "Malayalam" -> "ml-IN"
            "Marathi" -> "mr-IN"
            "Serbian" -> "sr-RS"
            "Slovak" -> "sk-SK"
            "Telugu" -> "te-IN"
            "Thai" -> "th-TH"
            "Ukrainian" -> "uk-UA"
            "Vietnamese" -> "vi-VN"
            else -> ""
        }
    }


    fun speakAmazonPolly(text: String, speaker: String) {
        val voiceId = getVoiceForLanguageAmazonPolly(speaker)
        val cacheKey = "$text-$voiceId"
        //playing from a cache so we dont have to call the API twice and we are saving resources
        if (cache[cacheKey] != null) {
            playAudio(cache[cacheKey]!!)
            return
        }

        val requests = HashMap<String, String>()
        requests["text"] = text
        requests["speaker"] = voiceId


        val call = RetrofitClient.pollyService.getSpeech(requests)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    if (responseBody != null) {
                        val jsonObject = JSONObject(responseBody)
                        if (jsonObject.has("body")) {
                            val presignedUrl = jsonObject.getString("body")
                            if (presignedUrl.isNotEmpty()) {
                                cache.put(cacheKey, presignedUrl)
                                playAudio(presignedUrl)
//                                cachevalue()
                            }
                        } else {
                            Log.e("AmazonPolly API Error", "No body")
                        }
                    }
                } else {
                    Log.e("AmazonPolly API Error", "Response unsuccessful")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("AmazonPolly API Error", t.toString())
            }
        })
    }

    fun speakGoogleTTS(text: String, language: String) {
        val languageCode = getLanguageCodeGoogleTTS(language)
        val cacheKey = "$text-$languageCode"

        if (cache[cacheKey] != null) {
            playAudio(cache[cacheKey]!!)
            return
        }

        val requests = HashMap<String, String>()
        requests["text"] = text
        requests["language"] = languageCode

        val call = RetrofitClient.googleTTSService.getSpeech(requests)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    if (responseBody != null) {
                        val jsonResponse = JSONObject(responseBody)
                        val audioContent = jsonResponse.getJSONObject("body").getString("audioContent")
                        val audioUri = "data:audio/mp3;base64,$audioContent"
                        cache.put(cacheKey, audioUri)
                        playAudio(audioUri)
                    }
                } else {
                    Log.e("GoogleTTS API Error", "Response unsuccessful")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("GoogleTTS API Error", t.toString())
            }
        })
    }

private fun playAudio(presignedUrl: String) {
    exoPlayer?.release()
    exoPlayer = ExoPlayer.Builder(context).build().apply {
        val mediaItem = MediaItem.fromUri(presignedUrl)
        setMediaItem(mediaItem)
        playWhenReady = true
        addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e(
                    "SpeakingService",
                    "Error occurred while playing audio. Error: ${error.message}"
                )
            }
        })
        prepare()
    }
}


fun cachevalue() {
    cache.snapshot().forEach { (key, value) ->
        println("Key: $key, Value: $value")
    }
}
}