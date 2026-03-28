package com.euysoo.engtest.data.remote.model

import com.euysoo.engtest.data.remote.PhoneticDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DictionaryResponse(
    @SerialName("word") val word: String,
    @SerialName("phonetic") val phonetic: String? = null,
    @SerialName("phonetics") val phonetics: List<PhoneticDto>? = null,
    @SerialName("meanings") val meanings: List<MeaningResponse> = emptyList(),
)

@Serializable
data class MeaningResponse(
    @SerialName("partOfSpeech") val partOfSpeech: String = "",
    @SerialName("definitions") val definitions: List<DefinitionResponse> = emptyList(),
    @SerialName("synonyms") val synonyms: List<String> = emptyList(),
    @SerialName("antonyms") val antonyms: List<String> = emptyList(),
)

@Serializable
data class DefinitionResponse(
    @SerialName("definition") val definition: String = "",
    @SerialName("example") val example: String? = null,
)
