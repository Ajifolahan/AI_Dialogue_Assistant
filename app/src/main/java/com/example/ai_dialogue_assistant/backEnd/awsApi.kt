package com.example.ai_dialogue_assistant.backEnd

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

//for google TTS and amazon polly API
interface awsApi {
        @POST("Speech")
        fun getSpeech(@Body request: HashMap<String, String>): Call<ResponseBody>
}