package com.euysoo.engtest.data.entity

/**
 * 앱 전역 품사 정의.
 * - [label]: DB 저장값 및 화면 표시값 (한글)
 * - [abbr]: OCR·JSON 등에서 쓰는 영어 약자
 */
enum class PartOfSpeech(
    val label: String,
    val abbr: String,
) {
    NOUN("명사", "n"),
    VERB("동사", "v"),
    ADJECTIVE("형용사", "adj"),
    ADVERB("부사", "adv"),
    PRONOUN("대명사", "pron"),
    PREPOSITION("전치사", "prep"),
    CONJUNCTION("접속사", "conj"),
    INTERJECTION("감탄사", "interj"),
    ARTICLE("관사", "art"),
    ;

    companion object {
        /** 한글 label로 enum 조회 */
        fun fromLabel(label: String): PartOfSpeech? =
            entries.firstOrNull { it.label == label.trim() }

        /** 영어 약자(대소문자 무시, 점·공백 제거)로 enum 조회 */
        fun fromAbbr(abbr: String): PartOfSpeech? {
            val cleaned =
                abbr
                    .trim()
                    .trimEnd('.', ' ', '　')
                    .lowercase()
                    .replace(Regex("""\s+"""), "")
            if (cleaned == "int") return INTERJECTION
            if (cleaned == "det" || cleaned == "article") return ARTICLE
            return entries.firstOrNull { it.abbr == cleaned }
        }

        /** DB의 품사 문자열(쉼표·중점·슬래시 구분 또는 단일)을 label 목록으로 파싱 */
        fun parseLabels(raw: String): List<String> =
            raw
                .split(Regex("""[,·/,，]"""))
                .map { it.trim() }
                .filter { it.isNotEmpty() }

        /** label 목록 → DB 저장용 쉼표 구분 문자열 */
        fun toStorageString(labels: List<String>): String = labels.joinToString(", ")

        /** UI 체크박스 표시용 전체 한글 label */
        val allLabels: List<String>
            get() = entries.map { it.label }
    }
}
