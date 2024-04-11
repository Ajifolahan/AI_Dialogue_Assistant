package com.example.ai_dialogue_assistant.backEnd

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_dialogue_assistant.backEnd.model.languagesItem
import kotlinx.coroutines.launch

class LanguagesViewModel: ViewModel() {
    private val _languageNames = MutableLiveData<List<String>>()
    val languageNames: LiveData<List<String>>
        get() = _languageNames

    private val _languages = MutableLiveData<List<languagesItem>>()
    val languages: LiveData<List<languagesItem>>
        get() = _languages

    init {
        //running it asyncronously
        viewModelScope.launch {
            getLang()
        }
    }

    private suspend fun getLang() {
        val languagesItems = RetrofitClient.languageService.getLanguages()
        _languages.value = languagesItems
        _languageNames.value = languagesItems.map { it.name }

    }
}