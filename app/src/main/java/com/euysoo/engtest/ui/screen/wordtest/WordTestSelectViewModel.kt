package com.euysoo.engtest.ui.screen.wordtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.data.entity.WordBook
import com.euysoo.engtest.di.AppContainer
import com.euysoo.engtest.util.FlowDefaults
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class WordTestSelectViewModel(
    container: AppContainer,
) : ViewModel() {
    private val wordBookDao = container.database.wordBookDao()

    val books: StateFlow<List<WordBook>> =
        wordBookDao
            .getAllBooks()
            .stateIn(viewModelScope, FlowDefaults.whileSubscribed, emptyList())
}
