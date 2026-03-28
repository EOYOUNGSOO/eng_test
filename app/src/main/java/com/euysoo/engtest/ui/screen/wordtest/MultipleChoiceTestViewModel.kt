package com.euysoo.engtest.ui.screen.wordtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.data.entity.TestResult
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordDifficulty
import com.euysoo.engtest.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class McQuestion(
    val word: Word,
    val options: List<String>,
    val correctIndex: Int
)

class MultipleChoiceTestViewModel(
    private val application: EngTestApplication,
    private val difficultyKey: String
) : ViewModel() {

    private val wordDao = application.database.wordDao()
    private val wordBookDao = application.database.wordBookDao()
    private val testResultDao = application.database.testResultDao()

    private val _questions = MutableStateFlow<List<McQuestion>>(emptyList())
    val questions: StateFlow<List<McQuestion>> = _questions.asStateFlow()

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _answers = MutableStateFlow<List<Boolean>>(emptyList())
    val answers: StateFlow<List<Boolean>> = _answers.asStateFlow()

    private val _showResult = MutableStateFlow(false)
    val showResult: StateFlow<Boolean> = _showResult.asStateFlow()

    private val _testStartTime = MutableStateFlow(0L)
    val testStartTime: StateFlow<Long> = _testStartTime.asStateFlow()

    private val _loadFinished = MutableStateFlow(false)
    val loadFinished: StateFlow<Boolean> = _loadFinished.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val list = withContext(Dispatchers.IO) { loadWordsForTest() }
                if (list.isEmpty()) {
                    _words.value = emptyList()
                    _questions.value = emptyList()
                    _loadFinished.value = true
                    return@launch
                }
                val qs = withContext(Dispatchers.IO) { list.map { buildQuestion(it) } }
                _words.value = list
                _questions.value = qs
                _testStartTime.value = System.currentTimeMillis()
                _loadFinished.value = true
            } catch (e: Exception) {
                AppLogger.e(TAG, "init failed", e)
                _loadFinished.value = true
            }
        }
    }

    private suspend fun loadWordsForTest(): List<Word> {
        val bookId = parseMyBookId(difficultyKey)
        return when {
            bookId != null -> wordBookDao.getRandomWordsFromBook(bookId, 10)
            difficultyKey == DIFFICULTY_ELEMENTARY -> wordDao.getRandomWordsByDifficulty(WordDifficulty.ELEMENTARY, 10)
            difficultyKey == DIFFICULTY_MIDDLE -> wordDao.getRandomWordsByDifficulty(WordDifficulty.MIDDLE, 10)
            difficultyKey == DIFFICULTY_HIGH -> wordDao.getRandomWordsByDifficulty(WordDifficulty.HIGH, 10)
            else -> wordDao.getRandomWords(10)
        }
    }

    private suspend fun pickWrongMeanings(correct: Word, need: Int): List<String> {
        val blockedLower = mutableSetOf(correct.meaning.trim().lowercase())
        val result = mutableListOf<String>()
        var guard = 0
        while (result.size < need && guard < 30) {
            val batch = wordDao.getRandomWords(40)
            for (w in batch) {
                if (w.id == correct.id) continue
                val m = w.meaning.trim()
                if (m.isEmpty()) continue
                val low = m.lowercase()
                if (low in blockedLower) continue
                blockedLower.add(low)
                result.add(w.meaning)
                if (result.size >= need) break
            }
            guard++
        }
        var fill = 0
        while (result.size < need) {
            fill++
            result.add("(보기 $fill)")
        }
        return result.take(need)
    }

    private suspend fun buildQuestion(correct: Word): McQuestion {
        val wrong = pickWrongMeanings(correct, 3)
        val options = (wrong + correct.meaning).shuffled()
        val correctIndex = options.indexOfFirst { it == correct.meaning }
        val idx = if (correctIndex >= 0) correctIndex else 0
        return McQuestion(correct, options, idx)
    }

    fun submitChoice(selectedIndex: Int) {
        val qs = _questions.value
        val idx = _currentIndex.value
        if (idx >= qs.size || _showResult.value) return
        val q = qs[idx]
        val ok = selectedIndex == q.correctIndex
        _answers.value = _answers.value + ok
        if (idx < qs.size - 1) {
            _currentIndex.value = idx + 1
        } else {
            saveResult()
        }
    }

    private fun saveResult() {
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
                            difficulty = difficultyKey
                        )
                    )
                }
                _showResult.value = true
            } catch (e: Exception) {
                AppLogger.e(TAG, "saveResult failed", e)
            }
        }
    }

    fun getScore(): Int = _answers.value.count { it } * 10

    private companion object {
        const val TAG = "MultipleChoiceTestVM"
    }
}
