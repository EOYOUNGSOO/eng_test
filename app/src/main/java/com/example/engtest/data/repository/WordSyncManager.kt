package com.example.engtest.data.repository

import com.example.engtest.data.AppDatabase
import com.example.engtest.data.entity.Word
import com.example.engtest.data.entity.WordDifficulty
import com.example.engtest.data.entity.WordHistoryEntity
import com.example.engtest.data.model.EducationVocabRoot
import com.example.engtest.domain.model.SyncResult
import com.google.gson.Gson

class WordSyncManager(
    private val db: AppDatabase
) {
    private val sourceVersion = "1.0"

    suspend fun sync(jsonString: String): SyncResult {
        val vocabList = Gson().fromJson(jsonString, EducationVocabRoot::class.java).vocabulary

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

    private fun difficultyToLevel(difficulty: WordDifficulty): String = when (difficulty) {
        WordDifficulty.ELEMENTARY -> "초등"
        WordDifficulty.MIDDLE -> "중등"
        WordDifficulty.HIGH -> "고등"
    }
}
