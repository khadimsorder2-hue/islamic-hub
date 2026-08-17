package com.islamichub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.islamichub.app.data.repo.DailyAyahWorker
import com.islamichub.app.ui.navigation.IslamicHubNavGraph
import com.islamichub.app.ui.screens.applock.AppLockScreen
import com.islamichub.app.ui.screens.onboarding.OnboardingScreen
import com.islamichub.app.ui.theme.IslamicHubTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )

        // Schedule daily ayah notification
        DailyAyahWorker.schedule(this)

        setContent {
            IslamicHubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val container = (application as IslamicHubApp).container

                    // Check onboarding status
                    var showOnboarding by remember { mutableStateOf(false) }
                    var showAppLock by remember { mutableStateOf(false) }
                    var checked by remember { mutableStateOf(false) }

                    // Initial check
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        val onboardingDone = runBlocking {
                            container.settingsRepository.onboardingDone.first()
                        }
                        showOnboarding = !onboardingDone
                        // App lock — check if enabled (future: add setting)
                        // For now, skip app lock
                        checked = true
                    }

                    when {
                        !checked -> {
                            // Splash/loading
                        }
                        showOnboarding -> {
                            OnboardingScreen(
                                container = container,
                                onComplete = { showOnboarding = false }
                            )
                        }
                        showAppLock -> {
                            AppLockScreen(
                                onUnlock = { showAppLock = false }
                            )
                        }
                        else -> {
                            IslamicHubNavGraph(
                                container = container
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            (application as IslamicHubApp).container.audioController.release()
        } catch (_: Exception) { }
    }
}
