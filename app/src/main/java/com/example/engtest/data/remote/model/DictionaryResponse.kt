package com.example.engtest.data.remote.model

import com.google.gson.annotations.SerializedName

data class DictionaryResponse(
    @SerializedName("word") val word: String,
    @SerializedName("phonetic") val phonetic: String?,
    @SerializedName("meanings") val meanings: List<MeaningResponse>
)

data class MeaningResponse(
    @SerializedName("partOfSpeech") val partOfSpeech: String,
    @SerializedName("definitions") val definitions: List<DefinitionResponse>,
    @SerializedName("synonyms") val synonyms: List<String> = emptyList(),
    @SerializedName("antonyms") val antonyms: List<String> = emptyList()
)

data class DefinitionResponse(
    @SerializedName("definition") val definition: String,
    @SerializedName("example") val example: String?
)
