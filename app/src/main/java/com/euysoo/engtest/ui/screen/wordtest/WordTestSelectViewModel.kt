package com.euysoo.engtest.ui.screen.wordtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.data.entity.WordBook
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class WordTestSelectViewModel(
    application: EngTestApplication
) : ViewModel() {

    private val wordBookDao = application.database.wordBookDao()

    val books: StateFlow<List<WordBook>> = wordBookDao
        .getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
