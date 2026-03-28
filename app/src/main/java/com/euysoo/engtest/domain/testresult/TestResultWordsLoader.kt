package com.euysoo.engtest.domain.testresult

import com.euysoo.engtest.data.dao.WordDao
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.util.TestResultDetailsParser

/** 단일 테스트의 details 문자열 → (단어, 정답 여부) 목록 (N+1 방지 일괄 조회) */
object TestResultWordsLoader {
    suspend fun loadWordPairs(
        details: String,
        wordDao: WordDao,
    ): List<Pair<Word, Boolean>> {
        val idAndKnown = TestResultDetailsParser.parseToWordIdAndKnown(details)
        if (idAndKnown.isEmpty()) return emptyList()
        val ids = idAndKnown.map { it.first }.distinct()
        val wordsById = wordDao.getWordsByIds(ids).associateBy { it.id }
        return idAndKnown.mapNotNull { (id, known) ->
            wordsById[id]?.let { word -> word to known }
        }
    }
}
