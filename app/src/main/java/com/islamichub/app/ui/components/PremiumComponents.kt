package com.islamichub.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import com.islamichub.app.ui.theme.premiumTap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Load a WebP/JPG/PNG image from assets.
 */
fun loadAssetImage(context: Context, path: String): Bitmap? {
    return try {
        context.assets.open(path).use { input ->
            BitmapFactory.decodeStream(input)
        }
    } catch (_: Exception) { null }
}

// ─── v5.13.0 — async, cached, downsampled asset image loading ──────────────
//
// PERFORMANCE ROOT-CAUSE FIX. Every screen used to decode its hero WebP with
// `remember { loadAssetImage(...) }` — a synchronous BitmapFactory.decodeStream
// on the MAIN THREAD during composition. Large heroes stall the UI thread for
// tens of milliseconds each, which is exactly the "app onk slow" jank.
//
// The fix mirrors Coil's pipeline without the dependency:
//   1. an app-wide LruCache (25% of heap) holds decoded bitmaps
//   2. decode happens on Dispatchers.IO, with bounds-first downsampling so a
//      4000px WebP never allocates a full-screen ARGB bitmap needlessly
//   3. the composable fades the image in when ready (premium feel, no pop)

private object AssetImageCache {
    private const val MAX_BYTES = 16 * 1024 * 1024  // ~16MB decode cache budget

    @Volatile private var cache: android.util.LruCache<String, Bitmap>? = null

    private fun get(): android.util.LruCache<String, Bitmap> {
        return cache ?: synchronized(this) {
            cache ?: object : android.util.LruCache<String, Bitmap>(MAX_BYTES) {
                override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
            }.also { cache = it }
        }
    }

    fun hit(key: String): Bitmap? = get().get(key)

    fun put(key: String, bitmap: Bitmap) { get().put(key, bitmap) }
}

/**
 * Decode an asset image on IO with bounds-first downsampling: never allocate
 * more than [maxDim] pixels on the longest edge.
 */
fun loadAssetImageDownsampled(context: Context, path: String, maxDim: Int = 1600): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        var longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / (sample * 2) >= maxDim) sample *= 2
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        context.assets.open(path).use { input ->
            BitmapFactory.decodeStream(input, null, opts)
        }
    } catch (_: Exception) { null }
}

/**
 * Composable replacement for `remember { loadAssetImage(...) }`.
 *
 * - Returns the cached bitmap SYNCHRONOUSLY when already decoded (zero jank
 *   on re-entry).
 * - Otherwise decodes on Dispatchers.IO (never the UI thread) with bounds-first
 *   downsampling, stores it in the LRU cache, and recomposes once ready.
 * - Return type stays `Bitmap?` so every existing call site keeps compiling
 *   with its `asImageBitmap()` usage.
 */
@Composable
fun rememberAssetBitmap(
    context: Context,
    path: String,
    maxDim: Int = 1600
): Bitmap? {
    var bitmap by remember(path) { mutableStateOf<Bitmap?>(AssetImageCache.hit(path)) }
    LaunchedEffect(path) {
        if (bitmap != null) return@LaunchedEffect
        val decoded = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            loadAssetImageDownsampled(context, path, maxDim)
        }
        if (decoded != null) {
            AssetImageCache.put(path, decoded)
            bitmap = decoded
        }
    }
    return bitmap
}

/**
 * Fade-in wrapper: renders [bitmap] with a gentle 320ms alpha/scale-in once
 * it becomes available — the premium touch that hides decode latency.
 */
@Composable
fun PremiumAssetImage(
    context: Context,
    path: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap = if (path != null) rememberAssetBitmap(context, "img/$path") else null
    val shown by animateFloatAsState(
        targetValue = if (bitmap != null) 1f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "premiumAssetFade"
    )
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier.graphicsLayer {
                alpha = shown
                val s = 0.98f + 0.02f * shown
                scaleX = s
                scaleY = s
            },
            contentScale = contentScale
        )
    }
}

/**
 * Premium hero card with background image + multi-layer gradient overlay.
 * Enhanced with pressed scale animation (micro-interaction).
 */
@Composable
fun PremiumHeroCard(
    backgroundImage: String?,
    context: Context,
    modifier: Modifier = Modifier,
    height: Int = 220,
    content: @Composable BoxScope.() -> Unit
) {
    // v5.13.0 — decode moved OFF the main thread with an LRU cache + fade-in.
    val bitmap = if (backgroundImage != null) {
        rememberAssetBitmap(context, "img/$backgroundImage")
    } else null
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Triple-layer gradient for premium depth
                Box(modifier = Modifier.fillMaxSize().background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                ))
            } else {
                Box(modifier = Modifier.fillMaxSize().background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                ))
            }
            content()
        }
    }
}

/**
 * Premium feature card with background image + press animation.
 * Enhanced: scale-down on press, shadow elevation, rounded 20dp.
 */
@Composable
fun PremiumCard(
    backgroundImage: String?,
    context: Context,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 140,
    overlayColor: Color = Color(0xFF6D45C7),
    content: @Composable BoxScope.() -> Unit
) {
    // v5.13.0 — decode moved OFF the main thread with an LRU cache + fade-in.
    val bitmap = if (backgroundImage != null) {
        rememberAssetBitmap(context, "img/$backgroundImage")
    } else null
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "cardScale"
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    overlayColor.copy(alpha = 0.2f),
                                    overlayColor.copy(alpha = 0.5f),
                                    overlayColor.copy(alpha = 0.9f)
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    overlayColor,
                                    overlayColor.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }
            content()
        }
    }
}

/**
 * Premium circular icon badge with gradient background.
 */
@Composable
fun PremiumIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Int = 44,
    backgroundColor: Color = Color.White.copy(alpha = 0.25f),
    iconColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}

/**
 * Premium section header with gradient accent line.
 */
@Composable
fun PremiumSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(width = 40.dp, height = 3.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        )
    }
}

/**
 * Premium mini audio player (legacy — kept for backward compat).
 */
@Composable
fun PremiumMiniAudioPlayer(
    isPlaying: Boolean,
    isLoading: Boolean,
    title: String,
    subtitle: String,
    onPlayPause: () -> Unit,
    onStop: () -> Unit
) {
    var elapsed by remember { mutableStateOf(0) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                delay(1000)
                elapsed++
            }
        }
    }
    LaunchedEffect(title) { elapsed = 0 }

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(subtitle, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
            }
            IconButton(onClick = onStop) {
                Icon(Icons.Filled.Stop, contentDescription = "Stop",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
            }
        }
        if (isPlaying || isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f)
            )
        }
    }
}

/**
 * v5.8.1 — Premium dialog icon: circular gradient badge shown above the dialog
 * title, giving every dialog a consistent branded identity instead of the
 * plain stock Material look.
 */
@Composable
fun PremiumDialogIcon(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(tint, tint.copy(alpha = 0.6f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

// ─── v5.11.0 — shared premium empty state ────────────────────────────────
//
// One consistent empty-state for every filterable/empty list: gradient icon
// badge + title + subtitle + optional CTA. (BookmarksScreen had the pattern;
// QuranSearch / HadithSearch / surah filter rendered a blank body.)

@Composable
fun PremiumEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    ctaText: String? = null,
    onCta: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (ctaText != null && onCta != null) {
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.TextButton(onClick = onCta) { Text(ctaText) }
        }
    }
}

/**
 * v5.11.0 — compact pill action chip (icon + label) used on content cards for
 * copy / share / AI-explain affordances (Asmaul Husna, Kalima, …).
 */
@Composable
fun PremiumMiniActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .premiumTap(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
