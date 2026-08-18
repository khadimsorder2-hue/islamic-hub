package com.islamichub.app.ui.screens.profile

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.components.PremiumHeroCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { ProfileViewModel(container) }
    val state by vm.state.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(state.userName) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.profile_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT,
                                "🌙 Islamic Hub প্রোগ্রেস\n\n" +
                                "নাম: ${state.userName.ifBlank { "ব্যবহারকারী" }}\n\n" +
                                "📊 পরিসংখ্যান:\n" +
                                "• নামাজ স্ট্রিক: ${state.prayerStreak} দিন 🔥\n" +
                                "• তসবিহ: ${state.totalZikr}\n" +
                                "• আয়াত পঠিত: ${state.totalAyahs}\n" +
                                "• হাদিস পঠিত: ${state.totalHadiths}\n" +
                                "• বুকমার্ক: ${state.bookmarkCount}\n" +
                                "• রোজা: ${state.totalFasts}\n" +
                                "• কাযা: ${state.qadaPending}\n\n" +
                                "📖 খতম প্রগ্রেস: ${"%.1f".format(state.khatamPercent)}% (${state.khatamSurahs}/114 সূরা)\n\n" +
                                "আপনিও আপনার ইসলামিক কার্যক্রম ট্র্যাক করুন Islamic Hub অ্যাপে!"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Premium Profile Header ───
            item {
                PremiumHeroCard(
                    backgroundImage = "profile-premium-bg.webp",
                    context = context,
                    height = 200
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.userName.ifBlank { "ব্যবহারকারী" },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = " ${state.prayerStreak} দিন স্ট্রিক",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "🤲 মাশাআল্লাহ, আপনার যাত্রা চলছে",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        IconButton(onClick = {
                            editName = state.userName
                            showEditDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "নাম পরিবর্তন",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // ─── Achievement Level Card ───
            item {
                val level = when {
                    state.prayerStreak >= 30 -> Triple("অর্জনকারী", "🏆", Color(0xFFFFD700))
                    state.prayerStreak >= 14 -> Triple("নিয়মিত", "⭐", Color(0xFF6D45C7))
                    state.prayerStreak >= 7 -> Triple("উন্নতিশীল", "🌱", Color(0xFF2E7D32))
                    state.prayerStreak >= 1 -> Triple("শুরু", "🌿", Color(0xFF00ACC1))
                    else -> Triple("নতুন", "🌙", Color(0xFF7E57C2))
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        level.third,
                                        level.third.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(level.second, style = MaterialTheme.typography.headlineSmall)
                                }
                                Spacer(Modifier.size(12.dp))
                                Column {
                                    Text("লেভেল",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.85f))
                                    Text(level.first,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${state.prayerStreak}",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White)
                                Text("দিন স্ট্রিক",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f))
                            }
                        }
                    }
                }
            }

            // ─── Stats Grid Section Header ───
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text("  পরিসংখ্যান",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
            }

            // ─── Stats Grid (2-column) ───
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatGridCard(
                        icon = Icons.Filled.Spa,
                        value = state.totalZikr.toString(),
                        label = "তসবিহ",
                        color = Color(0xFFB36283),
                        modifier = Modifier.weight(1f)
                    )
                    StatGridCard(
                        icon = Icons.Filled.AutoStories,
                        value = state.totalAyahs.toString(),
                        label = "আয়াত পঠিত",
                        color = Color(0xFF6D45C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatGridCard(
                        icon = Icons.Filled.MenuBook,
                        value = state.totalHadiths.toString(),
                        label = "হাদিস পঠিত",
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.weight(1f)
                    )
                    StatGridCard(
                        icon = Icons.Filled.Bookmark,
                        value = state.bookmarkCount.toString(),
                        label = "বুকমার্ক",
                        color = Color(0xFF3949AB),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatGridCard(
                        icon = Icons.Filled.WbSunny,
                        value = state.totalFasts.toString(),
                        label = "রোজা রাখা",
                        color = Color(0xFFD84315),
                        modifier = Modifier.weight(1f)
                    )
                    StatGridCard(
                        icon = Icons.Filled.History,
                        value = state.qadaPending.toString(),
                        label = "কাযা বাকি",
                        color = Color(0xFFEF6C00),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ─── Quran Khatam Progress ───
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00897B))
                    )
                    Text("  খতম প্রগ্রেস",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF00897B),
                                        Color(0xFF00ACC1)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("পবিত্র কুরআন খতম",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White)
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.AutoStories, contentDescription = null,
                                        tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                            Text("${state.khatamSurahs} / 114 সূরা সম্পন্ন",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.95f))
                            LinearProgressIndicator(
                                progress = { state.khatamPercent / 100f },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.25f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${"%.1f".format(state.khatamPercent)}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White)
                                if (state.khatamPercent >= 100f) {
                                    Text("✓ খতম সম্পন্ন!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700))
                                } else {
                                    Text("${114 - state.khatamSurahs} সূরা বাকি",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.9f))
                                }
                            }
                        }
                    }
                }
            }

            // ─── Achievements ───
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF6B35))
                    )
                    Text("  অর্জন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AchievementCard(
                        icon = Icons.Filled.LocalFireDepartment,
                        title = "স্ট্রিক",
                        value = "${state.prayerStreak}",
                        subtitle = "দিন",
                        unlocked = state.prayerStreak >= 1,
                        color = Color(0xFFFF6B35),
                        modifier = Modifier.weight(1f)
                    )
                    AchievementCard(
                        icon = Icons.Filled.MenuBook,
                        title = "খতম",
                        value = "${state.khatamSurahs}",
                        subtitle = "/ ১১৪",
                        unlocked = state.khatamSurahs >= 1,
                        color = Color(0xFF00897B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AchievementCard(
                        icon = Icons.Filled.Spa,
                        title = "তসবিহ",
                        value = state.totalZikr.toString(),
                        subtitle = "জপ",
                        unlocked = state.totalZikr >= 33,
                        color = Color(0xFFB36283),
                        modifier = Modifier.weight(1f)
                    )
                    AchievementCard(
                        icon = Icons.Filled.AutoStories,
                        title = "আয়াত",
                        value = state.totalAyahs.toString(),
                        subtitle = "পঠিত",
                        unlocked = state.totalAyahs >= 100,
                        color = Color(0xFF6D45C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ─── Backup / Restore ───
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF5C6BC0))
                    )
                    Text("  ব্যাকআপ ও পুনরুদ্ধার",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BackupActionCard(
                        icon = Icons.Filled.CloudUpload,
                        title = "ব্যাকআপ",
                        subtitle = "ক্লাউডে সংরক্ষণ",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f),
                        onClick = { vm.backup() }
                    )
                    BackupActionCard(
                        icon = Icons.Filled.CloudDownload,
                        title = "পুনরুদ্ধার",
                        subtitle = "ক্লাউড থেকে",
                        color = Color(0xFF1565C0),
                        modifier = Modifier.weight(1f),
                        onClick = { vm.restore() }
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.profile_edit_name)) },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.profile_user_name)) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.setUserName(editName)
                    showEditDialog = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.StatGridCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.15f),
                            color.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
                Text(value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color)
                Text(label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.AchievementCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    unlocked: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) color.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (unlocked) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (unlocked) color else Color(0xFF9E9E9E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (unlocked) icon else Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (unlocked) color else Color(0xFF9E9E9E))
            Text(title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (unlocked) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BackupActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Text(title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color)
            Text(subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
