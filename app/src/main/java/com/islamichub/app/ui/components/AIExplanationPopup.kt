package com.islamichub.app.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AIService
import com.islamichub.app.ui.theme.premiumShimmer
import kotlinx.coroutines.launch

/**
 * Premium AI explanation popup — same design as web source's showPremiumAIModal.
 *
 * Shows a modal dialog with:
 *  - Premium header with gradient (✨ AI বিস্তারিত)
 *  - Question context card
 *  - AI answer (formatted)
 *  - Copy button
 *  - Loading state
 *
 * Usage:
 *   var showAI by remember { mutableStateOf(false) }
 *   AIExplanationPopup(
 *       container = container,
 *       title = "মিসকনসেপশন",
 *       question = "আল্লাহ কি আসমানে বসেন?",
 *       context = "আকীদা বিভাগ",
 *       show = showAI,
 *       onDismiss = { showAI = false }
 *   )
 */
@Composable
fun AIExplanationPopup(
    container: AppContainer,
    title: String,
    question: String,
    context: String = "",
    show: Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var answer by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val appContext = LocalContext.current

    LaunchedEffect(show) {
        if (show) {
            answer = null
            error = null
            isLoading = true
            scope.launch {
                val prompt = """
বিষয়: $title
প্রশ্ন: $question
${if (context.isNotBlank()) "কনটেক্সট: $context" else ""}

নিচের কাঠামোতে বিস্তারিত ইসলামিক ব্যাখ্যা দিন:

📖 মূল কথা: [একদম সহজ এক-দুই লাইনে মূল বার্তা]
🕰️ প্রেক্ষাপট: [কুরআনের আয়াত হলে কেন, কী ঘটনায় নাজিল হলো; হাদিস হলে কখন, কাকে, কেন বলা হলো — গল্পের মতো করে]
🌟 বর্তমান যুগে এর গুরুত্ব: [আজকের জীবনে কেন এটি দরকার — আধুনিক সমস্যার সাথে মিলিয়ে]
🏡 গ্রামের জীবনের উদাহরণ: [কৃষক, হাট-বাজার, পরিবার, প্রতিবেশী — রোজকার জীবনের অন্তত ১–২টি উদাহরণ দিয়ে বোঝান]
🔗 একই রকম কথা আর কোথায় আছে: [একই বিষয়ে অন্য আয়াত/সহিহ হাদিস — উৎসসহ]
✅ আমলের সহজ উপায়: [আমরা এখন কী করব — সহজ তালিকা]
🎯 এক লাইনে সারকথা

ভাষার নিয়ম: এমন সহজ বাংলা যেন একজন একদম নিরক্ষর বা মুর্খ গ্রামের মানুষও প্রথমবার শুনেই বুঝতে পারে। ছোট ছোট বাক্য লিখুন। কঠিন আরবি/ফারসি শব্দ এলে পাশে বন্ধনীতে সহজ বাংলা অর্থ দিন। কুরআন ও হাদিসের সূত্র (সূরা/আয়াত নম্বর, হাদিসের গ্রন্থ ও নম্বর) অবশ্যই উল্লেখ করুন।
""".trimIndent()

                val result = container.aiService.ask(prompt)
                isLoading = false
                if (result.error != null && result.error != "stale") {
                    error = result.error
                } else if (result.error == null) {
                    answer = result.answer
                }
            }
        }
    }

    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            com.islamichub.app.ui.components.PremiumDialogIcon(
                icon = Icons.Filled.AutoAwesome,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text("AI বিস্তারিত", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Question context card
                if (question.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("বিষয়বস্তু", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold)
                            Text(question, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                // Loading — v5.11.0 shimmer skeleton (was a lone spinner + text;
                // this matches the app's premium skeleton language)
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text("AI উত্তর তৈরি করছে…",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(Modifier.fillMaxWidth().height(14.dp).premiumShimmer(RoundedCornerShape(7.dp)))
                        Box(Modifier.fillMaxWidth(0.72f).height(14.dp).premiumShimmer(RoundedCornerShape(7.dp)))
                        Box(Modifier.fillMaxWidth(0.9f).height(14.dp).premiumShimmer(RoundedCornerShape(7.dp)))
                        Box(Modifier.fillMaxWidth(0.6f).height(14.dp).premiumShimmer(RoundedCornerShape(7.dp)))
                        Box(Modifier.fillMaxWidth(0.8f).height(14.dp).premiumShimmer(RoundedCornerShape(7.dp)))
                    }
                }

                // Error
                error?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error)
                }

                // Answer
                answer?.let { ans ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // AI badge
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier.size(24.dp).clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) { Text("🤲", style = MaterialTheme.typography.labelSmall) }
                                Text("Islamic Hub AI", style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            // v5.11.0 — render the emoji-headed sections the prompts ask
                            // for (📖 🌟 🏡 …) as a real typographic hierarchy instead of
                            // one flat text blob
                            remember(ans) { parseAiSectionLines(ans) }.forEach { line ->
                                if (line.isHeader) {
                                    val t = line.text.trimStart()
                                    val emoji = t.takeWhile { !it.isLetterOrDigit() }.trim()
                                    val rest = if (emoji.isNotEmpty()) t.removePrefix(emoji).trim() else t
                                    val colonIdx = rest.indexOf(':')
                                    if (colonIdx in 1..40) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                if (emoji.isNotEmpty()) {
                                                    Text(emoji, style = MaterialTheme.typography.titleSmall)
                                                }
                                                Text(
                                                    rest.substring(0, colonIdx).trim(),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Text(
                                                rest.substring(colonIdx + 1).trim(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (emoji.isNotEmpty()) {
                                                Text(emoji, style = MaterialTheme.typography.titleSmall)
                                            }
                                            Text(
                                                rest,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        line.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // v5.9.0 — the "কপি" button used to fire a share intent (mislabeled).
                    // Now: a real clipboard copy + a separate share button.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            val clipboard = appContext.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Islamic Hub", "$question\n\n$ans"))
                            android.widget.Toast.makeText(appContext, "কপি হয়েছে", android.widget.Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null,
                                modifier = Modifier.size(16.dp))
                            Text(" কপি", modifier = Modifier.padding(start = 4.dp))
                        }
                        TextButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "$question\n\n$ans")
                            }
                            appContext.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = null,
                                modifier = Modifier.size(16.dp))
                            Text(" শেয়ার", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("বন্ধ করুন") }
        }
    )
}

// ─── v5.11.0 — AI answer section parsing ────────────────────────────────

private data class AiSectionLine(val text: String, val isHeader: Boolean)

private val AI_SECTION_EMOJIS = listOf(
    "📖", "🕰️", "🌟", "🏡", "🔗", "✅", "🎯", "🕌", "📚", "✨", "🤲", "🌙", "❓", "💡"
)

private fun parseAiSectionLines(answer: String): List<AiSectionLine> {
    return answer.lines()
        .filter { it.isNotBlank() }
        .map { line ->
            val trimmed = line.trimStart()
            AiSectionLine(trimmed, AI_SECTION_EMOJIS.any { trimmed.startsWith(it) })
        }
}
