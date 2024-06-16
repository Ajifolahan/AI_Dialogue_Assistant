package com.example.ai_dialogue_assistant.backEnd

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.*

interface API_Interface {
    @POST("/conversations")
    fun createConversation(@Body conversation: Conversation): Call<Conversation>


    @GET("/conversations/{userId}/{conversationId}")
    fun getConversation(
        @Path("userId") userId: String,
        @Path("conversationId") conversationId: String
    ): Call<Conversation>

    @POST("/conversations/{userId}/{conversationId}/messages")
    fun addMessage(
        @Path("userId") userId: String,
        @Path("conversationId") conversationId: String,
        @Body message: Message
    ): Call<Conversation>

    companion object {
        // API URL - RUN THE BACKEND FIRST
        private const val BASE_URL = "http://10.0.2.2:3000"

        fun create(): API_Interface {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(API_Interface::class.java)
        }
    }
}
