package com.islamichub.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamichub.app.R
import com.islamichub.app.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val items = listOf(
        MoreItem(R.string.more_prayer_times, Icons.Filled.MenuBook, Screen.Prayer.route, Color(0xFF7E8CE0)),
        MoreItem(R.string.more_qibla, Icons.Filled.CompassCalibration, Screen.Qibla.route, Color(0xFF2E7D32)),
        MoreItem(R.string.more_tasbih, Icons.Filled.Spa, Screen.Tasbih.route, Color(0xFFB36283)),
        MoreItem(R.string.more_99_names, Icons.Filled.Favorite, Screen.Names.route, Color(0xFFE91E63)),
        MoreItem(R.string.more_duas, Icons.Filled.Book, Screen.Duas.route, Color(0xFF6B6E91)),
        MoreItem(R.string.more_calendar, Icons.Filled.DateRange, Screen.Calendar.route, Color(0xFF7E57C2)),
        MoreItem(R.string.more_hadith, Icons.Filled.Book, Screen.Hadith.route, Color(0xFF1B5E20)),
        MoreItem(R.string.more_qada, Icons.Filled.History, Screen.Qada.route, Color(0xFFD84315)),
        MoreItem(R.string.more_tracker, Icons.Filled.MenuBook, Screen.Tracker.route, Color(0xFF00ACC1)),
        MoreItem(R.string.more_bookmarks, Icons.Filled.Bookmark, Screen.Bookmarks.route, Color(0xFF66BB6A)),
        MoreItem(R.string.more_khatam, Icons.Filled.MenuBook, Screen.Khatam.route, Color(0xFF26A69A)),
        MoreItem(R.string.more_profile, Icons.Filled.Person, Screen.Profile.route, Color(0xFF5E35B1)),
        MoreItem(R.string.more_settings, Icons.Filled.Settings, Screen.Settings.route, Color(0xFF607D8B)),
        MoreItem(R.string.more_misconceptions, Icons.Filled.MenuBook, Screen.Misconceptions.route, Color(0xFFEF6C00)),
        MoreItem(R.string.more_namaz_shikkha, Icons.Filled.MenuBook, Screen.NamazShikkha.route, Color(0xFFC9A34E)),
        MoreItem(R.string.more_namaz_extras, Icons.Filled.MenuBook, Screen.NamazExtras.route, Color(0xFF558B2F)),
        MoreItem(R.string.more_ai_scholar, Icons.Filled.Bolt, Screen.AiScholar.route, Color(0xFF8E24AA)),
        MoreItem(R.string.more_tajweed, Icons.Filled.MenuBook, Screen.TajweedChecker.route, Color(0xFF3949AB)),
        MoreItem(R.string.more_scanner, Icons.Filled.PhotoLibrary, Screen.Scanner.route, Color(0xFF43A047)),
        MoreItem(R.string.more_stories, Icons.Filled.Book, Screen.Stories.route, Color(0xFF8D6E63)),
        MoreItem(R.string.more_kalima, Icons.Filled.Book, Screen.Kalima.route, Color(0xFF5C6BC0)),
        MoreItem(R.string.more_qa, Icons.Filled.MenuBook, Screen.Qa.route, Color(0xFF00897B)),
        MoreItem(R.string.more_zakat, Icons.Filled.Calculate, Screen.Zakat.route, Color(0xFF00ACC1)),
        MoreItem(R.string.more_quiz, Icons.Filled.EmojiEvents, Screen.Quiz.route, Color(0xFFFF6B35)),
        MoreItem(R.string.more_fasting, Icons.Filled.WbSunny, Screen.Fasting.route, Color(0xFFD84315)),
        MoreItem(R.string.more_topic_study, Icons.Filled.AccountTree, Screen.TopicStudyList.route, Color(0xFF8E24AA)),
        MoreItem(R.string.more_hadith_topic_study, Icons.Filled.MenuBook, Screen.HadithTopicStudyList.route, Color(0xFF1B5E20))
    )

    // Group items into rows of 3 for the grid
    val rows = items.chunked(3)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.more_title),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Premium hero (kept small, no image)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            Text("সব ফিচার",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${items.size}টি ফিচার উপলব্ধ",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }
            }

            // 3-column grid rows
            rows.forEach { rowItems ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { item ->
                            MoreGridCard(
                                item = item,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate(item.route) }
                            )
                        }
                        // No placeholder — items flow naturally without empty boxes
                        if (rowItems.size < 3) {
                            // Use Spacer instead of visible Box to avoid layout artifacts
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(3 - rowItems.size.toFloat().toInt().coerceAtLeast(1)))
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.MoreGridCard(
    item: MoreItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            item.color.copy(alpha = 0.18f),
                            item.color.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icon avatar (large, gradient circle)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(item.color, item.color.copy(alpha = 0.7f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                // Label
                Text(
                    text = stringResource(item.labelRes),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    minLines = 2
                )
            }
        }
    }
}

private data class MoreItem(
    val labelRes: Int,
    val icon: ImageVector,
    val route: String,
    val color: Color
)
