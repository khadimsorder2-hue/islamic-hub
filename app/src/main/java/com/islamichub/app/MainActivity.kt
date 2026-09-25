package com.islamichub.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.islamichub.app.data.repo.UpdateChecker
import com.islamichub.app.ui.navigation.IslamicHubNavGraph
import com.islamichub.app.ui.components.PremiumDialogIcon
import com.islamichub.app.ui.screens.applock.AppLockScreen
import com.islamichub.app.ui.screens.onboarding.OnboardingScreen
import com.islamichub.app.ui.theme.AppColors
import com.islamichub.app.ui.theme.IslamicHubTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )

        // v5.13.0 — 120Hz/90Hz high refresh rate opt-in. Android silently caps
        // many apps at 60Hz on high-refresh displays; requesting the device's
        // fastest supported mode unlocks the full 120fps experience.
        enableHighRefreshRate()

        // v5.13.0 — daily-ayah scheduling moved off the main thread. The old
        // runBlocking { ... .first() } read DataStore synchronously BEFORE the
        // first frame — the single biggest cold-start stall.
        (application as IslamicHubApp).scheduleDailyAyahIfEnabled(this)

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

                    // Initial check — LaunchedEffect is ALREADY a suspend
                    // context (v5.13.0: the inner runBlocking blocks were
                    // pure overhead, freezing the main thread at startup).
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        val onboardingDone = container.settingsRepository.onboardingDone.first()
                        showOnboarding = !onboardingDone
                        // v5.9.0 — App Lock is finally wired end-to-end: if the user
                        // enabled it in Settings AND the device actually has a lock
                        // (biometric or credential), gate the whole app behind the
                        // biometric prompt. Without the capability check a user could
                        // lock themselves out of the app entirely.
                        if (!showOnboarding) {
                            val lockEnabled = container.settingsRepository.appLockEnabled.first()
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
                            // v5.11.0 — was an empty composition (blank flash between
                            // the system splash and the first real frame); show a
                            // branded loading state instead.
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(AppColors.brandPrimary, AppColors.brandPrimaryDark)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Mosque,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Islamic Hub",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(24.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
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

    // v5.13.0 — re-apply on resume (some OEM skins reset the display mode
    // when the activity is backgrounded and restored).
    override fun onResume() {
        super.onResume()
        enableHighRefreshRate()
    }

    /**
     * v5.13.0 — request the highest refresh rate the panel supports at the
     * current resolution. Uses preferredDisplayModeId on R+ (strongest,
     * mode-locked signal) and preferredRefreshRate as the pre-R fallback.
     * Wrapped defensively — a display quirk must never crash launch.
     */
    private fun enableHighRefreshRate() {
        try {
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                this.display ?: windowManager.defaultDisplay
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            } ?: return
            val current = display.mode
            val best = display.supportedModes
                .filter {
                    it.physicalWidth == current.physicalWidth &&
                        it.physicalHeight == current.physicalHeight
                }
                .maxByOrNull { it.refreshRate } ?: return
            if (best.refreshRate <= current.refreshRate + 0.1f) return
            val attrs = window.attributes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                attrs.preferredDisplayModeId = best.modeId
            } else {
                attrs.preferredRefreshRate = best.refreshRate
            }
            window.attributes = attrs
        } catch (_: Exception) {
            // Display quirks on exotic OEM skins — never worth crashing for.
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
