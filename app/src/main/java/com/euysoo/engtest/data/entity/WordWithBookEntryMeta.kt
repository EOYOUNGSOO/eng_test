package com.euysoo.engtest.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * 단어장에 담긴 단어 + 단어장에 넣은 시각(정렬·강조용).
 * [Word.addedAt]과 구분하기 위해 조인 컬럼은 entryAddedAt 으로 받습니다.
 */
data class WordWithBookEntryMeta(
    @Embedded val word: Word,
    @ColumnInfo(name = "entryAddedAt") val entryAddedAt: Long
)
