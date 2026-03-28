package com.euysoo.engtest.data.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Free Dictionary API.
 *
 * **R8 + Retrofit:** `suspend fun` 은 컴파일 후 시그니처가 Continuation 기반이라
 * 축약/난독화 시 `HttpServiceMethod.parseAnnotations` 에서 `ParameterizedType` 캐스트 오류가 날 수 있음.
 * 동기 [Call] + [execute] 로만 정의한다 (본문은 [ResponseBody], 파싱은 호출부).
 */
interface DictionaryApiService {
    @GET("api/v2/entries/en/{word}")
    fun getEntries(
        @Path("word") word: String,
    ): Call<ResponseBody>
}
