package com.example.engtest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_history")
data class WordHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val word: String,
    val action: String,
    val beforePos: String? = null,
    val beforeMeaning: String? = null,
    val beforeLevel: String? = null,
    val afterPos: String? = null,
    val afterMeaning: String? = null,
    val afterLevel: String? = null,
    val sourceVersion: String = "1.0",
    val recordedAt: Long = System.currentTimeMillis()
)
