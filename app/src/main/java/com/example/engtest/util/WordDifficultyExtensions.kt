package com.example.engtest.util

import com.example.engtest.data.entity.Word
import com.example.engtest.data.entity.WordDifficulty

/** 난이도별 별 개수 (초등 1, 중등 2, 고등 3) */
val WordDifficulty.starCount: Int
    get() = when (this) {
        WordDifficulty.ELEMENTARY -> 1
        WordDifficulty.MIDDLE -> 2
        WordDifficulty.HIGH -> 3
    }

/** 발음 기호 표시 문자열: 있으면 [phonetic], 없으면 [발음 확인 중...] */
fun Word.phoneticDisplayText(): String =
    if (!phonetic.isNullOrBlank()) "[$phonetic]" else "[발음 확인 중...]"
