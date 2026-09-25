package com.islamichub.app.data.repo

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

private val Context.settingsStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

enum class BackgroundMode(val label: String, val labelBn: String) {
    WHITE("White", "সাদা"),
    CREAM("Cream", "ক্রিম"),
    SEPIA("Sepia", "সেপিয়া"),
    DARK("Dark", "ডার্ক")
}

enum class TafsirSource(val editionId: String, val displayName: String, val displayNameBn: String) {
    BN_BENGALI("bn.bengali", "Muhiuddin Khan (Bangla)", "মুহিউদ্দীন খান (বাংলা)"),
    BN_MUKHTASAR("bn.mukhtasar", "Tafsir Mukhtasar", "তাফসীর মুখতাসার"),
    EN_SAHIH("en.sahih", "Sahih International (English)", "সহীহ ইন্টারন্যাশনাল (ইংরেজি)"),
    EN_JALALAYN("en.jalalayn", "Tafsir Jalalayn (English)", "তাফসীর জালালাইন (ইংরেজি)")
}

enum class AutoPauseOption(val minutes: Int, val label: String, val labelBn: String) {
    OFF(0, "Off", "বন্ধ"),
    MIN_5(5, "5 minutes", "৫ মিনিট"),
    MIN_15(15, "15 minutes", "১৫ মিনিট"),
    MIN_30(30, "30 minutes", "৩০ মিনিট"),
    MIN_60(60, "60 minutes", "৬০ মিনিট")
}

/**
 * App-wide settings — single source of truth.
 */
class SettingsRepository(private val context: Context) {

    /** AI cache (used for clearAICache action) */
    private val aiCacheRepo by lazy { AICacheRepository(context) }

    // Quran font scale (1.0 = default, 0.85 = small, 1.3 = large, 1.6 = extra large)
    val quranFontScale: Flow<Float> = context.settingsStore.data.map { it[FONT_SCALE] ?: 1.0f }

    // v5.5 — per-script reading text scales (apply to every reading screen)
    val arabicFontScale: Flow<Float> = context.settingsStore.data.map { it[ARABIC_FONT_SCALE] ?: 1.0f }
    val banglaFontScale: Flow<Float> = context.settingsStore.data.map { it[BANGLA_FONT_SCALE] ?: 1.0f }
    val englishFontScale: Flow<Float> = context.settingsStore.data.map { it[ENGLISH_FONT_SCALE] ?: 1.0f }

    // v5.5 — show Bangla transliteration (uccaron) line under Arabic ayahs
    val showTransliteration: Flow<Boolean> = context.settingsStore.data.map { it[SHOW_TRANSLITERATION] ?: true }

    // App-wide background mode (for Quran reader)
    val backgroundMode: Flow<BackgroundMode> = context.settingsStore.data.map {
        val name = it[BACKGROUND_MODE] ?: BackgroundMode.CREAM.name
        BackgroundMode.valueOf(name)
    }

    // Theme (auto = follow system, light, dark)
    val themeMode: Flow<String> = context.settingsStore.data.map { it[THEME_MODE] ?: "auto" }

    // Selected reciter edition ID
    val selectedReciter: Flow<String> = context.settingsStore.data.map {
        it[RECITER] ?: "ar.alafasy"
    }

    // Selected tafsir source
    val tafsirSource: Flow<TafsirSource> = context.settingsStore.data.map {
        val name = it[TAFSIR_SOURCE] ?: TafsirSource.BN_BENGALI.name
        try { TafsirSource.valueOf(name) } catch (_: Exception) { TafsirSource.BN_BENGALI }
    }

    // Auto-pause timer for audio
    val autoPauseMinutes: Flow<AutoPauseOption> = context.settingsStore.data.map {
        val name = it[AUTO_PAUSE] ?: AutoPauseOption.OFF.name
        try { AutoPauseOption.valueOf(name) } catch (_: Exception) { AutoPauseOption.OFF }
    }

    // Bangla meaning audio (read aloud translation after Arabic)
    val banglaAudioEnabled: Flow<Boolean> = context.settingsStore.data.map {
        it[BN_AUDIO_ENABLED] ?: false
    }

    // Word-by-word audio (tajweed helper)
    val wordByWordAudioEnabled: Flow<Boolean> = context.settingsStore.data.map {
        it[WORD_AUDIO_ENABLED] ?: true
    }

