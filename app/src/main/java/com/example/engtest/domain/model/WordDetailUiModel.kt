package com.example.engtest.domain.model

import com.example.engtest.data.entity.WordDetailEntity
import com.example.engtest.data.remote.model.DictionaryResponse
import com.example.engtest.data.remote.model.MeaningResponse
import com.google.gson.Gson

data class WordDetailUiModel(
    val word: String,
    val phonetic: String,
    val meanings: List<MeaningUiModel>
)

data class MeaningUiModel(
    val partOfSpeech: String,
    val partOfSpeechKo: String,
    val definitions: List<String>,
    val examples: List<String>,
    val synonyms: List<String>,
    val antonyms: List<String>
)

fun String.toKoreanPos(): String = when (this.lowercase()) {
    "noun" -> "명사"
    "verb" -> "동사"
    "adjective" -> "형용사"
    "adverb" -> "부사"
    "pronoun" -> "대명사"
    "preposition" -> "전치사"
    "conjunction" -> "접속사"
    "interjection" -> "감탄사"
    "article" -> "관사"
    "exclamation" -> "감탄사"
    else -> this
}

fun DictionaryResponse.toUiModel(): WordDetailUiModel {
    return WordDetailUiModel(
        word = word,
        phonetic = phonetic.orEmpty(),
        meanings = meanings.map { meaning ->
            MeaningUiModel(
                partOfSpeech = meaning.partOfSpeech,
                partOfSpeechKo = meaning.partOfSpeech.toKoreanPos(),
                definitions = meaning.definitions.map { it.definition },
                examples = meaning.definitions.mapNotNull { it.example }.filter { it.isNotBlank() },
                synonyms = meaning.synonyms.take(5),
                antonyms = meaning.antonyms.take(5)
            )
        }
    )
}

fun WordDetailEntity.toUiModel(): WordDetailUiModel {
    val gson = Gson()
    // TypeToken<List<T>> / getParameterized 는 R8 환경에서 제네릭 메타데이터 손상 시 실패할 수 있음.
    // Array<T> 파싱 후 toList() 가 ProGuard 안전.
    val meanings: List<MeaningResponse> = try {
        gson.fromJson(meaningsJson, Array<MeaningResponse>::class.java)
            ?.toList()
            ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }
    return DictionaryResponse(
        word = word,
        phonetic = phonetic,
        meanings = meanings
    ).toUiModel()
}
