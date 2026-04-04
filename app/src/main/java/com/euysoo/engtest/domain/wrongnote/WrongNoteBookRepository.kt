package com.euysoo.engtest.domain.wrongnote

import com.euysoo.engtest.data.dao.TestResultDao
import com.euysoo.engtest.data.dao.WordBookDao
import com.euysoo.engtest.data.dao.WordDao
import com.euysoo.engtest.domain.testresult.TestResultWordStats

class WrongNoteBookRepository(
    private val testResultDao: TestResultDao,
    private val wordDao: WordDao,
    private val wordBookDao: WordBookDao,
) {
    suspend fun createWrongNoteBook(
        title: String,
        difficulty: WrongNoteDifficultyOption,
        fill: WrongNoteFillSelection,
    ): WrongNoteOutcome {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return WrongNoteOutcome.EmptyTitle

        val stats = TestResultWordStats.aggregateCorrectTotals(testResultDao.getAllResultsOnce())
        val hasAnyWrong = stats.any { (_, p) -> p.second > p.first }
        if (!hasAnyWrong) return WrongNoteOutcome.NoWrongHistory

        val pool = wrongWordPoolFromStats(stats, difficulty)
        if (pool.isEmpty()) return WrongNoteOutcome.NoWrongAfterDifficulty

        return when (fill) {
            WrongNoteFillSelection.WRONG_RANDOM_100 -> {
                if (pool.size < WRONG_NOTE_TARGET) {
                    WrongNoteOutcome.NeedMixConfirm(pool.size)
                } else {
                    val ids = pool.shuffled().take(WRONG_NOTE_TARGET)
                    WrongNoteOutcome.Created(wordBookDao.createBookWithWordIds(trimmed, ids))
                }
            }
            WrongNoteFillSelection.MIX_100 -> {
                val wrongPart = pool.shuffled().take(pool.size.coerceAtMost(WRONG_NOTE_TARGET))
                val need = WRONG_NOTE_TARGET - wrongPart.size
                val filler =
                    randomWordIdsExcluding(
                        difficulty = difficulty,
                        exclude = wrongPart.toSet(),
                        need = need,
                    )
                val combined = (wrongPart + filler).distinct().take(WRONG_NOTE_TARGET)
                WrongNoteOutcome.Created(wordBookDao.createBookWithWordIds(trimmed, combined))
            }
            WrongNoteFillSelection.WRONG_ONLY_AVAILABLE -> {
                WrongNoteOutcome.Created(wordBookDao.createBookWithWordIds(trimmed, pool.shuffled()))
            }
        }
    }

    private suspend fun wrongWordPoolFromStats(
        stats: Map<Long, Pair<Int, Int>>,
        difficulty: WrongNoteDifficultyOption,
    ): List<Long> {
        val wrongIds = stats.filter { (_, p) -> p.second > p.first }.keys.toList()
        if (wrongIds.isEmpty()) return emptyList()
        val words = wordDao.getWordsByIds(wrongIds)
        val d = difficulty.toWordDifficultyOrNull()
        val filtered = if (d == null) words else words.filter { it.difficulty == d }
        return filtered.map { it.id }.distinct()
    }

    private suspend fun randomWordIdsExcluding(
        difficulty: WrongNoteDifficultyOption,
        exclude: Set<Long>,
        need: Int,
    ): List<Long> {
        if (need <= 0) return emptyList()
        val diff = difficulty.toWordDifficultyOrNull()
        val out = LinkedHashSet<Long>()
        var guard = 0
        while (out.size < need && guard < 60) {
            val ask = ((need - out.size) * 5).coerceIn(24, 400)
            val batch =
                if (diff == null) {
                    wordDao.getRandomWords(ask)
                } else {
                    wordDao.getRandomWordsByDifficulty(diff, ask)
                }
            batch.shuffled().forEach { w ->
                if (w.id !in exclude) out.add(w.id)
            }
            guard++
        }
        return out.take(need)
    }

    companion object {
        const val WRONG_NOTE_TARGET = 100
    }
}
