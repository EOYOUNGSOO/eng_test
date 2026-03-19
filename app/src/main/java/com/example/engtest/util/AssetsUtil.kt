package com.example.engtest.util

import android.content.Context
import java.io.InputStreamReader

/**
 * assets 폴더의 파일을 UTF-8 문자열로 읽는 유틸리티.
 */
object AssetsUtil {

    /**
     * @param context AssetManager 접근용
     * @param fileName assets 루트 기준 파일명 (예: "교육부_필수어휘_초중고.json")
     * @return 파일 전체 내용, 없으면 빈 문자열
     */
    fun readAsString(context: Context, fileName: String): String {
        return runCatching {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { it.readText() }
            }
        }.getOrElse { "" }
    }
}
