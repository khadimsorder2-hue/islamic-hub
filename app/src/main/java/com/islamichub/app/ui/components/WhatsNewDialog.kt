package com.islamichub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamichub.app.BuildConfig
import com.islamichub.app.data.AppContainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A "What's New" dialog that appears on first launch of each new app version.
 *
 * This guarantees that users SEE the upgrade — even if the new feature is hidden
 * behind a state check, requires a network call, or only appears deep in the
 * navigation tree. The dialog is dismissed after the user reads it, and won't
 * reappear until the next version bump.
 *
 * Trigger: shown when BuildConfig.VERSION_NAME differs from the version stored
 * in DataStore under "last_whats_new_version".
 */
@Composable
fun WhatsNewDialog(container: AppContainer) {
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    // On first composition, check if we need to show the dialog.
    LaunchedEffect(Unit) {
        val lastShown = container.settingsRepository.lastWhatsNewShownVersion.first()
        val current = BuildConfig.VERSION_NAME
        if (lastShown != current) {
            showDialog = true
        }
    }

    if (!showDialog) return

    AlertDialog(
        onDismissRequest = {
            // Dismiss + persist so it doesn't reappear on recomposition
            scope.launch {
                container.settingsRepository.setLastWhatsNewShownVersion(BuildConfig.VERSION_NAME)
                showDialog = false
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.NewReleases,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(
                        "Islamic Hub",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "v${BuildConfig.VERSION_NAME} — নতুন কী আছে",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(getWhatsNewForVersion(BuildConfig.VERSION_NAME)) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = when (item.type) {
                                    WhatsNewItemType.NEW -> MaterialTheme.colorScheme.primary
                                    WhatsNewItemType.FIXED -> MaterialTheme.colorScheme.tertiary
                                    WhatsNewItemType.IMPROVED -> MaterialTheme.colorScheme.secondary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(10.dp))
                            Column {
                                Text(
                                    item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
                // Always-shown version info footer
                item {
                    Text(
                        "অ্যাপ্লিকেশন ভার্সন: ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})\n" +
                        "আপনি যদি নতুন ফিচার দেখতে না পান, অ্যাপটি সম্পূর্ণ বন্ধ করে আবার খুলুন অথবা Settings → Cache Clear করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    container.settingsRepository.setLastWhatsNewShownVersion(BuildConfig.VERSION_NAME)
                    showDialog = false
                }
            }) {
                Text("ঠিক আছে", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

private enum class WhatsNewItemType { NEW, FIXED, IMPROVED }

private data class WhatsNewItem(
    val type: WhatsNewItemType,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.CheckCircle
)

/**
 * Returns the human-readable list of what's new for the given version.
 * Keep this in sync with CHANGELOG.md.
 */
private fun getWhatsNewForVersion(version: String): List<WhatsNewItem> {
    return when (version) {
        "5.5.0" -> listOf(
            WhatsNewItem(
                WhatsNewItemType.NEW,
                "সর্বদা দৃশ্যমান আপগ্রেড ইন্ডিকেটর",
                "এই 'নতুন কী আছে' ডায়ালগ এখন প্রতিটি ভার্সনে স্বয়ংক্রিয়ভাবে দেখাবে। আর কোনো আপগ্রেড চোখের আড়ালে থাকবে না।"
            ),
            WhatsNewItem(
                WhatsNewItemType.NEW,
                "বাড়িতে ভার্সন ব্যাজ",
                "HomeScreen-এ 'v5.5.0' ব্যাজ দেখা যাবে যাতে আপনি নিশ্চিত হন সঠিক ভার্সন চলছে।"
            ),
            WhatsNewItem(
                WhatsNewItemType.IMPROVED,
                "Tafsir মাল্টি-অনুবাদ এখন সর্বদা দৃশ্যমান",
                "আগে অনলাইন অনুবাদ লোড না হলে সেকশনটি লুকিয়ে ছিল। এখন 'লোড হচ্ছে…' বা 'লোড করতে ব্যর্থ' অবস্থা দেখাবে।"
            ),
            WhatsNewItem(
                WhatsNewItemType.FIXED,
                "v5.0–v5.4 রিলিজ নোট সমস্যা",
                "আগের সব রিলিজে 'What's new (v1.2.0)' দেখাত — v5.4 থেকে CHANGELOG.md থেকে সঠিক তথ্য আসে।"
            )
        )
        "5.4.0" -> listOf(
            WhatsNewItem(
                WhatsNewItemType.FIXED,
                "রিলিজ নোট সমস্যা সমাধান",
                "আগে প্রতিটা রিলিজে একই 'What's new (v1.2.0)' দেখাত। এখন CHANGELOG.md থেকে সঠিক তথ্য আসে।"
            )
        )
        "5.3.0" -> listOf(
            WhatsNewItem(
                WhatsNewItemType.NEW,
                "QuranReader-এ মাল্টি-বাংলা অনুবাদ",
                "4 জন অনুবাদকারকের মধ্যে switch করতে পারবেন: মুহিউদ্দীন খান, তাইসিরুল, যাকারিয়া, রাওয়ায়ে বয়ান।"
            ),
            WhatsNewItem(
                WhatsNewItemType.IMPROVED,
                "DesignSystem tokens এখন ব্যবহৃত",
                "HomeScreen-এ AppColors/AppSpacing/AppRadius টোকেন active।"
            )
        )
        "5.2.0" -> listOf(
            WhatsNewItem(
                WhatsNewItemType.NEW,
                "Tafsir-এ মাল্টি-অনুবাদ + মাল্টি-তাফসীর",
                "Full screen Tafsir popup-এ এখন 4 অনুবাদ + 2 তাফসীর chip আকারে switch করা যায়।"
            )
        )
        "5.1.0" -> listOf(
            WhatsNewItem(
                WhatsNewItemType.NEW,
                "Quran.com API সম্প্রসারণ",
                "4 বাংলা অনুবাদ + 4 বাংলা তাফসীরের data layer। (UI v5.2 থেকে শুরু)"
            )
        )
        else -> listOf(
            WhatsNewItem(
                WhatsNewItemType.NEW,
                "নতুন ভার্সন",
                "এই ভার্সনের তথ্য এখনও যোগ হয়নি। CHANGELOG.md দেখুন।"
            )
        )
    }
}
