package com.euysoo.engtest.util

/** UI 관련 Flow·애니메이션 타이밍 (ms) */
object UiFlowConstants {
    const val SEARCH_DEBOUNCE_MS: Long = 250L
    const val WORD_BOOK_HIGHLIGHT_CLEAR_DELAY_MS: Long = 4000L

    /** 단어 관리 목록 필터용 검색어 디바운스 (타이핑 중 불필요한 combine 재계산 감소) */
    const val WORD_MANAGE_SEARCH_DEBOUNCE_MS: Long = 300L
}
