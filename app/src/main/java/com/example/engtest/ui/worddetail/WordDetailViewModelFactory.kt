package com.example.engtest.ui.worddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.engtest.EngTestApplication

class WordDetailViewModelFactory(
    private val application: EngTestApplication
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordDetailViewModel::class.java)) {
            return WordDetailViewModel(application.database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
