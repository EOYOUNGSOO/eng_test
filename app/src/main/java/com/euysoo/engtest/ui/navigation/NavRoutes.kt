package com.euysoo.engtest.ui.navigation

object NavRoutes {
    const val MAIN = "main"
    const val WORD_MANAGE = "word_manage"
    const val MY_WORD_BOOK = "my_word_book"
    const val WORD_TEST_SELECT = "word_test_select"
    const val WORD_TEST = "word_test"
    const val MULTIPLE_CHOICE_TEST = "multiple_choice_test"
    const val RECORDS = "records"
    const val SETTINGS = "settings"
    const val OCR_GUIDE = "ocr_guide"

    const val RECORDS_DETAIL_ROUTE = "records_detail/{resultId}"

    const val MY_WORD_BOOK_DETAIL_ROUTE = "my_word_book_detail/{bookId}"

    fun recordsDetail(resultId: Long) = "records_detail/$resultId"

    /** 단어 테스트 화면 라우트 (난이도: all, elementary, middle, high, my_book_{id}) */
    fun wordTest(difficulty: String) = "$WORD_TEST/$difficulty"

    fun multipleChoiceTest(difficulty: String) = "$MULTIPLE_CHOICE_TEST/$difficulty"

    fun myWordBookDetail(bookId: Long) = "my_word_book_detail/$bookId"
}
