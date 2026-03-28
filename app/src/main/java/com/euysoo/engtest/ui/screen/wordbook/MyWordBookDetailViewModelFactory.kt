package com.euysoo.engtest.ui.screen.wordbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.euysoo.engtest.EngTestApplication

class MyWordBookDetailViewModelFactory(
    private val application: EngTestApplication,
    private val bookId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyWordBookDetailViewModel::class.java)) {
            return MyWordBookDetailViewModel(application, bookId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
