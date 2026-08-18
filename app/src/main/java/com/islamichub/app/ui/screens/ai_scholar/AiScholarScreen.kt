package com.islamichub.app.ui.screens.ai_scholar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Refresh
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cache
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AIService
import com.islamichub.app.ui.components.PremiumHeroCard
import com.islamichub.app.ui.components.loadAssetImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScholarScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val vm = remember { AiScholarViewModel(container) }
    val state by vm.state.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    // Auto-scroll to bottom on new message
    LaunchedEffect(state.messages.size, state.isThinking) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("AI ক্যাশ মুছবেন?") },
            text = { Text("এতে ${state.cacheCount}টি সংরক্ষিত উত্তর মুছে যাবে। এটি পূর্বাবস্থায় ফেরানো যাবে না।") },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearAICache()
                    showClearCacheDialog = false
                }) { Text("মুছুন", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) { Text("বাতিল") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Psychology,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "  AI Scholar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Cache button with badge
                    IconButton(onClick = { showClearCacheDialog = true }) {
                        Box {
                            Icon(Icons.Filled.Cache, contentDescription = "AI Cache")
                            if (state.cacheCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.TopEnd)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (state.cacheCount > 99) "99+" else state.cacheCount.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                    if (state.messages.isNotEmpty()) {
                        IconButton(onClick = vm::clearChat) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear chat")
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Cache / from-cache indicator
                    AnimatedVisibility(visible = state.lastAnswerFromCache) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "  ক্যাশ থেকে তাৎক্ষণিক উত্তর",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            placeholder = { Text("আপনার প্রশ্ন লিখুন…") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4
                        )
                        IconButton(
                            onClick = {
                                if (input.isNotBlank() && !state.isThinking) {
                                    vm.sendQuestion(input.trim())
                                    input = ""
                                }
                            },
                            enabled = input.isNotBlank() && !state.isThinking && state.apiKeyConfigured
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (!state.apiKeyConfigured) {
            // No API key — show premium empty state
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "AI Scholar সক্রিয় করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Settings → AI তে গিয়ে ফ্রি Gemini API key যোগ করুন। ১ মিনিটে সেটআপ।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(Modifier.height(24.dp))
                androidx.compose.material3.Button(onClick = onNavigateToSettings) {
                    Text("AI সেটআপ করুন")
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.messages.isEmpty()) {
                // Premium hero header
                item {
                    val bgBitmap = remember { loadAssetImage(context, "img/voice-ai-bg.webp") }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            if (bgBitmap != null) {
                                Image(
                                    bitmap = bgBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(modifier = Modifier.fillMaxSize().background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF6D45C7).copy(alpha = 0.85f),
                                            Color(0xFF8E24AA).copy(alpha = 0.95f)
                                        )
                                    )
                                ))
                            } else {
                                Box(modifier = Modifier.fillMaxSize().background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ))
                            }
                            Column(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Psychology, contentDescription = null,
                                        tint = Color.White, modifier = Modifier.size(28.dp))
                                    Text("  Islamic Hub AI Scholar",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Text("কুরআন ও সহীহ হাদিস ভিত্তিক প্রশ্ন করুন।",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.95f),
                                    modifier = Modifier.padding(top = 4.dp))
                                Text("সব উত্তর সোর্স সহ দেওয়া হবে।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                    }
                }
                // Quick questions
                item {
                    com.islamichub.app.ui.components.PremiumSectionHeader(title = "দ্রুত প্রশ্ন")
                }
                val quickQuestions = listOf(
                    "নামাজের গুরুত্ব কী?" to "🕌",
                    "নামাজে ভুল হলে কী করব?" to "⚠️",
                    "জুমআর নামাজের নিয়ম কী?" to "📿",
                    "মুসাফিরের নামাজের বিধান কী?" to "✈️",
                    "রমজানের ফজিলত কী?" to "🌙",
                    "হজ্জের নিয়ম কী?" to "🕋",
                    "যাকাত কাদের প্রদান করতে হয়?" to "💰",
                    "তালাকের ইসলামিক বিধান কী?" to "⚖️"
                )
                items(quickQuestions) { (q, emoji) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { vm.sendQuickQuestion(q) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                text = q,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            items(state.messages) { msg ->
                ChatMessageBubble(
                    message = msg,
                    onCopy = { text ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("AI Answer", text))
                    },
                    onShare = { text ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                    },
                    onRegenerate = if (msg.role == "assistant") ({ vm.regenerateLast() }) else null
                )
            }

            if (state.isThinking) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Column {
                                    Text(
                                        text = "🤲 AI ভাবছে…",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "কুরআন ও হাদিস থেকে সোর্স খুঁজছে",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            state.warning?.let { warning ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = warning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            state.error?.let { err ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * AI chat message bubble with structured colorful output.
 * Parses sections starting with emoji + label (📖, 📚, ⚖️, etc.) and renders
 * them as colored chips with the content below.
 */
@Composable
private fun ChatMessageBubble(
    message: AIService.ChatMessage,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onRegenerate: (() -> Unit)? = null
) {
    val isUser = message.role == "user"

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 1f),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isUser) 0.dp else 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header: avatar + name + actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isUser) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🤲", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(
                            text = if (isUser) "আপনি" else "  AI Scholar",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f)
                                    else MaterialTheme.colorScheme.primary
                        )
                    }
                    if (!isUser) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Copy button
                            IconButton(
                                onClick = { onCopy(message.content) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            // Share button
                            IconButton(
                                onClick = { onShare(message.content) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            // Regenerate button (only on latest message)
                            if (onRegenerate != null) {
                                IconButton(
                                    onClick = onRegenerate,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Refresh,
                                        contentDescription = "Regenerate",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Content: structured if AI, plain if user
                if (isUser) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    StructuredAIAnswer(message.content)
                }
            }
        }
    }
}

/**
 * Renders AI answer with structured colorful sections.
 *
 * Detects lines starting with section emoji/label like:
 *   📖 কুরআন থেকে: ...
 *   📚 হাদিস থেকে: ...
 *   ⚖️ ইসলামি বিধান: ...
 *
 * And renders each as a colored chip + content block.
 *
 * Lines without section header are rendered as body text.
 * **bold** text is highlighted.
 */
@Composable
private fun StructuredAIAnswer(answer: String) {
    // Split by section markers
    val sectionPattern = Regex("^(📖|📚|⚖️|💡|⚠️|✅|🎯|🔗|🔄|🌙|🕌|🤲|✨|💎)\\s*[^.\\n]*[:：]", RegexOption.MULTILINE)

    val sections = mutableListOf<Pair<String?, String>>()
    val matches = sectionPattern.findAll(answer).toList()

    if (matches.isEmpty()) {
        // No structured sections — render as plain text with markdown-style bold
        RenderMarkdownBold(answer)
        return
    }

    // Capture pre-section intro (if any)
    if (matches.first().range.first > 0) {
        val intro = answer.substring(0, matches.first().range.first).trim()
        if (intro.isNotEmpty()) {
            sections.add(null to intro)
        }
    }

    for ((i, m) in matches.withIndex()) {
        val end = if (i + 1 < matches.size) matches[i + 1].range.first else answer.length
        val sectionText = answer.substring(m.range.first, end).trim()
        sections.add(m.value to sectionText)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        sections.forEach { (header, content) ->
            if (header == null) {
                // intro block
                RenderMarkdownBold(content)
            } else {
                // section with header
                val emoji = header.firstOrNull()?.toString() ?: ""
                val labelColor = colorForSection(emoji)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(labelColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(header.take(40),  // just the label line
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = labelColor)
                    }
                    // content (without header line)
                    val contentBody = content.substringAfter("\n").trim()
                    if (contentBody.isNotEmpty()) {
                        RenderMarkdownBold(contentBody)
                    }
                }
            }
        }
    }
}

/** Color for each section emoji */
private fun colorForSection(emoji: String): Color = when (emoji) {
    "📖" -> Color(0xFF1B5E20)        // Quran — green
    "📚" -> Color(0xFF1565C0)        // Hadith — blue
    "⚖️" -> Color(0xFFEF6C00)        // Fiqh — orange
    "💡" -> Color(0xFF8E24AA)        // Spiritual — purple
    "⚠️" -> Color(0xFFC62828)        // Warning — red
    "✅" -> Color(0xFF2E7D32)        // Action — green
    "🎯" -> Color(0xFF00897B)        // Summary — teal
    "🔗" -> Color(0xFF3949AB)        // Related — indigo
    "🔄" -> Color(0xFFD84315)        // Other books — orange-red
    "🌙" -> Color(0xFF5C6BC0)        // Ramadan — indigo
    "🕌" -> Color(0xFFC9A34E)        // Prayer — gold
    "🤲" -> Color(0xFF7E57C2)        // Spiritual — purple
    "✨" -> Color(0xFFFF6B35)        // Highlight — orange
    "💎" -> Color(0xFF00ACC1)        // Treasure — cyan
    else -> MaterialTheme.colorScheme.primary
}

/**
 * Renders text with **bold** sections highlighted.
 * Splits by ** pairs and renders bold parts in primary color.
 */
@Composable
private fun RenderMarkdownBold(text: String) {
    val parts = text.split("\\*\\*".toRegex())
    Column {
        parts.forEachIndexed { idx, part ->
            val isBold = idx % 2 == 1   // odd indices are between **...**
            if (part.isNotBlank()) {
                if (isBold) {
                    Text(
                        text = part,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = part,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
