package com.euysoo.engtest.domain.wrongnote

import com.euysoo.engtest.data.entity.WordDifficulty

enum class WrongNoteDifficultyOption {
    ALL,
    ELEMENTARY,
    MIDDLE,
    HIGH,
    ;

    fun toWordDifficultyOrNull(): WordDifficulty? =
        when (this) {
            ALL -> null
            ELEMENTARY -> WordDifficulty.ELEMENTARY
            MIDDLE -> WordDifficulty.MIDDLE
            HIGH -> WordDifficulty.HIGH
        }
}

/**
 * - [WRONG_RANDOM_100]: 틀린 단어만 임의로 최대 100개 (부족 시 [WrongNoteOutcome.NeedMixConfirm])
 * - [MIX_100]: 틀린 단어 + 임의 단어로 100개 채우기
 * - [WRONG_ONLY_AVAILABLE]: 틀린 단어 전부만 담기 (100 미만 허용, 섞기 거절 시)
 */
enum class WrongNoteFillSelection {
    WRONG_RANDOM_100,
    MIX_100,
    WRONG_ONLY_AVAILABLE,
}

sealed class WrongNoteOutcome {
    data class Created(val bookId: Long) : WrongNoteOutcome()

    /** 제목 없음 */
    data object EmptyTitle : WrongNoteOutcome()

    /** 테스트 이력에 오답이 없음 */
    data object NoWrongHistory : WrongNoteOutcome()

    /** 난이도 필터 후 오답 단어 없음 */
    data object NoWrongAfterDifficulty : WrongNoteOutcome()

    /** 틀린 단어만 100개 모드인데 100개 미만 → 임의 단어 섞기 제안 */
    data class NeedMixConfirm(val wrongCount: Int) : WrongNoteOutcome()

    /** DB 등 예외 */
    data object CreateFailed : WrongNoteOutcome()
}
