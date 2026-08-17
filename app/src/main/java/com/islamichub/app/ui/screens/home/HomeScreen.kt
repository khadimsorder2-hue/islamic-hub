package com.islamichub.app.ui.screens.home

import android.graphics.BitmapFactory
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.components.PremiumCard
import com.islamichub.app.ui.components.PremiumHeroCard
import com.islamichub.app.ui.components.PremiumIconBadge
import com.islamichub.app.ui.components.PremiumSectionHeader
import com.islamichub.app.ui.components.loadAssetImage
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
        // ─── Premium Hero (splash-like) ───
        item {
            val heroBitmap = remember { loadAssetImage(context, "img/hero-premium-masjid.webp") }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(32.dp))
            ) {
                if (heroBitmap != null) {
                    Image(
                        bitmap = heroBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Multi-layer gradient
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        )
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(28.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top: greeting + hijri
                    Column {
                        Text(
                            text = "আসসালামু আলাইকুম",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = state.hijriDate.ifBlank { "—" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // Middle: next prayer card (glassmorphism)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("পরবর্তী নামাজ",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.85f))
                            Text(state.nextPrayerName.ifBlank { "—" },
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold, color = Color.White)
                            Text(state.nextPrayerTime.ifBlank { "--:--" },
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.9f))
                        }
                        // Countdown badge (glassmorphism)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalFireDepartment,
                                    contentDescription = null, tint = Color.White,
                                    modifier = Modifier.size(16.dp))
                                Text(" বাকি", style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f))
                            }
                            Text(state.timeRemaining.ifBlank { "--" },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // ─── Quick Access (4 cards) ───
        item { PremiumSectionHeader(title = "দ্রুত অ্যাক্সেস") }
        item {
            val features = listOf(
                GridFeature("আল-কুরআন", "১১৪ সূরা", Icons.Filled.AutoStories, Screen.Quran.route, "quran-premium-bg.webp", Color(0xFF6D45C7)),
                GridFeature("নামাজ শিক্ষা", "সম্পূর্ণ", Icons.Filled.MenuBook, Screen.NamazShikkha.route, "namaz-premium-bg.webp", Color(0xFFC9A34E)),
                GridFeature("নামাজের সময়", "৫ ওয়াক্ত", Icons.Filled.CalendarMonth, Screen.Prayer.route, "prayer-premium-bg.webp", Color(0xFF7E8CE0)),
                GridFeature("কিবলা", "কম্পাস", Icons.Filled.CompassCalibration, Screen.Qibla.route, "qibla-premium-bg.webp", Color(0xFF2E7D32))
            )
            features.chunked(2).forEach { rowFeatures ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowFeatures.forEach { feature ->
                        Box(modifier = Modifier.weight(1f)) {
                            PremiumCard(backgroundImage = feature.bgImage, context = context,
                                onClick = { onNavigate(feature.route) }, height = 130,
                                overlayColor = feature.color) {
                                PremiumCardContent(feature)
                            }
                        }
                    }
                    if (rowFeatures.size == 1) { Box(modifier = Modifier.weight(1f)) {} }
                }
            }
        }

        // ─── Knowledge & Research ───
        item { PremiumSectionHeader(title = "জ্ঞান ও গবেষণা") }
        item {
            val features = listOf(
                GridFeature("হাদিস", "২৪,৪২৪টি", Icons.Filled.Book, Screen.Hadith.route, "hadith-premium-bg.webp", Color(0xFF1B5E20)),
                GridFeature("হাদিস বিষয়াদি", "৩১ বিষয়", Icons.Filled.Book, Screen.HadithTopics.route, "hadith-premium-bg.webp", Color(0xFF0F766E)),
                GridFeature("AI স্কলার", "জিজ্ঞাসা করুন", Icons.Filled.Bolt, Screen.AiScholar.route, "voice-ai-bg.webp", Color(0xFF8E24AA)),
                GridFeature("ভুল বোঝাবুঝি", "১৯২+", Icons.Filled.Warning, Screen.Misconceptions.route, null, Color(0xFFEF6C00)),
                GridFeature("প্রশ্ন-উত্তর", "৮৮৭ টি", Icons.Filled.QuestionAnswer, Screen.Qa.route, null, Color(0xFF00897B)),
                GridFeature("তাজবীদ", "চেকার", Icons.Filled.AutoStories, Screen.TajweedChecker.route, "tajweed-premium-bg.webp", Color(0xFF3949AB))
            )
            features.chunked(2).forEach { rowFeatures ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowFeatures.forEach { feature ->
                        Box(modifier = Modifier.weight(1f)) {
                            PremiumCard(backgroundImage = feature.bgImage, context = context,
                                onClick = { onNavigate(feature.route) }, height = 130,
                                overlayColor = feature.color) {
                                PremiumCardContent(feature)
                            }
                        }
                    }
                    if (rowFeatures.size == 1) { Box(modifier = Modifier.weight(1f)) {} }
                }
            }
        }

        // ─── Tools ───
        item { PremiumSectionHeader(title = "টুলস") }
        item {
            val features = listOf(
                GridFeature("তসবিহ", "কাউন্টার", Icons.Filled.Spa, Screen.Tasbih.route, "tasbih-bg.webp", Color(0xFFB36283)),
                GridFeature("৯৯ নাম", "আসমাউল হুসনা", Icons.Filled.Favorite, Screen.Names.route, "asmaul_husna_light_bg.webp", Color(0xFFE91E63)),
                GridFeature("দোয়া", "২৮ টি", Icons.Filled.Bedtime, Screen.Duas.route, "dua-premium-bg.webp", Color(0xFF6B6E91)),
                GridFeature("AI স্ক্যানার", "ছবি বিশ্লেষণ", Icons.Filled.CameraAlt, Screen.Scanner.route, null, Color(0xFF43A047))
            )
            features.chunked(2).forEach { rowFeatures ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowFeatures.forEach { feature ->
                        Box(modifier = Modifier.weight(1f)) {
                            PremiumCard(backgroundImage = feature.bgImage, context = context,
                                onClick = { onNavigate(feature.route) }, height = 130,
                                overlayColor = feature.color) {
                                PremiumCardContent(feature)
                            }
                        }
                    }
                    if (rowFeatures.size == 1) { Box(modifier = Modifier.weight(1f)) {} }
                }
            }
        }

        // ─── Tracker ───
        item { PremiumSectionHeader(title = "ট্র্যাকার") }
        item {
            val features = listOf(
                GridFeature("কাযা", "ট্র্যাকার", Icons.Filled.History, Screen.Qada.route, "salah-premium-bg.webp", Color(0xFFD84315)),
                GridFeature("ট্র্যাকার", "দৈনিক", Icons.Filled.Dashboard, Screen.Tracker.route, "salah-premium-bg.webp", Color(0xFF00ACC1)),
                GridFeature("বুকমার্ক", "সংরক্ষিত", Icons.Filled.Bookmark, Screen.Bookmarks.route, null, Color(0xFF66BB6A)),
                GridFeature("খতম", "কুরআন", Icons.Filled.MenuBook, Screen.Khatam.route, "premium-quran-bg.webp", Color(0xFF26A69A))
            )
            features.chunked(2).forEach { rowFeatures ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowFeatures.forEach { feature ->
                        Box(modifier = Modifier.weight(1f)) {
                            PremiumCard(backgroundImage = feature.bgImage, context = context,
                                onClick = { onNavigate(feature.route) }, height = 130,
                                overlayColor = feature.color) {
                                PremiumCardContent(feature)
                            }
                        }
                    }
                    if (rowFeatures.size == 1) { Box(modifier = Modifier.weight(1f)) {} }
                }
            }
        }

        // ─── More ───
        item { PremiumSectionHeader(title = "আরও") }
        item {
            val features = listOf(
                GridFeature("গল্প", "৯ নবী + ৪ খলিফা", Icons.Filled.Book, Screen.Stories.route, "stories-premium-bg.webp", Color(0xFF8D6E63)),
                GridFeature("৬ কালিমা", "কালিমা", Icons.Filled.MenuBook, Screen.Kalima.route, null, Color(0xFF5C6BC0)),
                GridFeature("ক্যালেন্ডার", "হিজরি", Icons.Filled.CalendarMonth, Screen.Calendar.route, "quran-premium-bg.webp", Color(0xFF7E57C2)),
                GridFeature("অতিরিক্ত নামাজ", "জুমআ, ঈদ", Icons.Filled.MenuBook, Screen.NamazExtras.route, "namaz-premium-bg.webp", Color(0xFF558B2F)),
                GridFeature("আরও", "সব দেখুন", Icons.Filled.Dashboard, Screen.More.route, "topics-premium-bg.webp", Color(0xFF455A64)),
                GridFeature("প্রোফাইল", "আপনার", Icons.Filled.Person, Screen.Profile.route, "profile-premium-bg.webp", Color(0xFF5E35B1)),
                GridFeature("সেটিংস", "কনফিগ", Icons.Filled.Dashboard, Screen.Settings.route, null, Color(0xFF607D8B))
            )
            features.chunked(2).forEach { rowFeatures ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowFeatures.forEach { feature ->
                        Box(modifier = Modifier.weight(1f)) {
                            PremiumCard(backgroundImage = feature.bgImage, context = context,
                                onClick = { onNavigate(feature.route) }, height = 130,
                                overlayColor = feature.color) {
                                PremiumCardContent(feature)
                            }
                        }
                    }
                    if (rowFeatures.size == 1) { Box(modifier = Modifier.weight(1f)) {} }
                }
            }
        }

        // ─── Ayah of the day ───
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoStories, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                        Text("  আজকের আয়াত",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                    }
                    state.ayahOfDay?.arabic?.let { arabic ->
                        Text(arabic, style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.fillMaxWidth())
                    }
                    state.ayahOfDay?.bengali?.let { bn ->
                        Text(bn, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f))
                    }
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.PremiumCardContent(feature: GridFeature) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        PremiumIconBadge(icon = feature.icon, size = 40)
        Column {
            Text(feature.title, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = Color.White)
            Text(feature.subtitle, style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f))
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
