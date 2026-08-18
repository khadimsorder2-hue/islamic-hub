package com.islamichub.app.ui.screens.khatam

import android.content.Intent
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.components.PremiumHeroCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhatamScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onSurahClick: (Int) -> Unit
) {
    val khatam by remember { container.khatamRepository.currentKhatam }.collectAsState(initial = null)
    val progressPercent by remember { container.khatamRepository.progressPercent }.collectAsState(initial = 0f)
    val completedCount by remember { container.khatamRepository.completedSurahCount }.collectAsState(initial = 0)
    val context = LocalContext.current
    val scope = remember { kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main) }

    val khatamShareText = stringResource(R.string.khatam_share_text)
    val khatamTitle = stringResource(R.string.khatam_title)
    val khatamStart = stringResource(R.string.khatam_start)
    val khatamShare = stringResource(R.string.khatam_share)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(khatamTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (khatam != null) {
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT,
                                    khatamShareText + "\n\n" +
                                    "Progress: ${"%.1f".format(progressPercent * 100)}% ($completedCount/114 surahs)"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Khatam"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share")
                        }
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
            // ─── Premium Hero Progress Card ───
            item {
                PremiumHeroCard(
                    backgroundImage = "premium-quran-bg.webp",
                    context = context,
                    height = 260
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top: icon + title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoStories,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "কুরআন খতম",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Middle: percentage
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${"%.1f".format(progressPercent * 100)}%",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "সম্পন্ন",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        // Bottom: stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            KhatamStatItem(
                                value = "$completedCount",
                                label = "সূরা সম্পন্ন",
                                total = "/ 114"
                            )
                            KhatamStatItem(
                                value = "${(progressPercent * 6236).toInt()}",
                                label = "আয়াত পড়া",
                                total = "/ 6236"
                            )
                        }

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // ─── Start / Reset / Khatam Player ───
            if (khatam == null) {
                item {
                    Button(
                        onClick = {
                            scope.launch { container.khatamRepository.startNew() }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.AutoStories, contentDescription = null)
                        Text("  $khatamStart", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                item {
                    Text(
                        text = "নতুন খতম শুরু করে আপনার কুরআন পড়ার অগ্রগতি ট্র্যাক করুন। সূরা একের পর এক পড়ুন এবং ১১৪ সূরা সম্পন্ন করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                // Khatam Player button — also available before khatam starts (auto-creates a khatam)
                item {
                    OutlinedButton(
                        onClick = {
                            // Initialize khatam if not exists, then start player
                            scope.launch {
                                if (khatam == null) {
                                    container.khatamRepository.startNew()
                                }
                                container.audioController.startKhatamPlayer(1)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Text("  খতম প্লেয়ার শুরু করুন (অটো-ইনিশিয়ালাইজ)",
                            modifier = Modifier.padding(start = 8.dp))
                    }
                }
            } else {
                // Khatam Player button
                item {
                    Button(
                        onClick = {
                            container.audioController.startKhatamPlayer(1)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Text("  খতম প্লেয়ার শুরু করুন", modifier = Modifier.padding(start = 8.dp))
                    }
                }

                // Reset button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch { container.khatamRepository.reset() }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("রিসেট") }
                        if (khatam!!.isComplete) {
                            Button(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, khatamShareText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Khatam"))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null)
                                Text("  $khatamShare", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }

                // ─── Para-wise surah list (3-column grid, no image) ───
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text("  পারা অনুযায়ী তালিকা",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                    }
                }

                // Group surahs by para (1-30)
                val surahsByPara = remember { ParaSurahMap.surahsByPara() }
                val paraColors = listOf(
                    Color(0xFF6D45C7), Color(0xFF1B5E20), Color(0xFFC9A34E),
                    Color(0xFF1565C0), Color(0xFFD84315), Color(0xFF00897B),
                    Color(0xFF8D6E63), Color(0xFFEF6C00)
                )

                surahsByPara.forEach { (paraNum, surahsInPara) ->
                    // Para header card
                    item {
                        val paraCompletedSurahs = surahsInPara.count { khatam?.completedSurahs?.contains(it) == true }
                        val paraProgress = if (surahsInPara.isNotEmpty()) paraCompletedSurahs.toFloat() / surahsInPara.size else 0f
                        val paraColor = paraColors[(paraNum - 1) % paraColors.size]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(paraColor, paraColor.copy(alpha = 0.7f))
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("পারা $paraNum",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White)
                                        }
                                        Spacer(Modifier.size(12.dp))
                                        Column {
                                            Text(
                                                if (paraProgress >= 1f) "✓ সম্পূর্ণ" else "${surahsInPara.size} সূরা",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text("$paraCompletedSurahs / ${surahsInPara.size} সম্পন্ন",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.9f))
                                        }
                                    }
                                    // Progress percentage badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White.copy(alpha = 0.25f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("${(paraProgress * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // 3-column surah grid for this para
                    val chunkedSurahs = surahsInPara.chunked(3)
                    chunkedSurahs.forEach { rowSurahs ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowSurahs.forEach { surahNum ->
                                    val isCompleted = khatam?.completedSurahs?.contains(surahNum) == true
                                    KhatamSurahGridCard(
                                        surahNumber = surahNum,
                                        isCompleted = isCompleted,
                                        paraColor = paraColors[(paraNum - 1) % paraColors.size],
                                        modifier = Modifier.weight(1f),
                                        onClick = { onSurahClick(surahNum) }
                                    )
                                }
                                repeat(3 - rowSurahs.size) {
                                    Box(modifier = Modifier.weight(1f)) {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KhatamStatItem(
    value: String,
    label: String,
    total: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = total,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.KhatamSurahGridCard(
    surahNumber: Int,
    isCompleted: Boolean,
    paraColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 2.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = if (isCompleted)
                            listOf(paraColor.copy(alpha = 0.25f), paraColor.copy(alpha = 0.1f))
                        else
                            listOf(paraColor.copy(alpha = 0.1f), paraColor.copy(alpha = 0.03f))
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Number badge (circular)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) paraColor else paraColor.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCompleted) "✓" else surahNumber.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "সূরা $surahNumber",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCompleted) paraColor
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
