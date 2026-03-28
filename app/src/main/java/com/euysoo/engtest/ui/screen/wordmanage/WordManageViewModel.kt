package com.euysoo.engtest.ui.screen.wordmanage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.R
import com.euysoo.engtest.data.assets.EducationVocabAssets
import com.euysoo.engtest.data.entity.PHONETIC_UNAVAILABLE
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordBook
import com.euysoo.engtest.data.entity.WordBookEntry
import com.euysoo.engtest.data.entity.WordDifficulty
import com.euysoo.engtest.di.AppContainer
import com.euysoo.engtest.domain.model.SyncResult
import com.euysoo.engtest.domain.model.WordWithStats
import com.euysoo.engtest.domain.model.WordWithStatsListBuilder
import com.euysoo.engtest.domain.testresult.TestResultWordStats
import com.euysoo.engtest.util.AppLogger
import com.euysoo.engtest.util.FlowDefaults
import com.euysoo.engtest.util.UiFlowConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AddWordOutcome {
    ADDED,
    DUPLICATE,
    FAILED,
}

/**
 * 단어 목록·필터·검색·동기화·단어장 연동을 담당한다.
 * [wordsWithStats]는 전체 단어 Flow, 디바운스 검색, 시험 통계, 최근 추가 목록을 합성한다.
 */
