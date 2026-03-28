package com.euysoo.engtest.ui.screen.wordtest

import com.euysoo.engtest.data.dao.WordBookDao

/** Nav 인자·TestResult.difficulty에 쓰는 난이도 키 */
const val DIFFICULTY_ALL = "all"
const val DIFFICULTY_ELEMENTARY = "elementary"
const val DIFFICULTY_MIDDLE = "middle"
const val DIFFICULTY_HIGH = "high"

/** 나의 단어장 출제: `my_book_{bookId}` */
const val MY_BOOK_PREFIX = "my_book_"

fun myBookDifficultyKey(bookId: Long) = "${MY_BOOK_PREFIX}$bookId"

fun parseMyBookId(difficultyKey: String): Long? =
    if (difficultyKey.startsWith(MY_BOOK_PREFIX)) {
        difficultyKey.removePrefix(MY_BOOK_PREFIX).toLongOrNull()
    } else {
        null
    }

suspend fun resolveDifficultyLabelForResult(key: String, wordBookDao: WordBookDao): String {
    val bid = parseMyBookId(key) ?: return formatDifficultyLabel(key)
    val name = wordBookDao.getBookById(bid)?.name
    return formatDifficultyLabel(key) { id -> if (id == bid) name else null }
}

fun formatDifficultyLabel(key: String, bookNameById: (Long) -> String? = { null }): String = when (key) {
    DIFFICULTY_ALL -> "전체"
    DIFFICULTY_ELEMENTARY -> "초등"
    DIFFICULTY_MIDDLE -> "중등"
    DIFFICULTY_HIGH -> "고등"
    else -> {
        val id = parseMyBookId(key)
        if (id != null) {
            val n = bookNameById(id)
            if (n != null) "나의 단어장 · $n" else "나의 단어장 #$id"
        } else {
            key
        }
    }
}
