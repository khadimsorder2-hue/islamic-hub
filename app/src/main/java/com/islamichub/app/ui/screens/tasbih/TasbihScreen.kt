package com.islamichub.app.ui.screens.tasbih

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.DhikrOption
import com.islamichub.app.ui.theme.premiumTap
import com.islamichub.app.ui.theme.rememberPremiumHaptic
import com.islamichub.app.ui.theme.staggerEntrance
import com.islamichub.app.ui.theme.toBanglaDigits
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@Composable
fun TasbihScreen(container: AppContainer) {
    val vm = remember { TasbihViewModel(container) }
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showResetAllConfirm by remember { mutableStateOf(false) }
    // v5.11.0 — single reset is destructive too; confirm it like Reset All
    var showResetConfirm by remember { mutableStateOf(false) }
    // v5.11.0 — the most tactile screen in the app had no haptics
    val haptic = rememberPremiumHaptic()

    LaunchedEffect(state.justCompletedRound) {
        if (state.justCompletedRound) {
            haptic(HapticFeedbackType.LongPress)
            snackbarHostState.showSnackbar("এক রাউন্ড সম্পূর্ণ! আলহামদুলিল্লাহ।")
            vm.clearRoundFlag()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // v5.11.0 — edge-to-edge fix
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Premium hero
            com.islamichub.app.ui.components.PremiumHeroCard(
                backgroundImage = "tasbih-bg.webp",
                context = androidx.compose.ui.platform.LocalContext.current,
                height = 120
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.tasbih_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Text(
                        text = "তসবিহ গণনা করুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Dhikr selector
            if (state.dhikrOptions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.dhikrOptions.take(3).forEach { opt ->
                        DhikrChip(
                            option = opt,
                            selected = opt.id == state.currentDhikrId,
                            onClick = { vm.onDhikrChange(opt.id) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.dhikrOptions.drop(3).forEach { opt ->
                        DhikrChip(
                            option = opt,
                            selected = opt.id == state.currentDhikrId,
                            onClick = { vm.onDhikrChange(opt.id) }
                        )
                    }
                }
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth().staggerEntrance(1),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.tasbih_round),
                    value = state.round.toBanglaDigits(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.tasbih_total),
                    value = state.total.toBanglaDigits(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Big tap counter — CENTERED in middle of screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .staggerEntrance(2),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .clickable {
                            // v5.11.0 — haptic tick on every dhikr
                            haptic(HapticFeedbackType.TextHandleMove)
                            vm.onIncrement()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.count.toBanglaDigits(),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = String.format(stringResource(R.string.tasbih_target), state.target).toBanglaDigits(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Premium dhikr details card — AT THE BOTTOM (after counter)
            state.dhikrOptions.find { it.id == state.currentDhikrId }?.let { current ->
                var showDetails by remember(current.id) { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.animateContentSize() // v5.11.0 — expand/collapse animates (was snap)
                        ) {
                            // Header: Arabic + Info icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = current.arabic,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { showDetails = !showDetails }) {
                                    Icon(
                                        imageVector = if (showDetails) Icons.Filled.ExpandLess
                                                      else Icons.Filled.ExpandMore,
                                        contentDescription = "বিস্তারিত",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            // Pronunciation + Bangla translation
                            Text("উচ্চারণ: ${current.banglaPronunciation.ifBlank { current.transliteration }}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text("অর্থ: ${current.banglaTranslation.ifBlank { current.translation }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            // Expandable details
                            if (showDetails) {
                                Spacer(Modifier.height(8.dp))
                                if (current.banglaMeaning.isNotBlank()) {
                                    DhikrDetailRow(label = "📖 অর্থ বিস্তারিত", value = current.banglaMeaning)
                                }
                                if (current.whyRecite.isNotBlank()) {
                                    DhikrDetailRow(label = "🤲 কেন পড়বেন", value = current.whyRecite)
                                }
                                if (current.reward.isNotBlank()) {
                                    DhikrDetailRow(label = "✨ ফজিলত", value = current.reward)
                                }
                                if (current.reference.isNotBlank()) {
                                    DhikrDetailRow(label = "📚 সূত্র", value = current.reference)
                                }
                            }
                        }
                    }
                }
            }

            // Reset buttons — at the very bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.tasbih_reset)) }
                Button(
                    // v5.9.0 — destructive Reset All now asks for confirmation and
                    // reports completion through the (previously unused) snackbar.
                    onClick = { showResetAllConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) { Text("সব রিসেট") }
            }
        }

        if (showResetConfirm) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                icon = {
                    com.islamichub.app.ui.components.PremiumDialogIcon(
                        icon = androidx.compose.material.icons.Icons.Filled.Delete,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("বর্তমান জিকির রিসেট?") },
                text = { Text("এই জিকিরের সংখ্যা শূন্য হয়ে যাবে। রিসেট করবেন?") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        showResetConfirm = false
                        vm.onReset()
                    }) { Text("হ্যাঁ, রিসেট করুন", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showResetConfirm = false }) {
                        Text("না")
                    }
                }
            )
        }

        if (showResetAllConfirm) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showResetAllConfirm = false },
                icon = {
                    com.islamichub.app.ui.components.PremiumDialogIcon(
                        icon = androidx.compose.material.icons.Icons.Filled.Delete,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("সব সংখ্যা রিসেট?") },
                text = { Text("আপনার সব জিকিরের সংখ্যা মুছে যাবে। এটা ফিরিয়ে আনা যাবে না — আপনি কি নিশ্চিত?") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        showResetAllConfirm = false
                        vm.onResetAll()
                    }) { Text("হ্যাঁ, রিসেট করুন", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showResetAllConfirm = false }) {
                        Text("না")
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data -> Snackbar(snackbarData = data) }
    }
}

@Composable
private fun DhikrChip(
    option: DhikrOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .premiumTap(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = option.transliteration,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DhikrDetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
