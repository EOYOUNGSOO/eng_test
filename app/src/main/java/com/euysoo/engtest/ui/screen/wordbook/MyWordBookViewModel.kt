package com.euysoo.engtest.ui.screen.wordbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.data.entity.WordBook
import com.euysoo.engtest.di.AppContainer
import com.euysoo.engtest.domain.wrongnote.WrongNoteBookRepository
import com.euysoo.engtest.domain.wrongnote.WrongNoteDifficultyOption
import com.euysoo.engtest.domain.wrongnote.WrongNoteFillSelection
import com.euysoo.engtest.domain.wrongnote.WrongNoteOutcome
import com.euysoo.engtest.util.AppLogger
import com.euysoo.engtest.util.FlowDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyWordBookViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val wordBookDao = container.database.wordBookDao()
    private val wrongNoteRepo =
        WrongNoteBookRepository(
            container.database.testResultDao(),
            container.database.wordDao(),
            wordBookDao,
        )

    val books: StateFlow<List<WordBook>> =
        wordBookDao
            .getAllBooks()
            .stateIn(viewModelScope, FlowDefaults.whileSubscribed, emptyList())

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

    fun renameBook(
        book: WordBook,
        newName: String,
    ) {
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

    fun createWrongNoteBook(
        title: String,
        difficulty: WrongNoteDifficultyOption,
        fill: WrongNoteFillSelection,
        onOutcome: (WrongNoteOutcome) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val outcome =
                    withContext(Dispatchers.IO) {
                        wrongNoteRepo.createWrongNoteBook(title, difficulty, fill)
                    }
                onOutcome(outcome)
            } catch (e: Exception) {
                AppLogger.e(TAG, "createWrongNoteBook failed", e)
                onOutcome(WrongNoteOutcome.CreateFailed)
            }
        }
    }

    private companion object {
        const val TAG = "MyWordBookVM"
    }
}
