package com.euysoo.engtest.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.EngTestApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** 홈 상단 요약: 전체 단어 수, 총 테스트 건, 총 평균점수(0~100) */
data class HomeStats(
    val wordCount: Int,
    val testCount: Int,
    val averageScore: Float
)

class MainViewModel(
    private val application: EngTestApplication
) : ViewModel() {

    private val wordDao = application.database.wordDao()
    private val testResultDao = application.database.testResultDao()

    /** 통계만 비동기 로드 (전체 단어/결과 목록 로드 없음) */
    val stats: StateFlow<HomeStats> = combine(
        wordDao.getCountFlow(),
        testResultDao.getCountFlow(),
        testResultDao.getAverageScoreFlow()
    ) { wordCount, testCount, avgScore ->
        HomeStats(
            wordCount = wordCount,
            testCount = testCount,
            averageScore = avgScore ?: 0f
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = HomeStats(0, 0, 0f),
        started = SharingStarted.WhileSubscribed(5000)
    )
}
