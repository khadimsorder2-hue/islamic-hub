package com.islamichub.app.ui.screens.more

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val items = listOf(
        MoreItem(R.string.more_prayer_times, Icons.Filled.MenuBook, Screen.Prayer.route, "prayer-premium-bg.webp", Color(0xFF7E8CE0)),
        MoreItem(R.string.more_qibla, Icons.Filled.CompassCalibration, Screen.Qibla.route, "qibla-premium-bg.webp", Color(0xFF2E7D32)),
        MoreItem(R.string.more_tasbih, Icons.Filled.Spa, Screen.Tasbih.route, "tasbih-bg.webp", Color(0xFFB36283)),
        MoreItem(R.string.more_99_names, Icons.Filled.Favorite, Screen.Names.route, "asmaul_husna_light_bg.webp", Color(0xFFE91E63)),
        MoreItem(R.string.more_duas, Icons.Filled.Book, Screen.Duas.route, "dua-premium-bg.webp", Color(0xFF6B6E91)),
        MoreItem(R.string.more_calendar, Icons.Filled.DateRange, Screen.Calendar.route, "quran-premium-bg.webp", Color(0xFF7E57C2)),
        MoreItem(R.string.more_hadith, Icons.Filled.Book, Screen.Hadith.route, "hadith-premium-bg.webp", Color(0xFF1B5E20)),
        MoreItem(R.string.more_qada, Icons.Filled.History, Screen.Qada.route, "salah-premium-bg.webp", Color(0xFFD84315)),
        MoreItem(R.string.more_tracker, Icons.Filled.MenuBook, Screen.Tracker.route, "salah-premium-bg.webp", Color(0xFF00ACC1)),
        MoreItem(R.string.more_bookmarks, Icons.Filled.Bookmark, Screen.Bookmarks.route, "quran-premium-bg.webp", Color(0xFF66BB6A)),
        MoreItem(R.string.more_khatam, Icons.Filled.MenuBook, Screen.Khatam.route, "premium-quran-bg.webp", Color(0xFF26A69A)),
        MoreItem(R.string.more_profile, Icons.Filled.Person, Screen.Profile.route, "profile-premium-bg.webp", Color(0xFF5E35B1)),
        MoreItem(R.string.more_settings, Icons.Filled.Settings, Screen.Settings.route, "topics-premium-bg.webp", Color(0xFF607D8B)),
        MoreItem(R.string.more_misconceptions, Icons.Filled.MenuBook, Screen.Misconceptions.route, "topics-premium-bg.webp", Color(0xFFEF6C00)),
        MoreItem(R.string.more_namaz_shikkha, Icons.Filled.MenuBook, Screen.NamazShikkha.route, "namaz-premium-bg.webp", Color(0xFFC9A34E)),
        MoreItem(R.string.more_namaz_extras, Icons.Filled.MenuBook, Screen.NamazExtras.route, "namaz-premium-bg.webp", Color(0xFF558B2F)),
        MoreItem(R.string.more_ai_scholar, Icons.Filled.Bolt, Screen.AiScholar.route, "voice-ai-bg.webp", Color(0xFF8E24AA)),
        MoreItem(R.string.more_tajweed, Icons.Filled.MenuBook, Screen.TajweedChecker.route, "tajweed-premium-bg.webp", Color(0xFF3949AB)),
        MoreItem(R.string.more_scanner, Icons.Filled.PhotoLibrary, Screen.Scanner.route, "topics-premium-bg.webp", Color(0xFF43A047)),
        MoreItem(R.string.more_stories, Icons.Filled.Book, Screen.Stories.route, "stories-premium-bg.webp", Color(0xFF8D6E63)),
        MoreItem(R.string.more_kalima, Icons.Filled.Book, Screen.Kalima.route, "quran-premium-bg.webp", Color(0xFF5C6BC0)),
        MoreItem(R.string.more_qa, Icons.Filled.MenuBook, Screen.Qa.route, "topics-premium-bg.webp", Color(0xFF00897B)),
        MoreItem(R.string.more_zakat, Icons.Filled.Calculate, Screen.Zakat.route, "salah-premium-bg.webp", Color(0xFF00ACC1)),
        MoreItem(R.string.more_quiz, Icons.Filled.EmojiEvents, Screen.Quiz.route, "topics-premium-bg.webp", Color(0xFFFF6B35)),
        MoreItem(R.string.more_fasting, Icons.Filled.WbSunny, Screen.Fasting.route, "salah-premium-bg.webp", Color(0xFFD84315)),
        MoreItem(R.string.more_topic_study, Icons.Filled.AccountTree, Screen.TopicStudyList.route, "premium-quran-bg.webp", Color(0xFF8E24AA)),
        MoreItem(R.string.more_hadith_topic_study, Icons.Filled.MenuBook, Screen.HadithTopicStudyList.route, "hadith-premium-bg.webp", Color(0xFF1B5E20))
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.more_title)) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Premium hero
            item {
                com.islamichub.app.ui.components.PremiumHeroCard(
                    backgroundImage = "topics-premium-bg.webp",
                    context = context,
                    height = 140
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("সব ফিচার",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${items.size}টি ফিচার উপলব্ধ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }

            items(items, key = { it.labelRes }) { item ->
                MoreCard(item, context) { onNavigate(item.route) }
            }
        }
    }
}

@Composable
private fun MoreCard(item: MoreItem, context: android.content.Context, onClick: () -> Unit) {
    val bgBitmap = remember(item.labelRes) {
        com.islamichub.app.ui.components.loadAssetImage(context, "img/${item.bgImage}")
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            if (bgBitmap != null) {
                Image(
                    bitmap = bgBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(item.color.copy(alpha = 0.6f), item.color.copy(alpha = 0.9f))
                    )
                ))
            } else {
                Box(modifier = Modifier.fillMaxSize().background(item.color))
            }
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = stringResource(item.labelRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

private data class MoreItem(
    val labelRes: Int,
    val icon: ImageVector,
    val route: String,
    val bgImage: String,
    val color: Color
)
