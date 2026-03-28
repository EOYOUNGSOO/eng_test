package com.euysoo.engtest.data.remote

import com.euysoo.engtest.data.remote.model.MeaningResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Free Dictionary API 배열 항목 — 발음(phonetics) 추출용.
 * 전체 응답과 동일 스키마이나, 파싱 시 필요한 필드만 사용.
 */
@Serializable
data class DictionaryEntryDto(
    @SerialName("word") val word: String = "",
    @SerialName("phonetic") val phonetic: String? = null,
    @SerialName("phonetics") val phonetics: List<PhoneticDto> = emptyList(),
    @SerialName("meanings") val meanings: List<MeaningResponse> = emptyList(),
)

/**
 * Free Dictionary API 응답 내 `phonetics` 배열 항목.
 */
@Serializable
data class PhoneticDto(
    @SerialName("text") val text: String? = null,
    @SerialName("audio") val audio: String? = null,
)
