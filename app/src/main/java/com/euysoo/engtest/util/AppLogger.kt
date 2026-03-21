package com.euysoo.engtest.util

import android.util.Log

/**
 * Logcat 출력 + 앱 내 로그 뷰어용 메모리 버퍼.
 * 실시간 로그는 앱 재시작 시 초기화됨.
 */
object AppLogger {

    private const val MAX_LOG_SIZE = 300

    private val logLock = Any()

    data class LogEntry(
        val level: String,
        val tag: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _logs = mutableListOf<LogEntry>()

    val logs: List<LogEntry>
        get() = synchronized(logLock) { _logs.toList() }

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
        add("D", tag, msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
        add("I", tag, msg)
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
        add("W", tag, msg)
    }

    fun w(tag: String, msg: String, throwable: Throwable?) {
        Log.w(tag, msg, throwable)
        val fullMsg = if (throwable != null) {
            "$msg\n${throwable.stackTraceToString()}"
        } else msg
        add("W", tag, fullMsg)
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        Log.e(tag, msg, throwable)
        val fullMsg = if (throwable != null) {
            "$msg\n${throwable.stackTraceToString()}"
        } else msg
        add("E", tag, fullMsg)
    }

    /** [e]와 동일 (기존 호출부 호환). */
    fun eRelease(tag: String, message: String, throwable: Throwable?) {
        e(tag, message, throwable)
    }

    fun clear() {
        synchronized(logLock) { _logs.clear() }
    }

    private fun add(level: String, tag: String, message: String) {
        synchronized(logLock) {
            while (_logs.size >= MAX_LOG_SIZE) {
                _logs.removeAt(0)
            }
            _logs.add(LogEntry(level, tag, message))
        }
    }
}
