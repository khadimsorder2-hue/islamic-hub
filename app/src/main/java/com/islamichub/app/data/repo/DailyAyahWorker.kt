package com.islamichub.app.data.repo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.islamichub.app.MainActivity
import com.islamichub.app.R
import com.islamichub.app.data.local.QuranData
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Daily Ayah notification — shows a random ayah every day at 8 AM.
 * Uses WorkManager periodic work (runs every 24 hours).
 */
class DailyAyahWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            showDailyAyahNotification(applicationContext)
            return Result.success()
        } catch (_: Exception) {
            return Result.retry()
        }
    }

    private fun showDailyAyahNotification(context: Context) {
        val pool = QuranData.ayahOfDayPool
        val idx = (System.currentTimeMillis() / 86_400_000L).toInt().mod(pool.size)
        val (arabic, en, bn) = pool[idx]

        val notificationManager = context.getSystemService(NotificationManager::class.java)

        // Create channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "দৈনিক আয়াত",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "প্রতিদিন একটি কুরআনের আয়াত"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val tapPending = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_splash_logo)
            .setContentTitle("📖 আজকের আয়াত")
            .setContentText(bn.take(80) + if (bn.length > 80) "…" else "")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$arabic\n\n$bn\n\n$en"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(tapPending)
            .build()

        notificationManager.notify(DAILY_AYAH_NOTIF_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "daily_ayah"
        private const val DAILY_AYAH_NOTIF_ID = 9999
        private const val WORK_NAME = "daily_ayah_notification"

        /**
         * Schedule daily ayah notification (every 24 hours).
         * Call this from Application.onCreate().
         */
        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= now.timeInMillis) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val initialDelay = target.timeInMillis - now.timeInMillis

            val workRequest = PeriodicWorkRequestBuilder<DailyAyahWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        /**
         * Cancel daily ayah notifications.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
