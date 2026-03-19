package com.example.engtest.ui.screen.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.engtest.EngTestApplication

class RecordsViewModelFactory(
    private val application: EngTestApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordsViewModel::class.java)) {
            return RecordsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
