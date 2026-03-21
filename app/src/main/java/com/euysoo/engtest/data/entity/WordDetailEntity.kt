package com.euysoo.engtest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_details")
data class WordDetailEntity(
    @PrimaryKey
    val word: String,
    val phonetic: String?,
    val meaningsJson: String,
    val fetchedAt: Long = System.currentTimeMillis()
)
