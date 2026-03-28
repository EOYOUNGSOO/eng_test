package com.euysoo.engtest.ui.screen.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.euysoo.engtest.di.AppContainer

class RecordsViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordsViewModel::class.java)) {
            return RecordsViewModel(container) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
