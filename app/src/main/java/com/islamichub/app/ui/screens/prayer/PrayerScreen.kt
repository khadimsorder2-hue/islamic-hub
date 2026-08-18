package com.islamichub.app.ui.screens.prayer

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.islamichub.app.ui.components.loadAssetImage
import androidx.compose.ui.graphics.asImageBitmap
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

@Composable
fun PrayerScreen(container: AppContainer) {
    val vm = remember { PrayerViewModel(container) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val jamatTimes by remember { container.jamatTimeRepository.jamatTimes }.collectAsState(initial = emptyList())
    var showJamatDialog by remember { mutableStateOf(false) }
    var editingJamat by remember { mutableStateOf<JamatTime?>(null) }
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }

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

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Premium hero with bg image (kept per user request)
        item {
            val bgBitmap = remember { loadAssetImage(context, "img/prayer-premium-bg.webp") }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(28.dp))
            ) {
                if (bgBitmap != null) {
                    Image(
                        bitmap = bgBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.7f))
                            )
                        )
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ))
                }
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.prayer_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
                        Text(
                            text = state.times?.locationName ?: locationFallback,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.95f)
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

        // Adhan sound player
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .clickable {
                        try {
                            exoPlayer.stop()
                            exoPlayer.clearMediaItems()
                            val mediaItem = MediaItem.fromUri("asset:///namaz_audio/azan2.mp3")
                            exoPlayer.setMediaItem(mediaItem)
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        } catch (_: Exception) { }
                    },
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
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
                    items(rows, key = { it.first }) { row ->
                        val jamat = jamatTimes.firstOrNull { it.prayerName == row.first }
                        PrayerRowPremium(row.first, row.second, row.third, jamat, context)
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
            }
        )
    }
}

@Composable
private fun PrayerRowPremium(
    name: String, time: String, isFard: Boolean,
    jamat: JamatTime?, context: android.content.Context
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
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: emoji + name
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
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (jamat != null && jamat.enabled) {
                            Text(
                                text = "🕌 জামাত: ${jamat.jamatTime}",
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
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onEdit),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                Text("জামাত: ${jamat.jamatTime}", style = MaterialTheme.typography.bodyMedium,
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
    onSave: (JamatTime) -> Unit
) {
    var prayerName by remember { mutableStateOf(existing?.prayerName ?: "ফজর") }
    var jamatTime by remember { mutableStateOf(existing?.jamatTime ?: "") }
    var mosqueName by remember { mutableStateOf(existing?.mosqueName ?: "") }
    var enabled by remember { mutableStateOf(existing?.enabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}
