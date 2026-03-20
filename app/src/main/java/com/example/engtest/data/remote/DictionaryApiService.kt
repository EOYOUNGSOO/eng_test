package com.example.engtest.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Free Dictionary API (무료) - 영어 단어 발음 기호 조회.
 * https://api.dictionaryapi.dev/api/v2/entries/en/{word}
 *
 * 응답은 JSON 배열이므로 원칙적으로 [List] 제네릭 + GsonConverterFactory 조합이 가능하지만,
 * R8 릴리즈에서 Retrofit/Gson이 ParameterizedType 을 읽다가 실패하는 기기가 있어
 * [ResponseBody] 로 받고 호출부에서 Array<T> 로 Gson 파싱한다.
 */
interface DictionaryApiService {

    @GET("api/v2/entries/en/{word}")
    suspend fun getEntries(@Path("word") word: String): Response<ResponseBody>

    @GET("api/v2/entries/en/{word}")
    suspend fun getWordDetail(@Path("word") word: String): Response<ResponseBody>
}
