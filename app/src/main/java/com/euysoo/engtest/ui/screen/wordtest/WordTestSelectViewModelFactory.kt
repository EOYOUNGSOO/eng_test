package com.euysoo.engtest.ui.screen.wordtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.euysoo.engtest.EngTestApplication

class WordTestSelectViewModelFactory(
    private val application: EngTestApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordTestSelectViewModel::class.java)) {
            return WordTestSelectViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
