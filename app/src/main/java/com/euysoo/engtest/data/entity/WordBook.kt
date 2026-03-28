package com.euysoo.engtest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_books")
data class WordBook(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
