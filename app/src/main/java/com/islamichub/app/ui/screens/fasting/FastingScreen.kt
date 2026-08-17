package com.islamichub.app.ui.screens.fasting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.components.PremiumHeroCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                onSelect = vm::selectType,
                onConfirm = vm::addTodayFast,
                onDismiss = vm::hideAddSheet
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("সব ডেটা মুছবেন?") },
            text = { Text("এটি সমস্ত রোজার রেকর্ড মুছে ফেলবে। এটি পূর্বাবস্থায় ফেরানো যাবে না।") },
            confirmButton = {
                TextButton(onClick = {
                    vm.resetAll()
                    showResetDialog = false
                }) { Text("মুছুন", color = Color(0xFFC62828)) }
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
                    ).forEach { row ->
                        StatCardMini(row)
                    }
                }
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
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("এখনও কোনো রোজা যোগ করা হয়নি। + বোতাম চাপুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(state.recentEntries) { entry ->
                    FastEntryRow(entry) { vm.removeFast(entry.date, entry.type) }
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
                    QuickAddRow("আইয়ামে বিজ (১৩, ১৪, ১৫ তারিখ)", "সুন্নত মুয়াক্কাদা", Color(0xFF00ACC1)) {
                        vm.addFastToday(FastType.SUNNAH)
                    }
                    QuickAddRow("সোম ও বৃহস্পতিবার", "প্রিয় দিন", Color(0xFF3949AB)) {
                        vm.addFastToday(FastType.MON_THU)
                    }
                    QuickAddRow("শাওয়ালের ৬টি রোজা", "রমজানের ৩০ = সারাজীবন", Color(0xFF2E7D32)) {
                        vm.addFastToday(FastType.SHAWWAL)
                    }
                    QuickAddRow("আশুরা (১০ মুহররম)", "বড় ফজিলত", Color(0xFF8D6E63)) {
                        vm.addFastToday(FastType.ASHURA)
                    }
                    QuickAddRow("আরাফাহ (৯ যিলহজ)", "গত বছরের-আগামী বছরের গুনাহ মাফ", Color(0xFF7E8CE0)) {
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (todayFast != null) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surfaceVariant
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
                        Text("  $currentStreak দিন",
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
                        Text("  $longestStreak দিন",
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
private fun StatCardMini(row: StatRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun FastEntryRow(entry: FastEntry, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
private fun QuickAddRow(title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFastSheet(
    selectedType: FastType,
    onSelect: (FastType) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("রোজার ধরন নির্বাচন করুন",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)

        FastType.values().forEach { type ->
            val isSelected = type == selectedType
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(type) }
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surfaceVariant
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
}
