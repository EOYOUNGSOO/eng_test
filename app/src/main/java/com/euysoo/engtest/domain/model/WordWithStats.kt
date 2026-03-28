package com.euysoo.engtest.domain.model

import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordDifficulty

/** 단어 + 테스트 정답/제시 횟수 (정답률·오답률 표시 및 정렬용) */
data class WordWithStats(
    val word: Word,
    val correctCount: Int,
    val totalCount: Int,
) {
    val correctRate: Float? = if (totalCount > 0) correctCount.toFloat() / totalCount else null
    val wrongRate: Float? = if (totalCount > 0) (totalCount - correctCount).toFloat() / totalCount else null
}

/**
 * 필터·검색·통계·최근 추가 단어를 반영한 목록 생성 (순수 함수).
 */
object WordWithStatsListBuilder {
    /**
     * 최근 추가 단어를 목록에 합친 뒤 난이도·검색으로 거르고, 정답률·최근 추가 우선으로 정렬한다.
     */
    fun build(
        allWords: List<Word>,
        filter: WordDifficulty?,
        searchQuery: String,
        stats: Map<Long, Pair<Int, Int>>,
        recentlyAddedWords: List<Word>,
    ): List<WordWithStats> {
        val listWithPending =
            allWords +
                recentlyAddedWords.filter { w ->
                    w.id !in allWords.map { it.id }
                }
        var filtered = if (filter == null) listWithPending else listWithPending.filter { it.difficulty == filter }
        val q = searchQuery.trim()
        if (q.isNotEmpty()) {
            filtered = filtered.filter { it.word.contains(q, ignoreCase = true) }
        }
        val recentIds = recentlyAddedWords.map { it.id }.toSet()
        val withStats =
            filtered.map { word ->
                val (c, t) = stats[word.id] ?: (0 to 0)
                WordWithStats(word = word, correctCount = c, totalCount = t)
            }
        val baseOrder =
            compareBy<WordWithStats> { it.totalCount == 0 }
                .thenBy { it.correctRate ?: Float.MAX_VALUE }
        return if (recentIds.isEmpty()) {
            withStats.sortedWith(baseOrder)
        } else {
            withStats.sortedWith(
                compareBy<WordWithStats> { it.word.id !in recentIds }
                    .thenBy { it.totalCount == 0 }
                    .thenBy { it.correctRate ?: Float.MAX_VALUE },
            )
        }
    }
}
