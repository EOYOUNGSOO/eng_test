package com.euysoo.engtest.ui.navigation

object NavRoutes {
    const val Main = "main"
    const val WordManage = "word_manage"
    const val WordTestSelect = "word_test_select"
    const val WordTest = "word_test"
    const val Records = "records"

    /** 단어 테스트 화면 라우트 (난이도: all, elementary, middle, high) */
    fun wordTest(difficulty: String) = "word_test/$difficulty"
}
