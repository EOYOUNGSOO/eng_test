package com.euysoo.engtest.ui.navigation

object NavRoutes {
    const val Main = "main"
    const val WordManage = "word_manage"
    const val MyWordBook = "my_word_book"
    const val WordTestSelect = "word_test_select"
    const val WordTest = "word_test"
    const val MultipleChoiceTest = "multiple_choice_test"
    const val Records = "records"

    const val MyWordBookDetailRoute = "my_word_book_detail/{bookId}"

    /** 단어 테스트 화면 라우트 (난이도: all, elementary, middle, high, my_book_{id}) */
    fun wordTest(difficulty: String) = "word_test/$difficulty"

    fun multipleChoiceTest(difficulty: String) = "multiple_choice_test/$difficulty"

    fun myWordBookDetail(bookId: Long) = "my_word_book_detail/$bookId"
}
