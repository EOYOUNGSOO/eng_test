package com.euysoo.engtest.ui.screen.wordbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.euysoo.engtest.di.AppContainer

class MyWordBookDetailViewModelFactory(
    private val container: AppContainer,
    private val bookId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyWordBookDetailViewModel::class.java)) {
            return MyWordBookDetailViewModel(container, bookId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
