package com.euysoo.engtest.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 교육부_필수어휘_초중고.json 루트 구조.
 * - vocabulary: 단어 배열
 */
@Serializable
data class EducationVocabRoot(
    @SerialName("title") val title: String? = null,
    @SerialName("source") val source: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("total") val total: Int? = null,
    @SerialName("vocabulary") val vocabulary: List<EducationVocabItem> = emptyList()
)

/**
 * JSON 항목: id, word, pos, meaning, level(초등|중등|고등).
 */
@Serializable
data class EducationVocabItem(
    @SerialName("id") val id: Int,
    @SerialName("word") val word: String,
    @SerialName("pos") val pos: String,
    @SerialName("meaning") val meaning: String,
    @SerialName("level") val level: String
)
