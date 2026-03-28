package com.euysoo.engtest.ui.screen.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.R
import com.euysoo.engtest.data.entity.PHONETIC_UNAVAILABLE
import com.euysoo.engtest.data.entity.TestResult
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.di.AppContainer
import com.euysoo.engtest.domain.testresult.TestResultWordStats
import com.euysoo.engtest.domain.testresult.TestResultWordsLoader
import com.euysoo.engtest.util.AppLogger
import com.euysoo.engtest.util.CalendarDateBounds
import com.euysoo.engtest.util.FlowDefaults
import com.euysoo.engtest.util.TimeConstants
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "RecordsVM"

/**
 * 기록 목록(날짜 구간)과 선택한 시험의 상세 단어·통계·발음 보강을 담당한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecordsViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val testResultDao = container.database.testResultDao()
    private val wordDao = container.database.wordDao()
    private val phoneticRepository = container.phoneticRepository
    private val appContext = container.applicationContext
    private val dateBounds = CalendarDateBounds()

    private val _fromMillis = MutableStateFlow(dateBounds.defaultFromMillis())
    private val _toMillis = MutableStateFlow(dateBounds.defaultToEndOfDayMillis())

    val fromMillis: StateFlow<Long> = _fromMillis.asStateFlow()
    val toMillis: StateFlow<Long> = _toMillis.asStateFlow()

    /** DAO Flow를 그대로 stateIn — 중간 MutableStateFlow 복사 제거로 이중 갱신 방지 */
    val resultsList: StateFlow<List<TestResult>> =
        combine(_fromMillis, _toMillis) { f, t -> f to t }
            .flatMapLatest { (f, t) -> testResultDao.getResultsBetween(f, t) }
            .stateIn(
                scope = viewModelScope,
                started = FlowDefaults.whileSubscribed,
                initialValue = emptyList(),
            )

    private var detailLoadJob: Job? = null

    fun setFromMillis(millis: Long) {
        _fromMillis.value = dateBounds.startOfDayMillis(millis)
        if (_toMillis.value < _fromMillis.value) {
            _toMillis.value = _fromMillis.value + TimeConstants.MILLIS_PER_DAY - 1
        }
    }

    fun setToMillis(millis: Long) {
        val endOfDay = dateBounds.startOfDayMillis(millis) + TimeConstants.MILLIS_PER_DAY - 1
        _toMillis.value = endOfDay
        if (_fromMillis.value > _toMillis.value) {
            _fromMillis.value = dateBounds.startOfDayMillis(millis)
        }
    }

    private val _selectedResult = MutableStateFlow<TestResult?>(null)
    val selectedResult: StateFlow<TestResult?> = _selectedResult.asStateFlow()

    private val _resultWords = MutableStateFlow<List<Pair<Word, Boolean>>>(emptyList())
    val resultWords: StateFlow<List<Pair<Word, Boolean>>> = _resultWords.asStateFlow()

    private val _resultWordStats = MutableStateFlow<Map<Long, Pair<Int, Int>>>(emptyMap())
    val resultWordStats: StateFlow<Map<Long, Pair<Int, Int>>> = _resultWordStats.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun consumeSnackbarMessage() {
        _snackbarMessage.value = null
    }

    /**
     * 상세 패널에 표시할 결과를 바꾼다. 이전 로드 [Job]은 취소되며,
     * 단어 목록·누적 통계를 IO에서 로드한 뒤 발음이 비어 있으면 순차로 API를 시도한다.
     */
    fun setSelectedResult(result: TestResult?) {
        detailLoadJob?.cancel()
        detailLoadJob = null
        _selectedResult.value = result
        if (result == null) {
            _resultWords.value = emptyList()
            _resultWordStats.value = emptyMap()
            return
        }
        detailLoadJob =
            viewModelScope.launch {
                try {
                    val words =
                        withContext(Dispatchers.IO) {
                            TestResultWordsLoader.loadWordPairs(result.details, wordDao)
                        }
                    _resultWords.value = words
                    _resultWordStats.value = withContext(Dispatchers.IO) { loadWordStatsMap() }
                    for ((word, _) in words) {
                        ensureActive()
                        if (word.phonetic.isNullOrBlank()) {
                            try {
                                val phonetic = phoneticRepository.getPhonetic(word.word) ?: PHONETIC_UNAVAILABLE
                                withContext(Dispatchers.IO) { wordDao.update(word.copy(phonetic = phonetic)) }
                            } catch (e: Exception) {
                                AppLogger.w(TAG, "fetchPhonetic failed: ${word.word}", e)
                            }
                        }
                    }
                    ensureActive()
                    _resultWords.value =
                        withContext(Dispatchers.IO) {
                            TestResultWordsLoader.loadWordPairs(result.details, wordDao)
                        }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    AppLogger.e(TAG, "setSelectedResult failed", e)
                    _resultWords.value = emptyList()
                    _resultWordStats.value = emptyMap()
                    _snackbarMessage.value = appContext.getString(R.string.snackbar_records_detail_failed)
                }
            }
    }

    private suspend fun loadWordStatsMap(): Map<Long, Pair<Int, Int>> =
        withContext(Dispatchers.IO) {
            val results = testResultDao.getAllResults().first()
            TestResultWordStats.aggregateCorrectTotals(results)
        }

    fun clearSelection() {
        detailLoadJob?.cancel()
        detailLoadJob = null
        _selectedResult.value = null
        _resultWords.value = emptyList()
        _resultWordStats.value = emptyMap()
    }
}
