package com.example.engtest

import android.app.Application
import androidx.room.Room
import com.example.engtest.util.AppLogger
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.engtest.data.AppDatabase
import com.example.engtest.data.MIGRATION_1_2
import com.example.engtest.data.MIGRATION_2_3
import com.example.engtest.data.MIGRATION_3_4
import com.example.engtest.data.loader.WordAssetLoader
import com.example.engtest.data.remote.DictionaryApiService
import com.example.engtest.worker.PhoneticFetchWorker
import com.example.engtest.data.repository.PhoneticRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 앱 전역 Application.
 * - Room DB 싱글톤 제공
 * - 앱 최초 실행 시(또는 DB 비어 있을 때) assets/words.json → DB 적재
 *
 * DB는 첫 접근 시 빌드·마이그레이션이 실행되므로, 메인 스레드에서 접근하면
 * "Skipped N frames" / 블랙아웃이 발생할 수 있음. 따라서 onCreate()에서
 * 백그라운드에서 미리 초기화(워밍업)하여, UI 진입 시에는 이미 빌드된 인스턴스만 사용하도록 함.
 */
class EngTestApplication : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "eng_test_db"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    /** Free Dictionary API용 Retrofit 싱글톤 (OkHttp 클라이언트는 Retrofit 기본 사용). */
    private val dictionaryRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val phoneticRepository: PhoneticRepository by lazy {
        PhoneticRepository(dictionaryRetrofit.create(DictionaryApiService::class.java))
    }

    override fun onCreate() {
        super.onCreate()
        warmUpDatabaseOnBackground()
        loadInitialWordsIfNeeded()
        schedulePhoneticFetchWork()
    }

    /** 와이파이 시 15분마다 발음기호 없는 단어 최대 10개 API 조회 (WorkManager 최소 주기 15분) */
    private fun schedulePhoneticFetchWork() {
        val request = PeriodicWorkRequestBuilder<PhoneticFetchWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "phonetic_fetch",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Room DB 빌드·마이그레이션을 메인 스레드가 아닌 백그라운드에서 미리 실행.
     * 완료될 때까지 onCreate()에서 대기하여, 첫 화면 진입 시 메인 스레드가 블로킹되지 않도록 함.
     */
    private fun warmUpDatabaseOnBackground() {
        runBlocking(Dispatchers.IO) {
            database.openHelper.writableDatabase
        }
    }

    /** DB가 비어 있으면 assets/words.json을 DB에 적재 */
    private fun loadInitialWordsIfNeeded() {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val dao = database.wordDao()
                if (dao.getCount() == 0) {
                    WordAssetLoader.loadFromAssets(applicationContext, dao)
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "loadInitialWordsIfNeeded failed", e)
            }
        }
    }

    private companion object {
        const val TAG = "EngTestApplication"
    }
}