@OptIn(FlowPreview::class)
class WordManageViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val wordDao = container.database.wordDao()
    private val wordBookDao = container.database.wordBookDao()
    private val testResultDao = container.database.testResultDao()
    private val phoneticRepository = container.phoneticRepository
    private val syncManager get() = container.wordSyncManager
    private val appContext = container.applicationContext

    val wordBooks: StateFlow<List<WordBook>> =
        wordBookDao
            .getAllBooks()
            .stateIn(
                scope = viewModelScope,
                started = FlowDefaults.whileSubscribed,
                initialValue = emptyList(),
            )

    private val allWords: StateFlow<List<Word>> =
        wordDao
            .getAllWords()
            .stateIn(
                scope = viewModelScope,
                started = FlowDefaults.whileSubscribed,
                initialValue = emptyList(),
            )

    private val wordStatsMapState = MutableStateFlow<Map<Long, Pair<Int, Int>>>(emptyMap())

    init {
        testResultDao
            .getAllResults()
            .onEach { results ->
                wordStatsMapState.value = TestResultWordStats.aggregateCorrectTotals(results)
            }.launchIn(viewModelScope)
    }

    private val _filter = MutableStateFlow<WordDifficulty?>(null)
    val filter: StateFlow<WordDifficulty?> = _filter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val debouncedSearchQuery =
        _searchQuery
            .debounce(UiFlowConstants.WORD_MANAGE_SEARCH_DEBOUNCE_MS)
            .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _recentlyAddedWords = MutableStateFlow<List<Word>>(emptyList())
    val recentlyAddedIds: StateFlow<Set<Long>> =
        _recentlyAddedWords
            .asStateFlow()
            .map { words ->
                words.map { it.id }.toSet()
            }.stateIn(
                scope = viewModelScope,
                started = FlowDefaults.whileSubscribed,
                initialValue = emptySet(),
            )

    val wordsWithStats: StateFlow<List<WordWithStats>> =
        combine(
            allWords,
            _filter,
            debouncedSearchQuery,
            wordStatsMapState,
            _recentlyAddedWords,
        ) { list, f, query, stats, recentWords ->
            WordWithStatsListBuilder.build(list, f, query, stats, recentWords)
        }.stateIn(
            scope = viewModelScope,
            started = FlowDefaults.whileSubscribed,
            initialValue = emptyList(),
        )

    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()
    private val _showInitButton = MutableStateFlow(true)
    val showInitButton: StateFlow<Boolean> = _showInitButton.asStateFlow()
    val totalCount: StateFlow<Int> =
        wordDao
            .getCountFlow()
            .stateIn(
                scope = viewModelScope,
                started = FlowDefaults.whileSubscribed,
                initialValue = 0,
            )

    private val _wordToEdit = MutableStateFlow<Word?>(null)
    val wordToEdit: StateFlow<Word?> = _wordToEdit.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun consumeSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun setFilter(difficulty: WordDifficulty?) {
        _recentlyAddedWords.value = emptyList()
        _filter.value = difficulty
    }

    fun clearRecentlyAdded() {
        _recentlyAddedWords.value = emptyList()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteWord(word: Word) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { wordDao.delete(word) }
                _recentlyAddedWords.value = _recentlyAddedWords.value.filter { it.id != word.id }
            } catch (e: Exception) {
                AppLogger.e(TAG, "deleteWord failed", e)
                _snackbarMessage.value = appContext.getString(R.string.snackbar_word_delete_failed)
            }
        }
    }

    fun setWordToEdit(word: Word?) {
        _wordToEdit.value = word
    }

    fun updateWord(word: Word) {
        viewModelScope.launch {
            try {
                val toSave =
                    if (word.phonetic.isNullOrBlank()) {
                        word.copy(phonetic = phoneticRepository.getPhonetic(word.word) ?: PHONETIC_UNAVAILABLE)
                    } else {
                        word
                    }
                withContext(Dispatchers.IO) { wordDao.update(toSave) }
                _recentlyAddedWords.value = _recentlyAddedWords.value.map { if (it.id == toSave.id) toSave else it }
                _wordToEdit.value = null
                _showInitButton.value = true
            } catch (e: Exception) {
                AppLogger.e(TAG, "updateWord failed", e)
                _snackbarMessage.value = appContext.getString(R.string.snackbar_word_save_failed)
            }
        }
    }

    fun addWord(word: Word) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { wordDao.insert(word) }
            } catch (e: Exception) {
                AppLogger.e(TAG, "addWord failed", e)
                _snackbarMessage.value = appContext.getString(R.string.snackbar_word_add_failed)
            }
        }
    }

    suspend fun addWordIfNew(word: Word): AddWordOutcome {
        return runCatching {
            val trimmedWord = word.word.trim()
            if (wordDao.countByWord(trimmedWord) > 0) return@runCatching AddWordOutcome.DUPLICATE
            var toInsert = word.copy(word = trimmedWord)
            if (toInsert.phonetic.isNullOrBlank()) {
                val phonetic = phoneticRepository.getPhonetic(trimmedWord) ?: PHONETIC_UNAVAILABLE
                toInsert = toInsert.copy(phonetic = phonetic)
            }
            val newId = withContext(Dispatchers.IO) { wordDao.insert(toInsert) }
            val newWord = toInsert.copy(id = newId)
            _recentlyAddedWords.value = _recentlyAddedWords.value + newWord
            _showInitButton.value = true
            AddWordOutcome.ADDED
        }.onFailure { e ->
            AppLogger.e(TAG, "addWordIfNew failed", e)
        }.getOrElse { AddWordOutcome.FAILED }
    }

    fun fetchPhoneticForWordIfNeeded(word: Word) {
        if (!word.phonetic.isNullOrBlank()) return
        viewModelScope.launch {
            try {
                val phonetic = phoneticRepository.getPhonetic(word.word) ?: PHONETIC_UNAVAILABLE
                withContext(Dispatchers.IO) { wordDao.update(word.copy(phonetic = phonetic)) }
            } catch (e: Exception) {
                AppLogger.e(TAG, "fetchPhoneticForWordIfNeeded failed", e)
            }
        }
    }

    fun loadInitialWords() {
        viewModelScope.launch {
            _syncState.value = SyncUiState.Loading
            try {
                val jsonString =
                    withContext(Dispatchers.IO) {
                        EducationVocabAssets.readJsonOrThrow(appContext.assets)
                    }
                val result = withContext(Dispatchers.IO) { syncManager.sync(jsonString) }
                _syncState.value = SyncUiState.Success(result)
                if (result.addedCount > 0) {
                    _showInitButton.value = false
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "loadInitialWords failed", e)
                _syncState.value =
                    SyncUiState.Error(
                        appContext.getString(R.string.word_manage_sync_init_failed, e.message ?: ""),
                    )
            }
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncUiState.Idle
    }

    suspend fun addWordToWordBook(
        wordId: Long,
        bookId: Long,
    ): Boolean =
        withContext(Dispatchers.IO) {
            if (wordBookDao.countEntry(bookId, wordId) > 0) return@withContext false
            wordBookDao.insertEntry(WordBookEntry(bookId = bookId, wordId = wordId))
            true
        }

    suspend fun createWordBookAndAddWord(
        wordId: Long,
        name: String,
    ): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        return runCatching {
            withContext(Dispatchers.IO) {
                val bookId = wordBookDao.insertBook(WordBook(name = trimmed))
                wordBookDao.insertEntry(WordBookEntry(bookId = bookId, wordId = wordId))
            }
            true
        }.onFailure { e -> AppLogger.e(TAG, "createWordBookAndAddWord failed", e) }
            .getOrElse { false }
    }

    private companion object {
        const val TAG = "WordManageVM"
    }
}

sealed class SyncUiState {
    data object Idle : SyncUiState()

    data object Loading : SyncUiState()

    data class Success(
        val result: SyncResult,
    ) : SyncUiState()

    data class Error(
        val message: String,
    ) : SyncUiState()
}
