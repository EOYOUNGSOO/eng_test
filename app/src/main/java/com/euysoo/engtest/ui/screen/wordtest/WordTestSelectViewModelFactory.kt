package com.euysoo.engtest.ui.screen.wordtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.euysoo.engtest.di.AppContainer

class WordTestSelectViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordTestSelectViewModel::class.java)) {
            return WordTestSelectViewModel(container) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
