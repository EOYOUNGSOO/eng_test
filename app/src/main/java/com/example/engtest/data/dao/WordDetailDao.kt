package com.example.engtest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.engtest.data.entity.WordDetailEntity

@Dao
interface WordDetailDao {
    @Query("SELECT * FROM word_details WHERE LOWER(word) = LOWER(:word) LIMIT 1")
    suspend fun getByWord(word: String): WordDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WordDetailEntity)

    @Query("DELETE FROM word_details")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM word_details")
    suspend fun getCachedCount(): Int
}
