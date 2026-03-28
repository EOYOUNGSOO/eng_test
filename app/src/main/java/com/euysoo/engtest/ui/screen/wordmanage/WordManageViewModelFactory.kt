package com.euysoo.engtest.ui.screen.wordmanage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.euysoo.engtest.di.AppContainer

class WordManageViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordManageViewModel::class.java)) {
            return WordManageViewModel(container) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
