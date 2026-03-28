package com.euysoo.engtest.ui.screen.wordtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.euysoo.engtest.di.AppContainer

class WordTestViewModelFactory(
    private val container: AppContainer,
    private val difficultyKey: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordTestViewModel::class.java)) {
            return WordTestViewModel(container, difficultyKey) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
