package com.islamichub.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.islamichub.app.data.local.ContentAssetSource
import com.islamichub.app.data.local.DuaData
import com.islamichub.app.data.local.HadithAssetSource
import com.islamichub.app.data.local.NamesData
import com.islamichub.app.data.local.QuranAssetSource
import com.islamichub.app.data.local.QuranData
import com.islamichub.app.data.remote.AladhanApi
import com.islamichub.app.data.repo.AICacheRepository
import com.islamichub.app.data.repo.AIService
import com.islamichub.app.data.repo.AudioController
import com.islamichub.app.data.repo.AudioDownloadService
import com.islamichub.app.data.repo.BackupRestoreService
import com.islamichub.app.data.repo.BookmarkRepository
import com.islamichub.app.data.repo.ContentRepository
import com.islamichub.app.data.repo.DuaRepository
import com.islamichub.app.data.repo.HadithRepository
import com.islamichub.app.data.repo.JamatTimeRepository
import com.islamichub.app.data.repo.KhatamRepository
import com.islamichub.app.data.repo.LastReadRepository
import com.islamichub.app.data.repo.NamesRepository
import com.islamichub.app.data.repo.PrayerRepository
import com.islamichub.app.data.repo.PrayerScheduler
import com.islamichub.app.data.repo.QadaRepository
import com.islamichub.app.data.repo.QuranRepository
import com.islamichub.app.data.repo.SettingsRepository
import com.islamichub.app.data.repo.TafsirRepository
import com.islamichub.app.data.repo.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "islamichub_prefs")

/**
 * Manual DI container. Avoids Hilt to keep the build simple and fast.
 * Single instance created in [IslamicHubApp].
 */
class AppContainer(internal val context: Context) {

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(AladhanApi.BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val islamicAppRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(com.islamichub.app.data.remote.IslamicAppApi.BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val aladhanApi: AladhanApi by lazy { retrofit.create(AladhanApi::class.java) }
    val islamicAppApi: com.islamichub.app.data.remote.IslamicAppApi by lazy {
        islamicAppRetrofit.create(com.islamichub.app.data.remote.IslamicAppApi::class.java)
    }

    private val quranAssetSource: QuranAssetSource by lazy { QuranAssetSource(context) }
    private val hadithAssetSource: HadithAssetSource by lazy { HadithAssetSource(context) }
    private val contentAssetSource: ContentAssetSource by lazy { ContentAssetSource(context) }

    val quranRepository: QuranRepository by lazy {
        QuranRepository(QuranData, quranAssetSource)
    }
    val hadithRepository: HadithRepository by lazy { HadithRepository(hadithAssetSource) }
    val contentRepository: ContentRepository by lazy { ContentRepository(contentAssetSource) }
    val namesRepository: NamesRepository by lazy { NamesRepository(NamesData.names) }
    val duaRepository: DuaRepository by lazy { DuaRepository(DuaData.duas, DuaData.dhikrOptions) }
    val prayerRepository: PrayerRepository by lazy { PrayerRepository(aladhanApi, context) }
    val tasbihRepository: TasbihRepository by lazy { TasbihRepository(context) }
    val audioController: AudioController by lazy { AudioController(context) }
    val prayerScheduler: PrayerScheduler by lazy { PrayerScheduler(context, prayerRepository) }
    val aiService: AIService by lazy { AIService(context) }

    /** AI response cache — shared across all AI screens (Tafsir, Hadith, Scholar) */
    val aiCacheRepository: AICacheRepository by lazy {
        AICacheRepository(context).also { cache ->
            // Inject cache into AIService so all ask() calls auto-cache
            aiService.cache = cache
        }
    }
    // New v1.2.0 repositories
    val bookmarkRepository: BookmarkRepository by lazy { BookmarkRepository(context) }
    val qadaRepository: QadaRepository by lazy { QadaRepository(context) }
    val trackerRepository: TrackerRepository by lazy { TrackerRepository(context) }
    val lastReadRepository: LastReadRepository by lazy { LastReadRepository(context) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(context) }
    val tafsirRepository: TafsirRepository by lazy { TafsirRepository(context) }
    val khatamRepository: KhatamRepository by lazy { KhatamRepository(context) }
    val jamatTimeRepository: JamatTimeRepository by lazy { JamatTimeRepository(context) }

    // v1.9.0 new services
    val audioDownloadService: AudioDownloadService by lazy { AudioDownloadService(context) }
    val backupRestoreService: BackupRestoreService by lazy {
        BackupRestoreService(
            context = context,
            bookmarkRepo = bookmarkRepository,
            qadaRepo = qadaRepository,
            trackerRepo = trackerRepository,
            khatamRepo = khatamRepository,
            settingsRepo = settingsRepository
        )
    }

    // v3.1.0 new services
    val fastingRepository by lazy {
        com.islamichub.app.ui.screens.fasting.FastingRepository(context)
    }

    // v3.3.0 — API-driven topic study + Hadith topic study
    val topicStudyRepository by lazy {
        com.islamichub.app.data.repo.TopicStudyRepository(
            api = islamicAppApi,
            quranData = QuranData,
            quranAssetSource = quranAssetSource
        )
    }

    val hadithTopicRepository by lazy {
        com.islamichub.app.data.repo.HadithTopicRepository(hadithAssetSource)
    }
}

/** Tiny preferences wrapper for tasbih counts. */
class TasbihRepository(private val context: Context) {

    private val KEY_COUNT = intPreferencesKey("tasbih_count")
    private val KEY_TOTAL = intPreferencesKey("tasbih_total")
    private val KEY_ROUND = intPreferencesKey("tasbih_round")
    private val KEY_DHIKR_ID = stringPreferencesKey("tasbih_dhikr_id")

    val count: Flow<Int> = context.dataStore.data.map { it[KEY_COUNT] ?: 0 }
    val total: Flow<Int> = context.dataStore.data.map { it[KEY_TOTAL] ?: 0 }
    val round: Flow<Int> = context.dataStore.data.map { it[KEY_ROUND] ?: 0 }
    val dhikrId: Flow<String> = context.dataStore.data.map { it[KEY_DHIKR_ID] ?: "subhanallah" }

    suspend fun setDhikr(id: String) {
        context.dataStore.edit { it[KEY_DHIKR_ID] = id }
    }

    suspend fun increment(): Int {
        var newCount = 0
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_COUNT] ?: 0
            val currentTotal = prefs[KEY_TOTAL] ?: 0
            newCount = current + 1
            prefs[KEY_COUNT] = newCount
            prefs[KEY_TOTAL] = currentTotal + 1
        }
        return newCount
    }

    suspend fun checkRoundComplete(target: Int): Boolean {
        var completed = false
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_COUNT] ?: 0
            if (current >= target) {
                prefs[KEY_COUNT] = 0
                prefs[KEY_ROUND] = (prefs[KEY_ROUND] ?: 0) + 1
                completed = true
            }
        }
        return completed
    }

    suspend fun reset() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_COUNT)
            prefs.remove(KEY_ROUND)
        }
    }

    suspend fun resetAll() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_COUNT)
            prefs.remove(KEY_ROUND)
            prefs.remove(KEY_TOTAL)
        }
    }
}
