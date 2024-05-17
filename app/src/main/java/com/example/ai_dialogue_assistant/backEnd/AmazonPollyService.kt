package com.example.ai_dialogue_assistant.backEnd

import android.media.MediaPlayer
import android.widget.Toast
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import org.json.JSONObject


class AmazonPollyService(private val context: Context) {
    private var exoPlayer: ExoPlayer? = null
    private val cache = mutableMapOf<String, String>()

    private fun getVoiceForLanguage(language: String): String {

        // mapping between languages and voice
        return when (language) {
            "Arabic" -> "Zeina"
            "Dutch, Flemish" -> "Laura"
            "Catalan, Valencian" -> "Arlet"
            "Chinese" -> "Hiujin"
            "Danish" -> "Naja"
            "English" -> "Joanna"
            "Finnish" -> "Suvi"
            "French" -> "Celine"
            "German" -> "Vicki"
            "Hindi" -> "Aditi"
            "Icelandic" -> "Dora"
            "Italian" -> "Carla"
            "Japanese" -> "Mizuki"
            "Korean" -> "Seoyeon"
            "Norwegian" -> "Liv"
            "Norwegian Bokmal" -> "Liv"
            "Norwegian Nynorsk" -> "Liv"
            "Polish" -> "Ewa"
            "Romanian, Moldavian, Moldovan" -> "Carmen"
            "Russian" -> "Tatyana"
            "Spanish, Castilian" -> "Conchita"
            "Swedish" -> "Astrid"
            "Turkish" -> "Filiz"
            "Welsh" -> "Gwyneth"
            else -> ""
        }
    }


    fun speak(text: String, speaker: String) {
        val voiceId = getVoiceForLanguage(speaker)
        val cacheKey = "$text-$voiceId"
        //playing from a cache so we dont have to call the API twice and we are saving resources
        // if needed we can implement a different type of caching- redis or memcached
        if (cache.containsKey(cacheKey)) {
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
                        val presignedUrl = jsonObject.getString("body")
                        if (presignedUrl.isNotEmpty()) {
                            cache[cacheKey] = presignedUrl
                            playAudio(presignedUrl)
                        }
                    }
                } else {
                    println("Error while using API")
                }
            }


            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println(t.toString())
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
                        "ExoPlayer",
                        "Error occurred while playing audio. Error: ${error.message}"
                    )
                }
            })
            prepare()
        }
    }
}
