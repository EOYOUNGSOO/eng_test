package com.euysoo.engtest.ui.screen.wordmanage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.data.entity.PHONETIC_UNAVAILABLE
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordBook
import com.euysoo.engtest.data.entity.WordBookEntry
import com.euysoo.engtest.data.entity.WordDifficulty
import com.euysoo.engtest.data.repository.WordSyncManager
import com.euysoo.engtest.domain.model.SyncResult
import com.euysoo.engtest.util.AppLogger
import com.euysoo.engtest.data.repository.PhoneticRepository
import com.euysoo.engtest.util.TestResultDetailsParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 단어 + 테스트 정답/제시 횟수 (정답률·오답률 표시 및 정렬용) */
data class WordWithStats(
    val word: Word,
    val correctCount: Int,
    val totalCount: Int
) {
    val correctRate: Float? = if (totalCount > 0) correctCount.toFloat() / totalCount else null
    val wrongRate: Float? = if (totalCount > 0) (totalCount - correctCount).toFloat() / totalCount else null
}

class WordManageViewModel(
    private val application: EngTestApplication
) : ViewModel() {

    private val wordDao = application.database.wordDao()
    private val wordBookDao = application.database.wordBookDao()
    private val testResultDao = application.database.testResultDao()
    private val phoneticRepository: PhoneticRepository = application.phoneticRepository
    private val syncManager = WordSyncManager(application.database)

    val wordBooks: StateFlow<List<WordBook>> = wordBookDao
        .getAllBooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** 등록된 단어 목록 (전체) */
    private val allWords: StateFlow<List<Word>> = wordDao
        .getAllWords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** 단어별 (정답 횟수, 제시 횟수) — 테스트 결과 details 집계 */
    private val _wordStatsMap = MutableStateFlow<Map<Long, Pair<Int, Int>>>(emptyMap())

    init {
        testResultDao.getAllResults()
            .onEach { results ->
                val map = mutableMapOf<Long, Pair<Int, Int>>()
                results.forEach { result ->
                    TestResultDetailsParser.parseToWordIdAndKnown(result.details).forEach { (wordId, known) ->
                        val (c, t) = map.getOrDefault(wordId, 0 to 0)
                        map[wordId] = (c + if (known) 1 else 0) to (t + 1)
                    }
                }
                _wordStatsMap.value = map
            }
            .launchIn(viewModelScope)
    }

    /** 필터: null = 전체, 그 외 = 해당 난이도만 */
    private val _filter = MutableStateFlow<WordDifficulty?>(null)
    val filter: StateFlow<WordDifficulty?> = _filter.asStateFlow()

    /** 검색어 (영어 단어로 목록 필터) */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** 이번 세션에서 추가된 단어 (DB emit 전에도 맨 위 표시 + 배경 강조용, 난이도 클릭/화면 진입 시 초기화) */
    private val _recentlyAddedWords = MutableStateFlow<List<Word>>(emptyList())
    val recentlyAddedIds: StateFlow<Set<Long>> = _recentlyAddedWords.asStateFlow().map { words ->
        words.map { it.id }.toSet()
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptySet())

    /** 필터 + 검색 적용 + 정렬(정답률 낮은 순, 미제시 단어 맨 뒤)된 목록. 최근 추가 단어는 즉시 맨 위. */
    val wordsWithStats: StateFlow<List<WordWithStats>> = combine(
        allWords,
        _filter,
        _searchQuery,
        _wordStatsMap,
        _recentlyAddedWords
    ) { list, f, query, stats, recentWords ->
        val listWithPending = list + recentWords.filter { w -> w.id !in list.map { it.id } }
        var filtered = if (f == null) listWithPending else listWithPending.filter { it.difficulty == f }
        val q = query.trim()
        if (q.isNotEmpty()) {
            filtered = filtered.filter { it.word.contains(q, ignoreCase = true) }
        }
        val recentIds = recentWords.map { it.id }.toSet()
        val withStats = filtered.map { word ->
            val (c, t) = stats[word.id] ?: (0 to 0)
            WordWithStats(word = word, correctCount = c, totalCount = t)
        }
        val baseOrder = compareBy<WordWithStats> { it.totalCount == 0 }
            .thenBy { it.correctRate ?: Float.MAX_VALUE }
        if (recentIds.isEmpty()) {
            withStats.sortedWith(baseOrder)
        } else {
            withStats.sortedWith(
                compareBy<WordWithStats> { it.word.id !in recentIds }
                    .thenBy { it.totalCount == 0 }
                    .thenBy { it.correctRate ?: Float.MAX_VALUE }
            )
        }
      }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()
    private val _showInitButton = MutableStateFlow(true)
    val showInitButton: StateFlow<Boolean> = _showInitButton.asStateFlow()
    val totalCount: StateFlow<Int> = wordDao
        .getCountFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    /** 편집할 단어 (null이 아니면 편집 다이얼로그 표시) */
    private val _wordToEdit = MutableStateFlow<Word?>(null)
    val wordToEdit: StateFlow<Word?> = _wordToEdit.asStateFlow()

    fun setFilter(difficulty: WordDifficulty?) {
        _recentlyAddedWords.value = emptyList()
        _filter.value = difficulty
    }

    /** 단어 관리 상세 화면 진입 시 호출: 최근 추가 강조 해제 후 기존 정렬/배경으로 표시 */
    fun clearRecentlyAdded() {
        _recentlyAddedWords.value = emptyList()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteWord(word: Word) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { wordDao.delete(word) }
            _recentlyAddedWords.value = _recentlyAddedWords.value.filter { it.id != word.id }
        }
    }

    fun setWordToEdit(word: Word?) {
        _wordToEdit.value = word
    }

    fun updateWord(word: Word) {
        viewModelScope.launch {
            try {
                val toSave = if (word.phonetic.isNullOrBlank()) {
                    word.copy(phonetic = phoneticRepository.getPhonetic(word.word) ?: PHONETIC_UNAVAILABLE)
                } else word
                withContext(Dispatchers.IO) { wordDao.update(toSave) }
                _recentlyAddedWords.value = _recentlyAddedWords.value.map { if (it.id == toSave.id) toSave else it }
                _wordToEdit.value = null
                _showInitButton.value = true
            } catch (e: Exception) {
                AppLogger.e(TAG, "updateWord failed", e)
            }
        }
    }

    /** 신규 단어 추가 (id=0으로 전달하면 DB에서 자동 생성) */
    fun addWord(word: Word) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { wordDao.insert(word) }
            } catch (e: Exception) {
                AppLogger.e(TAG, "addWord failed", e)
            }
        }
    }

    /** 신규 단어 추가 시도. 이미 같은 영어 단어가 있으면 false, 없으면 추가 후 true. 발음 기호가 비어 있으면 API로 조회 후 저장(조회 실패 시 [발음 확인 불가]). */
    suspend fun addWordIfNew(word: Word): Boolean {
        return runCatching {
            val trimmedWord = word.word.trim()
            if (wordDao.countByWord(trimmedWord) > 0) return@runCatching false
            var toInsert = word.copy(word = trimmedWord)
            if (toInsert.phonetic.isNullOrBlank()) {
                val phonetic = phoneticRepository.getPhonetic(trimmedWord) ?: PHONETIC_UNAVAILABLE
                toInsert = toInsert.copy(phonetic = phonetic)
            }
            val newId = withContext(Dispatchers.IO) { wordDao.insert(toInsert) }
            val newWord = toInsert.copy(id = newId)
            _recentlyAddedWords.value = _recentlyAddedWords.value + newWord
            _showInitButton.value = true
            true
        }.onFailure { e -> AppLogger.e(TAG, "addWordIfNew failed", e) }.getOrElse { false }
    }

    /** 발음 기호가 비어 있으면 API로 조회 후 DB 업데이트(조회 실패 시 [발음 확인 불가]). */
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
                val jsonString = withContext(Dispatchers.IO) {
                    val assets = application.applicationContext.assets
                    val fileName = runCatching { assets.open("교육부_필수어휘_3000.json").close(); "교육부_필수어휘_3000.json" }
                        .getOrElse { "교육부_필수어휘_초중고.json" }
                    assets.open(fileName).bufferedReader().use { it.readText() }
                }
                val result = withContext(Dispatchers.IO) { syncManager.sync(jsonString) }
                _syncState.value = SyncUiState.Success(result)
                if (result.addedCount > 0) {
                    _showInitButton.value = false
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "loadInitialWords failed", e)
                _syncState.value = SyncUiState.Error("초기화 실패: ${e.message}")
            }
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncUiState.Idle
    }

    /** 단어장에 단어 추가. 이미 있으면 false */
    suspend fun addWordToWordBook(wordId: Long, bookId: Long): Boolean =
        withContext(Dispatchers.IO) {
            if (wordBookDao.countEntry(bookId, wordId) > 0) return@withContext false
            wordBookDao.insertEntry(WordBookEntry(bookId = bookId, wordId = wordId))
            true
        }

    /** 새 단어장 생성 후 단어 추가. 생성 실패 시 null */
    suspend fun createWordBookAndAddWord(wordId: Long, name: String): Boolean {
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
    data class Success(val result: SyncResult) : SyncUiState()
    data class Error(val message: String) : SyncUiState()
}
