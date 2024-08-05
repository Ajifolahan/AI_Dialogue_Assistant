package com.example.ai_dialogue_assistant.backEnd

import com.example.ai_dialogue_assistant.BuildConfig
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface API_Interface {
    @POST("/conversations")
    fun createConversation(@Body conversation: Conversation): Call<Conversation>

    @GET("/conversations/{userId}/{conversationId}")
    fun getConversation(
        @Path("userId") userId: String,
        @Path("conversationId") conversationId: String
    ): Call<Conversation>

    @GET("/conversations/{userId}")
    fun getConversations(
        @Path("userId") userId: String
    ): Call<Conversation>

    @GET("/conversations/{userId}")
    fun getAllConversations(
        @Path("userId") userId: String
    ): Call<List<Conversation>>



    @POST("/conversations/{userId}/{conversationId}/messages")
    fun addMessage(
        @Path("userId") userId: String,
        @Path("conversationId") conversationId: String,
        @Body message: Message
    ): Call<Conversation>

    @DELETE("/conversations/{userId}/{conversationId}")
    fun deleteConversation(
        @Path("userId") userId: String,
        @Path("conversationId") conversationId: String
    ): Call<Unit>

    companion object {
        private const val BASE_URL = BuildConfig.VERCEL_URL

        fun create(): API_Interface {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(API_Interface::class.java)
        }
    }
}



