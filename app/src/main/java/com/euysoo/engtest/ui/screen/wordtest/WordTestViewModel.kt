package com.euysoo.engtest.ui.screen.wordtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.data.entity.TestResult
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordDifficulty
import com.euysoo.engtest.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WordTestViewModel(
    private val application: EngTestApplication,
    private val difficultyKey: String
) : ViewModel() {

    private val wordDao = application.database.wordDao()
    private val wordBookDao = application.database.wordBookDao()
    private val testResultDao = application.database.testResultDao()

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    /** 단어별 정답 여부 (알고 있음 = true) */
    private val _answers = MutableStateFlow<List<Boolean>>(emptyList())
    val answers: StateFlow<List<Boolean>> = _answers.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(5)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _testStartTime = MutableStateFlow(0L)
    val testStartTime: StateFlow<Long> = _testStartTime.asStateFlow()

    /** 테스트 결과 화면 표시 여부 (10번째 선택 후 true) */
    private val _showResult = MutableStateFlow(false)
    val showResult: StateFlow<Boolean> = _showResult.asStateFlow()

    /** 한글 뜻 노출 중 여부 (버튼 클릭 후 사용자가 "확인"할 때까지 true) */
    private val _showingMeaning = MutableStateFlow(false)
    val showingMeaning: StateFlow<Boolean> = _showingMeaning.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    val bookId = parseMyBookId(difficultyKey)
                    when {
                        bookId != null -> wordBookDao.getRandomWordsFromBook(bookId, 10)
                        difficultyKey == DIFFICULTY_ELEMENTARY -> wordDao.getRandomWordsByDifficulty(WordDifficulty.ELEMENTARY, 10)
                        difficultyKey == DIFFICULTY_MIDDLE -> wordDao.getRandomWordsByDifficulty(WordDifficulty.MIDDLE, 10)
                        difficultyKey == DIFFICULTY_HIGH -> wordDao.getRandomWordsByDifficulty(WordDifficulty.HIGH, 10)
                        else -> wordDao.getRandomWords(10)
                    }
                }
                _words.value = list
                if (list.isNotEmpty()) {
                    _testStartTime.value = System.currentTimeMillis()
                    startTimer()
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "init load words failed", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    private fun startTimer() {
        timerJob?.cancel()
        _remainingSeconds.value = 5
        timerJob = viewModelScope.launch {
            for (s in 5 downTo 1) {
                delay(1000)
                _remainingSeconds.value = s - 1
            }
            onTimeUp()
        }
    }

    /** 알겠음/모름 선택 시 기록 → 한글 뜻 노출 (사용자가 "확인" 누를 때까지 대기) */
    fun recordAnswer(known: Boolean) {
        timerJob?.cancel()
        val list = _words.value
        val idx = _currentIndex.value
        if (idx >= list.size) return
        _answers.value = _answers.value + known
        _showingMeaning.value = true
    }

    /** 결과 확인 화면에서 마지막 선택을 "모름"으로 변경 (알고 있음 선택했을 때만 사용) */
    fun changeLastAnswerToUnknown() {
        val ans = _answers.value
        if (ans.isEmpty()) return
        _answers.value = ans.dropLast(1) + false
    }

    /** 확인 클릭 → 다음 단어 또는 결과 화면으로 이동 */
    fun confirmAndProceed() {
        if (!_showingMeaning.value) return
        val idx = _currentIndex.value
        val list = _words.value
        if (idx < list.size - 1) {
            _showingMeaning.value = false
            _currentIndex.value = idx + 1
            startTimer()
        } else {
            saveResultAndShowResultScreen()
        }
    }

    fun onTimeUp() {
        recordAnswer(false)
    }

    private fun nextWord() {
        val next = _currentIndex.value + 1
        _currentIndex.value = next
        if (next < _words.value.size) {
            startTimer()
        }
    }

    /** 모든 문항을 끝낸 경우에만 기록 저장 후 결과 화면 표시 */
    private fun saveResultAndShowResultScreen() {
        viewModelScope.launch {
            val list = _words.value
            val ans = _answers.value
            if (list.isEmpty() || ans.size != list.size) return@launch
            try {
                val score = ans.count { it }
                val details = list.zip(ans).joinToString(",") { "${it.first.id}:${it.second}" }
                withContext(Dispatchers.IO) {
                    testResultDao.insert(
                        TestResult(
                            testDateMillis = _testStartTime.value,
                            score = score,
                            details = details,
                            difficulty = difficultyKey,
                            testType = TestResult.TEST_TYPE_SELF
                        )
                    )
                }
                _showingMeaning.value = false
                _showResult.value = true
            } catch (e: Exception) {
                AppLogger.e(TAG, "saveResultAndShowResultScreen failed", e)
            }
        }
    }

    /** 결과 화면용: 알고 있음 개수 * 10 */
    fun getScore(): Int = _answers.value.count { it } * 10

    /** 제목용: "yyyy. M. d. HH:mm - N번째 단어 테스트" */
    fun getTitleString(): String {
        val time = _testStartTime.value
        if (time == 0L) return "단어 테스트"
        val formatter = SimpleDateFormat("yyyy. M. d. HH:mm", Locale.getDefault())
        val dateStr = formatter.format(Date(time))
        val n = _currentIndex.value + 1
        return "$dateStr - ${n}번째 단어 테스트"
    }

    private companion object {
        const val TAG = "WordTestVM"
    }
}
