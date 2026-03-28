package com.euysoo.engtest.data.remote

/** Free Dictionary API 공통 설정 ([PhoneticRepository]·단어 상세·동일 Retrofit 인스턴스) */
object DictionaryNetworkConfig {
    const val BASE_URL: String = "https://api.dictionaryapi.dev/"
    const val CLIENT_TIMEOUT_SECONDS: Long = 10L
}
