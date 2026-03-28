package com.euysoo.engtest.util

import kotlinx.coroutines.flow.SharingStarted

/** StateFlow.stateIn / shareIn 공통 옵션 */
object FlowDefaults {
    const val WHILE_SUBSCRIBED_TIMEOUT_MS: Long = 5000L

    val whileSubscribed: SharingStarted
        get() = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_TIMEOUT_MS)
}
