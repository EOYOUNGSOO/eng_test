package com.euysoo.engtest.ui.theme

import androidx.compose.ui.graphics.Color

/** 아이콘/이모지 원형 배경 + 전경색 (파스텔·MZ 톤). */
data class MzIconAccent(
    val background: Color,
    val foreground: Color,
)

private val mzAccents =
    listOf(
        MzIconAccent(Color(0xFFFFE4EC), Color(0xFFE11D48)),
        MzIconAccent(Color(0xFFE0F7FA), Color(0xFF0891B2)),
        MzIconAccent(Color(0xFFEDE9FE), Color(0xFF7C3AED)),
        MzIconAccent(Color(0xFFFFF3C4), Color(0xFFD97706)),
        MzIconAccent(Color(0xFFD1FAE5), Color(0xFF059669)),
        MzIconAccent(Color(0xFFFFE8CC), Color(0xFFEA580C)),
        MzIconAccent(Color(0xFFFCE7F3), Color(0xFFDB2777)),
        MzIconAccent(Color(0xFFE0E7FF), Color(0xFF4F46E5)),
    )

/** 0부터 시작하는 순번에 따라 액센트를 순환한다. */
fun mzIconAccent(ordinal: Int): MzIconAccent {
    val n = mzAccents.size
    val i = ((ordinal % n) + n) % n
    return mzAccents[i]
}

private val bookEmojis = listOf("📒", "📗", "📘", "📙", "📕", "🗂️", "✨", "🌟")

/** 단어장·목록 행용 이모지 (순번별 순환). */
fun mzBookEmoji(ordinal: Int): String {
    val n = bookEmojis.size
    val i = ((ordinal % n) + n) % n
    return bookEmojis[i]
}

private val choiceKeycaps = listOf("1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣")

/** 객관식 보기 등 순번 표시용 키캡 이모지. */
fun mzChoiceKeycap(ordinal: Int): String {
    val n = choiceKeycaps.size
    val i = ((ordinal % n) + n) % n
    return choiceKeycaps[i]
}
