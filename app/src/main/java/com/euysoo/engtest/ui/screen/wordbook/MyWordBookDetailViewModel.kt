package com.euysoo.engtest.ui.screen.wordbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordBook
import com.euysoo.engtest.data.entity.WordBookEntry
import com.euysoo.engtest.data.entity.WordWithBookEntryMeta
import com.euysoo.engtest.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class MyWordBookDetailViewModel(
    private val application: EngTestApplication,
    val bookId: Long
) : ViewModel() {

    private val wordBookDao = application.database.wordBookDao()
    private val wordDao = application.database.wordDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _rawSearchResults = MutableStateFlow<List<Word>>(emptyList())

    private val _book = MutableStateFlow<WordBook?>(null)
    val book: StateFlow<WordBook?> = _book.asStateFlow()

    private val _highlightWordIds = MutableStateFlow<Set<Long>>(emptySet())
    val highlightWordIds: StateFlow<Set<Long>> = _highlightWordIds.asStateFlow()

    val wordItems: StateFlow<List<WordWithBookEntryMeta>> = if (bookId > 0) {
        wordBookDao.getWordsInBook(bookId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } else {
        flowOf<List<WordWithBookEntryMeta>>(emptyList())
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }

    init {
        if (bookId > 0) {
            viewModelScope.launch {
                _book.value = withContext(Dispatchers.IO) { wordBookDao.getBookById(bookId) }
            }
        }
        viewModelScope.launch {
            _searchQuery
                .debounce(250)
                .distinctUntilChanged()
                .collect { q ->
                    val trimmed = q.trim()
                    _rawSearchResults.value = if (trimmed.isEmpty()) {
                        emptyList()
                    } else {
                        withContext(Dispatchers.IO) {
                            wordDao.searchWordsLike(trimmed)
                        }
                    }
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** 이미 단어장에 담긴 단어는 제외한 검색 결과 */
    val searchResultsNotInBook: StateFlow<List<Word>> = combine(
        _rawSearchResults,
        wordItems
    ) { results, items ->
        val inIds = items.map { it.word.id }.toSet()
        results.filter { it.id !in inIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeWord(wordId: Long) {
        if (bookId <= 0) return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    wordBookDao.deleteEntry(bookId, wordId)
                }
                _highlightWordIds.update { it - wordId }
            } catch (e: Exception) {
                AppLogger.e(TAG, "removeWord failed", e)
            }
        }
    }

    fun addWordToBook(wordId: Long) {
        if (bookId <= 0) return
        viewModelScope.launch {
            try {
                val actuallyAdded = withContext(Dispatchers.IO) {
                    if (wordBookDao.countEntry(bookId, wordId) > 0) {
                        false
                    } else {
                        wordBookDao.insertEntry(
                            WordBookEntry(
                                bookId = bookId,
                                wordId = wordId,
                                addedAt = System.currentTimeMillis()
                            )
                        )
                        true
                    }
                }
                if (actuallyAdded) {
                    _highlightWordIds.update { it + wordId }
                    delay(4_000)
                    _highlightWordIds.update { it - wordId }
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "addWordToBook failed", e)
            }
        }
    }

    private companion object {
        const val TAG = "MyWordBookDetailVM"
    }
}
