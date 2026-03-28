package com.euysoo.engtest.ui.screen.wordbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.euysoo.engtest.EngTestApplication

class MyWordBookViewModelFactory(
    private val application: EngTestApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyWordBookViewModel::class.java)) {
            return MyWordBookViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
