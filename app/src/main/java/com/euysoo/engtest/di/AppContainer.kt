package com.euysoo.engtest.di

import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.data.AppDatabase
import com.euysoo.engtest.data.remote.DictionaryApiService
import com.euysoo.engtest.data.repository.PhoneticRepository
import com.euysoo.engtest.data.repository.WordSyncManager

/**
 * 앱 프로세스 수명과 맞춘 의존성 묶음.
 * ViewModel 등은 [EngTestApplication] 직접 참조 대신 이 컨테이너를 주입받아 결합도를 낮춘다.
 */
class AppContainer(
    private val application: EngTestApplication,
) {
    val applicationContext get() = application.applicationContext
    val database: AppDatabase get() = application.database
    val dictionaryApi: DictionaryApiService get() = application.dictionaryApi
    val phoneticRepository: PhoneticRepository get() = application.phoneticRepository

    val wordSyncManager: WordSyncManager by lazy { WordSyncManager(database) }
}