    // v5.9.0 — biometric App Lock (face/fingerprint/device credential)
    val appLockEnabled: Flow<Boolean> = context.settingsStore.data.map {
        it[APP_LOCK_ENABLED] ?: false
    }

    // Show Arabic text in Quran reader
    val showArabic: Flow<Boolean> = context.settingsStore.data.map { it[SHOW_ARABIC] ?: true }

    // Show Bangla translation
    val showBangla: Flow<Boolean> = context.settingsStore.data.map { it[SHOW_BANGLA] ?: true }

    // Show English translation
    val showEnglish: Flow<Boolean> = context.settingsStore.data.map { it[SHOW_ENGLISH] ?: true }

    // First launch / onboarding done
    val onboardingDone: Flow<Boolean> = context.settingsStore.data.map { it[ONBOARDING_DONE] ?: false }

    // User profile (name)
    val userName: Flow<String> = context.settingsStore.data.map { it[USER_NAME] ?: "" }

    // AI Scholar config — v5.7.0: built-in default key + Gemini 3 model (works out of the box)
    val aiApiKey: Flow<String> = context.settingsStore.data.map { it[AI_API_KEY] ?: AIService.DEFAULT_API_KEY }
    val aiBaseUrl: Flow<String> = context.settingsStore.data.map {
        it[AI_BASE_URL] ?: "https://generativelanguage.googleapis.com/v1beta"
    }
    val aiModel: Flow<String> = context.settingsStore.data.map {
        it[AI_MODEL] ?: AIService.DEFAULT_MODEL
    }
    val aiProvider: Flow<String> = context.settingsStore.data.map {
        it[AI_PROVIDER] ?: "gemini"
    }

    // v5.6.0: in-app update auto-check throttle (last successful/attempted check)
    val lastUpdateCheckMs: Flow<Long> = context.settingsStore.data.map {
        it[LAST_UPDATE_CHECK_MS] ?: 0L
    }

