package com.example.engtest.ui.screen.wordtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.engtest.EngTestApplication

class WordTestViewModelFactory(
    private val application: EngTestApplication,
    private val difficultyKey: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordTestViewModel::class.java)) {
            return WordTestViewModel(application, difficultyKey) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
