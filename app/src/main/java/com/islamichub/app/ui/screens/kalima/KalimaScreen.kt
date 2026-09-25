package com.islamichub.app.ui.screens.kalima

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.Kalima
import com.islamichub.app.ui.components.PremiumHeroCard
import com.islamichub.app.ui.theme.arabicSp
import com.islamichub.app.ui.theme.banglaSp
import com.islamichub.app.ui.theme.englishSp
import com.islamichub.app.ui.theme.staggerEntrance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalimaScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { KalimaViewModel(container) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    // Track expanded state per kalima
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("৬ কালিমা", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val kalimas = state.data?.kalimas
        if (state.isLoading || kalimas == null) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("কালিমা লোড হচ্ছে…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("⚠️",
                        style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("কালিমা লোড করা যায়নি",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("অ্যাপটি আবার খুলে চেষ্টা করুন",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Premium hero
            item {
                PremiumHeroCard(
                    backgroundImage = "quran-premium-bg.webp",
                    context = context,
                    height = 140
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("৬ কালিমা",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("ইসলামের মৌলিক বিশ্বাস • উচ্চারণ + অর্থ + ব্যাখ্যা",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }

            items(kalimas, key = { it.id }) { kalima ->
                val idx = kalimas.indexOfFirst { it.id == kalima.id }.coerceAtLeast(0)
                val isExpanded = expandedStates[kalima.id] ?: false
                KalimaPremiumCard(
                    kalima = kalima,
                    index = idx,
                    isExpanded = isExpanded,
                    onToggleExpand = { expandedStates[kalima.id] = !isExpanded }
                )
            }
        }
    }
}

@Composable
private fun KalimaPremiumCard(
    kalima: Kalima,
    index: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val kalimaColors = listOf(
        Color(0xFF6D45C7), Color(0xFF1B5E20), Color(0xFFC9A34E),
        Color(0xFF1565C0), Color(0xFFD84315), Color(0xFF00897B)
    )
    val accent = kalimaColors[(kalima.id - 1) % kalimaColors.size]

    Card(
        modifier = Modifier.fillMaxWidth().staggerEntrance(index).clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.15f), accent.copy(alpha = 0.05f))
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header: number badge + name + expand icon
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
                                .background(accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = kalima.id.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text(
                                text = kalima.name ?: "কালিমা ${kalima.id}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            kalima.nameEn?.let { en ->
                                Text(
                                    text = en,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess
                                          else Icons.Filled.ExpandMore,
                            contentDescription = "বিস্তারিত",
                            tint = accent
                        )
                    }
                }

                // Arabic text (always visible)
                kalima.arabic?.let { ar ->
                    if (ar.isNotBlank()) {
                        Text(
                            text = ar,
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = arabicSp(MaterialTheme.typography.headlineSmall.fontSize)),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }
                }

                // Bangla pronunciation (always visible)
                kalima.banglaPronunciation?.let { pron ->
                    if (pron.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔊 উচ্চারণ: ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = accent)
                            Text(pron,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = banglaSp(MaterialTheme.typography.bodyMedium.fontSize)),
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Bangla translation (always visible)
                kalima.bangla?.let { bn ->
                    if (bn.isNotBlank()) {
                        Text(
                            text = bn,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = banglaSp(MaterialTheme.typography.bodyMedium.fontSize)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Expandable details
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Spacer(Modifier.height(4.dp))
                        // Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(accent.copy(alpha = 0.3f))
                        )

                        // Meaning
                        kalima.meaning?.let { meaning ->
                            if (meaning.isNotBlank()) {
                                KalimaDetailRow(label = "📖 অর্থ", value = meaning, accent = accent)
                            }
                        }

                        // Explanation
                        kalima.explanation?.let { expl ->
                            if (expl.isNotBlank()) {
                                KalimaDetailRow(label = "🤲 ব্যাখ্যা", value = expl, accent = accent)
                            }
                        }

                        // Transliteration (English)
                        kalima.transliteration?.let { tr ->
                            if (tr.isNotBlank()) {
                                KalimaDetailRow(label = "🔤 ইংরেজি উচ্চারণ", value = tr, accent = accent, valueIsEnglish = true)
                            }
                        }

                        // v5.9.0 — the fake "অডিও শুনুন" button was removed: no kalima
                        // audio files were ever bundled, so the button silently did
                        // nothing. The screen already teaches correct recitation through
                        // the Bangla + English pronunciation rows above.
                    }
                }
            }
        }
    }
}

@Composable
private fun KalimaDetailRow(label: String, value: String, accent: Color, valueIsEnglish: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = accent
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = if (valueIsEnglish) englishSp(MaterialTheme.typography.bodySmall.fontSize)
                else banglaSp(MaterialTheme.typography.bodySmall.fontSize)
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