    suspend fun setQuranFontScale(scale: Float) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[FONT_SCALE] = scale.coerceIn(0.7f, 2.0f) }
    }

    suspend fun setArabicFontScale(scale: Float) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[ARABIC_FONT_SCALE] = scale.coerceIn(0.7f, 1.8f) }
    }

    suspend fun setBanglaFontScale(scale: Float) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[BANGLA_FONT_SCALE] = scale.coerceIn(0.7f, 1.8f) }
    }

    suspend fun setEnglishFontScale(scale: Float) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[ENGLISH_FONT_SCALE] = scale.coerceIn(0.7f, 1.8f) }
    }

    suspend fun setShowTransliteration(show: Boolean) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[SHOW_TRANSLITERATION] = show }
    }

    suspend fun setBackgroundMode(mode: BackgroundMode) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[BACKGROUND_MODE] = mode.name }
    }

    suspend fun setThemeMode(mode: String) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setSelectedReciter(reciterId: String) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[RECITER] = reciterId }
    }

    suspend fun setTafsirSource(source: TafsirSource) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[TAFSIR_SOURCE] = source.name }
    }

    suspend fun setAutoPause(option: AutoPauseOption) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[AUTO_PAUSE] = option.name }
    }

    suspend fun setBanglaAudioEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[BN_AUDIO_ENABLED] = enabled }
    }

    suspend fun setWordByWordAudioEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[WORD_AUDIO_ENABLED] = enabled }
    }

    // v5.9.0 — App Lock toggle
    suspend fun setAppLockEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[APP_LOCK_ENABLED] = enabled }
    }

    // v5.10.0: notification toggles + keep-screen-on while reading
    val prayerNotificationsEnabled: Flow<Boolean> = context.settingsStore.data.map { it[PRAYER_NOTIFICATIONS_ENABLED] ?: true }
    val dailyAyahEnabled: Flow<Boolean> = context.settingsStore.data.map { it[DAILY_AYAH_ENABLED] ?: true }
    val keepScreenOnReading: Flow<Boolean> = context.settingsStore.data.map { it[KEEP_SCREEN_ON_READING] ?: true }

    suspend fun setShowArabic(show: Boolean) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[SHOW_ARABIC] = show }
    }

    suspend fun setShowBangla(show: Boolean) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[SHOW_BANGLA] = show }
    }

    suspend fun setShowEnglish(show: Boolean) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[SHOW_ENGLISH] = show }
    }

    suspend fun setOnboardingDone(done: Boolean = true) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[ONBOARDING_DONE] = done }
    }

    suspend fun setUserName(name: String) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[USER_NAME] = name }
    }

    // AI Scholar settings
    suspend fun setAiApiKey(key: String) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[AI_API_KEY] = key }
    }
    suspend fun setAiBaseUrl(url: String) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[AI_BASE_URL] = url }
    }
    suspend fun setAiModel(model: String) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[AI_MODEL] = model }
    }
    suspend fun setAiProvider(provider: String) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[AI_PROVIDER] = provider }
    }

    suspend fun setPrayerNotificationsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[PRAYER_NOTIFICATIONS_ENABLED] = enabled }
    }
    suspend fun setDailyAyahEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[DAILY_AYAH_ENABLED] = enabled }
    }
    suspend fun setKeepScreenOnReading(enabled: Boolean) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[KEEP_SCREEN_ON_READING] = enabled }
    }

    // v5.6.0: persist the last time we auto-checked for app updates
    suspend fun setLastUpdateCheckMs(ms: Long) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[LAST_UPDATE_CHECK_MS] = ms }
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        // Clear tafsir cache directory
        val tafsirDir = context.filesDir.resolve("tafsir_cache")
        tafsirDir.deleteRecursively()
        // Clear offline translation cache (Quran reader downloads)
        context.filesDir.resolve("translation_cache").deleteRecursively()
        // Clear audio cache (glide/coil)
        context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
    }

    suspend fun clearAICache() = withContext(Dispatchers.IO) {
        aiCacheRepo.clearAll()
    }

    // Ayah Notes
    private fun noteKey(surah: Int, ayah: Int) = stringPreferencesKey("note_${surah}_$ayah")
    suspend fun getAyahNote(surah: Int, ayah: Int): String {
        val key = noteKey(surah, ayah)
        return context.settingsStore.data.map { it[key] ?: "" }.first()
    }
    suspend fun setAyahNote(surah: Int, ayah: Int, text: String) = withContext(Dispatchers.IO) {
        context.settingsStore.edit { it[noteKey(surah, ayah)] = text }
    }

    companion object {
        private val FONT_SCALE = floatPreferencesKey("quran_font_scale")
        private val ARABIC_FONT_SCALE = floatPreferencesKey("arabic_font_scale")
        private val BANGLA_FONT_SCALE = floatPreferencesKey("bangla_font_scale")
        private val ENGLISH_FONT_SCALE = floatPreferencesKey("english_font_scale")
        private val SHOW_TRANSLITERATION = booleanPreferencesKey("show_transliteration")
        private val BACKGROUND_MODE = stringPreferencesKey("background_mode")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val RECITER = stringPreferencesKey("selected_reciter")
        private val TAFSIR_SOURCE = stringPreferencesKey("tafsir_source")
        private val AUTO_PAUSE = stringPreferencesKey("auto_pause")
        private val BN_AUDIO_ENABLED = booleanPreferencesKey("bn_audio_enabled")
        private val WORD_AUDIO_ENABLED = booleanPreferencesKey("word_audio_enabled")
        private val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        private val PRAYER_NOTIFICATIONS_ENABLED = booleanPreferencesKey("prayer_notifications_enabled")
        private val DAILY_AYAH_ENABLED = booleanPreferencesKey("daily_ayah_enabled")
        private val KEEP_SCREEN_ON_READING = booleanPreferencesKey("keep_screen_on_reading")
        private val SHOW_ARABIC = booleanPreferencesKey("show_arabic")
        private val SHOW_BANGLA = booleanPreferencesKey("show_bangla")
        private val SHOW_ENGLISH = booleanPreferencesKey("show_english")
        private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val AI_API_KEY = stringPreferencesKey("ai_api_key")
        private val AI_BASE_URL = stringPreferencesKey("ai_base_url")
        private val AI_MODEL = stringPreferencesKey("ai_model")
        private val AI_PROVIDER = stringPreferencesKey("ai_provider")
        private val LAST_UPDATE_CHECK_MS = longPreferencesKey("last_update_check_ms")
    }
}
