package com.example.engtest.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 영어 단어 정보 Entity
 * - 초등/중등/고등 교육부 어휘 구분용 difficulty 사용
 * - difficulty 인덱스: 난이도별 조회/필터 성능
 * - word 인덱스: countByWord(LOWER(word)) 및 검색 성능
 */
@Entity(
    tableName = "words",
    indices = [Index(value = ["difficulty"]), Index(value = ["word"])]
)
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 영어 단어 */
    val word: String,
    /** 품사 (noun, verb, adjective 등) */
    val partOfSpeech: String,
    /** 뜻 */
    val meaning: String,
    /**
     * 난이도 구분
     * ELEMENTARY = 초등, MIDDLE = 중등, HIGH = 고등
     */
    val difficulty: WordDifficulty,
    /** 최초 DB 등록 시각 (epoch millis), 이후 변경하지 않음 */
    val addedAt: Long = System.currentTimeMillis(),
    /** 마지막 수정 시각 (epoch millis), 수정 시 갱신 */
    val updatedAt: Long = System.currentTimeMillis(),
    /** 데이터 출처 버전 (어휘 파일 버전) */
    val sourceVersion: String = "1.0",
    /**
     * 발음 기호 (IPA 등). Free Dictionary API 등으로 조회.
     * null/빈 문자열: 미조회 → "[발음 확인 중...]"
     * PHONETIC_UNAVAILABLE: 조회했으나 없음 → "[발음 확인 불가]"
     */
    val phonetic: String? = null
)

enum class WordDifficulty {
    ELEMENTARY,  // 초등
    MIDDLE,      // 중등
    HIGH         // 고등
}
