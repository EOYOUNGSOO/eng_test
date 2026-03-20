package com.example.engtest.ui.screen.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engtest.EngTestApplication
import com.example.engtest.data.entity.PHONETIC_UNAVAILABLE
import com.example.engtest.util.AppLogger
import com.example.engtest.data.entity.TestResult
import com.example.engtest.data.entity.Word
import com.example.engtest.data.repository.PhoneticRepository
import com.example.engtest.util.TestResultDetailsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

private const val TAG = "RecordsVM"

@OptIn(ExperimentalCoroutinesApi::class)
class RecordsViewModel(
    private val application: EngTestApplication
) : ViewModel() {

    private val testResultDao = application.database.testResultDao()
    private val wordDao = application.database.wordDao()
    private val phoneticRepository: PhoneticRepository = application.phoneticRepository
    private val calendar = Calendar.getInstance(Locale.getDefault())

    private fun startOfDayMillis(millis: Long): Long {
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun defaultFromMillis(): Long {
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_MONTH, -30)
        return startOfDayMillis(calendar.timeInMillis)
    }

    private fun defaultToMillis(): Long {
        return startOfDayMillis(System.currentTimeMillis()) + 86400_000L - 1
    }

    private val _fromMillis = MutableStateFlow(defaultFromMillis())
    private val _toMillis = MutableStateFlow(defaultToMillis())

    val fromMillis: StateFlow<Long> = _fromMillis.asStateFlow()
    val toMillis: StateFlow<Long> = _toMillis.asStateFlow()

    private val _resultsList = MutableStateFlow<List<TestResult>>(emptyList())
    val resultsList: StateFlow<List<TestResult>> = _resultsList.asStateFlow()

    init {
        combine(_fromMillis, _toMillis) { f, t -> Pair(f, t) }
            .flatMapLatest { (f, t) -> testResultDao.getResultsBetween(f, t) }
            .onEach { list ->
                _resultsList.value = list
            }
            .launchIn(viewModelScope)
    }

    fun setDateRange(fromMillis: Long, toMillis: Long) {
        _fromMillis.value = startOfDayMillis(fromMillis)
        _toMillis.value = startOfDayMillis(toMillis) + 86400_000L - 1
    }

    fun setFromMillis(millis: Long) {
        _fromMillis.value = startOfDayMillis(millis)
        if (_toMillis.value < _fromMillis.value) {
            _toMillis.value = _fromMillis.value + 86400_000L - 1
        }
    }

    fun setToMillis(millis: Long) {
        val endOfDay = startOfDayMillis(millis) + 86400_000L - 1
        _toMillis.value = endOfDay
        if (_fromMillis.value > _toMillis.value) {
            _fromMillis.value = startOfDayMillis(millis)
        }
    }

    private val _selectedResult = MutableStateFlow<TestResult?>(null)
    val selectedResult: StateFlow<TestResult?> = _selectedResult.asStateFlow()

    private val _resultWords = MutableStateFlow<List<Pair<Word, Boolean>>>(emptyList())
    val resultWords: StateFlow<List<Pair<Word, Boolean>>> = _resultWords.asStateFlow()

    /** 단어별 (정답 횟수, 시도 횟수) — 테스트 결과 상세 4줄에서 정답율/오답율/시도회수 표시용 */
    private val _resultWordStats = MutableStateFlow<Map<Long, Pair<Int, Int>>>(emptyMap())
    val resultWordStats: StateFlow<Map<Long, Pair<Int, Int>>> = _resultWordStats.asStateFlow()

    fun setSelectedResult(result: TestResult?) {
        _selectedResult.value = result
        if (result == null) {
            _resultWords.value = emptyList()
            _resultWordStats.value = emptyMap()
            return
        }
        viewModelScope.launch {
            try {
                _resultWords.value = withContext(Dispatchers.IO) { loadWordsByDetails(result.details) }
                _resultWordStats.value = withContext(Dispatchers.IO) { loadWordStatsMap() }
                _resultWords.value.forEach { (word, _) ->
                    if (word.phonetic.isNullOrBlank()) {
                        try {
                            val phonetic = phoneticRepository.getPhonetic(word.word) ?: PHONETIC_UNAVAILABLE
                            withContext(Dispatchers.IO) { wordDao.update(word.copy(phonetic = phonetic)) }
                        } catch (e: Exception) {
                            AppLogger.w(TAG, "fetchPhonetic failed: ${word.word}", e)
                        }
                    }
                }
                _resultWords.value = withContext(Dispatchers.IO) { loadWordsByDetails(result.details) }
            } catch (e: Exception) {
                AppLogger.e(TAG, "setSelectedResult failed", e)
            }
        }
    }

    /** 전체 테스트 이력에서 단어별 (정답 횟수, 시도 횟수) 집계 */
    private suspend fun loadWordStatsMap(): Map<Long, Pair<Int, Int>> = withContext(Dispatchers.IO) {
        val results = testResultDao.getAllResults().first()
        val map = mutableMapOf<Long, Pair<Int, Int>>()
        results.forEach { testResult ->
            TestResultDetailsParser.parseToWordIdAndKnown(testResult.details).forEach { (wordId, known) ->
                val (c, t) = map.getOrDefault(wordId, 0 to 0)
                map[wordId] = (c + if (known) 1 else 0) to (t + 1)
            }
        }
        map
    }

    /** details 문자열 파싱 후 단어 ID 목록으로 일괄 조회 (N+1 방지) */
    private suspend fun loadWordsByDetails(details: String): List<Pair<Word, Boolean>> {
        val idAndKnown = TestResultDetailsParser.parseToWordIdAndKnown(details)
        if (idAndKnown.isEmpty()) return emptyList()
        val ids = idAndKnown.map { it.first }.distinct()
        val wordsById = wordDao.getWordsByIds(ids).associateBy { it.id }
        return idAndKnown.mapNotNull { (id, known) ->
            wordsById[id]?.let { word -> word to known }
        }
    }

    fun clearSelection() {
        _selectedResult.value = null
        _resultWords.value = emptyList()
        _resultWordStats.value = emptyMap()
    }
}
