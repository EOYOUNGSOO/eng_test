package com.euysoo.engtest.util

import java.util.Calendar
import java.util.Locale

/** 기록 화면 기본 기간·일 단위 자정 정렬 (Calendar 인스턴스당 한 스레드에서만 사용) */
class CalendarDateBounds(
    private val calendar: Calendar = Calendar.getInstance(Locale.getDefault()),
) {
    fun startOfDayMillis(millis: Long): Long {
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun defaultFromMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        calendar.timeInMillis = nowMillis
        calendar.add(Calendar.DAY_OF_MONTH, -TimeConstants.RECORDS_DEFAULT_RANGE_DAYS)
        return startOfDayMillis(calendar.timeInMillis)
    }

    fun defaultToEndOfDayMillis(nowMillis: Long = System.currentTimeMillis()): Long =
        startOfDayMillis(nowMillis) + TimeConstants.MILLIS_PER_DAY - 1
}
