package com.euysoo.engtest.ui.worddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.euysoo.engtest.di.AppContainer

class WordDetailViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordDetailViewModel::class.java)) {
            return WordDetailViewModel(container.database, container.dictionaryApi) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
