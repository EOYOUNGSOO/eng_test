package com.example.engtest.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Free Dictionary API (무료) - 영어 단어 발음 기호 조회.
 * https://api.dictionaryapi.dev/api/v2/entries/en/{word}
 */
interface DictionaryApiService {

    @GET("api/v2/entries/en/{word}")
    suspend fun getEntries(@Path("word") word: String): Response<List<DictionaryEntryDto>>
}
