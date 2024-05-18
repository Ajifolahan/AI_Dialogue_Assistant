package com.example.ai_dialogue_assistant.backEnd

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface LanguageService {
    //launching it inside a coroutine so we dont block the UI
    @GET("languages")
    fun getLanguages(): Call<ResponseBody>
}