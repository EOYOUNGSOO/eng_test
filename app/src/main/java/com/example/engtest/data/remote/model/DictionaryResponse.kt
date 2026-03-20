package com.example.engtest.data.remote.model

data class DictionaryResponse(
    val word: String,
    val phonetic: String?,
    val meanings: List<MeaningResponse>
)

data class MeaningResponse(
    val partOfSpeech: String,
    val definitions: List<DefinitionResponse>,
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList()
)

data class DefinitionResponse(
    val definition: String,
    val example: String?
)
