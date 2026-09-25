package com.islamichub.app.ui.screens.fasting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.components.PremiumHeroCard
import com.islamichub.app.ui.theme.AppColors
import com.islamichub.app.ui.theme.AppRadius
import com.islamichub.app.ui.theme.AppSpacing
import com.islamichub.app.ui.theme.PremiumCountUpText
import com.islamichub.app.ui.theme.premiumTap
import com.islamichub.app.ui.theme.staggerEntrance
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Stacked-bar color per fast type (others → gray). */
private fun fastTypeColor(type: FastType): Color = when (type) {
    FastType.RAMADAN -> AppColors.brandSecondary   // রমজান — সোনালি 0xFFC9A34E
    FastType.NAFL -> AppColors.info                // নফল — নীল 0xFF1565C0
    FastType.QADA -> AppColors.error               // কাযা — লাল 0xFFC62828
    FastType.SUNNAH -> AppColors.success           // সুন্নত — সবুজ 0xFF2E7D32
    else -> Color(0xFF9E9E9E)                      // অন্যান্য — ধূসর
}

/** Stable display order for the type segments of the history bars. */
private val fastBarOrder = listOf(FastType.RAMADAN, FastType.QADA, FastType.SUNNAH, FastType.NAFL)

private fun fastSegments(byType: Map<FastType, Int>): List<Pair<FastType, Int>> {
    val ordered = fastBarOrder.mapNotNull { type ->
        byType[type]?.takeIf { it > 0 }?.let { type to it }
    }
    val rest = byType.entries
        .filter { it.key !in fastBarOrder && it.value > 0 }
        .map { it.key to it.value }
    return ordered + rest
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastingScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { FastingViewModel(container) }
    val state by vm.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    if (state.showAddSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { vm.hideAddSheet() },
            sheetState = sheetState
        ) {
            AddFastSheet(
                selectedType = state.selectedType,
                selectedLogDate = state.selectedLogDate,
                onSelect = vm::selectType,
                onSelectLogDate = vm::selectLogDate,
                onConfirm = vm::addTodayFast,
                onDismiss = vm::hideAddSheet
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                com.islamichub.app.ui.components.PremiumDialogIcon(
                    icon = androidx.compose.material.icons.Icons.Filled.Delete,
                    tint = androidx.compose.ui.graphics.Color(0xFFC62828)
                )
            },
            title = { Text("সব ডেটা মুছবেন?") },
            text = { Text("এটি সমস্ত রোজার রেকর্ড মুছে ফেলবে। এটি পূর্বাবস্থায় ফেরানো যাবে না।") },
            confirmButton = {
                TextButton(onClick = {
                    vm.resetAll()
                    showResetDialog = false
                }) { Text("মুছুন", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("বাতিল") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("রোজা ট্র্যাকার", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = vm::showAddSheet,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Fast")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Premium hero
            item {
                PremiumHeroCard(
                    backgroundImage = "salah-premium-bg.webp",
                    context = context,
                    height = 160
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("রোজা ট্র্যাকার",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("রমজান, সুন্নত ও নফল রোজার হিসাব",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }

            // Today status card
            item {
                TodayFastCard(
                    todayFast = state.todayFast,
                    onQuickAdd = vm::addFastToday
                )
            }

            // Streak banner
            item {
                StreakBanner(
                    currentStreak = state.stats.currentStreak,
                    longestStreak = state.stats.longestStreak
                )
            }

            // Stats grid
            item {
                Text("পরিসংখ্যান",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp))
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        StatRow("মোট রোজা", state.stats.totalFasts.toString(), Color(0xFF1B5E20)),
                        StatRow("রমজান", state.stats.ramadanFasts.toString(), Color(0xFFD84315)),
                        StatRow("সুন্নত ও নফল", state.stats.allNafl.toString(), Color(0xFF00ACC1)),
                        StatRow("কাযা", state.stats.qadaFasts.toString(), Color(0xFFEF6C00)),
                        StatRow("এ মাসে", state.stats.thisMonthCount.toString(), Color(0xFF7E57C2))
                    ).forEachIndexed { index, row ->
                        StatCardMini(row, modifier = Modifier.staggerEntrance(index))
                    }
                }
            }

            // NEW: history dashboard — period tabs + type-stacked bars
            item {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Text("রোজার ইতিহাস",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp))
                    val periods = listOf(FastPeriod.DAY, FastPeriod.WEEK, FastPeriod.MONTH, FastPeriod.YEAR)
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
                    if (state.periodStats.isEmpty()) {
                        Text("এই সময়কালে কোনো রোজার রেকর্ড নেই।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            itemsIndexed(state.periodStats) { index, stat ->
                FastHistoryRow(stat = stat, modifier = Modifier.staggerEntrance(index))
            }

            // Recent entries
            item {
                Text("সাম্প্রতিক রোজা",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp))
            }
            if (state.recentEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Text("এখনও কোনো রোজা যোগ করা হয়নি। + বোতাম চাপুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                itemsIndexed(state.recentEntries) { index, entry ->
                    FastEntryRow(
                        entry = entry,
                        modifier = Modifier.staggerEntrance(index)
                    ) { vm.removeFast(entry.date, entry.type) }
                }
            }

            // Quick add — recommended fasts
            item {
                Text("প্রস্তাবিত নফল রোজা",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp))
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickAddRow("আইয়ামে বিজ (১৩, ১৪, ১৫ তারিখ)", "সুন্নত মুয়াক্কাদা", Color(0xFF00ACC1), Modifier.staggerEntrance(0)) {
                        vm.addFastToday(FastType.SUNNAH)
                    }
                    QuickAddRow("সোম ও বৃহস্পতিবার", "প্রিয় দিন", Color(0xFF3949AB), Modifier.staggerEntrance(1)) {
                        vm.addFastToday(FastType.MON_THU)
                    }
                    QuickAddRow("শাওয়ালের ৬টি রোজা", "রমজানের ৩০ = সারাজীবন", Color(0xFF2E7D32), Modifier.staggerEntrance(2)) {
                        vm.addFastToday(FastType.SHAWWAL)
                    }
                    QuickAddRow("আশুরা (১০ মুহররম)", "বড় ফজিলত", Color(0xFF8D6E63), Modifier.staggerEntrance(3)) {
                        vm.addFastToday(FastType.ASHURA)
                    }
                    QuickAddRow("আরাফাহ (৯ যিলহজ)", "গত বছরের-আগামী বছরের গুনাহ মাফ", Color(0xFF7E8CE0), Modifier.staggerEntrance(4)) {
                        vm.addFastToday(FastType.ARAFAH)
                    }
                }
            }

            // spacer for FAB
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun TodayFastCard(
    todayFast: FastType?,
    onQuickAdd: (FastType) -> Unit
) {
    val today = SimpleDateFormat("EEEE, d MMMM", Locale("bn", "BD")).format(Date())
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (todayFast != null) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = if (todayFast != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("  আজকের রোজা",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (todayFast != null) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(today,
                style = MaterialTheme.typography.bodySmall,
                color = if (todayFast != null) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (todayFast != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                        Text("  ${todayFast.bangla} ✓",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White)
                    }
                } else {
                    Text("আজ রোজা রাখেননি",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }
            if (todayFast == null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onQuickAdd(FastType.NAFL) },
                        modifier = Modifier.weight(1f)
                    ) { Text("নফল রোজা") }
                    Button(
                        onClick = { onQuickAdd(FastType.SUNNAH) },
                        modifier = Modifier.weight(1f)
                    ) { Text("সুন্নত") }
                }
            }
        }
    }
}

@Composable
private fun StreakBanner(currentStreak: Int, longestStreak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFF6B35), Color(0xFFFF8C42))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.size(6.dp))
                        PremiumCountUpText(
                            targetValue = currentStreak.toFloat(),
                            format = { it.toInt().toString() },
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Text(" দিন",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text("বর্তমান স্ট্রিক",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.TrendingUp, contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.size(6.dp))
                        PremiumCountUpText(
                            targetValue = longestStreak.toFloat(),
                            format = { it.toInt().toString() },
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(" দিন",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Text("সর্বোচ্চ রেকর্ড",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f))
                }
            }
        }
    }
}

