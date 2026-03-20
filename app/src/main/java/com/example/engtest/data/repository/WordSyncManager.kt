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
            val existing = db.wordDao().getByWord(normalizedWord)

            when {
                existing == null -> {
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

                isCoreChanged(
                    existing = existing,
                    incomingWord = normalizedWord,
                    incomingPos = newPos,
                    incomingMeaning = newMeaning,
                    incomingDifficulty = newDifficulty
                ) -> {
                    db.wordDao().updateWord(
                        word = normalizedWord,
                        partOfSpeech = newPos,
                        meaning = newMeaning,
                        difficulty = newDifficulty,
                        updatedAt = now,
                        sourceVersion = sourceVersion
                    )
                    historyList.add(
                        WordHistoryEntity(
                            word = normalizedWord,
                            action = "UPDATED",
                            beforePos = existing.partOfSpeech,
                            beforeMeaning = existing.meaning,
                            beforeLevel = difficultyToLevel(existing.difficulty),
                            afterPos = newPos,
                            afterMeaning = newMeaning,
                            afterLevel = item.level,
                            sourceVersion = sourceVersion,
                            recordedAt = now
                        )
                    )
                    updatedCount++
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

    /**
     * 변경 감지는 핵심 단어 정보(단어/품사/뜻/난이도)만 사용한다.
     * phonetic, 상세 캐시(word_details), timestamp 등 부가 정보는 카운트에 영향 없음.
     */
    private fun isCoreChanged(
        existing: Word,
        incomingWord: String,
        incomingPos: String,
        incomingMeaning: String,
        incomingDifficulty: WordDifficulty
    ): Boolean {
        return existing.word.trim().lowercase() != incomingWord ||
            existing.partOfSpeech.trim() != incomingPos ||
            existing.meaning.trim() != incomingMeaning ||
            existing.difficulty != incomingDifficulty
    }
}
