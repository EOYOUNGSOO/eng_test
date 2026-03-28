package com.euysoo.engtest.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "word_book_entries",
    primaryKeys = ["bookId", "wordId"],
    foreignKeys = [
        ForeignKey(
            entity = WordBook::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Word::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookId"]),
        Index(value = ["wordId"])
    ]
)
data class WordBookEntry(
    val bookId: Long,
    val wordId: Long,
    /** 이 단어장에 연결된 시각(ms). 최근 추가가 위로 오도록 정렬에 사용 */
    val addedAt: Long = System.currentTimeMillis()
)
