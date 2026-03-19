package com.example.engtest.ui.screen.wordmanage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.engtest.EngTestApplication

class WordManageViewModelFactory(
    private val application: EngTestApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordManageViewModel::class.java)) {
            return WordManageViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
