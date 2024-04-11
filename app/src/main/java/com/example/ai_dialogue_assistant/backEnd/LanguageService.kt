package com.example.ai_dialogue_assistant.backEnd

import com.example.ai_dialogue_assistant.BuildConfig
import com.example.ai_dialogue_assistant.backEnd.model.languagesItem
import retrofit2.http.GET
import retrofit2.http.Headers

interface LanguageService {
    //launching it inside a coroutine so we dont block the UI
    @Headers(
        BuildConfig.LANGUAGE_KEY,
        "X-RapidAPI-Host: list-of-all-countries-and-languages-with-their-codes.p.rapidapi.com"
    )
    @GET("/languages")
    suspend fun getLanguages(): List<languagesItem>

    companion object {

        // base URL for the REST API
        const val BASE_URL =
            "https://list-of-all-countries-and-languages-with-their-codes.p.rapidapi.com"

    }
}