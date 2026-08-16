package com.islamichub.app.data.repo

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.islamichub.app.MainActivity
import com.islamichub.app.R
import com.islamichub.app.data.model.PrayerTimes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

/**
 * Schedules per-prayer notifications via AlarmManager for exact alarm behavior.
 *
 * Concurrency rules (per MD plan §9, §14):
 *  - Deterministic notification IDs:  (surah-equivalent) dayOfYear * 100 + prayerIndex
 *  - Cancel-then-set: each schedule cancels existing same-ID alarm before setting
 *  - Boot receiver reschedules everything from scratch
 *  - Single StateFlow exposes last-known schedule for UI
 */
class PrayerScheduler(
    private val context: Context,
    private val prayerRepository: PrayerRepository
) {
    data class ScheduleState(
        val lastScheduledFor: String? = null,
        val nextNotificationTitle: String? = null,
        val nextNotificationAt: Long = 0L,
        val error: String? = null
    )

    private val _state = MutableStateFlow(ScheduleState())
    val state: StateFlow<ScheduleState> = _state.asStateFlow()

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val CHANNEL_ID = "prayer_notifications"
        const val CHANNEL_NAME = "Prayer Times"

        // Deterministic notification ID: prayerIndex (1..5) so each prayer slot has unique stable ID
        private fun notificationId(prayerIndex: Int): Int = 5000 + prayerIndex
        private fun requestCode(prayerIndex: Int): Int = 6000 + prayerIndex

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_PRAYER_INDEX = "extra_prayer_index"

        // Atomic guard — prevents concurrent reschedules (race condition rule §10 Rule 5)
        private val scheduleLock = AtomicLong(0L)
    }

    init {
        ensureChannel()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for the five daily prayers"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            val mgr = context.getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(channel)
        }
    }

    /**
     * Schedule notifications for all 5 daily prayers for the given PrayerTimes.
     * Idempotent: cancels any existing alarms for the same prayer slots first.
     */
    suspend fun scheduleToday(times: PrayerTimes) = withContext(Dispatchers.IO) {
        // Acquire lock — only one scheduler at a time
        val myToken = System.nanoTime()
        if (!scheduleLock.compareAndSet(0L, myToken)) {
            return@withContext  // Another schedule is running
        }
        try {
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val now = System.currentTimeMillis()
            val entries = listOf(
                Triple(1, "Fajr", times.fajr),
                Triple(2, "Dhuhr", times.dhuhr),
                Triple(3, "Asr", times.asr),
                Triple(4, "Maghrib", times.maghrib),
                Triple(5, "Isha", times.isha)
            )

            var nextTitle: String? = null
            var nextAt: Long = Long.MAX_VALUE

            for ((idx, name, timeStr) in entries) {
                val triggerAt = parseTimeToday(timeStr)
                if (triggerAt <= now) continue  // Skip past times for today

                if (triggerAt < nextAt) {
                    nextAt = triggerAt
                    nextTitle = "$name · $timeStr"
                }

                // Cancel any existing alarm with this request code
                val pending = buildPendingIntent(name, timeStr, idx)
                alarmManager.cancel(pending)

                // Set new alarm — use setExactAndAllowWhileIdle for doze-mode reliability
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerAt,
                                pending
                            )
                        } else {
                            // Inexact fallback
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerAt,
                                pending
                            )
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAt,
                            pending
                        )
                    }
                } catch (_: SecurityException) {
                    // Fall back to inexact
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pending
                    )
                }
            }

            _state.value = ScheduleState(
                lastScheduledFor = times.date,
                nextNotificationTitle = nextTitle,
                nextNotificationAt = if (nextAt == Long.MAX_VALUE) 0L else nextAt
            )
        } finally {
            scheduleLock.set(0L)
        }
    }

    /**
     * Cancel all scheduled prayer alarms. Idempotent.
     */
    fun cancelAll() {
        listOf(1, 2, 3, 4, 5).forEach { idx ->
            alarmManager.cancel(buildPendingIntent("", "", idx))
        }
        // Also cancel any shown notifications
        val mgr = context.getSystemService(NotificationManager::class.java)
        listOf(1, 2, 3, 4, 5).forEach { idx -> mgr.cancel(notificationId(idx)) }
        _state.value = ScheduleState()
    }

    private fun buildPendingIntent(name: String, time: String, idx: Int): PendingIntent {
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            putExtra(EXTRA_PRAYER_NAME, name)
            putExtra(EXTRA_PRAYER_TIME, time)
            putExtra(EXTRA_PRAYER_INDEX, idx)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getBroadcast(
            context,
            requestCode(idx),
            intent,
            flags
        )
    }

    private fun parseTimeToday(hhmm: String): Long {
        return try {
            val parts = hhmm.split(":")
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                set(Calendar.MINUTE, parts.getOrNull(1)?.takeIf { it.matches(Regex("\\d+")) }?.toInt() ?: 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        } catch (_: Exception) {
            System.currentTimeMillis() + 3_600_000L  // fallback +1h
        }
    }

    fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }
}

/**
 * Receives AlarmManager broadcasts and posts the prayer notification.
 */
class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(PrayerScheduler.EXTRA_PRAYER_NAME) ?: return
        val time = intent.getStringExtra(PrayerScheduler.EXTRA_PRAYER_TIME) ?: ""
        val idx = intent.getIntExtra(PrayerScheduler.EXTRA_PRAYER_INDEX, 1)

        val notifId = 5000 + idx

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val tapPending = PendingIntent.getActivity(
            context,
            notifId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(context, PrayerScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_splash_logo)
            .setContentTitle("🕌 $name time")
            .setContentText("It's time for $name prayer ($time)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(tapPending)
            .build()

        val mgr = context.getSystemService(NotificationManager::class.java)
        mgr.notify(notifId, notification)
    }
}

/**
 * Reschedules alarms after device boot, timezone change, or time-set.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            "android.intent.action.MY_PACKAGE_REPLACED" -> {
                // Schedule via GlobalScope — fire-and-forget, safe because
                // PrayerScheduler uses an internal lock to deduplicate.
                GlobalScope.launch {
                    try {
                        val app = context.applicationContext as? com.islamichub.app.IslamicHubApp
                            ?: return@launch
                        val repo = app.container.prayerRepository
                        val times = if (repo.hasLocationPermission() && repo.isLocationEnabled()) {
                            val loc = repo.getCurrentLocation()
                            if (loc != null) {
                                repo.getPrayerTimes(loc.latitude, loc.longitude).getOrNull()
                            } else null
                        } else null
                        val finalTimes = times ?: repo.getDefaultPrayerTimes().getOrNull()
                        if (finalTimes != null) {
                            app.container.prayerScheduler.scheduleToday(finalTimes)
                        }
                    } catch (_: Exception) {
                        // Best-effort — boot receivers must not crash
                    }
                }
            }
        }
    }
}
