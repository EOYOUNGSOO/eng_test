package com.euysoo.engtest.data.assets

import android.content.res.AssetManager

/**
 * 교육부 필수 어휘 JSON (assets).
 * 우선 파일이 없으면 예비 파일을 사용한다.
 */
object EducationVocabAssets {
    const val PRIMARY_FILE_NAME: String = "교육부_필수어휘_3000.json"
    const val FALLBACK_FILE_NAME: String = "교육부_필수어휘_초중고.json"

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
