package com.example.engtest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.engtest.data.entity.TestResult
import kotlinx.coroutines.flow.Flow

/**
 * 테스트 결과(TestResult) 테이블 접근용 DAO
 */
@Dao
interface TestResultDao {

    /** 전체 테스트 이력 (최신순) */
    @Query("SELECT * FROM test_results ORDER BY testDateMillis DESC")
    fun getAllResults(): Flow<List<TestResult>>

    /** 기간별 테스트 이력 (FROM ~ TO, 최신순) */
    @Query("SELECT * FROM test_results WHERE testDateMillis >= :fromMillis AND testDateMillis <= :toMillis ORDER BY testDateMillis DESC")
    fun getResultsBetween(fromMillis: Long, toMillis: Long): Flow<List<TestResult>>

    /** id로 결과 한 건 조회 */
    @Query("SELECT * FROM test_results WHERE id = :id")
    suspend fun getResultById(id: Long): TestResult?

    /** 최근 N건만 조회 (통계/기록 화면용) */
    @Query("SELECT * FROM test_results ORDER BY testDateMillis DESC LIMIT :limit")
    fun getRecentResults(limit: Int): Flow<List<TestResult>>

    @Insert
    suspend fun insert(result: TestResult): Long

    /** 테스트 건수 Flow (통계용, 전체 목록 로드 없음) */
    @Query("SELECT COUNT(*) FROM test_results")
    fun getCountFlow(): Flow<Int>

    /** 평균 점수(0~100) Flow (통계용) */
    @Query("SELECT AVG(score * 10) FROM test_results")
    fun getAverageScoreFlow(): Flow<Float?>
}
