package com.euysoo.engtest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.euysoo.engtest.data.entity.WordHistoryEntity

@Dao
interface WordHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: WordHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(histories: List<WordHistoryEntity>)

    @Query("SELECT * FROM word_history WHERE word = :word ORDER BY recordedAt DESC")
    suspend fun getHistoryByWord(word: String): List<WordHistoryEntity>

    @Query("SELECT * FROM word_history WHERE action = :action ORDER BY recordedAt DESC")
    suspend fun getHistoryByAction(action: String): List<WordHistoryEntity>

    @Query(
        """
        SELECT action, COUNT(*) as count
        FROM word_history
        WHERE recordedAt >= :since
        GROUP BY action
        """,
    )
    suspend fun getSummaryAfter(since: Long): List<ActionSummary>

    @Query("DELETE FROM word_history")
    suspend fun clearAll()
}

data class ActionSummary(
    val action: String,
    val count: Int,
)
