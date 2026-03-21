package com.euysoo.engtest.data.json

import kotlinx.serialization.json.Json

/**
 * 앱 전역 JSON 설정 (kotlinx.serialization).
 * Retrofit 컨버터·캐시 직렬화·assets 파싱에 동일 인스턴스 사용.
 */
object AppJson {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
}
