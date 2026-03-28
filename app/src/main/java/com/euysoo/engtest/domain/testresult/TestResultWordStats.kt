package com.euysoo.engtest.domain.testresult

import com.euysoo.engtest.data.entity.TestResult
import com.euysoo.engtest.util.TestResultDetailsParser

/** 테스트 결과(details)로부터 단어별 (정답 횟수, 제시 횟수) 집계 */
object TestResultWordStats {
    /**
     * 모든 [TestResult]의 details를 순회하며 단어 ID별 정답 수·총 출제 수를 누적한다.
     */
    fun aggregateCorrectTotals(results: List<TestResult>): Map<Long, Pair<Int, Int>> {
        val map = mutableMapOf<Long, Pair<Int, Int>>()
        results.forEach { result ->
            TestResultDetailsParser.parseToWordIdAndKnown(result.details).forEach { (wordId, known) ->
                val (c, t) = map.getOrDefault(wordId, 0 to 0)
                map[wordId] = (c + if (known) 1 else 0) to (t + 1)
            }
        }
        return map
    }
}
