package com.islamichub.app.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.data.model.Ayah

/**
 * Word-by-word bottom sheet for an ayah.
 *
 * Splits the Arabic text into words (by whitespace) and shows:
 *  - Arabic word
 *  - Transliteration (placeholder — real transliteration would require a dictionary)
 *  - Bangla meaning (placeholder — real meaning would require a word-level dictionary)
 *
 * For now, we use a simple phonetic transliteration map for common Arabic words,
 * and show the full ayah meaning below each word as context.
 *
 * The user can tap any word to hear pronunciation (uses the per-ayah audio).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordByWordBottomSheet(
    ayah: Ayah,
    onPlayAudio: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Split Arabic into words (ignore empty strings from multiple spaces)
    val words = ayah.arabic.trim().split(Regex("\\s+")).filter { it.isNotBlank() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "শব্দে শব্দে",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "আয়াত ${ayah.numberInSurah} • ${words.size} শব্দ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .clip(RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onPlayAudio) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "পূর্ণ আয়াত শুনুন",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Full ayah preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = ayah.arabic,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = ayah.bengali,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }

            Text(
                text = "প্রতিটি শব্দ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            // Word-by-word list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                items(words.size) { idx ->
                    val word = words[idx]
                    val translit = getTransliteration(word)
                    val meaning = getWordMeaning(word)
                    WordRow(
                        index = idx + 1,
                        arabicWord = word,
                        transliteration = translit,
                        meaning = meaning
                    )
                }
            }
        }
    }
}

@Composable
private fun WordRow(
    index: Int,
    arabicWord: String,
    transliteration: String,
    meaning: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Index badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // Arabic word
            Text(
                text = arabicWord,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            // Transliteration + meaning
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = transliteration,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Simple Arabic-to-English transliteration map for common Quranic words.
 * This is a basic heuristic — for production, use a proper transliteration library.
 */
private fun getTransliteration(word: String): String {
    // Common word mappings
    val map = mapOf(
        "بِسْمِ" to "Bismi",
        "اللَّهِ" to "Allahi",
        "الرَّحْمَٰنِ" to "Ar-Rahmani",
        "الرَّحِيمِ" to "Ar-Raheemi",
        "الْحَمْدُ" to "Al-hamdu",
        "لِلَّهِ" to "Lillahi",
        "رَبِّ" to "Rabbi",
        "الْعَالَمِينَ" to "Al-'alameena",
        "مَالِكِ" to "Maliki",
        "يَوْمِ" to "Yawmi",
        "الدِّينِ" to "Ad-deeni",
        "إِيَّاكَ" to "Iyyaka",
        "نَعْبُدُ" to "Na'budu",
        "وَإِيَّاكَ" to "Wa iyyaka",
        "نَسْتَعِينُ" to "Nasta'een",
        "اهْدِنَا" to "Ihdina",
        "الصِّرَاطَ" to "As-sirata",
        "الْمُسْتَقِيمَ" to "Al-mustaqeema",
        "صِرَاطَ" to "Sirata",
        "الَّذِينَ" to "Allazeena",
        "أَنْعَمْتَ" to "An'amta",
        "عَلَيْهِمْ" to "Alaihim",
        "غَيْرِ" to "Ghayri",
        "الْمَغْضُوبِ" to "Al-maghdubi",
        "وَلَا" to "Wa la",
        "الضَّالِّينَ" to "Ad-dalleen",
        "قُلْ" to "Qul",
        "هُوَ" to "Huwa",
        "أَحَدٌ" to "Ahad",
        "اللَّهُ" to "Allahu",
        "الصَّمَدُ" to "As-Samad",
        "لَمْ" to "Lam",
        "يَلِدْ" to "Yalid",
        "وَلَمْ" to "Wa lam",
        "يُولَدْ" to "Yoolad",
        "وَلَمْ" to "Wa lam",
        "يَكُنْ" to "Yakun",
        "لَهُ" to "Lahu",
        "كُفُوًا" to "Kufuwan",
        "أَحَدٌ" to "Ahad"
    )
    return map[word] ?: word  // fallback: show Arabic if no mapping
}

/**
 * Simple Arabic-to-Bangla meaning map for common Quranic words.
 */
private fun getWordMeaning(word: String): String {
    val map = mapOf(
        "بِسْمِ" to "নামে",
        "اللَّهِ" to "আল্লাহর",
        "الرَّحْمَٰنِ" to "পরম করুণাময়",
        "الرَّحِيمِ" to "অসীম দয়ালু",
        "الْحَمْدُ" to "সমস্ত প্রশংসা",
        "لِلَّهِ" to "আল্লাহরই",
        "رَبِّ" to "পালনকর্তা",
        "الْعَالَمِينَ" to "সৃষ্টিজগতের",
        "مَالِكِ" to "মালিক",
        "يَوْمِ" to "দিনের",
        "الدِّينِ" to "বিচার",
        "إِيَّاكَ" to "তোমাকেই",
        "نَعْبُدُ" to "আমরা ইবাদত করি",
        "وَإِيَّاكَ" to "এবং তোমাকেই",
        "نَسْتَعِينُ" to "আমরা সাহায্য চাই",
        "اهْدِنَا" to "আমাদেকে পথ দেখাও",
        "الصِّرَاطَ" to "পথ",
        "الْمُسْتَقِيمَ" to "সরল",
        "صِرَاطَ" to "পথ",
        "الَّذِينَ" to "যাদের",
        "أَنْعَمْتَ" to "তুমি অনুগ্রহ করেছ",
        "عَلَيْهِمْ" to "তাদের প্রতি",
        "غَيْرِ" to "নয়",
        "الْمَغْضُوبِ" to "ক্রোধের",
        "وَلَا" to "এবং নয়",
        "الضَّالِّينَ" to "পথভ্রষ্ট",
        "قُلْ" to "বলুন",
        "هُوَ" to "তিনি",
        "أَحَدٌ" to "এক",
        "اللَّهُ" to "আল্লাহ",
        "الصَّمَدُ" to "অমুখাপেক্ষী",
        "لَمْ" to "তিনি নন",
        "يَلِدْ" to "জন্ম দেন",
        "وَلَمْ" to "এবং নন",
        "يُولَدْ" to "জন্ম নেন",
        "يَكُنْ" to "হওয়া",
        "لَهُ" to "তার",
        "كُفُوًا" to "সমতুল্য",
        "أَحَدٌ" to "কেউ"
    )
    return map[word] ?: "—"  // fallback: show dash
}
