package com.euysoo.engtest.data.repository

import com.euysoo.engtest.data.json.AppJson
import com.euysoo.engtest.data.remote.DictionaryApiService
import com.euysoo.engtest.data.remote.DictionaryEntryDto
import com.euysoo.engtest.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer

/**
 * Free Dictionary API를 사용해 영어 단어의 발음 기호(IPA)를 조회하는 Repository.
 * 단어 추가/편집 시 phonetic이 비어 있을 때 사용. 모든 호출은 Dispatchers.IO에서 실행.
 */
class PhoneticRepository(
    private val api: DictionaryApiService,
) {
    /**
     * 단어의 발음 기호를 조회합니다. (백그라운드 스레드, 예외 시 null 반환)
     * @param word 영어 단어 (공백 제거 후 사용)
     * @return 발음 기호 문자열(예: "/həˈloʊ/") 또는 조회 실패 시 null
     */
    suspend fun getPhonetic(word: String): String? =
        withContext(Dispatchers.IO) {
            val trimmed = word.trim()
            if (trimmed.isBlank()) return@withContext null
            try {
                val response = api.getEntries(trimmed).execute()
                if (!response.isSuccessful) {
                    AppLogger.w(TAG, "HTTP ${response.code()}: word=$word")
                    return@withContext null
                }
                val bodyString = response.body()?.string() ?: return@withContext null
                val entries =
                    AppJson.json.decodeFromString(
                        ListSerializer(DictionaryEntryDto.serializer()),
                        bodyString,
                    )
                val entry = entries.firstOrNull() ?: return@withContext null
                entry.phonetics
                    .firstOrNull { it.text?.isNotBlank() == true }
                    ?.text
                    ?.trim()
                    ?: entry.phonetic?.takeIf { it.isNotBlank() }?.trim()
            } catch (e: Exception) {
                AppLogger.e(TAG, "getPhonetic failed: word=$word", e)
                null
            }
        }

    private companion object {
        const val TAG = "PhoneticRepository"
    }
}
