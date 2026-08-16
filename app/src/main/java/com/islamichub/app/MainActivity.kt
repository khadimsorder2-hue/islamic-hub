package com.islamichub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.islamichub.app.ui.navigation.IslamicHubNavGraph
import com.islamichub.app.ui.theme.IslamicHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        setContent {
            IslamicHubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    IslamicHubNavGraph(
                        container = (application as IslamicHubApp).container
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release Media3 player to free audio focus
        try {
            (application as IslamicHubApp).container.audioController.release()
        } catch (_: Exception) {
            // Best-effort — never crash during teardown
        }
    }
}
