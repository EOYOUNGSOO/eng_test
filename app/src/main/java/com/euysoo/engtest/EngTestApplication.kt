package com.euysoo.engtest

import android.app.Application
import androidx.room.Room
import com.euysoo.engtest.util.AppLogger
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.euysoo.engtest.data.AppDatabase
import com.euysoo.engtest.data.MIGRATION_1_2
import com.euysoo.engtest.data.MIGRATION_2_3
import com.euysoo.engtest.data.MIGRATION_3_4
import com.euysoo.engtest.data.MIGRATION_4_5
import com.euysoo.engtest.data.MIGRATION_5_6
import com.euysoo.engtest.data.MIGRATION_6_7
import com.euysoo.engtest.data.MIGRATION_7_8
import com.euysoo.engtest.data.MIGRATION_8_9
import com.euysoo.engtest.data.loader.WordAssetLoader
import com.euysoo.engtest.data.remote.DictionaryApiService
import com.euysoo.engtest.data.repository.PhoneticRepository
import com.euysoo.engtest.worker.PhoneticFetchWorker
import okhttp3.OkHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

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
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    /** Free Dictionary API용 Retrofit 싱글톤 */
    private val dictionaryRetrofit: Retrofit by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/")
            .client(client)
            .build()
    }

    val phoneticRepository: PhoneticRepository by lazy {
        PhoneticRepository(dictionaryRetrofit.create(DictionaryApiService::class.java))
    }

    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLogger.e(
                tag = "CRASH",
                msg = "미처리 예외 발생 (Thread: ${thread.name})",
                throwable = throwable
            )
            saveCrashLog(throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        warmUpDatabaseOnBackground()
        loadInitialWordsIfNeeded()
        schedulePhoneticFetchWork()
    }

    private fun saveCrashLog(throwable: Throwable) {
        try {
            val prefs = getSharedPreferences("crash_log", MODE_PRIVATE)
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val log = buildString {
                append("시각: ${fmt.format(Date())}\n")
                append("오류: ${throwable.javaClass.simpleName}\n")
                append("메시지: ${throwable.message}\n\n")
                append("스택트레이스:\n")
                append(throwable.stackTraceToString())
            }
            prefs.edit().putString("last_crash", log).apply()
        } catch (_: Exception) {
            // 저장 실패 시 무시
        }
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
