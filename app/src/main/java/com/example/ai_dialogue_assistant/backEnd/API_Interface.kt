package com.example.ai_dialogue_assistant.backEnd

import android.os.Build
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.net.InetAddress
import java.net.NetworkInterface

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
    ): Call<List<Conversation>>

    @POST("/conversations/{userId}/{conversationId}/messages")
    fun addMessage(
        @Path("userId") userId: String,
        @Path("conversationId") conversationId: String,
        @Body message: Message
    ): Call<Conversation>

    companion object {
        private fun getBaseUrl(): String {
            val emulatorUrl = "http://10.0.2.2:3000"
            val localIpUrl = "http://${getLocalIpAddress()}:3000"
            return if (Build.PRODUCT.contains("sdk") || Build.MODEL.contains("Emulator")) {
                emulatorUrl
            } else {
                localIpUrl
            }
        }

        private fun getLocalIpAddress(): String {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces().toList()
                for (networkInterface in interfaces) {
                    val addresses = networkInterface.inetAddresses.toList()
                    for (address in addresses) {
                        if (!address.isLoopbackAddress && address is InetAddress) {
                            val hostAddress = address.hostAddress
                            if (hostAddress != null && hostAddress.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))) {
                                return hostAddress
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return "localhost"
        }

        fun create(): API_Interface {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(getBaseUrl())
                .build()
            return retrofit.create(API_Interface::class.java)
        }
    }
}
