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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
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

এই বিষয়ে বিস্তারিত ইসলামিক ব্যাখ্যা দিন। সহজ বাংলায়, উদাহরণ দিয়ে, গ্রামের খতিবের ভাষায়।
কুরআন ও হাদিসের সূত্র উল্লেখ করুন।
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
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", style = MaterialTheme.typography.titleSmall)
                }
                Text("AI বিস্তারিত", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
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

                // Loading
                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Text("  AI উত্তর তৈরি করছে…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Text(ans, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }

                    // Copy button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "$question\n\n$ans")
                            }
                            appContext.startActivity(Intent.createChooser(shareIntent, "Copy / Share"))
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null,
                                modifier = Modifier.size(16.dp))
                            Text(" কপি", modifier = Modifier.padding(start = 4.dp))
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
