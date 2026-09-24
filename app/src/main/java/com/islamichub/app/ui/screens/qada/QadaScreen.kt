package com.islamichub.app.ui.screens.qada

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.QadaPeriod
import com.islamichub.app.data.repo.QadaPeriodStat
import com.islamichub.app.ui.theme.AppColors
import com.islamichub.app.ui.theme.AppRadius
import com.islamichub.app.ui.theme.AppSpacing
import com.islamichub.app.ui.theme.PremiumProgressBar
import com.islamichub.app.ui.theme.premiumTap
import com.islamichub.app.ui.theme.staggerEntrance
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Settle color-shift (outstanding decreases → red → amber → green)
private val QadaSettleRed = Color(0xFFC62828)
private val QadaSettleAmber = Color(0xFFFFC107)
private val QadaSettleGreen = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QadaScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { QadaViewModel(container) }
    val state by vm.state.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // NEW: date-picker visibility + selected stat detail sheet
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedStat by remember { mutableStateOf<QadaPeriodStat?>(null) }

    // NEW: settle progress baseline — the highest outstanding total seen this
    // session; progress = how much of it has been settled (1 - outstanding/initial).
    var initialOutstanding by remember { mutableStateOf(0) }
    val outstandingTotal = state.summary.total
    if (outstandingTotal > initialOutstanding) initialOutstanding = outstandingTotal
    val settleProgress by animateFloatAsState(
        targetValue = if (initialOutstanding > 0) {
            (1f - outstandingTotal.toFloat() / initialOutstanding).coerceIn(0f, 1f)
        } else 0f,
        animationSpec = tween(700),
        label = "qadaSettleProgress"
    )
    val settleColor = if (settleProgress <= 0.5f) {
        lerp(QadaSettleRed, QadaSettleAmber, settleProgress * 2f)
    } else {
        lerp(QadaSettleAmber, QadaSettleGreen, (settleProgress - 0.5f) * 2f)
    }

    // NEW: Bangla label for the selected log date ("আজ" for today)
    val logDateLabel = remember(state.selectedLogDate) {
        banglaDateLabel(state.selectedLogDate)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.qada_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Reset")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Premium hero — outstanding count with red→amber→green settle tint
            item {
                com.islamichub.app.ui.components.PremiumHeroCard(
                    backgroundImage = "salah-premium-bg.webp",
                    context = context,
                    height = 160
                ) {
                    // NEW: color-shift overlay over the hero image
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(settleColor.copy(alpha = 0.22f))
                    )
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${state.summary.total}", style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text(stringResource(R.string.qada_subtitle), style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f))
                        PremiumProgressBar(
                            progress = settleProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = AppSpacing.sm),
                            trackColor = Color.White.copy(alpha = 0.35f),
                            fillColor = settleColor,
                            barHeight = 8.dp
                        )
                    }
                }
            }

            // NEW: selected log-date chip (opens Material3 DatePickerDialog)
            item {
                val isToday = state.selectedLogDate == QadaUiState.today()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .premiumTap { showDatePicker = true },
                        shape = RoundedCornerShape(AppRadius.md),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    "কাযা লগের তারিখ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    logDateLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    if (!isToday) {
                        TextButton(onClick = { vm.selectLogDate(QadaUiState.today()) }) {
                            Text("আজ")
                        }
                    }
                }
            }

            val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
            val prayerBn = listOf("ফজর", "যোহর", "আসর", "মাগরিব", "এশা")
            val counts = listOf(
                state.summary.fajr, state.summary.dhuhr, state.summary.asr,
                state.summary.maghrib, state.summary.isha
            )

            items(prayers.size) { idx ->
                QadaPrayerRow(
                    prayerEn = prayers[idx],
                    prayerBn = prayerBn[idx],
                    remaining = counts[idx],
                    modifier = Modifier.staggerEntrance(idx),
                    onAddMissed = { vm.addMissed(prayers[idx], 1, state.selectedLogDate) },
                    onMarkCompleted = { vm.markCompleted(prayers[idx]) }
                )
            }

            // NEW: history dashboard — period tabs + prayer filter chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Text(
                        "ইতিহাস ড্যাশবোর্ড",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = AppSpacing.sm)
                    )
                    val periods = listOf(QadaPeriod.DAY, QadaPeriod.WEEK, QadaPeriod.MONTH, QadaPeriod.YEAR)
                    val periodNames = listOf("দিন", "সপ্তাহ", "মাস", "বছর")
                    TabRow(selectedTabIndex = periods.indexOf(state.selectedPeriod).coerceAtLeast(0)) {
                        periods.forEachIndexed { index, period ->
                            Tab(
                                selected = state.selectedPeriod == period,
                                onClick = { vm.selectPeriod(period) },
                                text = { Text(periodNames[index]) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        val filters = listOf<Pair<String?, String>>(
                            null to "সব",
                            "Fajr" to "ফজর", "Dhuhr" to "যোহর", "Asr" to "আসর",
                            "Maghrib" to "মাগরিব", "Isha" to "এশা"
                        )
                        filters.forEach { (prayerKey, chipLabel) ->
                            FilterChip(
                                selected = state.selectedPrayerFilter == prayerKey,
                                onClick = { vm.selectPrayerFilter(prayerKey) },
                                label = { Text(chipLabel) }
                            )
                        }
                    }
                    if (state.periodStats.isEmpty()) {
                        Text(
                            "এই সময়কালে কোনো রেকর্ড নেই।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // NEW: period stat rows — stacked missed/completed bars
            itemsIndexed(state.periodStats) { index, stat ->
                QadaStatRow(
                    stat = stat,
                    modifier = Modifier.staggerEntrance(index),
                    onClick = { selectedStat = stat }
                )
            }

            item {
                Text(
                    text = stringResource(R.string.qada_no_entries).take(60) + "…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

    // NEW: Material3 DatePickerDialog — past dates only (future rejected)
    if (showDatePicker) {
        val todayIso = remember { todayIso() }
        val maxMillis = remember { isoToUtcMillis(todayIso) ?: System.currentTimeMillis() }
        val seedMillis = remember(state.selectedLogDate) {
            isoToUtcMillis(state.selectedLogDate) ?: maxMillis
        }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = seedMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= maxMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val pickedIso = pickerState.selectedDateMillis?.let { millisToIso(it) }
                    // ISO "yyyy-MM-dd" strings compare chronologically → future dates rejected
                    if (pickedIso != null && pickedIso <= todayIso) {
                        vm.selectLogDate(pickedIso)
                    }
                    // ভবিষ্যতের/অবৈধ তারিখ হলে নীরবে বাতিল — আগের নির্বাচনই থাকে
                    showDatePicker = false
                }) { Text("ঠিক আছে") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("বাতিল") }
            }
        ) {
            DatePicker(
                state = pickerState,
                title = {
                    Text(
                        "তারিখ নির্বাচন করুন",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = AppSpacing.xl, top = AppSpacing.lg)
                    )
                }
            )
        }
    }

    // NEW: stat detail bottom sheet (entriesOn is not exposed on the ViewModel,
    // so per spec this shows the period stat breakdown instead)
    val detailStat = selectedStat
    if (detailStat != null) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { selectedStat = null },
            sheetState = sheetState
        ) {
            QadaStatDetailContent(stat = detailStat)
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.qada_reset_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.reset()
                    showResetDialog = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun QadaPrayerRow(
    prayerEn: String,
    prayerBn: String,
    remaining: Int,
    modifier: Modifier = Modifier,
    onAddMissed: () -> Unit,
    onMarkCompleted: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = prayerBn.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prayerEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.qada_remaining, remaining),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remaining > 0) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                )
            }
            FilledTonalIconButton(onClick = onAddMissed) {
                Icon(Icons.Filled.Add, contentDescription = "Add missed")
            }
            FilledTonalIconButton(onClick = onMarkCompleted) {
                Icon(Icons.Filled.Check, contentDescription = "Mark completed")
            }
        }
    }
}

