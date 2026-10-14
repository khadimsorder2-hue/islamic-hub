package com.islamichub.app.ui.screens.alphabet

import android.media.MediaPlayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamichub.app.ui.theme.banglaSp
import com.islamichub.app.ui.theme.premiumTap
import com.islamichub.app.ui.theme.staggerEntrance
import com.islamichub.app.ui.theme.toBanglaDigits

/**
 * v5.18.0 — Arabic alphabet learning, fully adopted from the Muslimeen app:
 * 29 letters, each with its name (Bangla) and a recorded letter sound
 * (assets/audio/letters/0XX-*.mp3). Tap a letter card to hear it.
 */
data class ArabicLetter(
    val number: Int,
    val arabic: String,
    val nameBn: String,
    val soundFile: String
)

private val arabicLetters: List<ArabicLetter> = listOf(
    ArabicLetter(1, "ا", "আলিফ", "001-alif.mp3"),
    ArabicLetter(2, "ب", "বা", "002-ba.mp3"),
    ArabicLetter(3, "ت", "তা", "003-taa.mp3"),
    ArabicLetter(4, "ث", "ছা", "004-tha.mp3"),
    ArabicLetter(5, "ج", "জীম", "005-jeem.mp3"),
    ArabicLetter(6, "ح", "হা", "006-haa.mp3"),
    ArabicLetter(7, "خ", "খা", "007-khaa.mp3"),
    ArabicLetter(8, "د", "দাল", "008-daal.mp3"),
    ArabicLetter(9, "ذ", "যাল", "009-dhaal.mp3"),
    ArabicLetter(10, "ر", "রা", "010-raa.mp3"),
    ArabicLetter(11, "ز", "যা", "011-zaa.mp3"),
    ArabicLetter(12, "س", "সীন", "012-seen.mp3"),
    ArabicLetter(13, "ش", "শীন", "013-sheen.mp3"),
    ArabicLetter(14, "ص", "সওয়াদ", "014-saad.mp3"),
    ArabicLetter(15, "ض", "দওয়াদ", "015-daad.mp3"),
    ArabicLetter(16, "ط", "তোয়া", "016-taa.mp3"),
    ArabicLetter(17, "ظ", "যোয়া", "017-dhaa.mp3"),
    ArabicLetter(18, "ع", "আইন", "018-ayn.mp3"),
    ArabicLetter(19, "غ", "গাইন", "019-ghayn.mp3"),
    ArabicLetter(20, "ف", "ফা", "020-faa.mp3"),
    ArabicLetter(21, "ق", "ক্বাফ", "021-qaaf.mp3"),
    ArabicLetter(22, "ك", "কাফ", "022-kaaf.mp3"),
    ArabicLetter(23, "ل", "লাম", "023-laam.mp3"),
    ArabicLetter(24, "م", "মীম", "024-meem.mp3"),
    ArabicLetter(25, "ن", "নূন", "025-noon.mp3"),
    ArabicLetter(26, "ه", "হা", "026-haa.mp3"),
    ArabicLetter(27, "و", "ওয়াও", "027-waaw.mp3"),
    ArabicLetter(28, "ي", "ইয়া", "028-yaa.mp3"),
    ArabicLetter(29, "ء", "হামজা", "029-hamza.mp3")
)

private val letterAccents = listOf(
    Color(0xFF0E7C5A), Color(0xFF1565C0), Color(0xFF6D45C7),
    Color(0xFFC9A34E), Color(0xFF8D6E63), Color(0xFF00838F)
)

@Composable
fun AlphabetScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var playingIdx by remember { mutableStateOf<Int?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    // v5.18.0 — release the player on leave (no leaked MediaPlayer)
    DisposableEffect(Unit) {
        onDispose {
            player?.let { try { it.stop(); it.release() } catch (_: Exception) {} }
        }
    }

    val playLetter: (Int, ArabicLetter) -> Unit = { idx, letter ->
        player?.let { try { it.stop(); it.release() } catch (_: Exception) {} }
        try {
            val afd = context.assets.openFd("audio/letters/${letter.soundFile}")
            val mp = MediaPlayer()
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.prepare()
            mp.setOnCompletionListener {
                playingIdx = null
                try { it.release() } catch (_: Exception) {}
            }
            mp.start()
            player = mp
            playingIdx = idx
        } catch (_: Exception) {
            playingIdx = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // compact header — same 52dp pattern as the Zikr hub
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text("আরবি বর্ণমালা", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("২৯ অক্ষর • উচ্চারণ শুনুন", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(arabicLetters, key = { _, l -> l.number }) { idx, letter ->
                val accent = letterAccents[idx % letterAccents.size]
                LetterCard(
                    letter = letter,
                    accent = accent,
                    isPlaying = playingIdx == idx,
                    onClick = { playLetter(idx, letter) }
                )
            }
        }
    }
}

@Composable
private fun LetterCard(
    letter: ArabicLetter,
    accent: Color,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.08f else 1f,
        animationSpec = tween(180),
        label = "letterScale"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = if (isPlaying) 0.28f else 0.14f), accent.copy(alpha = 0.05f))
                )
            )
            .premiumTap(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (isPlaying) accent else accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "শুনুন",
                tint = if (isPlaying) Color.White else accent,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            letter.arabic,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            letter.nameBn,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = banglaSp(MaterialTheme.typography.labelMedium.fontSize)),
            fontWeight = FontWeight.SemiBold,
            color = accent
        )
        Text(
            "${letter.number}".toBanglaDigits(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
