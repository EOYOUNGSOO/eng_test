package com.euysoo.engtest.ui.screen.wordbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.euysoo.engtest.di.AppContainer

class MyWordBookViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyWordBookViewModel::class.java)) {
            return MyWordBookViewModel(container) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
