package com.euysoo.engtest.data.repository

import com.euysoo.engtest.data.AppDatabase
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordDifficulty
import com.euysoo.engtest.data.entity.WordHistoryEntity
import com.euysoo.engtest.data.json.AppJson
import com.euysoo.engtest.data.model.EducationVocabRoot
import com.euysoo.engtest.domain.model.SyncResult
import com.euysoo.engtest.util.AppLogger

class WordSyncManager(
    private val db: AppDatabase
) {
    private val sourceVersion = "1.0"

    private companion object {
        const val TAG = "WordSync"
    }

    suspend fun sync(jsonString: String): SyncResult {
        val root: EducationVocabRoot = try {
            AppJson.json.decodeFromString(EducationVocabRoot.serializer(), jsonString).also {
                AppLogger.i(TAG, "JSON 파싱 완료: ${it.vocabulary.size}개")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "JSON 파싱 실패", e)
            throw IllegalStateException("어휘 파일 파싱 실패: ${e.message}", e)
        }
        val vocabList = root.vocabulary

        var addedCount = 0
        var updatedCount = 0
        var skippedCount = 0
        val now = System.currentTimeMillis()
        val historyList = mutableListOf<WordHistoryEntity>()

        vocabList.forEach { item ->
            val normalizedWord = item.word.trim().lowercase()
            val newPos = item.pos.trim()
            val newMeaning = item.meaning.trim()
            val newDifficulty = levelToDifficulty(item.level)
            val sameWordEntries = db.wordDao().getByWordAll(normalizedWord)
            val exactMatch = sameWordEntries.firstOrNull { existing ->
                existing.partOfSpeech.trim() == newPos &&
                    existing.meaning.trim() == newMeaning &&
                    existing.difficulty == newDifficulty
            }

            when {
                exactMatch == null -> {
                    db.wordDao().insert(
                        Word(
                            word = normalizedWord,
                            partOfSpeech = newPos,
                            meaning = newMeaning,
                            difficulty = newDifficulty,
                            addedAt = now,
                            updatedAt = now,
                            sourceVersion = sourceVersion
                        )
                    )
                    historyList.add(
                        WordHistoryEntity(
                            word = normalizedWord,
                            action = "ADDED",
                            afterPos = newPos,
                            afterMeaning = newMeaning,
                            afterLevel = item.level,
                            sourceVersion = sourceVersion,
                            recordedAt = now
                        )
                    )
                    addedCount++
                }
                else -> skippedCount++
            }
        }

        if (historyList.isNotEmpty()) {
            db.wordHistoryDao().insertAll(historyList)
        }

        return SyncResult(
            totalInFile = vocabList.size,
            addedCount = addedCount,
            updatedCount = updatedCount,
            skippedCount = skippedCount,
            sourceVersion = sourceVersion
        )
    }

    private fun levelToDifficulty(level: String): WordDifficulty = when (level) {
        "초등" -> WordDifficulty.ELEMENTARY
        "중등" -> WordDifficulty.MIDDLE
        "고등" -> WordDifficulty.HIGH
        else -> WordDifficulty.ELEMENTARY
    }

    @Suppress("unused")
    private fun difficultyToLevel(difficulty: WordDifficulty): String = when (difficulty) {
        WordDifficulty.ELEMENTARY -> "초등"
        WordDifficulty.MIDDLE -> "중등"
        WordDifficulty.HIGH -> "고등"
    }
}
