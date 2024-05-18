package com.example.ai_dialogue_assistant.backEnd

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LanguagesViewModel: ViewModel() {
    private val _languageNames = MutableLiveData<List<String>>()
    val languageNames: LiveData<List<String>>
        get() = _languageNames

    init {
        //running it asyncronously
        viewModelScope.launch {
            getLang()
        }
    }

    private fun getLang() {
        val call = RetrofitClient.languageService.getLanguages()
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        val responseBodyString = responseBody.string()
                        val jsonObject = JSONObject(responseBodyString)
                        if (jsonObject.getInt("statusCode") == 200) {
                            val bodyString = jsonObject.getString("body")
                            val cleanedResponse = bodyString.trim('"').replace("\\\"", "\"")
                            val languageNames: List<String> = Gson().fromJson(cleanedResponse, object : TypeToken<List<String>>() {}.type)
                            _languageNames.postValue(languageNames)
                        }
                    }
                } else {
                    Log.e("LanguagesViewModel", "Failed to get languages: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("LanguagesViewModel", "Failed to get languages: ${t.message}")
            }
        })
    }
}