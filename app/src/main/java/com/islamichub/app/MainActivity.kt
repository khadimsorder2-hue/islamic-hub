package com.islamichub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.islamichub.app.ui.navigation.IslamicHubNavGraph
import com.islamichub.app.ui.theme.IslamicHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.Transparent, Color.Transparent),
            navigationBarStyle = SystemBarStyle.auto(Color.Transparent, Color.Transparent)
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
}
