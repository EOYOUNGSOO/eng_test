package com.example.engtest.util

import android.util.Log
import com.example.engtest.BuildConfig

/**
 * 배포 빌드에서는 디버그/경고 로그를 제외하고, 예외는 안전하게만 기록.
 * DEBUG 빌드에서만 d/w/e 모두 출력, 릴리즈에서는 로그 제외로 성능·개인정보 유출 방지.
 */
object AppLogger {

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.w(tag, message)
    }

    fun w(tag: String, message: String, throwable: Throwable?) {
        if (BuildConfig.DEBUG) Log.w(tag, message, throwable)
    }

    fun e(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.e(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable?) {
        if (BuildConfig.DEBUG) Log.e(tag, message, throwable)
    }
}
