package com.example.engtest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.engtest.data.dao.TestResultDao
import com.example.engtest.data.dao.WordDao
import com.example.engtest.data.entity.TestResult
import com.example.engtest.data.entity.Word

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

/**
 * Room DB 정의.
 * Entity: Word, TestResult
 * 버전 올릴 때는 version 증가 + Migration 처리.
 */
@Database(
    entities = [Word::class, TestResult::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun testResultDao(): TestResultDao
}
