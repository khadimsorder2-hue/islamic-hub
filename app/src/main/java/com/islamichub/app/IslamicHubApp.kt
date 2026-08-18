package com.islamichub.app

import android.app.Application
import com.islamichub.app.data.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class IslamicHubApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = AppContainer(this)
        // Initialize AI cache (so all ask() calls auto-cache)
        @Suppress("UNUSED_EXPRESSION")
        container.aiCacheRepository
        // Initialize AI config from persisted settings — fixes Quran AI not working.
        // Without this, AIService.config stays at default (empty apiKey) and every
        // ask() returns "কোনো API key কনফিগার করা নেই" even if user set it in Settings.
        appScope.launch {
            try {
                val key = container.settingsRepository.aiApiKey.first()
                val baseUrl = container.settingsRepository.aiBaseUrl.first()
                val model = container.settingsRepository.aiModel.first()
                val provider = container.settingsRepository.aiProvider.first()
                container.aiService.updateConfig(
                    com.islamichub.app.data.repo.AIService.Config(
                        apiKey = key,
                        baseUrl = baseUrl,
                        model = model,
                        provider = provider
                    )
                )
            } catch (_: Exception) { /* fail silent — Settings will reconfigure */ }
        }
    }

    companion object {
        lateinit var instance: IslamicHubApp
            private set
    }
}
