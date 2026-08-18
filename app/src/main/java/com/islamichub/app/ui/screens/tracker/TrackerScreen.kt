package com.islamichub.app.ui.screens.tracker

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { TrackerViewModel(container) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    val prayerBn = listOf("ফজর", "যোহর", "আসর", "মাগরিব", "এশা")
    val prayerEmojis = listOf("🌅", "☀️", "🌤️", "🌆", "🌙")
    val doneFlags = listOf(
        state.today.fajrDone,
        state.today.dhuhrDone,
        state.today.asrDone,
        state.today.maghribDone,
        state.today.ishaDone
    )
    val doneCount = doneFlags.count { it }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.tracker_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Premium Streak Hero Card ───
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
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFF6B35),
                                        Color(0xFFFF8C42)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = null,
                                        tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                                Spacer(Modifier.size(16.dp))
                                Column {
                                    Text("নামাজ স্ট্রিক",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.9f))
                                    Text("${state.streak} দিন",
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("আজ",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.9f))
                                Text("$doneCount/5",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White)
                            }
                        }
                    }
                }
            }

            // ─── Today's Prayer Progress Card ───
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text("  আজকের নামাজ প্রগ্রেস",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Progress bar
                        LinearProgressIndicator(
                            progress = { doneCount / 5f },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Text("${doneCount} / ৫ ওয়াক্ত সম্পন্ন",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface)
                        if (doneCount == 5) {
                            Text("🎉 মাশাআল্লাহ! আজকের সব নামাজ সম্পন্ন",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ─── 5 Prayer Check Cards (one per row) ───
            items(prayers.size) { idx ->
                PremiumPrayerCheckCard(
                    prayerEn = prayers[idx],
                    prayerBn = prayerBn[idx],
                    emoji = prayerEmojis[idx],
                    isDone = doneFlags[idx],
                    onToggle = { vm.togglePrayer(prayers[idx]) }
                )
            }

            // ─── Stats Section Header ───
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Text("  পরিসংখ্যান",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
            }

            // ─── 2-column stats grid ───
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatGridCard(
                        icon = Icons.Filled.Spa,
                        value = state.totalZikr.toString(),
                        label = "মোট তসবিহ",
                        color = Color(0xFFB36283),
                        modifier = Modifier.weight(1f)
                    )
                    StatGridCard(
                        icon = Icons.Filled.AutoStories,
                        value = state.totalAyahs.toString(),
                        label = "আয়াত পঠিত",
                        color = Color(0xFF6D45C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatGridCard(
                        icon = Icons.Filled.MenuBook,
                        value = state.totalHadiths.toString(),
                        label = "হাদিস পঠিত",
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.weight(1f)
                    )
                    StatGridCard(
                        icon = Icons.Filled.Bolt,
                        value = "${(doneCount * 100) / 5}%",
                        label = "আজকের প্রগ্রেস",
                        color = Color(0xFFFF6B35),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.StatGridCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                            color.copy(alpha = 0.15f),
                            color.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
                Text(value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color)
                Text(label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun PremiumPrayerCheckCard(
    prayerEn: String,
    prayerBn: String,
    emoji: String,
    isDone: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDone) 2.dp else 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDone)
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                        )
                    else
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Emoji avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDone) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Text(emoji, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = prayerBn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDone) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = prayerEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Done / Undone status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isDone) "✓ সম্পন্ন" else "ট্যাপ করুন",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDone) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
