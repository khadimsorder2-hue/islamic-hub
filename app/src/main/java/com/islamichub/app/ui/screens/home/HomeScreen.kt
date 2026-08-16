package com.islamichub.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.navigation.Screen

@Composable
fun HomeScreen(
    container: AppContainer,
    onNavigate: (String) -> Unit
) {
    val vm = remember { HomeViewModel(container) }
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero with background image
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(28.dp))
            ) {
                // Background image
                try {
                    val assetManager = context.assets
                    val inputStream = assetManager.open("img/hero-premium-masjid.webp")
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } catch (_: Exception) {
                    // Fallback gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )
                }

                // Dark overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.home_greeting),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = state.hijriDate.ifBlank { "—" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.home_next_prayer),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = state.nextPrayerName.ifBlank { "—" },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = state.nextPrayerTime.ifBlank { "--:--" },
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.home_time_remaining),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Text(
                                text = state.timeRemaining.ifBlank { "--" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Premium 2-column grid of all features
        item {
            Text(
                text = "সব ফিচার",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        item {
            val features = listOf(
                GridFeature("আল-কুরআন", "১১৪ সূরা", Icons.Filled.AutoStories, Screen.Quran.route, "quran-premium-bg.webp", Color(0xFF6D45C7)),
                GridFeature("হাদিস", "২৪,৪২৪টি", Icons.Filled.Book, Screen.Hadith.route, "hadith-premium-bg.webp", Color(0xFF1B5E20)),
                GridFeature("নামাজ শিক্ষা", "সম্পূর্ণ", Icons.Filled.MenuBook, Screen.NamazShikkha.route, "namaz-premium-bg.webp", Color(0xFFC9A34E)),
                GridFeature("নামাজের সময়", "৫ ওয়াক্ত", Icons.Filled.CalendarMonth, Screen.Prayer.route, "prayer-premium-bg.webp", Color(0xFF7E8CE0)),
                GridFeature("কিবলা", "কম্পাস", Icons.Filled.CompassCalibration, Screen.Qibla.route, "qibla-premium-bg.webp", Color(0xFF2E7D32)),
                GridFeature("তসবিহ", "কাউন্টার", Icons.Filled.Spa, Screen.Tasbih.route, "tasbih-bg.webp", Color(0xFFB36283)),
                GridFeature("৯৯ নাম", "আসমাউল হুসনা", Icons.Filled.Favorite, Screen.Names.route, "asmaul_husna_light_bg.webp", Color(0xFFE91E63)),
                GridFeature("দোয়া", "প্রতিদিনের", Icons.Filled.Bedtime, Screen.Duas.route, "dua-premium-bg.webp", Color(0xFF6B6E91)),
                GridFeature("ভুল বোঝাবুঝি", "৩০০+", Icons.Filled.Warning, Screen.Misconceptions.route, null, Color(0xFFEF6C00)),
                GridFeature("প্রশ্ন-উত্তর", "Q&A", Icons.Filled.QuestionAnswer, Screen.Qa.route, null, Color(0xFF00897B)),
                GridFeature("AI স্কলার", "জিজ্ঞাসা করুন", Icons.Filled.Bolt, Screen.AiScholar.route, "voice-ai-bg.webp", Color(0xFF8E24AA)),
                GridFeature("তাজবীদ", "চেকার", Icons.Filled.AutoStories, Screen.TajweedChecker.route, "tajweed-premium-bg.webp", Color(0xFF3949AB)),
                GridFeature("AI স্ক্যানার", "ছবি বিশ্লেষণ", Icons.Filled.CameraAlt, Screen.Scanner.route, null, Color(0xFF43A047)),
                GridFeature("গল্প", "নবী ও খলিফা", Icons.Filled.Book, Screen.Stories.route, "stories-premium-bg.webp", Color(0xFF8D6E63)),
                GridFeature("৬ কালিমা", "কালিমা", Icons.Filled.MenuBook, Screen.Kalima.route, null, Color(0xFF5C6BC0)),
                GridFeature("কাযা", "ট্র্যাকার", Icons.Filled.History, Screen.Qada.route, null, Color(0xFFD84315)),
                GridFeature("ট্র্যাকার", "দৈনিক", Icons.Filled.Dashboard, Screen.Tracker.route, "salah-premium-bg.webp", Color(0xFF00ACC1)),
                GridFeature("বুকমার্ক", "সংরক্ষিত", Icons.Filled.Bookmark, Screen.Bookmarks.route, null, Color(0xFF66BB6A)),
                GridFeature("খতম", "কুরআন", Icons.Filled.MenuBook, Screen.Khatam.route, "premium-quran-bg.webp", Color(0xFF26A69A)),
                GridFeature("ক্যালেন্ডার", "হিজরি", Icons.Filled.CalendarMonth, Screen.Calendar.route, null, Color(0xFF7E57C2)),
                GridFeature("অতিরিক্ত নামাজ", "জুমআ, ঈদ", Icons.Filled.MenuBook, Screen.NamazExtras.route, "namaz-premium-bg.webp", Color(0xFF558B2F)),
                GridFeature("আরও", "সব দেখুন", Icons.Filled.Dashboard, Screen.More.route, "topics-premium-bg.webp", Color(0xFF455A64)),
                GridFeature("প্রোফাইল", "আপনার", Icons.Filled.Person, Screen.Profile.route, "profile-premium-bg.webp", Color(0xFF5E35B1)),
                GridFeature("সেটিংস", "কনফিগ", Icons.Filled.Dashboard, Screen.Settings.route, null, Color(0xFF607D8B))
            )
            // Render grid as rows of 2
            val rows = features.chunked(2)
            rows.forEach { rowFeatures ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowFeatures.forEach { feature ->
                        Box(modifier = Modifier.weight(1f)) {
                            PremiumFeatureCard(feature, context) { onNavigate(feature.route) }
                        }
                    }
                    if (rowFeatures.size == 1) {
                        Box(modifier = Modifier.weight(1f)) {}
                    }
                }
            }
        }

        // Ayah of the day
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_ayah_of_day),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    state.ayahOfDay?.arabic?.let { arabic ->
                        Text(
                            text = arabic,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    state.ayahOfDay?.bengali?.let { bn ->
                        Text(
                            text = bn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumFeatureCard(
    feature: GridFeature,
    context: android.content.Context,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            // Background image (if available)
            if (feature.bgImage != null) {
                try {
                    val assetManager = context.assets
                    val inputStream = assetManager.open("img/${feature.bgImage}")
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Dark overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        feature.color.copy(alpha = 0.3f),
                                        feature.color.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )
                } catch (_: Exception) {
                    // Fallback to solid gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        feature.color,
                                        feature.color.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )
                }
            } else {
                // Solid gradient background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    feature.color,
                                    feature.color.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon in circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Text
                Column {
                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = feature.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

private data class GridFeature(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
    val bgImage: String?,
    val color: Color
)
