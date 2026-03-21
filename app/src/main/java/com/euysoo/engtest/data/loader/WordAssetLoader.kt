package com.euysoo.engtest.data.loader

import android.content.Context
import com.euysoo.engtest.data.dao.WordDao
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordDifficulty
import com.euysoo.engtest.data.json.AppJson
import com.euysoo.engtest.data.model.EducationVocabRoot
import com.euysoo.engtest.util.AppLogger
import com.euysoo.engtest.util.AssetsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import java.io.InputStreamReader

/**
 * assets 폴더의 JSON 파일을 읽어 DB에 적재하는 로더.
 *
 * JSON 형식 예시:
 * [
 *   {"word": "apple", "partOfSpeech": "n.", "meaning": "사과", "difficulty": "ELEMENTARY"},
 *   ...
 * ]
 *
 * - insertAll 한 번 호출로 단일 트랜잭션 처리 (Room 기본 동작)
 * - difficulty: ELEMENTARY | MIDDLE | HIGH
 */
object WordAssetLoader {

    private const val ASSET_FILE_NAME = "words.json"

    /**
     * assets/words.json을 읽어 DB에 삽입.
     * @param context AssetManager 접근용
     * @param wordDao WordDao (insertAll 사용)
     * @return 삽입된 단어 개수
     */
    suspend fun loadFromAssets(context: Context, wordDao: WordDao): Int =
        withContext(Dispatchers.IO) {
            runCatching {
                val jsonString = context.assets.open(ASSET_FILE_NAME).use { input ->
                    InputStreamReader(input, Charsets.UTF_8).use { it.readText() }
                }
                val words = AppJson.json.decodeFromString(
                    ListSerializer(WordJson.serializer()),
                    jsonString
                ).map { json ->
                    Word(
                        word = json.word,
                        partOfSpeech = json.partOfSpeech,
                        meaning = json.meaning,
                        difficulty = WordDifficulty.valueOf(json.difficulty)
                    )
                }
                wordDao.insertAll(words)
                words.size
            }.onFailure { e ->
                AppLogger.e(TAG, "loadFromAssets failed", e)
            }.getOrElse { 0 }
        }

    /**
     * 단어 테이블 전체 삭제 후 assets/words.json에서 다시 적재.
     * '초기 데이터로 초기화' 기능에서 사용.
     *
     * @param context AssetManager 접근용
     * @param wordDao WordDao
     * @return 삽입된 단어 개수
     */
    suspend fun resetDatabase(context: Context, wordDao: WordDao): Int =
        withContext(Dispatchers.IO) {
            runCatching {
                wordDao.deleteAll()
                loadFromAssets(context, wordDao)
            }.getOrElse { 0 }
        }

    /** 교육부 필수어휘 JSON 파일명 */
    private const val ASSET_EDUCATION_VOCAB = "교육부_필수어휘_초중고.json"

    /**
     * 기존 단어 전체 삭제 후 교육부_필수어휘_초중고.json을 읽어 DB에 upsert.
     * - id는 JSON 값 사용 → REPLACE로 없으면 삽입, 있으면 업데이트
     *
     * @return 등록된 단어 개수
     */
    suspend fun loadEducationVocabFromAssets(context: Context, wordDao: WordDao): Int =
        withContext(Dispatchers.IO) {
            runCatching {
                wordDao.deleteAll()
                val jsonString = AssetsUtil.readAsString(context, ASSET_EDUCATION_VOCAB)
                if (jsonString.isBlank()) return@runCatching 0
                val root = AppJson.json.decodeFromString(EducationVocabRoot.serializer(), jsonString)
                val words = root.vocabulary.map { item ->
                    Word(
                        id = item.id.toLong(),
                        word = item.word,
                        partOfSpeech = item.pos,
                        meaning = item.meaning,
                        difficulty = levelToDifficulty(item.level)
                    )
                }
                wordDao.insertAll(words)
                words.size
            }.onFailure { e ->
                AppLogger.e(TAG, "loadEducationVocabFromAssets failed", e)
            }.getOrElse { 0 }
        }

    private fun levelToDifficulty(level: String): WordDifficulty = when (level) {
        "초등" -> WordDifficulty.ELEMENTARY
        "중등" -> WordDifficulty.MIDDLE
        "고등" -> WordDifficulty.HIGH
        else -> WordDifficulty.ELEMENTARY
    }

    private const val TAG = "WordAssetLoader"

    @Serializable
    private data class WordJson(
        val word: String,
        @SerialName("partOfSpeech") val partOfSpeech: String,
        val meaning: String,
        val difficulty: String // "ELEMENTARY" | "MIDDLE" | "HIGH"
    )
}
