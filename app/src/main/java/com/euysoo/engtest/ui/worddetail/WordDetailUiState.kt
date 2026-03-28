package com.euysoo.engtest.ui.worddetail

import com.euysoo.engtest.domain.model.WordDetailUiModel

sealed class WordDetailUiState {
    data object Idle : WordDetailUiState()

    data object Loading : WordDetailUiState()

    data object NotFound : WordDetailUiState()

    data class Success(
        val data: WordDetailUiModel,
    ) : WordDetailUiState()

    data class Error(
        val message: String,
    ) : WordDetailUiState()
}
