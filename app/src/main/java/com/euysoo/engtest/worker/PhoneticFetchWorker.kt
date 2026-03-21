package com.euysoo.engtest.worker

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.CoroutineWorker
import com.euysoo.engtest.util.AppLogger
import androidx.work.WorkerParameters
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.data.entity.PHONETIC_UNAVAILABLE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 와이파이 연결 시 발음기호가 비어 있는 단어에 대해 API로 발음기호를 조회해 DB에 반영.
 * 15분마다 최대 10개 단어 처리 (Android WorkManager 최소 주기 15분).
 */
class PhoneticFetchWorker(
    context: android.content.Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!isWifiConnected()) {
            AppLogger.d(TAG, "WiFi not connected, skip")
            return@withContext Result.success()
        }
        val app = applicationContext.applicationContext as? EngTestApplication ?: run {
            AppLogger.w(TAG, "Application is not EngTestApplication")
            return@withContext Result.retry()
        }
        val dao = app.database.wordDao()
        val repo = app.phoneticRepository
        val words = dao.getWordsWithEmptyPhonetic(10)
        if (words.isEmpty()) {
            AppLogger.d(TAG, "No words with empty phonetic")
            return@withContext Result.success()
        }
        var updated = 0
        for (word in words) {
            try {
                val phonetic = repo.getPhonetic(word.word) ?: PHONETIC_UNAVAILABLE
                dao.update(word.copy(phonetic = phonetic))
                updated++
            } catch (e: Exception) {
                AppLogger.w(TAG, "Phonetic fetch failed for word: ${word.word}", e)
            }
        }
        AppLogger.d(TAG, "Phonetic fetch updated $updated words")
        Result.success()
    }

    private fun isWifiConnected(): Boolean {
        val cm = applicationContext.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    companion object {
        private const val TAG = "PhoneticFetchWorker"
    }
}
