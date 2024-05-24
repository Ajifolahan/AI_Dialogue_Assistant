package com.example.ai_dialogue_assistant.backEnd

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import android.util.Log
import android.util.LruCache
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import org.json.JSONObject


class AmazonPollyService(private val context: Context) {
    private var exoPlayer: ExoPlayer? = null
    private val cache = LruCache<String, String>(50)

    private fun getVoiceForLanguage(language: String): String {

        // mapping between languages and voice
        return when (language) {
            "Arabic" -> "Zeina"
            "Dutch" -> "Laura"
            "Catalan" -> "Arlet"
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


    fun speak(text: String, speaker: String) {
        val voiceId = getVoiceForLanguage(speaker)
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
                            Log.e("API Error", "No body")
                        }
                    }
                } else {
                    Log.e("API Error", "Response unsuccessful")
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


    fun cachevalue() {
        cache.snapshot().forEach { (key, value) ->
            println("Key: $key, Value: $value")
        }
    }
}
