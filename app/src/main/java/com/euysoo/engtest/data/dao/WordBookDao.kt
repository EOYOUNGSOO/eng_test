package com.euysoo.engtest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordBook
import com.euysoo.engtest.data.entity.WordBookEntry
import com.euysoo.engtest.data.entity.WordWithBookEntryMeta
import kotlinx.coroutines.flow.Flow

@Dao
interface WordBookDao {
    @Query("SELECT * FROM word_books ORDER BY createdAt DESC, id DESC")
    fun getAllBooks(): Flow<List<WordBook>>

    @Query("SELECT * FROM word_books WHERE id = :id")
    suspend fun getBookById(id: Long): WordBook?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBook(book: WordBook): Long

    @Update
    suspend fun updateBook(book: WordBook)

    @Query("DELETE FROM word_books WHERE id = :id")
    suspend fun deleteBookById(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEntry(entry: WordBookEntry)

    @Query("DELETE FROM word_book_entries WHERE bookId = :bookId AND wordId = :wordId")
    suspend fun deleteEntry(
        bookId: Long,
        wordId: Long,
    )

    @Query(
        """
        SELECT w.*, e.addedAt AS entryAddedAt FROM words w
        INNER JOIN word_book_entries e ON w.id = e.wordId
        WHERE e.bookId = :bookId
        ORDER BY e.addedAt DESC, w.word ASC
        """,
    )
    fun getWordsInBook(bookId: Long): Flow<List<WordWithBookEntryMeta>>

    @Query(
        """
        SELECT w.* FROM words w
        INNER JOIN word_book_entries e ON w.id = e.wordId
        WHERE e.bookId = :bookId
        ORDER BY RANDOM()
        LIMIT :limit
        """,
    )
    suspend fun getRandomWordsFromBook(
        bookId: Long,
        limit: Int,
    ): List<Word>

    @Query("SELECT COUNT(*) FROM word_book_entries WHERE bookId = :bookId AND wordId = :wordId")
    suspend fun countEntry(
        bookId: Long,
        wordId: Long,
    ): Int
}
