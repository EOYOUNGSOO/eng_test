package com.example.engtest.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Free Dictionary API (https://api.dictionaryapi.dev/api/v2/entries/en/{word}) 응답 DTO.
 * 발음 기호(phonetics[].text) 추출용.
 */
data class DictionaryEntryDto(
    @SerializedName("word") val word: String? = null,
    @SerializedName("phonetics") val phonetics: List<PhoneticDto>? = null
)

data class PhoneticDto(
    @SerializedName("text") val text: String? = null,
    @SerializedName("audio") val audio: String? = null
)
