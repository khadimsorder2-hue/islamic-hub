package com.islamichub.app.ui.screens.names

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.NameOfAllah
import com.islamichub.app.ui.theme.staggerEntrance
import com.islamichub.app.ui.theme.arabicSp
import com.islamichub.app.ui.theme.banglaSp
import com.islamichub.app.ui.theme.englishSp
import com.islamichub.app.ui.theme.premiumTap
import androidx.compose.material.icons.filled.ContentCopy

@Composable
fun NamesScreen(container: AppContainer) {
    val vm = remember { NamesViewModel(container) }
    val state by vm.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    // v5.11.0 — AI explain on any name (drop-in shared popup)
    var aiName by remember { mutableStateOf<NameOfAllah?>(null) }

    // v5.11.0 — status-bar-aware top padding (edge-to-edge fix)
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = statusBarTop + 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Premium hero
        item {
            com.islamichub.app.ui.components.PremiumHeroCard(
                backgroundImage = "asmaul_husna_light_bg.webp",
                context = context,
                height = 160
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.names_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Text(
                        text = stringResource(R.string.names_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
        itemsIndexed(state.names, key = { _, name -> name.number }) { index, name ->
            Box(modifier = Modifier.staggerEntrance(index)) {
                NameRow(
                    name = name,
                    onCopy = {
                        val text = "${name.number}. ${name.arabic}\n" +
                            "${name.transliteration}\n" +
                            "${name.englishMeaning}\n" +
                            "${name.bengaliMeaning}"
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("islamic-hub-name", text))
                        android.widget.Toast.makeText(context, "কপি হয়েছে ✅", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        val text = "${name.number}. ${name.arabic}\n" +
                            "${name.transliteration} — ${name.englishMeaning}\n" +
                            "${name.bengaliMeaning}\n\n" +
                            "— Islamic Hub থেকে শেয়ার করা হয়েছে"
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "শেয়ার করুন"))
                    },
                    onAskAi = { aiName = name }
                )
            }
        }
    }

    // v5.11.0 — shared AI explanation popup
    aiName?.let { n ->
        com.islamichub.app.ui.components.AIExplanationPopup(
            container = container,
            title = "আসমাউল হুসনা",
            question = "আল্লাহর নাম \"${n.transliteration}\" (${n.arabic}) সম্পর্কে বিস্তারিত বলুন — অর্থ: ${n.bengaliMeaning}",
            context = "আল্লাহর ৯৯টি নামের মধ্যে ${n.number} নম্বর নাম",
            show = true,
            onDismiss = { aiName = null }
        )
    }
}

@Composable
private fun NameRow(
    name: NameOfAllah,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onAskAi: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.number.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.transliteration,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = englishSp(MaterialTheme.typography.titleMedium.fontSize)),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = name.englishMeaning,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = englishSp(MaterialTheme.typography.bodySmall.fontSize)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = name.bengaliMeaning,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = banglaSp(MaterialTheme.typography.bodySmall.fontSize)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = name.arabic,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = arabicSp(MaterialTheme.typography.headlineSmall.fontSize)),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End
            )
        }
        // v5.11.0 — copy / share / AI actions (this screen had none of them)
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
        ) {
            SmallActionChip(icon = Icons.Filled.AutoAwesome, label = "AI ব্যাখ্যা", onClick = onAskAi)
            SmallActionChip(icon = Icons.Filled.Share, label = "শেয়ার", onClick = onShare)
            SmallActionChip(
                icon = Icons.Filled.ContentCopy,
                label = "কপি",
                onClick = onCopy
            )
        }
    }
}

@Composable
private fun SmallActionChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .premiumTap(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = banglaSp(MaterialTheme.typography.labelSmall.fontSize)),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
