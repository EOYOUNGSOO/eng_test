package com.euysoo.engtest.data.assets

import android.content.res.AssetManager
import com.euysoo.engtest.data.json.AppJson
import com.euysoo.engtest.data.model.EducationVocabRoot

/**
 * 교육부 필수 어휘 JSON (assets).
 * 우선 파일이 없으면 예비 파일을 사용한다.
 */
object EducationVocabAssets {
    const val PRIMARY_FILE_NAME: String = "교육부_필수어휘_3000.json"
    const val FALLBACK_FILE_NAME: String = "교육부_필수어휘_초중고.json"

    /** 초기화(리셋) 시 적재할 교육부 필수 어휘 개수 */
    const val INITIAL_VOCAB_COUNT: Int = 3000

    /**
     * 단어 DB 초기화용 JSON.
     * - [PRIMARY_FILE_NAME]이 있으면 그대로 사용
     * - 없으면 [FALLBACK_FILE_NAME]에서 앞쪽 [INITIAL_VOCAB_COUNT]건만 잘라 동일 스키마로 직렬화
     */
    fun readJsonForInitialVocabularyReset(assets: AssetManager): String {
        if (assetExists(assets, PRIMARY_FILE_NAME)) {
            return assets.open(PRIMARY_FILE_NAME).bufferedReader().use { it.readText() }
        }
        val baseJson =
            assets.open(FALLBACK_FILE_NAME).bufferedReader().use { it.readText() }
        val root = AppJson.json.decodeFromString(EducationVocabRoot.serializer(), baseJson)
        val limited = root.vocabulary.take(INITIAL_VOCAB_COUNT)
        val trimmed =
            root.copy(
                vocabulary = limited,
                total = limited.size,
            )
        return AppJson.json.encodeToString(EducationVocabRoot.serializer(), trimmed)
    }

    fun readJsonOrThrow(assets: AssetManager): String {
        val name =
            if (assetExists(assets, PRIMARY_FILE_NAME)) {
                PRIMARY_FILE_NAME
            } else {
                FALLBACK_FILE_NAME
            }
        return assets.open(name).bufferedReader().use { it.readText() }
    }

    private fun assetExists(
        assets: AssetManager,
        fileName: String,
    ): Boolean =
        runCatching {
            assets.open(fileName).close()
            true
        }.getOrDefault(false)
}
