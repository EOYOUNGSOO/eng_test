package com.example.engtest.util

/**
 * 테스트 결과 details 문자열 파싱 유틸.
 * 형식: "wordId:known,wordId:known,..." (예: "1:true,2:false,3:true")
 *
 * 단어 관리(통계 집계)와 기록 상세(단어 목록 로드)에서 공통 사용.
 */
object TestResultDetailsParser {

    /**
     * details 문자열을 (wordId, 정답여부) 목록으로 파싱.
     * @return 유효하지 않은 항목은 제외됨
     */
    fun parseToWordIdAndKnown(details: String): List<Pair<Long, Boolean>> {
        if (details.isBlank()) return emptyList()
        return details.split(",").mapNotNull { part ->
            val kv = part.split(":")
            if (kv.size != 2) return@mapNotNull null
            val wordId = kv[0].trim().toLongOrNull() ?: return@mapNotNull null
            val known = kv[1].trim() == "true"
            wordId to known
        }
    }
}
