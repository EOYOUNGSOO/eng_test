package com.example.engtest.data.model

import com.google.gson.annotations.SerializedName

/**
 * 교육부_필수어휘_초중고.json 루트 구조.
 * - vocabulary: 단어 배열
 */
data class EducationVocabRoot(
    @SerializedName("vocabulary") val vocabulary: List<EducationVocabItem>
)

/**
 * JSON 항목: id, word, pos, meaning, level(초등|중등|고등).
 * Word Entity와 필드 매핑: pos→partOfSpeech, level→difficulty(enum)
 */
data class EducationVocabItem(
    @SerializedName("id") val id: Int,
    @SerializedName("word") val word: String,
    @SerializedName("pos") val pos: String,
    @SerializedName("meaning") val meaning: String,
    @SerializedName("level") val level: String
)
