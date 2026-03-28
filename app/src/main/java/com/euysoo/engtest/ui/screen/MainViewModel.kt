package com.euysoo.engtest.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.di.AppContainer
import com.euysoo.engtest.util.FlowDefaults
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** 홈 상단 요약: 전체 단어 수, 총 테스트 건, 총 평균점수(0~100) */
data class HomeStats(
    val wordCount: Int,
    val testCount: Int,
    val averageScore: Float,
)

class MainViewModel(
    container: AppContainer,
) : ViewModel() {
    private val wordDao = container.database.wordDao()
    private val testResultDao = container.database.testResultDao()

    val stats: StateFlow<HomeStats> =
        combine(
            wordDao.getCountFlow(),
            testResultDao.getCountFlow(),
            testResultDao.getAverageScoreFlow(),
        ) { wordCount, testCount, avgScore ->
            HomeStats(
                wordCount = wordCount,
                testCount = testCount,
                averageScore = avgScore ?: 0f,
            )
        }.stateIn(
            scope = viewModelScope,
            initialValue = HomeStats(0, 0, 0f),
            started = FlowDefaults.whileSubscribed,
        )
}
