package com.islamichub.app.ui.screens.tajweed

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.islamichub.app.ui.theme.premiumTap
import com.islamichub.app.ui.theme.staggerEntrance

/**
 * v5.16.0 — Tajweed VOICE checker screen. 1:1 Android port of the web
 * TajbeedCheckerService modal (islamic.html "tajbeed checker"): the user
 * records up to 30s, previews it, sends it to the AI, and gets segmented
 * result cards (transcription / Quran + accuracy & tajweed notes / hadith +
 * grade / dua / general conversation) — all in Bangla.
 *
 * Colors follow the web THEME: deep-green voice-AI palette (#05140C bg,
 * #0D5C3A primary, gold accents) while keeping Material-typography
 * consistency with the rest of the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TajweedVoiceScreen(
    container: com.islamichub.app.data.AppContainer,
    onBack: () -> Unit,
    embedded: Boolean = false
) {
    val vm = remember { TajweedVoiceViewModel(container) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    // Web parity: dark-green voice-AI palette
    val vBg = Color(0xFF05140C)
    val vPrimary = Color(0xFF0D5C3A)
    val vLight = Color(0xFF1A8A56)
    val vGold = Color(0xFFC49A2B)
    val vGoldLight = Color(0xFFF0C040)
    val vTextSoft = Color(0xFFA8D5B5)
    val vPurple = Color(0xFF7C3AED)
    val vBlue = Color(0xFF2563EB)
    val vRed = Color(0xFFB44343)
    val glassWhite = Color.White.copy(alpha = 0.12f)
    val glassBorder = Color.White.copy(alpha = 0.22f)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.startRecording(context)
        else android.widget.Toast.makeText(context, "মাইক্রোফোন অনুমতি প্রয়োজন", android.widget.Toast.LENGTH_SHORT).show()
    }

    fun beginRecording() {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) vm.startRecording(context)
        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(state.autoStopInfo) {
        state.autoStopInfo?.let {
            snackbar.showSnackbar(it)
            vm.dismissAutoStopInfo()
        }
    }

    if (embedded) {
        // Tab-embedded mode: no own Scaffold — the parent checker screen
        // provides the top bar; we fill the remaining space.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(vBg, Color(0xFF072013), vBg)))
        ) {
            VoicePhaseContent(
                state, vm, ::beginRecording, context,
                vBg, vPrimary, vLight, vGold, vGoldLight, vTextSoft, vPurple, vBlue, vRed,
                glassWhite, glassBorder, snackbar
            )
        }
        return
    }

    Scaffold(containerColor = vBg, snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("তাজবীদ ভয়েস AI", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = vBg
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(vBg, Color(0xFF072013), vBg)))
                .padding(padding)
        ) {
            VoicePhaseContent(
                state, vm, ::beginRecording, context,
                vBg, vPrimary, vLight, vGold, vGoldLight, vTextSoft, vPurple, vBlue, vRed,
                glassWhite, glassBorder, snackbar
            )
        }
    }
}

@Composable
private fun VoicePhaseContent(
    state: TajweedVoiceViewModel.UiState,
    vm: TajweedVoiceViewModel,
    beginRecording: () -> Unit,
    context: android.content.Context,
    vBg: Color, vPrimary: Color, vLight: Color, vGold: Color, vGoldLight: Color,
    vTextSoft: Color, vPurple: Color, vBlue: Color, vRed: Color,
    glassWhite: Color, glassBorder: Color,
    snackbar: SnackbarHostState
) {
    when (state.phase) {
        TajweedVoiceViewModel.Phase.INTRO -> IntroSection(
            onRecord = beginRecording,
            vPrimary = vPrimary, vLight = vLight, vGold = vGold, vTextSoft = vTextSoft,
            glassWhite = glassWhite, glassBorder = glassBorder,
            apiKeyConfigured = state.apiKeyConfigured, error = state.error
        )
        TajweedVoiceViewModel.Phase.RECORDING -> RecordingSection(
            elapsedSec = state.elapsedSec,
            amplitude = state.amplitude,
            onStop = vm::stopRecording,
            onCancel = vm::reRecord,
            vPrimary = vPrimary, vRed = vRed, vTextSoft = vTextSoft, glassWhite = glassWhite
        )
        TajweedVoiceViewModel.Phase.REVIEW -> ReviewSection(
            elapsedSec = state.elapsedSec,
            isPlaying = state.isPlaying,
            onTogglePreview = { vm.togglePreview(context) },
            onSubmit = vm::submitAudio,
            onReRecord = beginRecording,
            error = state.error,
            vPrimary = vPrimary, vLight = vLight, vGold = vGold, vTextSoft = vTextSoft,
            glassWhite = glassWhite, glassBorder = glassBorder
        )
        TajweedVoiceViewModel.Phase.ANALYZING -> AnalyzingSection(vGold = vGold, vTextSoft = vTextSoft)
        TajweedVoiceViewModel.Phase.RESULT -> ResultSection(
            analysis = state.analysis,
            onReRecord = { vm.reRecord() },
            vPrimary = vPrimary, vLight = vLight, vGold = vGold, vGoldLight = vGoldLight,
            vTextSoft = vTextSoft, vPurple = vPurple, vBlue = vBlue, vRed = vRed,
            glassWhite = glassWhite, glassBorder = glassBorder
        )
    }
}

@Composable
private fun IntroSection(
    onRecord: () -> Unit,
    vPrimary: Color, vLight: Color, vGold: Color, vTextSoft: Color,
    glassWhite: Color, glassBorder: Color,
    apiKeyConfigured: Boolean, error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "🎤 তাজবীদ চেকার",
            color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "সুরা, দোয়া বা আয়াত পড়ুন — AI আপনার তেলাওয়াত শুনে তাজবীদ ভুল, শুদ্ধতার স্কোর, অর্থ ও শান-এ-নুজুল বাংলায় জানাবে।",
            color = vTextSoft, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 24.sp
        )
        Spacer(Modifier.height(34.dp))

        val pulse = rememberInfiniteTransition(label = "micPulse")
        val scale by pulse.animateFloat(
            initialValue = 1f, targetValue = 1.08f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "micScale"
        )
        Box(
            modifier = Modifier
                .scale(scale)
                .size(120.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(vPrimary, vLight)))
                .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .premiumTap(onClick = onRecord),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Mic, contentDescription = "রেকর্ড শুরু করুন",
                tint = Color.White, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text("রেকর্ড শুরু করতে চাপ দিন", color = Color.White, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(26.dp))
        listOf(
            "১. মাইক বাটনে চাপ দিন — সর্বোচ্চ ৩০ সেকেন্ড রেকর্ড হবে",
            "২. সুরা ফাতিহা, দোয়া বা যেকোনো আয়াত পড়ুন",
            "৩. রেকর্ড শেষে AI কে পাঠান — ফলাফল বাংলায় পাবেন"
        ).forEachIndexed { i, tip ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .staggerEntrance(i),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(vGold)
                )
                Spacer(Modifier.width(10.dp))
                Text(tip, color = vTextSoft, fontSize = 13.sp)
            }
        }

        if (!apiKeyConfigured) {
            Spacer(Modifier.height(16.dp))
            Text(
                "AI API key কনফিগার করা নেই — Settings → AI Scholar এ যোগ করুন।",
                color = Color(0xFFF87171), fontSize = 13.sp, textAlign = TextAlign.Center
            )
        }
        error?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = Color(0xFFF87171), fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun RecordingSection(
    elapsedSec: Int,
    amplitude: Float,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    vPrimary: Color, vRed: Color, vTextSoft: Color, glassWhite: Color
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val pulseScale = 1f + amplitude * 0.45f
        Box(
            modifier = Modifier
                .scale(pulseScale)
                .size(150.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(vPrimary.copy(alpha = 0.85f), vPrimary)))
                .border(3.dp, Color.White.copy(alpha = 0.3f + amplitude * 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Mic, contentDescription = "রেকর্ডিং চলছে",
                tint = Color.White, modifier = Modifier.size(60.dp))
        }
        Spacer(Modifier.height(30.dp))
        // web: tabular-nums giant timer
        Text(
            String.format("%d:%02d", elapsedSec / 60, elapsedSec % 60),
            color = Color.White, fontSize = 58.sp, fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(6.dp))
        Text("আয়াত/দোয়া পড়ুন… (৩০ সেকেন্ড)", color = vTextSoft, fontSize = 14.sp)
        Spacer(Modifier.height(36.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
            // Cancel (web: trash circle button)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF555555))
                    .premiumTap(onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "বাতিল", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            // Stop
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(vRed)
                    .premiumTap(onClick = onStop),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Stop, contentDescription = "রেকর্ডিং শেষ করুন",
                    tint = Color.White, modifier = Modifier.size(34.dp))
            }
        }
    }
}

@Composable
private fun ReviewSection(
    elapsedSec: Int,
    isPlaying: Boolean,
    onTogglePreview: () -> Unit,
    onSubmit: () -> Unit,
    onReRecord: () -> Unit,
    error: String?,
    vPrimary: Color, vLight: Color, vGold: Color, vTextSoft: Color,
    glassWhite: Color, glassBorder: Color
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("রেকর্ড সম্পন্ন ✓", color = vGold, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Spacer(Modifier.height(18.dp))
        // Web parity: audio preview player
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(glassWhite)
                .border(1.dp, glassBorder, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(vPrimary)
                    .premiumTap(onClick = onTogglePreview),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "প্রিভিউ চালান",
                    tint = Color.White, modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("আপনার রেকর্ডিং", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    String.format("দৈর্ঘ্য: %d:%02d", elapsedSec / 60, elapsedSec % 60),
                    color = vTextSoft, fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(30.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = vPrimary)
        ) {
            Icon(Icons.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("AI কে পাঠান", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onReRecord,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("আবার রেকর্ড করুন", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        error?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = Color(0xFFF87171), fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AnalyzingSection(vGold: Color, vTextSoft: Color) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = vGold, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(24.dp))
        Text("AI প্রসেস করছে…", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text("আপনার তেলাওয়াত বিশ্লেষণ হচ্ছে — এক মিনিট অপেক্ষা করুন", color = vTextSoft, fontSize = 13.sp)
    }
}

@Composable
private fun ResultSection(
    analysis: com.islamichub.app.data.repo.AIService.VoiceAnalysis?,
    onReRecord: () -> Unit,
    vPrimary: Color, vLight: Color, vGold: Color, vGoldLight: Color,
    vTextSoft: Color, vPurple: Color, vBlue: Color, vRed: Color,
    glassWhite: Color, glassBorder: Color
) {
    val a = analysis
    val totalCards = (a?.qurans?.size ?: 0) + (a?.hadiths?.size ?: 0) +
            (a?.duas?.size ?: 0) + (a?.generals?.size ?: 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ─── Premium conclusion summary header (web parity) ───
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = glassWhite),
            modifier = Modifier.fillMaxWidth().staggerEntrance(0)
        ) {
            Column(modifier = Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(vGold.copy(alpha = 0.2f))
                        .border(1.dp, vGold.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("বিশ্লেষণ সম্পন্ন", color = vGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    a?.summary ?: "উৎস শনাক্ত করা হয়েছে",
                    color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center, lineHeight = 25.sp
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CountPill("💬", a?.generals?.size ?: 0, "কথোপকথন", vPrimary)
                    CountPill("📖", a?.qurans?.size ?: 0, "সুরা", vPrimary)
                    CountPill("📜", a?.hadiths?.size ?: 0, "হাদিস", vPrimary)
                    CountPill("🤲", a?.duas?.size ?: 0, "দোয়া", vPrimary)
                }
            }
        }

        if (totalCards == 0) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = glassWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("কিছু শনাক্ত করা যায়নি", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "আরো পরিষ্কারভাবে, কাছে গিয়ে পড়ুন — নয়তো একবার আবার রেকর্ড করুন।",
                        color = vTextSoft, fontSize = 13.sp, textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ─── Transcription card (web parity) ───
        a?.transcription?.let { t ->
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = glassWhite),
                modifier = Modifier.fillMaxWidth().staggerEntrance(1)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("মূল ট্রান্সক্রিপশন", color = vTextSoft, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "\"$t\"",
                        color = Color.White, fontSize = 15.sp, lineHeight = 26.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(12.dp)
                    )
                }
            }
        }

        // ─── General conversation cards ───
        a?.generals?.forEach { g ->
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = glassWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        ("শনাক্ত কথোপকথন: " + (g.type ?: "")),
                        color = vTextSoft, fontSize = 12.sp, fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(g.response ?: "", color = Color.White, fontSize = 15.sp, lineHeight = 25.sp)
                    g.note?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = vTextSoft, fontSize = 11.sp)
                    }
                }
            }
        }

        // ─── Quran cards: arabic + pronunciation + meaning + accuracy + tajweed ───
        a?.qurans?.forEachIndexed { i, q ->
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = glassWhite),
                modifier = Modifier.fillMaxWidth().staggerEntrance(i + 2)
            ) {
                Column {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(vPrimary.copy(alpha = 0.35f))
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            q.arabic ?: "",
                            color = Color.White, fontSize = 30.sp, lineHeight = 48.sp,
                            textAlign = TextAlign.Center
                        )
                        if (!q.pronunciationBn.isNullOrBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Text("[ ${q.pronunciationBn} ]", color = vTextSoft, fontSize = 13.sp,
                                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${q.surahNameBn ?: ""} (আয়াত ${q.ayahNumber ?: "?"})",
                            color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp,
                            textAlign = TextAlign.Center
                        )
                        // accuracy badge — the tajweed score (web: accuracy field)
                        q.accuracy?.let { acc ->
                            Spacer(Modifier.height(10.dp))
                            val accColor = when {
                                acc >= 80 -> Color(0xFF4ADE80)
                                acc >= 60 -> vGoldLight
                                else -> Color(0xFFF87171)
                            }
                            Text(
                                "তেলাওয়াতের শুদ্ধতা: $acc%",
                                color = accColor,
                                fontWeight = FontWeight.Black, fontSize = 14.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(accColor.copy(alpha = 0.15f))
                                    .border(1.dp, accColor.copy(alpha = 0.4f), RoundedCornerShape(50))
                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "অর্থ: ${q.meaning ?: ""}",
                            color = Color.White, fontSize = 15.sp, lineHeight = 25.sp
                        )
                        q.explanation?.let { exp ->
                            Spacer(Modifier.height(12.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                                    .padding(14.dp)
                            ) {
                                Text("তাজবীদ ও শান-এ-নুজুল", color = vGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                Spacer(Modifier.height(6.dp))
                                Text(exp, color = Color.White.copy(alpha = 0.88f), fontSize = 13.sp, lineHeight = 23.sp)
                            }
                        }
                    }
                }
            }
        }

        // ─── Hadith cards (web parity: purple top border + grade color) ───
        a?.hadiths?.forEach { h ->
            val gradeColor = when {
                h.grade?.contains("সহিহ") == true -> Color(0xFF4ADE80)
                h.grade?.contains("দঈফ") == true -> Color(0xFFFBBF24)
                else -> Color(0xFFF87171)
            }
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = glassWhite),
                modifier = Modifier.fillMaxWidth().border(5.dp, vPurple.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📜 হাদিস শনাক্ত", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(vPurple.copy(alpha = 0.25f))
                                .padding(horizontal = 12.dp, vertical = 5.dp))
                        h.grade?.let {
                            Text(it, color = gradeColor, fontSize = 11.sp, fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(gradeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 5.dp))
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(h.arabic ?: "", color = Color.White, fontSize = 23.sp, lineHeight = 38.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    if (!h.pronunciationBn.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text("[ ${h.pronunciationBn} ]", color = vTextSoft, fontSize = 12.sp,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("অর্থ: ${h.meaning ?: ""}", color = Color.White, fontSize = 15.sp, lineHeight = 25.sp)
                    h.explanation?.let {
                        Spacer(Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(14.dp)
                        ) {
                            Text(it, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, lineHeight = 23.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "উৎস: ${h.source ?: "?"}" + (h.aiScore?.let { "  |  AI স্কোর: $it/10" } ?: ""),
                                color = vPurple, fontSize = 11.sp, fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // ─── Dua cards (web parity: blue accent) ───
        a?.duas?.forEach { d ->
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = glassWhite),
                modifier = Modifier.fillMaxWidth().border(5.dp, vBlue.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(d.name ?: "দোয়া", color = vBlue, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(d.arabic ?: "", color = Color.White, fontSize = 25.sp, lineHeight = 40.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    if (!d.pronunciationBn.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("[ ${d.pronunciationBn} ]", color = vTextSoft, fontSize = 12.sp,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("অর্থ: ${d.meaning ?: ""}", color = Color.White, fontSize = 15.sp, lineHeight = 25.sp)
                    d.explanation?.let {
                        Spacer(Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(vBlue.copy(alpha = 0.1f))
                                .padding(14.dp)
                        ) {
                            Text("ফজিলত ও গুরুত্ব", color = vBlue, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(6.dp))
                            Text(it, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, lineHeight = 23.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Button(
            onClick = onReRecord,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = vPrimary)
        ) {
            Text("নতুন রেকর্ড শুরু করুন", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun CountPill(emoji: String, count: Int, label: String, bg: Color) {
    if (count > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(bg.copy(alpha = 0.5f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text("$emoji ${count}টি $label", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
