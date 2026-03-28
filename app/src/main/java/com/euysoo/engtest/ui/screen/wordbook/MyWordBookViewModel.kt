package com.euysoo.engtest.ui.screen.wordbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.data.entity.WordBook
import com.euysoo.engtest.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyWordBookViewModel(
    private val application: EngTestApplication
) : ViewModel() {

    private val wordBookDao = application.database.wordBookDao()

    val books: StateFlow<List<WordBook>> = wordBookDao
        .getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createBook(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    wordBookDao.insertBook(WordBook(name = trimmed))
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "createBook failed", e)
            }
        }
    }

    fun renameBook(book: WordBook, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    wordBookDao.updateBook(book.copy(name = trimmed))
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "renameBook failed", e)
            }
        }
    }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { wordBookDao.deleteBookById(bookId) }
            } catch (e: Exception) {
                AppLogger.e(TAG, "deleteBook failed", e)
            }
        }
    }

    private companion object {
        const val TAG = "MyWordBookVM"
    }
}
