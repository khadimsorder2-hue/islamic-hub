package com.islamichub.app.ui.screens.quran

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.RevelationType
import com.islamichub.app.data.repo.SurahSummary
import com.islamichub.app.ui.theme.AppColors
import com.islamichub.app.ui.theme.AppRadius
import com.islamichub.app.ui.theme.AppSpacing
import com.islamichub.app.ui.theme.AppElevation

@Composable
fun QuranListScreen(
    container: AppContainer,
    onSurahClick: (Int) -> Unit,
    onSearchClick: () -> Unit = {}
) {
    val vm = remember { QuranListViewModel(container) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ─── Premium Hero Header ───
        com.islamichub.app.ui.components.PremiumHeroCard(
            backgroundImage = "quran-premium-bg.webp",
            context = context,
            height = 180
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.xl),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "القرآن الكريم",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White
                )
                Spacer(Modifier.height(AppSpacing.xs))
                Text(
                    text = stringResource(R.string.quran_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(Modifier.height(AppSpacing.xs))
                Text(
                    text = "১১৪ সূরা • ৬২৩৬ আয়াত • ৪ বাংলা অনুবাদ • তাফসীর",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(AppSpacing.sm))
                TextButton(onClick = onSearchClick) {
                    Text("🔍 সব আয়াতে সার্চ করুন", color = Color.White)
                }
            }
        }

        // ─── Search Bar ───
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::onQueryChange,
            placeholder = { Text(stringResource(R.string.quran_search)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(AppRadius.pill),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm)
        )

        // ─── Surah List with Premium Cards ───
        LazyColumn(
            contentPadding = PaddingValues(
                start = AppSpacing.lg,
                end = AppSpacing.lg,
                bottom = AppSpacing.xxxl
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            items(state.surahs, key = { it.number }) { surah ->
                PremiumSurahCard(
                    surah = surah,
                    progress = state.progressMap[surah.number] ?: 0f,
                    onPlay = { vm.playSurah(surah.number) },
                    onOpen = { onSurahClick(surah.number) }
                )
            }
        }
    }
}

/**
 * Premium Surah Card — per the GLM Complete Plan spec:
 * ┌──────────────────────────────────────────┐
 * │ 01                                  1:7  │
 * │ الفاتحة                                 │
 * │ Al-Fatihah — The Opener                │
 * │ সূরা ফাতিহা                               │
 * │ Makki • 7 Ayahs                         │
 * │ Progress ━━━━━━━━━━━                     │
 * │ [▶ Play]        [Open]                  │
 * └──────────────────────────────────────────┘
 */
@Composable
private fun PremiumSurahCard(
    surah: SurahSummary,
    progress: Float,
    onPlay: () -> Unit,
    onOpen: () -> Unit
) {
    val isMeccan = surah.revelationType == RevelationType.MECCAN

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(AppRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.low)
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg)
        ) {
            // ─── Row 1: Surah number (left) + Arabic name (right) ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Surah number in styled badge
                Surface(
                    shape = RoundedCornerShape(AppRadius.xs),
                    color = if (isMeccan) AppColors.brandPrimary else AppColors.brandSecondary
                ) {
                    Text(
                        text = String.format("%02d", surah.number),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.weight(1f))
                // Juz info
                Text(
                    text = "পারা ${getJuzForSurah(surah.number)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(AppSpacing.sm))
                // Arabic name
                Text(
                    text = surah.nameArabic,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End
                )
            }

            Spacer(Modifier.height(AppSpacing.sm))

            // ─── Row 2: English name + meaning ───
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                Text(
                    text = surah.nameEnglish,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "— ${surah.englishMeaning}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(2.dp))

            // ─── Row 3: Bangla name ───
            Text(
                text = surah.nameBengali,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.brandPrimary,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(AppSpacing.xs))

            // ─── Row 4: Meta tags ───
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Revelation type tag
                Surface(
                    shape = RoundedCornerShape(AppRadius.pill),
                    color = if (isMeccan)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = if (isMeccan) "মাক্কী" else "মাদানী",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMeccan)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                // Ayah count tag
                Surface(
                    shape = RoundedCornerShape(AppRadius.pill),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${surah.ayahCount} আয়াত",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Translation availability badge
                if (surah.isFullTextAvailable) {
                    Surface(
                        shape = RoundedCornerShape(AppRadius.pill),
                        color = AppColors.success.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "4 অনুবাদ ✓",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.success,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ─── Progress Bar (if > 0) ───
            if (progress > 0f) {
                Spacer(Modifier.height(AppSpacing.sm))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "পড়ার অগ্রগতি",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.brandPrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AppColors.brandPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(AppSpacing.sm))

            // ─── Row 5: Action buttons ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                // Play button
                Surface(
                    shape = RoundedCornerShape(AppRadius.md),
                    color = AppColors.brandPrimary
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(onClick = onPlay)
                            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "চালান",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "চালান",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                // Open button
                Surface(
                    shape = RoundedCornerShape(AppRadius.md),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(onClick = onOpen)
                            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "খুলুন →",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

/** Approximate Juz number for a surah (surah 1→Juz 1, surah 78→Juz 29, etc.) */
private fun getJuzForSurah(surahNumber: Int): Int {
    val juzStarts = intArrayOf(1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12,13,13,14,14,15,15,16,16,17,17,18,18,19,19,20,20,21,21,22,22,23,23,24,24,25,25,26,26,27,27,28,28,29,29,30,30,30,30,30,30,30,30,30,30,30,30,30,30,30,30,29,29,29,29,29,29,29,29,29,29,29,30,30,30,30,30,30,30,30,30,30)
    return if (surahNumber in juzStarts.indices) juzStarts[surahNumber - 1] else surahNumber
}
