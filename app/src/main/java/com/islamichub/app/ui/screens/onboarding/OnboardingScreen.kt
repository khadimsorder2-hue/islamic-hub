package com.islamichub.app.ui.screens.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.components.PremiumHeroCard
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    container: AppContainer,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(0) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    val locationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    val steps = listOf(
        OnboardingStep(
            icon = Icons.Filled.AutoStories,
            title = "আসসালামু আলাইকুম",
            subtitle = "Islamic Hub এ স্বাগতম",
            description = "কুরআন, হাদিস, নামাজ, কিবলা, তসবিহ এবং আরও অনেক কিছু এক অ্যাপে।",
            bgImage = "hero-premium-masjid.webp"
        ),
        OnboardingStep(
            icon = Icons.Filled.CalendarMonth,
            title = "নামাজের সময়",
            subtitle = "৫ ওয়াক্ত নামাজের সময়",
            description = "আপনার অবস্থান অনুযায়ী নামাজের সময় এবং নোটিফিকেশন পান। লোকেশন অনুমতি দিন।",
            bgImage = "prayer-premium-bg.webp"
        ),
        OnboardingStep(
            icon = Icons.Filled.Notifications,
            title = "নোটিফিকেশন",
            subtitle = "নামাজের সময় এবং দৈনিক আয়াত",
            description = "নামাজের সময় এবং প্রতিদিন একটি কুরআনের আয়াত নোটিফিকেশন পান। নোটিফিকেশন অনুমতি দিন।",
            bgImage = "topics-premium-bg.webp"
        ),
        OnboardingStep(
            icon = Icons.Filled.Bolt,
            title = "AI স্কলার",
            subtitle = "ইসলামিক প্রশ্ন জিজ্ঞাসা করুন",
            description = "AI স্কলার দিয়ে যেকোনো ইসলামিক প্রশ্ন করুন। Settings এ গিয়ে API key যোগ করুন। শুরু করতে প্রস্তুত?",
            bgImage = "voice-ai-bg.webp"
        )
    )

    val step = steps[currentStep]

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        val bgBitmap = remember(step.bgImage) {
            if (step.bgImage != null) com.islamichub.app.ui.components.loadAssetImage(context, "img/${step.bgImage}")
            else null
        }
        if (bgBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bgBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = step.subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }

            // Middle: description
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Bottom: buttons + progress dots
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    steps.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentStep) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentStep) Color.White
                                    else Color.White.copy(alpha = 0.4f)
                                )
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStep > 0) {
                        TextButton(onClick = { currentStep-- }) {
                            Text("পেছনে", color = Color.White)
                        }
                    } else {
                        Box {}
                    }

                    Button(
                        onClick = {
                            when (currentStep) {
                                1 -> {
                                    // Request location permission
                                    locationPermLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                                2 -> {
                                    // Request notification permission
                                    notifPermLauncher.launch(
                                        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                                    )
                                }
                                3 -> {
                                    // Complete onboarding
                                    scope.launch {
                                        container.settingsRepository.setOnboardingDone(true)
                                        onComplete()
                                    }
                                    return@Button
                                }
                            }
                            if (currentStep < steps.size - 1) {
                                currentStep++
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (currentStep == steps.size - 1) "শুরু করুন" else "পরবর্তী"
                        )
                    }
                }
            }
        }
    }
}

private data class OnboardingStep(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
    val description: String,
    val bgImage: String?
)
