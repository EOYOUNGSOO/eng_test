package com.example.engtest.ui.worddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engtest.data.AppDatabase
import com.example.engtest.data.entity.WordDetailEntity
import com.example.engtest.data.remote.model.DictionaryResponse
import com.example.engtest.data.remote.RetrofitClient
import com.example.engtest.domain.model.toUiModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class WordDetailViewModel(
    private val db: AppDatabase
) : ViewModel() {
    private val _uiState = MutableStateFlow<WordDetailUiState>(WordDetailUiState.Idle)
    val uiState: StateFlow<WordDetailUiState> = _uiState.asStateFlow()

    fun loadWordDetail(word: String) {
        val query = word.trim().lowercase()
        if (query.isBlank()) return
        viewModelScope.launch {
            val cached = db.wordDetailDao().getByWord(query)
            if (cached != null) {
                _uiState.value = WordDetailUiState.Success(cached.toUiModel())
                return@launch
            }

            _uiState.value = WordDetailUiState.Loading
            try {
                val response = RetrofitClient.instance.getWordDetail(query)
                val gson = Gson()
                when {
                    response.isSuccessful -> {
                        val raw = response.body()?.string().orEmpty()
                        val list = runCatching {
                            gson.fromJson(raw, Array<DictionaryResponse>::class.java)?.toList()
                        }.getOrNull()
                        when {
                            !list.isNullOrEmpty() -> {
                                val result = list.first()
                                db.wordDetailDao().insert(
                                    WordDetailEntity(
                                        word = result.word.lowercase(),
                                        phonetic = result.phonetic,
                                        meaningsJson = gson.toJson(result.meanings)
                                    )
                                )
                                _uiState.value = WordDetailUiState.Success(result.toUiModel())
                            }
                            raw.isBlank() -> _uiState.value = WordDetailUiState.NotFound
                            else -> _uiState.value =
                                WordDetailUiState.Error("단어 정보를 해석할 수 없습니다")
                        }
                    }

                    response.code() == 404 -> _uiState.value = WordDetailUiState.NotFound
                    else -> _uiState.value = WordDetailUiState.Error("서버 오류: ${response.code()}")
                }
            } catch (_: UnknownHostException) {
                _uiState.value = WordDetailUiState.Error("인터넷 연결을 확인해주세요")
            } catch (e: Exception) {
                _uiState.value = WordDetailUiState.Error("오류: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = WordDetailUiState.Idle
    }
}
