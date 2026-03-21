package com.euysoo.engtest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.euysoo.engtest.data.dao.TestResultDao
import com.euysoo.engtest.data.dao.WordDetailDao
import com.euysoo.engtest.data.dao.WordHistoryDao
import com.euysoo.engtest.data.dao.WordDao
import com.euysoo.engtest.data.entity.TestResult
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordDetailEntity
import com.euysoo.engtest.data.entity.WordHistoryEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE test_results ADD COLUMN difficulty TEXT NOT NULL DEFAULT 'all'")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS index_words_difficulty ON words(difficulty)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_words_word ON words(word)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_test_results_testDateMillis ON test_results(testDateMillis)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE words ADD COLUMN phonetic TEXT")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val migrationNow = System.currentTimeMillis()
        db.execSQL("ALTER TABLE words ADD COLUMN addedAt INTEGER NOT NULL DEFAULT $migrationNow")
        db.execSQL("ALTER TABLE words ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $migrationNow")
        db.execSQL("ALTER TABLE words ADD COLUMN sourceVersion TEXT NOT NULL DEFAULT '1.0'")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS word_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                word TEXT NOT NULL,
                action TEXT NOT NULL,
                beforePos TEXT,
                beforeMeaning TEXT,
                beforeLevel TEXT,
                afterPos TEXT,
                afterMeaning TEXT,
                afterLevel TEXT,
                sourceVersion TEXT NOT NULL DEFAULT '1.0',
                recordedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS word_details (
                word TEXT NOT NULL PRIMARY KEY,
                phonetic TEXT,
                meaningsJson TEXT NOT NULL,
                fetchedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

/** Gson → kotlinx.serialization 전환으로 meaningsJson 형식이 달라짐: 기존 캐시 무효화 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM word_details")
    }
}

/**
 * Room DB 정의.
 * Entity: Word, TestResult
 * 버전 올릴 때는 version 증가 + Migration 처리.
 */
@Database(
    entities = [Word::class, TestResult::class, WordHistoryEntity::class, WordDetailEntity::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun testResultDao(): TestResultDao
    abstract fun wordHistoryDao(): WordHistoryDao
    abstract fun wordDetailDao(): WordDetailDao
}
