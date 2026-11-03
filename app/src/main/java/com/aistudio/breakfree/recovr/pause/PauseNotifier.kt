package com.aistudio.breakfree.recovr.pause

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aistudio.breakfree.recovr.MainActivity
import com.aistudio.breakfree.recovr.R
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage

/**
 * Schedules the three mid-pause notifications and posts them.
 *
 * Alarms are used on purpose instead of a coroutine: the whole point of the gate is that
 * the user puts the phone down, so the app may be backgrounded or killed. The alarms
 * survive both, and tapping one opens the app, which resumes the gate from storage.
 */
object PauseNotifier {

    const val CHANNEL_ID = "pause_gate"

    private const val TAG = "PauseNotifier"

    const val EXTRA_MESSAGE = "pause_message"
    const val EXTRA_NOTIFICATION_ID = "pause_notification_id"
    const val EXTRA_OPEN_PAUSE = "open_pause_gate"

    /** Notifications land at 3, 6 and 9 minutes into the ten-minute gate. */
    private val OFFSETS_MINUTES = listOf(3, 6, 9)
    private const val BASE_REQUEST_CODE = 7301

    fun schedule(context: Context, messages: List<String>, startedAtMs: Long) {
        val app = context.applicationContext
        ensureChannel(app)
        cancel(app, clearNotifications = false)
        val alarmManager = app.getSystemService(AlarmManager::class.java) ?: return
        // Exact where the platform allows it. An inexact alarm is batched, and on a phone
        // in a low standby bucket that batch can land after the gate has already closed,
        // which is the same as never arriving.
        val canBeExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        messages.take(OFFSETS_MINUTES.size).forEachIndexed { index, message ->
            val triggerAt = startedAtMs + OFFSETS_MINUTES[index] * 60_000L
            if (triggerAt <= System.currentTimeMillis()) return@forEachIndexed
            runCatching {
                if (canBeExact) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent(app, index, message)
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent(app, index, message)
                    )
                }
            }.onFailure {
                // Without this the reminder silently never exists.
                Log.w(TAG, "Could not schedule pause reminder $index: ${it.message}")
            }
        }
    }

    /**
     * Cancels the remaining alarms, and by default clears anything already on screen.
     * Rescheduling passes [clearNotifications] = false so a reminder the user already
     * received is not pulled out of the shade.
     */
    fun cancel(context: Context, clearNotifications: Boolean = true) {
        val app = context.applicationContext
        Log.i(TAG, "cancel(clear=$clearNotifications)")
        val alarmManager = app.getSystemService(AlarmManager::class.java)
        OFFSETS_MINUTES.indices.forEach { index ->
            val requestCode = BASE_REQUEST_CODE + index
            if (alarmManager != null) {
                val existing = PendingIntent.getBroadcast(
                    app,
                    requestCode,
                    Intent(app, PauseReminderReceiver::class.java),
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (existing != null) {
                    alarmManager.cancel(existing)
                    existing.cancel()
                }
            }
            if (clearNotifications) {
                runCatching { NotificationManagerCompat.from(app).cancel(requestCode) }
            }
        }
    }

    fun show(context: Context, message: String, notificationId: Int) {
        val app = context.applicationContext
        ensureChannel(app)
        if (!NotificationManagerCompat.from(app).areNotificationsEnabled()) {
            Log.i(TAG, "Pause reminder skipped: notifications are off")
            return
        }

        val openIntent = Intent(app, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra(EXTRA_OPEN_PAUSE, true)
        }
        val contentIntent = PendingIntent.getActivity(
            app,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val language = currentLanguage(app)
        val notification = NotificationCompat.Builder(app, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_neutral)
            .setContentTitle(Translations.t("pause_notif_title", language))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        runCatching { NotificationManagerCompat.from(app).notify(notificationId, notification) }
            .onFailure { Log.w(TAG, "Pause reminder failed: ${it.message}") }
    }

    private fun pendingIntent(context: Context, index: Int, message: String): PendingIntent {
        val intent = Intent(context, PauseReminderReceiver::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_NOTIFICATION_ID, BASE_REQUEST_CODE + index)
        }
        return PendingIntent.getBroadcast(
            context,
            BASE_REQUEST_CODE + index,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val language = currentLanguage(context)
        val channel = NotificationChannel(
            CHANNEL_ID,
            Translations.t("pause_notif_channel", language),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = Translations.t("pause_notif_channel_desc", language)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun currentLanguage(context: Context): AppLanguage {
        val code = context.getSharedPreferences("breakfree_prefs", Context.MODE_PRIVATE)
            .getString("selected_language", "en")
        return when (code) {
            "ar" -> AppLanguage.ARABIC
            "fr" -> AppLanguage.FRENCH
            else -> AppLanguage.ENGLISH
        }
    }
}
