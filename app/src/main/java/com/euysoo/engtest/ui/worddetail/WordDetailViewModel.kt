package com.euysoo.engtest.ui.worddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euysoo.engtest.data.AppDatabase
import com.euysoo.engtest.data.entity.WordDetailEntity
import com.euysoo.engtest.data.json.AppJson
import com.euysoo.engtest.data.remote.DictionaryApiService
import com.euysoo.engtest.data.remote.model.DictionaryResponse
import com.euysoo.engtest.data.remote.model.MeaningResponse
import com.euysoo.engtest.domain.model.toUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class WordDetailViewModel(
    private val db: AppDatabase,
    private val dictionaryApi: DictionaryApiService,
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
                val response =
                    withContext(Dispatchers.IO) {
                        dictionaryApi.getEntries(query).execute()
                    }
                when {
                    response.isSuccessful -> {
                        val bodyString = response.body()?.string()
                        if (bodyString.isNullOrBlank()) {
                            _uiState.value = WordDetailUiState.NotFound
                            return@launch
                        }

                        val entries =
                            try {
                                AppJson.json.decodeFromString(
                                    ListSerializer(DictionaryResponse.serializer()),
                                    bodyString,
                                )
                            } catch (_: Exception) {
                                // val preview = bodyString.take(500)
                                // AppLogger.e(TAG, "JSON 파싱 실패 (앞 500자): $preview", e)
                                _uiState.value = WordDetailUiState.Error("데이터 파싱 오류")
                                return@launch
                            }

                        // AppLogger.i(TAG, "API 파싱 완료: code=${response.code()}, entries=${entries.size}")

                        if (entries.isEmpty()) {
                            _uiState.value = WordDetailUiState.NotFound
                            return@launch
                        }

                        val result = entries.first()
                        val meaningsJson =
                            AppJson.json.encodeToString(
                                ListSerializer(MeaningResponse.serializer()),
                                result.meanings,
                            )
                        db.wordDetailDao().insert(
                            WordDetailEntity(
                                word = result.word.lowercase(),
                                phonetic = result.phonetic,
                                meaningsJson = meaningsJson,
                            ),
                        )
                        // AppLogger.i(TAG, "캐시 저장 완료: ${result.word.lowercase()}")
                        _uiState.value = WordDetailUiState.Success(result.toUiModel())
                    }

                    response.code() == 404 -> _uiState.value = WordDetailUiState.NotFound
                    else -> _uiState.value = WordDetailUiState.Error("서버 오류: ${response.code()}")
                }
            } catch (_: UnknownHostException) {
                _uiState.value = WordDetailUiState.Error("인터넷 연결을 확인해주세요")
            } catch (_: SocketTimeoutException) {
                _uiState.value = WordDetailUiState.Error("서버 응답 시간이 초과되었습니다")
            } catch (e: Exception) {
                // AppLogger.e(TAG, "loadWordDetail failed", e)
                _uiState.value = WordDetailUiState.Error("오류: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = WordDetailUiState.Idle
    }
}
