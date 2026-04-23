package com.euysoo.engtest.ui.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.BuildConfig
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordBook
import com.euysoo.engtest.data.entity.WordBookEntry
import com.euysoo.engtest.data.entity.WordDifficulty
import com.euysoo.engtest.di.AppContainer
import com.euysoo.engtest.util.AppLogger
import com.euysoo.engtest.util.GeminiOcrService
import com.euysoo.engtest.util.ImagePreprocessor
import com.euysoo.engtest.util.OcrException
import com.euysoo.engtest.util.OcrHelper
import com.euysoo.engtest.util.ParsedWord
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class OcrErrorType {
    IMAGE_TOO_SMALL,
    NO_TEXT,
    NO_WORDS,
    LOW_QUALITY,
    ENGINE_FAILURE,
    RATE_LIMIT,
    API_KEY_ERROR,
}

/** saveWords() 완료 후 UI에 전달하는 요약 */
data class OcrSaveSummary(
    val bookId: Long,
    val bookName: String,
    val inserted: Int,
    val existing: Int,
    val skipped: Int,
)

sealed class OcrUiState {
    object Idle : OcrUiState()

    data class Processing(val engineLabel: String = "인식 중...") : OcrUiState()

    object Saving : OcrUiState()

    data class Result(val words: List<ParsedWord>) : OcrUiState()

    data class PartialResult(val words: List<ParsedWord>, val warningMessage: String) : OcrUiState()

    data class Error(val type: OcrErrorType, val message: String, val showGuide: Boolean = false) : OcrUiState()

    data class SaveSuccess(val summary: OcrSaveSummary) : OcrUiState()
}

class OcrViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _uiState = MutableStateFlow<OcrUiState>(OcrUiState.Idle)
    val uiState: StateFlow<OcrUiState> = _uiState

    private val _editingIndex = MutableStateFlow<Int?>(null)
    val editingIndex: StateFlow<Int?> = _editingIndex
    private var spellingDictionaryCache: Set<String>? = null

    fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _editingIndex.value = null
            _uiState.value = OcrUiState.Processing()

            OcrHelper.validateImageSize(bitmap)?.let {
                _uiState.value =
                    OcrUiState.Error(
                        OcrErrorType.IMAGE_TOO_SMALL,
                        "이미지가 너무 작습니다.\n더 가까이서 또렷하게 촬영해 주세요.",
                    )
                return@launch
            }

            val workBitmap =
                withContext(Dispatchers.IO) {
                    OcrHelper.safeCopyForOcr(bitmap)
                }
            if (workBitmap == null) {
                _uiState.value =
                    OcrUiState.Error(
                        OcrErrorType.ENGINE_FAILURE,
                        "이미지를 읽을 수 없습니다.\n다른 사진으로 다시 시도해 주세요.",
                    )
                return@launch
            }

            val isOnline = isNetworkAvailable()
            val hasGeminiKey = BuildConfig.GEMINI_API_KEY.isNotBlank()

            try {
                var geminiRateLimited = false

                val parsed: List<ParsedWord> =
                    if (isOnline && hasGeminiKey) {
                        _uiState.value = OcrUiState.Processing("Gemini AI로 인식 중...")
                        try {
                            val g =
                                withContext(Dispatchers.IO) {
                                    GeminiOcrService.extractWords(workBitmap)
                                }
                            if (g.isNotEmpty()) {
                                g
                            } else {
                                _uiState.value = OcrUiState.Processing("ML Kit으로 재시도 중...")
                                runMlKitOcr(workBitmap)
                            }
                        } catch (e: OcrException.RateLimitExceeded) {
                            geminiRateLimited = true
                            AppLogger.w("OcrViewModel", "Gemini rate limit exceeded, ML Kit fallback", e)
                            _uiState.value = OcrUiState.Processing("ML Kit으로 인식 중... (API 한도 초과)")
                            runMlKitOcr(workBitmap)
                        } catch (e: OcrException.InvalidApiKey) {
                            _uiState.value =
                                OcrUiState.Error(
                                    OcrErrorType.API_KEY_ERROR,
                                    "Gemini API 키가 올바르지 않습니다.\n" +
                                        "local.properties의 gemini.api.key 를 확인해 주세요.",
                                )
                            return@launch
                        } catch (e: Exception) {
                            AppLogger.e("OcrViewModel", "Gemini failed, fallback to ML Kit", e)
                            _uiState.value = OcrUiState.Processing("ML Kit으로 재시도 중...")
                            runMlKitOcr(workBitmap)
                        }
                    } else {
                        val label =
                            if (!isOnline) {
                                "오프라인 인식 중..."
                            } else {
                                "ML Kit으로 인식 중... (Gemini API 키 없음)"
                            }
                        _uiState.value = OcrUiState.Processing(label)
                        runMlKitOcr(workBitmap)
                    }

                if (parsed.isEmpty()) {
                    _uiState.value =
                        OcrUiState.Error(
                            OcrErrorType.NO_TEXT,
                            "텍스트를 인식하지 못했습니다.\n밝은 곳에서 다시 촬영해 주세요.",
                        )
                    return@launch
                }

                val corrected =
                    runCatching {
                        withContext(Dispatchers.IO) {
                            val dictionary = getOrLoadSpellingDictionary()
                            OcrHelper.correctWordsWithDictionary(parsed, dictionary)
                        }
                    }.getOrElse { e ->
                        AppLogger.w("OcrViewModel", "correctWordsWithDictionary failed, using raw OCR", e)
                        parsed
                    }

                val refined = OcrHelper.refineVocabularyTableResults(corrected)

                AppLogger.i(
                    "OcrViewModel",
                    "OCR_METRIC: geminiRateLimited=$geminiRateLimited online=$isOnline geminiKey=$hasGeminiKey " +
                        "final=${refined.size} selected=${refined.count { it.isSelected }} " +
                        "meaning=${refined.count { it.meaning.isNotBlank() }} " +
                        "pos=${refined.count { it.partOfSpeech.isNotBlank() }}",
                )

                val rateMsg =
                    if (geminiRateLimited) {
                        "⚠️ Gemini API 한도 초과(분당 15회). ML Kit으로 인식했습니다.\n" +
                            "정확도가 낮을 수 있으니 확인해 주세요.\n\n"
                    } else {
                        ""
                    }

                when (OcrHelper.validateParseResult(refined)) {
                    is OcrException.NoParsedWords -> {
                        _uiState.value =
                            OcrUiState.Error(
                                OcrErrorType.NO_WORDS,
                                "단어 목록 형식을 찾지 못했습니다.\n" +
                                    "「단어   품사   뜻」형식으로 작성됐는지 확인해 주세요.",
                                showGuide = true,
                            )
                    }
                    is OcrException.LowQualityResult -> {
                        _uiState.value =
                            OcrUiState.PartialResult(
                                refined,
                                rateMsg +
                                    "⚠️ 일부 항목의 뜻을 인식하지 못했습니다.\n직접 수정 후 저장해 주세요.",
                            )
                    }
                    null ->
                        if (geminiRateLimited) {
                            _uiState.value =
                                OcrUiState.PartialResult(refined, rateMsg.trimEnd())
                        } else {
                            _uiState.value = OcrUiState.Result(refined)
                        }
                    else -> {
                        if (geminiRateLimited) {
                            _uiState.value =
                                OcrUiState.PartialResult(refined, rateMsg.trimEnd())
                        } else {
                            _uiState.value = OcrUiState.Result(refined)
                        }
                    }
                }
            } catch (e: Exception) {
                AppLogger.e("OcrViewModel", "OCR failed", e)
                _uiState.value =
                    OcrUiState.Error(
                        OcrErrorType.ENGINE_FAILURE,
                        "OCR 처리 중 오류가 발생했습니다.\n잠시 후 다시 시도해 주세요.",
                    )
            } finally {
                withContext(NonCancellable + Dispatchers.IO) {
                    if (!workBitmap.isRecycled) {
                        runCatching { workBitmap.recycle() }
                    }
                }
            }
        }
    }

    /**
     * ML Kit Latin/Korean [Text]에서 3컬럼 표 파싱. 실제 앱 경로는 [runMlKitOcr]가
     * [OcrHelper.mergeAndParse]를 호출하며, 그 내부에서 동일 전략이 적용된다.
     */
    fun parseOcrResult(
        latin: Text,
        korean: Text,
    ): List<ParsedWord> = OcrHelper.parseOcrResult(latin, korean)

    private suspend fun runMlKitOcr(bitmap: Bitmap): List<ParsedWord> {
        return withContext(Dispatchers.IO) {
            suspend fun recognizeOne(b: Bitmap): List<ParsedWord> {
                val (latinResult, koreanResult) = OcrHelper.recognizeBoth(b)
                if (latinResult.text.isBlank() && koreanResult.text.isBlank()) {
                    return emptyList()
                }
                var parsed = OcrHelper.mergeAndParse(latinResult, koreanResult)
                if (parsed.isEmpty() && latinResult.text.isNotBlank()) {
                    parsed = OcrHelper.parseWordList(latinResult.text)
                }
                return parsed
            }

            fun recycleIfOwned(
                b: Bitmap,
                owner: Bitmap,
            ) {
                if (b !== owner && !b.isRecycled) {
                    runCatching { b.recycle() }
                }
            }

            runCatching { ImagePreprocessor.preprocessBitmap(bitmap) }.getOrNull()?.let { v ->
                try {
                    val p = recognizeOne(v)
                    if (p.isNotEmpty()) return@withContext p
                } finally {
                    recycleIfOwned(v, bitmap)
                }
            }

            runCatching { ImagePreprocessor.prepareForMlKitAdaptive(bitmap) }.getOrNull()?.let { v ->
                try {
                    val p = recognizeOne(v)
                    if (p.isNotEmpty()) return@withContext p
                } finally {
                    recycleIfOwned(v, bitmap)
                }
            }

            runCatching { ImagePreprocessor.prepareForMlKit(bitmap) }.getOrNull()?.let { v ->
                try {
                    val p = recognizeOne(v)
                    if (p.isNotEmpty()) return@withContext p
                } finally {
                    recycleIfOwned(v, bitmap)
                }
            }

            runCatching {
                val r = OcrHelper.recognizeAndParseBest(bitmap)
                if (r.words.isNotEmpty()) return@withContext r.words
            }

            recognizeOne(bitmap)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val cm =
                container.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    fun toggleSelection(index: Int) {
        val current = _uiState.value
        val words =
            when (current) {
                is OcrUiState.Result -> current.words
                is OcrUiState.PartialResult -> current.words
                else -> return
            }
        val updated =
            words.toMutableList().also {
                it[index] = it[index].copy(isSelected = !it[index].isSelected)
            }
        _uiState.value =
            when (current) {
                is OcrUiState.Result -> current.copy(words = updated)
                is OcrUiState.PartialResult -> current.copy(words = updated)
                else -> current
            }
    }

    fun updateWord(
        index: Int,
        word: String,
        pos: String,
        meaning: String,
    ) {
        val current = _uiState.value
        val words =
            when (current) {
                is OcrUiState.Result -> current.words
                is OcrUiState.PartialResult -> current.words
                else -> return
            }
        val normPos = OcrHelper.normalizePartOfSpeech(pos)
        val trimmedWord = word.trim()
        val trimmedMeaning = meaning.trim()
        val updated =
            words.toMutableList().also {
                it[index] =
                    it[index].copy(
                        word = word,
                        partOfSpeech = normPos,
                        meaning = meaning,
                        isAutoCorrected = false,
                        isSelected = OcrHelper.isOcrRowComplete(trimmedWord, normPos, trimmedMeaning),
                    )
            }
        _uiState.value =
            when (current) {
                is OcrUiState.Result -> current.copy(words = updated)
                is OcrUiState.PartialResult -> current.copy(words = updated)
                else -> current
            }
    }

    /**
     * [bookId] == -1L 이면 [bookName]으로 새 단어장 생성.
     * words 신규 INSERT / 기존 단어는 단어장만 연결 / 이미 단어장에 있으면 스킵.
     */
    fun saveWords(
        words: List<ParsedWord>,
        bookId: Long,
        bookName: String,
    ) {
        val toSave =
            words.filter {
                it.isSelected &&
                    it.word.isNotBlank() &&
                    OcrHelper.isOcrRowComplete(it.word, it.partOfSpeech, it.meaning)
            }
        if (toSave.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = OcrUiState.Saving
            try {
                val summary =
                    withContext(Dispatchers.IO) {
                        val wordDao = container.database.wordDao()
                        val bookDao = container.database.wordBookDao()
                        val now = System.currentTimeMillis()

                        val resolvedBookId: Long =
                            if (bookId == -1L) {
                                bookDao.insertBook(WordBook(name = bookName.trim()))
                            } else {
                                bookId
                            }
                        val resolvedBookName = bookName.trim()

                        var inserted = 0
                        var existing = 0
                        var skipped = 0

                        for (parsed in toSave) {
                            val trimmedWord = parsed.word.trim()
                            val existingWord = wordDao.getByWord(trimmedWord)
                            val wordId: Long =
                                if (existingWord == null) {
                                    val newWord =
                                        Word(
                                            word = trimmedWord,
                                            partOfSpeech = OcrHelper.normalizePartOfSpeech(parsed.partOfSpeech.trim()),
                                            meaning = parsed.meaning.trim(),
                                            difficulty = parsed.difficulty,
                                            addedAt = now,
                                            updatedAt = now,
                                            sourceVersion = "ocr",
                                            phonetic = null,
                                        )
                                    val newId = wordDao.insert(newWord)
                                    inserted++
                                    newId
                                } else {
                                    existing++
                                    existingWord.id
                                }

                            val alreadyInBook = bookDao.countEntry(resolvedBookId, wordId) > 0
                            if (alreadyInBook) {
                                skipped++
                                if (existingWord != null) {
                                    existing--
                                }
                            } else {
                                bookDao.insertEntry(
                                    WordBookEntry(
                                        bookId = resolvedBookId,
                                        wordId = wordId,
                                        addedAt = now,
                                    ),
                                )
                            }
                        }

                        OcrSaveSummary(
                            bookId = resolvedBookId,
                            bookName = resolvedBookName,
                            inserted = inserted,
                            existing = existing,
                            skipped = skipped,
                        )
                    }
                _uiState.value = OcrUiState.SaveSuccess(summary)
            } catch (e: Exception) {
                AppLogger.e("OcrViewModel", "saveWords failed", e)
                _uiState.value =
                    OcrUiState.Error(
                        OcrErrorType.ENGINE_FAILURE,
                        "저장 중 오류가 발생했습니다.\n잠시 후 다시 시도해 주세요.",
                    )
            }
        }
    }

    fun selectAll() {
        val current = _uiState.value
        val words =
            when (current) {
                is OcrUiState.Result -> current.words
                is OcrUiState.PartialResult -> current.words
                else -> return
            }
        val updated = words.map { it.copy(isSelected = true) }
        _uiState.value =
            when (current) {
                is OcrUiState.Result -> current.copy(words = updated)
                is OcrUiState.PartialResult -> current.copy(words = updated)
                else -> current
            }
    }

    fun deselectAll() {
        val current = _uiState.value
        val words =
            when (current) {
                is OcrUiState.Result -> current.words
                is OcrUiState.PartialResult -> current.words
                else -> return
            }
        val updated = words.map { it.copy(isSelected = false) }
        _uiState.value =
            when (current) {
                is OcrUiState.Result -> current.copy(words = updated)
                is OcrUiState.PartialResult -> current.copy(words = updated)
                else -> current
            }
    }

    fun openEditDialog(index: Int) {
        _editingIndex.value = index
    }

    fun closeEditDialog() {
        _editingIndex.value = null
    }

    fun saveEdit(
        index: Int,
        word: String,
        partOfSpeech: String,
        meaning: String,
        difficulty: WordDifficulty,
    ) {
        val current = _uiState.value
        val words =
            when (current) {
                is OcrUiState.Result -> current.words
                is OcrUiState.PartialResult -> current.words
                else -> return
            }
        if (index < 0 || index >= words.size) return

        val updated = words.toMutableList()
        val nw = word.trim()
        val np = OcrHelper.normalizePartOfSpeech(partOfSpeech.trim())
        val nm = meaning.trim()
        updated[index] =
            updated[index].copy(
                word = nw,
                partOfSpeech = np,
                meaning = nm,
                difficulty = difficulty,
                isAutoCorrected = false,
                isSelected = OcrHelper.isOcrRowComplete(nw, np, nm),
            )
        _uiState.value =
            when (current) {
                is OcrUiState.Result -> current.copy(words = updated)
                is OcrUiState.PartialResult -> current.copy(words = updated)
                else -> current
            }
        _editingIndex.value = null
    }

    fun reset() {
        _editingIndex.value = null
        _uiState.value = OcrUiState.Idle
    }

    private suspend fun getOrLoadSpellingDictionary(): Set<String> {
        spellingDictionaryCache?.let { return it }
        val loaded =
            container.database.wordDao()
                .getAllWordSpellings()
                .asSequence()
                .map { it.trim().lowercase() }
                .filter { it.length >= 3 }
                .toSet()
        spellingDictionaryCache = loaded
        return loaded
    }
}
