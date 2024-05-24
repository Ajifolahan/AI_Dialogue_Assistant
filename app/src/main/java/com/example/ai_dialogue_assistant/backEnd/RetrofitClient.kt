package com.example.ai_dialogue_assistant.backEnd

import com.example.ai_dialogue_assistant.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    //for logging and troubleshooting API result
    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private val httpClient = OkHttpClient.Builder().addInterceptor(logging).build()

    // Retrofit instance with the Gson converter factory and the OkHttpClient
    val pollyService: AmazonPollyApi by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.API_POLLY)
            .client(httpClient)
            .build()
            .create(AmazonPollyApi::class.java)

    }

}