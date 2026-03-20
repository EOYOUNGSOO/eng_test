package com.example.engtest.data.repository

import com.example.engtest.data.remote.DictionaryApiService
import com.example.engtest.data.remote.DictionaryEntryDto
import com.example.engtest.util.AppLogger
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Free Dictionary API를 사용해 영어 단어의 발음 기호(IPA)를 조회하는 Repository.
 * 단어 추가/편집 시 phonetic이 비어 있을 때 사용. 모든 호출은 Dispatchers.IO에서 실행.
 */
class PhoneticRepository(
    private val api: DictionaryApiService
) {

    /**
     * 단어의 발음 기호를 조회합니다. (백그라운드 스레드, 예외 시 null 반환)
     * @param word 영어 단어 (공백 제거 후 사용)
     * @return 발음 기호 문자열(예: "/həˈloʊ/") 또는 조회 실패 시 null
     */
    suspend fun getPhonetic(word: String): String? = withContext(Dispatchers.IO) {
        val trimmed = word.trim()
        if (trimmed.isBlank()) return@withContext null
        runCatching {
            val gson = Gson()
            api.getEntries(trimmed).let { response ->
                if (!response.isSuccessful) return@runCatching null
                val raw = response.body()?.string().orEmpty()
                val entries = runCatching {
                    gson.fromJson(raw, Array<DictionaryEntryDto>::class.java)?.toList()
                }.getOrNull().orEmpty()
                entries.firstOrNull()
                    ?.phonetics
                    ?.firstOrNull { it.text?.isNotBlank() == true }
                    ?.text
                    ?.trim()
            }
        }.onFailure { e ->
            AppLogger.w(TAG, "getPhonetic failed: word=$word", e)
        }.getOrNull()
    }

    private companion object {
        const val TAG = "PhoneticRepository"
    }
}
