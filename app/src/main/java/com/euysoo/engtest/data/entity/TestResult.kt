package com.euysoo.engtest.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 단어 테스트 결과 이력 Entity
 * - 테스트 날짜, 점수, 세부 결과(단어별 정답 여부) 저장
 * - testDateMillis 인덱스: 기간별 조회(getResultsBetween) 성능
 */
@Entity(
    tableName = "test_results",
    indices = [Index(value = ["testDateMillis"])]
)
data class TestResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 테스트 수행 시각 (UTC milliseconds) */
    val testDateMillis: Long,
    /** 맞은 개수 (0~10) */
    val score: Int,
    /**
     * 세부 결과. 예: "1:true,2:false,3:true,..."
     * (wordId:정답여부) 형태로 저장하여 나중에 파싱 가능
     */
    val details: String,
    /** 테스트 난이도: all, elementary, middle, high */
    val difficulty: String = "all"
)
