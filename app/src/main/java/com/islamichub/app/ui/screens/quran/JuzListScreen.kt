package com.islamichub.app.ui.screens.quran

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.theme.AppColors
import com.islamichub.app.ui.theme.AppRadius
import com.islamichub.app.ui.theme.AppSpacing

/** 30 Juz of the Quran with surah ranges */
@Composable
fun JuzListScreen(
    container: AppContainer,
    onSurahClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val juzData = remember { JuzData.all }
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(color = MaterialTheme.colorScheme.primaryContainer) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(AppSpacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("←", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onBack))
                Spacer(Modifier.weight(1f))
                Text("৩০ পারা (জুজ)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.weight(1f))
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            items(juzData) { juz ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSurahClick(juz.startSurah) },
                    shape = RoundedCornerShape(AppRadius.md),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(AppSpacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(AppRadius.xs),
                            color = AppColors.brandPrimary
                        ) {
                            Text("${juz.number}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(Modifier.width(AppSpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("পারা ${juz.number}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${juz.startSurahName} — ${juz.endSurahName}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

data class JuzInfo(val number: Int, val startSurah: Int, val endSurah: Int, val startSurahName: String, val endSurahName: String, val startAyah: Int, val endAyah: Int)

object JuzData {
    val all = listOf(
        JuzInfo(1,1,1,"আল-ফাতিহা","আল-ফাতিহা",1,7), JuzInfo(2,2,2,"আল-বাকারা","আল-বাকারা",1,141),
        JuzInfo(3,2,3,"আল-বাকারা","আলে ইমরান",142,200), JuzInfo(4,3,4,"আলে ইমরান","আন-নিসা",1,176),
        JuzInfo(5,4,5,"আন-নিসা","আল-মায়েদাহ",1,120), JuzInfo(6,5,6,"আল-মায়েদাহ","আল-আনআম",1,111),
        JuzInfo(7,7,8,"আল-আরাফ","আল-আনফাল",1,87), JuzInfo(8,8,9,"আল-আনফাল","আত-তাওবা",1,75),
        JuzInfo(9,9,10,"আত-তাওবা","ইউনুস",1,92), JuzInfo(10,10,11,"ইউনুস","হুদ","1,123),
        JuzInfo(11,11,12,"হুদ","ইউসুফ",1,111), JuzInfo(12,12,13,"ইউসুফ","রাদ","1,52),
        JuzInfo(13,13,14,"রাদ","ইবরাহীম",1,52), JuzInfo(14,15,16,"আল-হিজর","আন-নাহল",1,128),
        JuzInfo(15,16,17,"আন-নাহল","আল-ইসরা",1,111), JuzInfo(16,17,18,"আল-ইসরা","আল-কাহফ",1,110),
        JuzInfo(17,18,19,"আল-কাহফ","মারিয়াম",1,98), JuzInfo(18,19,20,"মারিয়াম","ত্বা-হা",1,135),
        JuzInfo(19,20,21,"ত্বা-হা","আল-আনকাবুত",1,112), JuzInfo(20,21,22,"আল-আনকাবুত","আল-হজ্জ",1,78),
        JuzInfo(21,22,23,"আল-হজ্জ","আল-মুমতাহিনা",1,118), JuzInfo(22,23,24,"আল-মুমতাহিনা","আন-নূর",1,77),
        JuzInfo(23,24,25,"আন-নূর","আল-ফুরকান",1,96), JuzInfo(24,25,26,"আল-ফুরকান","আশ-শুআরা",1,75),
        JuzInfo(25,26,27,"আশ-শুআরা","আর-রাহমান",1,93), JuzInfo(26,27,28,"আর-রাহমান","আন-কাবুত",1,88),
        JuzInfo(27,28,29,"আন-কাবুত","আল-বায়্‌ইনাহ",1,82), JuzInfo(28,29,30,"আল-বায়্‌ইনাহ","আন-নাসর",1,60),
        JuzInfo(29,30,30,"আন-নাসর","আন-নাসর",60,130), JuzInfo(30,30,30,"আন-নাসর","আন-নাসর",131,226)
    )
}