private data class StatRow(val label: String, val value: String, val color: Color)

@Composable
private fun StatCardMini(row: StatRow, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = row.color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(row.label, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(row.color)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(row.value, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/** One history-dashboard row: label + type-stacked bar + type legend. */
@Composable
private fun FastHistoryRow(stat: FastPeriodStat, modifier: Modifier = Modifier) {
    val segments = remember(stat.periodKey) { fastSegments(stat.byType) }
    val legend = remember(stat.periodKey) {
        segments.joinToString(" • ") { "${it.first.bangla} ${it.second}" }
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stat.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text("রোজা ${stat.count} টি",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StackedBarRow(
                segments = segments.map { fastTypeColor(it.first) to it.second.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
            if (legend.isNotBlank()) {
                Text(legend,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FastEntryRow(entry: FastEntry, modifier: Modifier = Modifier, onRemove: () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(entry.type.bangla,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
                Text(entry.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun QuickAddRow(
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .premiumTap(onClick = onClick),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Brightness4, contentDescription = null,
                        tint = color, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFastSheet(
    selectedType: FastType,
    selectedLogDate: String,
    onSelect: (FastType) -> Unit,
    onSelectLogDate: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateLabel = remember(selectedLogDate) { banglaDateLabel(selectedLogDate) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("রোজার ধরন নির্বাচন করুন",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)

        // NEW: log-date selector — lets users record a fast on a past date
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .premiumTap { showDatePicker = true },
            shape = RoundedCornerShape(AppRadius.sm),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("রোজার তারিখ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(dateLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Text("পরিবর্তন",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
        Text("অতীতের কোনো দিনের রোজা লিখতে তারিখে চাপ দিন।",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        FastType.values().forEach { type ->
            val isSelected = type == selectedType
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .premiumTap { onSelect(type) }
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .border(
                                2.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) Icon(Icons.Filled.Check, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    Column {
                        Text(type.bangla, style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium)
                        Text(type.label, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("যোগ করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }

    // NEW: Material3 DatePickerDialog — past dates only (future rejected silently)
    if (showDatePicker) {
        val todayIso = remember { todayIso() }
        val maxMillis = remember { isoToUtcMillis(todayIso) ?: System.currentTimeMillis() }
        val seedMillis = remember(selectedLogDate) {
            isoToUtcMillis(selectedLogDate) ?: maxMillis
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
                        onSelectLogDate(pickedIso)
                    }
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
                        "রোজার তারিখ",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = AppSpacing.xl, top = AppSpacing.lg)
                    )
                }
            )
        }
    }
}

// ─── Date helpers ─────────────────────────────────────────────────────────────
// NOTE: millis↔date conversions use SimpleDateFormat because the app has
// minSdk 24 WITHOUT coreLibraryDesugaring — java.time would crash on API 24/25
// (and NoClassDefFoundError is not an Exception). java.time is only used for
// the Bangla label inside banglaDateLabel(), guarded with Throwable.

/** Local "yyyy-MM-dd" for today (same format as FastingViewModel's dates). */
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
