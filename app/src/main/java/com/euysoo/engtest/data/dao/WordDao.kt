package com.euysoo.engtest.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordDifficulty
import kotlinx.coroutines.flow.Flow

/**
 * 단어(Word) 테이블 접근용 DAO
 */
@Dao
interface WordDao {

    /** 전체 단어 Flow (변경 시마다 갱신). 목록 UI용. */
    @Query("SELECT * FROM words ORDER BY id ASC")
    fun getAllWords(): Flow<List<Word>>

    /** 페이징: OFFSET 구간만 조회 (대량 목록 시 메모리 최적화용). */
    @Query("SELECT * FROM words ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getWordsPaginated(limit: Int, offset: Int): List<Word>

    /** id로 단어 한 건 조회 */
    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): Word?

    /** 영문 스펠링으로 단어 1건 조회 (중복/업데이트 체크용, 대소문자 무시) */
    @Query("SELECT * FROM words WHERE LOWER(word) = LOWER(:word) LIMIT 1")
    suspend fun getByWord(word: String): Word?

    /** 영문 스펠링으로 단어 목록 조회 (중복 단어 지원, 대소문자 무시) */
    @Query("SELECT * FROM words WHERE LOWER(word) = LOWER(:word)")
    suspend fun getByWordAll(word: String): List<Word>

    /** id 목록에 해당하는 단어 일괄 조회 (N+1 방지, 기록 상세 등) */
    @Query("SELECT * FROM words WHERE id IN (:ids)")
    suspend fun getWordsByIds(ids: List<Long>): List<Word>

    /** 난이도별 단어 목록 */
    @Query("SELECT * FROM words WHERE difficulty = :difficulty ORDER BY id ASC")
    fun getWordsByDifficulty(difficulty: WordDifficulty): Flow<List<Word>>

    /** 무작위로 N개 단어 추출 (테스트용, 전체) */
    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomWords(limit: Int): List<Word>

    /** 난이도별 무작위 N개 단어 추출 (테스트용) */
    @Query("SELECT * FROM words WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomWordsByDifficulty(difficulty: WordDifficulty, limit: Int): List<Word>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: Word): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<Word>)

    @Update
    suspend fun update(word: Word)

    /** 스펠링 기준 단어 핵심 필드 업데이트 */
    @Query(
        """
        UPDATE words
        SET partOfSpeech = :partOfSpeech,
            meaning = :meaning,
            difficulty = :difficulty,
            updatedAt = :updatedAt,
            sourceVersion = :sourceVersion
        WHERE LOWER(word) = LOWER(:word)
        """
    )
    suspend fun updateWord(
        word: String,
        partOfSpeech: String,
        meaning: String,
        difficulty: WordDifficulty,
        updatedAt: Long,
        sourceVersion: String
    )

    @Delete
    suspend fun delete(word: Word)

    /** 전체 삭제 (초기 데이터로 초기화 시 사용) */
    @Query("DELETE FROM words")
    suspend fun deleteAll()

    /** 전체 삭제 (완전 초기화 용도, deleteAll과 동일 의미 별칭) */
    @Query("DELETE FROM words")
    suspend fun clearAll()

    /** 테이블 행 개수 */
    @Query("SELECT COUNT(*) FROM words")
    suspend fun getCount(): Int

    /** 전체 단어 수 Flow (통계용, 전체 목록 로드 없음) */
    @Query("SELECT COUNT(*) FROM words")
    fun getCountFlow(): Flow<Int>

    /** 동일 영어 단어 개수 (대소문자 무시, 중복 검사용) */
    @Query("SELECT COUNT(*) FROM words WHERE LOWER(word) = LOWER(:word)")
    suspend fun countByWord(word: String): Int

    /**
     * 영문/뜻 부분 일치 검색 (단어장 편집 화면용).
     * [query]에는 사용자 입력만 넣고, 와일드카드는 쿼리에서 붙입니다.
     */
    @Query(
        """
        SELECT * FROM words
        WHERE LOWER(word) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(meaning) LIKE '%' || LOWER(:query) || '%'
        ORDER BY word ASC
        LIMIT 300
        """
    )
    suspend fun searchWordsLike(query: String): List<Word>

    /** 발음 기호가 비어 있는 단어 목록 (초기화 후 배치 조회용) */
    @Query("SELECT * FROM words WHERE phonetic IS NULL OR phonetic = '' LIMIT :limit")
    suspend fun getWordsWithEmptyPhonetic(limit: Int): List<Word>
}