/** One history-dashboard row: label + stacked missed/completed bar + counts. */
@Composable
private fun QadaStatRow(
    stat: QadaPeriodStat,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .premiumTap(onClick = onClick),
        shape = RoundedCornerShape(AppRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stat.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "মিস ${stat.missed} • আদায় ${stat.completed}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StackedBarRow(
                segments = listOf(
                    AppColors.error to stat.missed.toFloat(),      // মিস — লাল
                    AppColors.success to stat.completed.toFloat()  // আদায় — সবুজ
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = if (stat.outstanding > 0) "বাকি: ${stat.outstanding}" else "পূর্ণ আদায় হয়েছে ✓",
                style = MaterialTheme.typography.bodySmall,
                color = if (stat.outstanding > 0) AppColors.error else AppColors.success
            )
        }
    }
}

/** Detail sheet for one stat bucket — stat breakdown (label, bar, counts). */
@Composable
private fun QadaStatDetailContent(stat: QadaPeriodStat) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.xl)
            .padding(bottom = AppSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Text(
            "বিস্তারিত",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            stat.label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        StackedBarRow(
            segments = listOf(
                AppColors.error to stat.missed.toFloat(),
                AppColors.success to stat.completed.toFloat()
            ),
            modifier = Modifier.fillMaxWidth(),
            barHeight = 14.dp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            QadaCountTile("মিস", stat.missed, AppColors.error, Modifier.weight(1f))
            QadaCountTile("আদায়", stat.completed, AppColors.success, Modifier.weight(1f))
            QadaCountTile("বাকি", stat.outstanding, AppColors.warning, Modifier.weight(1f))
        }
        Text(
            "এই সময়কালে লগ করা মোট কাযা ও আদায়ের সারসংক্ষেপ।",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QadaCountTile(
    labelTxt: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.sm))
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "$value",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            labelTxt,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Horizontal stacked bar from (color, value) segments; weights by value. */
@Composable
private fun StackedBarRow(
    segments: List<Pair<Color, Float>>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 10.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val total = segments.sumOf { it.second.toDouble() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .clip(RoundedCornerShape(barHeight / 2))
            .background(trackColor)
    ) {
        if (total > 0.0) {
            segments.forEach { (color, value) ->
                if (value > 0f) {
                    Box(
                        modifier = Modifier
                            .weight((value / total).toFloat())
                            .fillMaxHeight()
                            .background(color)
                    )
                }
            }
        }
    }
}

// ─── Date helpers ─────────────────────────────────────────────────────────────
// NOTE: millis↔date conversions use Calendar/SimpleDateFormat because the app
// has minSdk 24 WITHOUT coreLibraryDesugaring — java.time would crash on
// API 24/25 (and NoClassDefFoundError is not an Exception). java.time is only
// used for the Bangla label inside banglaDateLabel(), guarded with Throwable.

/** Local "yyyy-MM-dd" for today (same format as QadaUiState.today()). */
private fun todayIso(): String = try {
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
} catch (_: Exception) {
    ""
}

/** DatePicker (UTC midnight) millis → "yyyy-MM-dd", null on failure. */
private fun millisToIso(millis: Long): String? = try {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    sdf.format(Date(millis))
} catch (_: Exception) {
    null
}

/** "yyyy-MM-dd" → UTC-normalized millis for the DatePicker, null on failure. */
private fun isoToUtcMillis(iso: String): Long? = try {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    sdf.parse(iso)?.time
} catch (_: Exception) {
    null
}

/** "yyyy-MM-dd" → Bangla "d MMMM yyyy"; "আজ" for today/blank; raw ISO as last resort. */
private fun banglaDateLabel(isoDate: String): String {
    val todayIsoStr = todayIso()
    if (isoDate.isBlank() || isoDate == todayIsoStr) return "আজ"
    return try {
        // java.time per spec — Throwable (not Exception) so missing classes on
        // API < 26 degrade to the raw ISO string instead of crashing.
        val date = LocalDate.parse(isoDate)
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("bn"))
        formatter.format(date)
    } catch (_: Throwable) {
        isoDate
    }
}
