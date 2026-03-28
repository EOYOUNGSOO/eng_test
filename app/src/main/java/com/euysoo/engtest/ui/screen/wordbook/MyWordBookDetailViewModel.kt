package com.euysoo.engtest.ui.screen.wordbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordBook
import com.euysoo.engtest.data.entity.WordBookEntry
import com.euysoo.engtest.data.entity.WordWithBookEntryMeta
import com.euysoo.engtest.di.AppContainer
import com.euysoo.engtest.util.AppLogger
import com.euysoo.engtest.util.FlowDefaults
import com.euysoo.engtest.util.UiFlowConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
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

/**
 * 단어장 상세: 단어 목록, 검색(디바운스), 단어 추가·제거.
 * 검색 결과는 [wordItems]에 이미 있는 단어는 [searchResultsNotInBook]에서 제외한다.
 */
@OptIn(FlowPreview::class)
class MyWordBookDetailViewModel(
    private val container: AppContainer,
    val bookId: Long,
) : ViewModel() {
    private val wordBookDao = container.database.wordBookDao()
    private val wordDao = container.database.wordDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val rawSearchResultsState = MutableStateFlow<List<Word>>(emptyList())

    private val _book = MutableStateFlow<WordBook?>(null)
    val book: StateFlow<WordBook?> = _book.asStateFlow()

    private val _highlightWordIds = MutableStateFlow<Set<Long>>(emptySet())
    val highlightWordIds: StateFlow<Set<Long>> = _highlightWordIds.asStateFlow()

    val wordItems: StateFlow<List<WordWithBookEntryMeta>> =
        if (bookId > 0) {
            wordBookDao
                .getWordsInBook(bookId)
                .stateIn(viewModelScope, FlowDefaults.whileSubscribed, emptyList())
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
                .debounce(UiFlowConstants.SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { q ->
                    try {
                        val trimmed = q.trim()
                        rawSearchResultsState.value =
                            if (trimmed.isEmpty()) {
                                emptyList()
                            } else {
                                withContext(Dispatchers.IO) {
                                    wordDao.searchWordsLike(trimmed)
                                }
                            }
                    } catch (e: Exception) {
                        AppLogger.e(TAG, "search collect failed", e)
                        rawSearchResultsState.value = emptyList()
                    }
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** 이미 단어장에 담긴 단어는 제외한 검색 결과 */
    val searchResultsNotInBook: StateFlow<List<Word>> =
        combine(
            rawSearchResultsState,
            wordItems,
        ) { results, items ->
            val inIds = items.map { it.word.id }.toSet()
            results.filter { it.id !in inIds }
        }.stateIn(viewModelScope, FlowDefaults.whileSubscribed, emptyList())

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
                val actuallyAdded =
                    withContext(Dispatchers.IO) {
                        if (wordBookDao.countEntry(bookId, wordId) > 0) {
                            false
                        } else {
                            wordBookDao.insertEntry(
                                WordBookEntry(
                                    bookId = bookId,
                                    wordId = wordId,
                                    addedAt = System.currentTimeMillis(),
                                ),
                            )
                            true
                        }
                    }
                if (actuallyAdded) {
                    _highlightWordIds.update { it + wordId }
                    delay(UiFlowConstants.WORD_BOOK_HIGHLIGHT_CLEAR_DELAY_MS)
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
