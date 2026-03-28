package com.euysoo.engtest.ui.screen.wordtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.euysoo.engtest.EngTestApplication

class MultipleChoiceTestViewModelFactory(
    private val application: EngTestApplication,
    private val difficultyKey: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MultipleChoiceTestViewModel::class.java)) {
            return MultipleChoiceTestViewModel(application, difficultyKey) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
