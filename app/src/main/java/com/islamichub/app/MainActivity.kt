package com.islamichub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.islamichub.app.data.repo.DailyAyahWorker
import com.islamichub.app.data.repo.UpdateChecker
import com.islamichub.app.ui.navigation.IslamicHubNavGraph
import com.islamichub.app.ui.components.PremiumDialogIcon
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

        // Schedule daily ayah notification (only if the user hasn't disabled it)
        val dailyAyahEnabled = kotlinx.coroutines.runBlocking {
            (application as IslamicHubApp).container.settingsRepository.dailyAyahEnabled.first()
        }
        if (dailyAyahEnabled) {
            DailyAyahWorker.schedule(this)
        } else {
            DailyAyahWorker.cancel(this)
        }

        setContent {
            val container = (application as IslamicHubApp).container
            // Collect theme mode reactively
            val themeMode by container.settingsRepository.themeMode
                .collectAsState(initial = "auto")
            // v5.5 — per-script reading text sizes (Arabic / Bangla / English)
            val arabicScale by container.settingsRepository.arabicFontScale
                .collectAsState(initial = 1f)
            val banglaScale by container.settingsRepository.banglaFontScale
                .collectAsState(initial = 1f)
            val englishScale by container.settingsRepository.englishFontScale
                .collectAsState(initial = 1f)

            androidx.compose.runtime.CompositionLocalProvider(
                com.islamichub.app.ui.theme.LocalArabicFontScale provides arabicScale,
                com.islamichub.app.ui.theme.LocalBanglaFontScale provides banglaScale,
                com.islamichub.app.ui.theme.LocalEnglishFontScale provides englishScale
            ) {
                IslamicHubTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Check onboarding status
                    var showOnboarding by remember { mutableStateOf(false) }
                    var showAppLock by remember { mutableStateOf(false) }
                    var checked by remember { mutableStateOf(false) }
                    val context = androidx.compose.ui.platform.LocalContext.current

                    // Initial check
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        val onboardingDone = runBlocking {
                            container.settingsRepository.onboardingDone.first()
                        }
                        showOnboarding = !onboardingDone
                        // v5.9.0 — App Lock is finally wired end-to-end: if the user
                        // enabled it in Settings AND the device actually has a lock
                        // (biometric or credential), gate the whole app behind the
                        // biometric prompt. Without the capability check a user could
                        // lock themselves out of the app entirely.
                        if (!showOnboarding) {
                            val lockEnabled = runBlocking {
                                container.settingsRepository.appLockEnabled.first()
                            }
                            if (lockEnabled) {
                                val bm = androidx.biometric.BiometricManager.from(context)
                                val canAuth = bm.canAuthenticate(
                                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK or
                                        androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
                                )
                                showAppLock = canAuth == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
                            }
                        }
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
                            // v5.6.0 — silent in-app update check (once per 24h)
                            var updateAvailable by remember {
                                mutableStateOf<UpdateChecker.AppUpdate?>(null)
                            }
                            androidx.compose.runtime.LaunchedEffect(Unit) {
                                try {
                                    val last = container.settingsRepository.lastUpdateCheckMs.first()
                                    val now = System.currentTimeMillis()
                                    if (now - last > UPDATE_CHECK_INTERVAL_MS) {
                                        val version = try {
                                            packageManager.getPackageInfo(packageName, 0)
                                                .versionName ?: "0.0.0"
                                        } catch (_: Exception) { "0.0.0" }
                                        val result = container.updateChecker.check(version)
                                        container.settingsRepository.setLastUpdateCheckMs(now)
                                        if (result is UpdateChecker.UpdateResult.Success &&
                                            result.update.isNewer
                                        ) {
                                            updateAvailable = result.update
                                        }
                                    }
                                } catch (_: Exception) {
                                    // silent — update check must never disturb launch
                                }
                            }

                            IslamicHubNavGraph(
                                container = container
                            )

                            updateAvailable?.let { upd ->
                                AlertDialog(
                                    onDismissRequest = { updateAvailable = null },
                                    icon = {
                                        PremiumDialogIcon(
                                            icon = Icons.Filled.SystemUpdate,
                                            tint = Color(0xFF2E7D32)
                                        )
                                    },
                                    title = {
                                        Text(
                                            "নতুন ভার্সন পাওয়া গেছে",
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    text = {
                                        Column {
                                            Text(
                                                "Islamic Hub v${upd.latestVersion} এখন উপলব্ধ। " +
                                                    "আপনি বর্তমানে v${upd.currentVersion} ব্যবহার করছেন।"
                                            )
                                            if (upd.releaseNotes.isNotBlank()) {
                                                Text(
                                                    text = upd.releaseNotes,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 5
                                                )
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            container.updateChecker
                                                .openDownloadPage(upd.downloadUrl)
                                            updateAvailable = null
                                        }) {
                                            Text("আপডেট করুন")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { updateAvailable = null }) {
                                            Text("পরে")
                                        }
                                    }
                                )
                            }
                        }
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

    private companion object {
        /** Auto update-check at most once per 24 hours. */
        const val UPDATE_CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L
    }
}
