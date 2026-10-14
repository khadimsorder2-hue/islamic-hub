package com.islamichub.app.ui.screens.prayer

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.JamatTime
import com.islamichub.app.ui.components.PremiumHeroCard
import com.islamichub.app.ui.components.PremiumSectionHeader
import com.islamichub.app.ui.components.rememberAssetBitmap
import com.islamichub.app.ui.theme.PremiumProgressBar
import com.islamichub.app.util.PrayerTimeFormatter
import com.islamichub.app.ui.theme.banglaSp
import com.islamichub.app.ui.theme.staggerEntrance
import androidx.compose.ui.graphics.asImageBitmap
import com.islamichub.app.ui.theme.premiumTap

@Composable
fun PrayerScreen(container: AppContainer) {
    val vm = remember { PrayerViewModel(container) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val jamatTimes by remember { container.jamatTimeRepository.jamatTimes }.collectAsState(initial = emptyList())
    var showJamatDialog by remember { mutableStateOf(false) }
    var editingJamat by remember { mutableStateOf<JamatTime?>(null) }

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants -> if (grants.values.any { it }) vm.load() }

    val locationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants -> if (grants.values.any { it }) vm.load() }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !container.prayerScheduler.hasNotificationPermission()) {
            notifPermLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }
        if (!container.prayerRepository.hasLocationPermission()) {
            locationPermLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    val fajrName = stringResource(R.string.prayer_fajr)
    val sunriseName = stringResource(R.string.prayer_sunrise)
    val dhuhrName = stringResource(R.string.prayer_dhuhr)
    val asrName = stringResource(R.string.prayer_asr)
    val maghribName = stringResource(R.string.prayer_maghrib)
    val ishaName = stringResource(R.string.prayer_isha)
    val locationFallback = stringResource(R.string.prayer_location)
    val loadingText = stringResource(R.string.prayer_loading)
    val errorText = stringResource(R.string.prayer_error)

    // v5.18.0 — Muslimeen-style live countdown: tick every 30s so the hero
    // card and the highlighted next-prayer row stay fresh while on screen.
    var nowTick by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30_000)
            nowTick = System.currentTimeMillis()
        }
    }

    // Next-prayer progress (fraction between previous and next prayer)
    // v5.18.0 — reading nowTick here makes the computation (and the whole
    // hero countdown) re-run on every 30s tick; the tick value itself is
    // only a recomposition key.
    val nextPrayerInfo = nowTick.let {
        state.times?.let { t ->
            computeNextPrayerProgress(t, fajrName, dhuhrName, asrName, maghribName, ishaName)
        }
    }

    // v5.11.0 — status-bar-aware top padding (edge-to-edge fix)
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = statusBarTop + 16.dp, bottom = 16.dp), // v5.17.1 — tight edge-to-edge (no extra spacer)
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Premium hero with bg image (kept per user request)
        item {
            // v5.18.0 — MUSLIMEEN-STYLE HERO: one big gradient card that leads
            // with the NEXT prayer + a live hour/minute countdown (their prayer
            // screen's signature), location + hijri date below. Replaces the
            // image banner + the separate progress card.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0E7C5A),
                                    Color(0xFF14A374),
                                    Color(0xFF1565C0)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(14.dp))
                            Text(
                                text = state.times?.locationName ?: locationFallback,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                        }
                        Text(
                            text = "পরবর্তী নামাজ",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        if (nextPrayerInfo != null) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = nextPrayerInfo.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = PrayerTimeFormatter.to12HourBangla(nextPrayerInfo.time),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            val remain = nextPrayerInfo.remainingMinutes
                            if (remain in 1..(24 * 60)) {
                                val h = remain / 60
                                val m = remain % 60
                                Text(
                                    text = if (h > 0) "বাকি $h ঘণ্টা $m মিনিট" else "বাকি $m মিনিট",
                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = banglaSp(MaterialTheme.typography.titleSmall.fontSize)),
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFFE082)
                                )
                            }
                            PremiumProgressBar(
                                progress = nextPrayerInfo.fraction,
                                modifier = Modifier.fillMaxWidth(),
                                fillColor = Color.White
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.prayer_title),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        state.times?.hijriDate?.let {
                            Text(it,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f))
                        }
                    }
                }
            }
        }

        // Adhan sound player
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .premiumTap(onClick = {
                        // Use shared AudioController so FloatingAudioPlayer shows
                        container.audioController.playAssetAudio(
                            assetPath = "namaz_audio/azan2.mp3",
                            title = "অযান (আজান)",
                            subtitle = "ট্যাপ করে অযান শুনুন"
                        )
                    }),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("অযান (আজান)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("ট্যাপ করে অযান শুনুন", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // Notification status
        if (state.notificationsScheduled) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Column {
                            Text("নামাজের নোটিফিকেশন চালু", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            state.nextNotificationTitle?.let { title ->
                                Text("পরবর্তী: $title", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f))
                            }
                        }
                    }
                }
            }
        }

        // Prayer times
        when {
            state.isLoading -> {
                item { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Text(loadingText, modifier = Modifier.padding(24.dp))
                } }
            }
            else -> {
                val t = state.times
                if (t != null) {
                    val rows = listOf(
                        Triple(fajrName, t.fajr, true),
                        Triple(sunriseName, t.sunrise, false),
                        Triple(dhuhrName, t.dhuhr, true),
                        Triple(asrName, t.asr, true),
                        Triple(maghribName, t.maghrib, true),
                        Triple(ishaName, t.isha, true)
                    )
                    itemsIndexed(rows, key = { _, row -> row.first }) { index, row ->
                        val jamat = jamatTimes.firstOrNull { it.prayerName == row.first }
                        // v5.18.0 — Muslimeen style: the NEXT prayer row is
                        // highlighted with an accent ring + "পরবর্তী" chip
                        PrayerRowPremium(
                            row.first, PrayerTimeFormatter.to12HourBangla(row.second), row.third, jamat, context,
                            isNext = nextPrayerInfo?.name == row.first,
                            modifier = Modifier.staggerEntrance(index)
                        )
                    }
                }
                state.error?.let { err ->
                    item {
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer) {
                            Text("$errorText\n($err)", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
                if (!state.notificationsScheduled && state.times != null) {
                    item {
                        Button(onClick = { vm.scheduleNotifications() }, modifier = Modifier.fillMaxWidth()) {
                            Text("নামাজের নোটিফিকেশন চালু করুন")
                        }
                    }
                }
            }
        }

        // ─── Custom Jamat Times ───
        item { PremiumSectionHeader(title = "জামাতের সময় (কাস্টম)") }
        item {
            OutlinedButton(
                onClick = {
                    editingJamat = null
                    showJamatDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("  জামাতের সময় যোগ করুন", modifier = Modifier.padding(start = 8.dp))
            }
        }
        items(jamatTimes, key = { it.prayerName }) { jamat ->
            JamatTimeCard(jamat) {
                editingJamat = jamat
                showJamatDialog = true
            }
        }
    }

    // Jamat time dialog
    if (showJamatDialog) {
        JamatTimeDialog(
            existing = editingJamat,
            onDismiss = { showJamatDialog = false },
            onSave = { jamat ->
                vm.saveJamatTime(jamat)
                showJamatDialog = false
            },
            onDelete = editingJamat?.let { existing ->
                {
                    vm.deleteJamatTime(existing.prayerName)
                    showJamatDialog = false
                }
            }
        )
    }
}

@Composable
private fun PrayerRowPremium(
    name: String, time: String, isFard: Boolean,
    jamat: JamatTime?, context: android.content.Context,
    isNext: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Minimal compact design — no image, gradient only
    val prayerColor = when (name) {
        "ফজর" -> Color(0xFF7E8CE0)        // dawn — soft blue
        "যোহর" -> Color(0xFFFF9800)       // noon — orange
        "আসর" -> Color(0xFF8BC34A)        // afternoon — green
        "মাগরিব" -> Color(0xFFAB47BC)     // sunset — purple
        "এশা" -> Color(0xFF3F51B5)         // night — deep blue
        else -> MaterialTheme.colorScheme.primary
    }
    val prayerEmoji = when (name) {
        "ফজর" -> "🌅"
        "যোহর" -> "☀️"
        "আসর" -> "🌤️"
        "মাগরিব" -> "🌆"
        "এশা" -> "🌙"
        else -> "🕌"
    }

    Card(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isNext) 4.dp else 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            prayerColor.copy(alpha = if (isFard) 0.15f else 0.08f),
                            prayerColor.copy(alpha = 0.03f)
                        )
                    )
                )
                // v5.18.0 — Muslimeen style: accent ring around the next prayer
                .then(
                    if (isNext) Modifier.border(
                        2.dp, prayerColor, RoundedCornerShape(16.dp)
                    ) else Modifier
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: emoji + name (+ "পরবর্তী" chip when this is next)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(prayerColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(prayerEmoji, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isNext) {
                                Spacer(Modifier.size(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(prayerColor)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "পরবর্তী",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (jamat != null && jamat.enabled) {
                            Text(
                                text = "🕌 জামাত: ${PrayerTimeFormatter.to12HourBangla(jamat.jamatTime)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                // Right: time
                Text(
                    text = time,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = prayerColor
                )
            }
        }
    }
}

@Composable
private fun JamatTimeCard(jamat: JamatTime, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).premiumTap(onClick = onEdit),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🕌", style = MaterialTheme.typography.titleMedium)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(jamat.prayerName, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text("জামাত: ${PrayerTimeFormatter.to12HourBangla(jamat.jamatTime)}", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary)
                if (jamat.mosqueName.isNotBlank()) {
                    Text(jamat.mosqueName, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(if (jamat.enabled) "🔔" else "🔕", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JamatTimeDialog(
    existing: JamatTime?,
    onDismiss: () -> Unit,
    onSave: (JamatTime) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var prayerName by remember { mutableStateOf(existing?.prayerName ?: "ফজর") }
    var jamatTime by remember { mutableStateOf(existing?.jamatTime ?: "") }
    var mosqueName by remember { mutableStateOf(existing?.mosqueName ?: "") }
    var enabled by remember { mutableStateOf(existing?.enabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            com.islamichub.app.ui.components.PremiumDialogIcon(
                icon = Icons.Filled.Groups,
                tint = androidx.compose.ui.graphics.Color(0xFF1565C0)
            )
        },
        title = { Text(if (existing != null) "জামাতের সময় পরিবর্তন" else "জামাতের সময় যোগ করুন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("নামাজ নির্বাচন করুন", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ফজর", "যোহর", "আসর", "মাগরিব", "এশা", "জুমআ").forEach { p ->
                        androidx.compose.material3.FilterChip(
                            selected = prayerName == p,
                            onClick = { prayerName = p },
                            label = { Text(p, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = jamatTime,
                    onValueChange = { jamatTime = it },
                    label = { Text("জামাতের সময় (HH:mm)") },
                    placeholder = { Text("যেমন: ০৫:৩০") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mosqueName,
                    onValueChange = { mosqueName = it },
                    label = { Text("মসজিদের নাম (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Switch(checked = enabled, onCheckedChange = { enabled = it })
                    Text("  নোটিফিকেশন চালু", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (jamatTime.isNotBlank()) {
                    onSave(JamatTime(prayerName, jamatTime, mosqueName, enabled))
                }
            }) { Text("সংরক্ষণ") }
        },
        dismissButton = {
            Row {
                // v5.10.0 — delete wrong jamat entries (removeJamatTime was dead code)
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("মুছুন", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("বাতিল") }
            }
        }
    )
}

private data class NextPrayerProgress(
    val name: String,
    val time: String,
    val fraction: Float,
    /** v5.18.0 — minutes until the next prayer, for the live countdown. */
    val remainingMinutes: Int = 0
)

/**
 * Fraction (0f..1f) of the interval between the previous and the next prayer,
 * for the premium progress bar. Times are expected as "HH:mm" strings and are
 * parsed defensively — returns null when nothing is parseable.
 */
private fun computeNextPrayerProgress(
    times: com.islamichub.app.data.model.PrayerTimes,
    fajrName: String,
    dhuhrName: String,
    asrName: String,
    maghribName: String,
    ishaName: String
): NextPrayerProgress? {
    fun toMinutes(raw: String): Int? {
        return try {
            val parts = raw.split(":")
            val h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return null
            val m = parts.getOrNull(1)?.trim()?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
            if (h in 0..23 && m in 0..59) h * 60 + m else null
        } catch (e: Exception) {
            null
        }
    }

    val anchors = listOf(
        fajrName to times.fajr,
        dhuhrName to times.dhuhr,
        asrName to times.asr,
        maghribName to times.maghrib,
        ishaName to times.isha
    )
    val parsed = anchors.mapNotNull { (name, raw) ->
        toMinutes(raw)?.let { Triple(name, it, raw) }
    }
    if (parsed.isEmpty()) return null
    val nowCal = java.util.Calendar.getInstance()
    val nowMin = nowCal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + nowCal.get(java.util.Calendar.MINUTE)
    val nextIdx = parsed.indexOfFirst { it.second > nowMin }
    return if (nextIdx >= 0) {
        val next = parsed[nextIdx]
        val prevMin = if (nextIdx > 0) parsed[nextIdx - 1].second else parsed.last().second - 24 * 60
        val span = (next.second - prevMin).coerceAtLeast(1)
        NextPrayerProgress(next.first, next.third, ((nowMin - prevMin).toFloat() / span).coerceIn(0f, 1f), next.second - nowMin)
    } else {
        // After Isha — the next prayer is tomorrow's Fajr
        val fajr = parsed.first()
        val isha = parsed.last()
        val span = (fajr.second + 24 * 60 - isha.second).coerceAtLeast(1)
        NextPrayerProgress(fajr.first, fajr.third, ((nowMin - isha.second).toFloat() / span).coerceIn(0f, 1f), fajr.second + 24 * 60 - nowMin)
    }
}
