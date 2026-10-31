package com.aistudio.breakfree.recovr.monitoring

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.aistudio.breakfree.recovr.MainActivity
import com.aistudio.breakfree.recovr.R
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage

/**
 * A lightweight foreground service whose only job is to keep the protection process alive.
 *
 * ## Why it exists
 *
 * Android (and aggressive OEMs such as Xiaomi MIUI) kill background processes under memory
 * pressure. When that happens the accessibility monitor and the DNS filter stop with the
 * process, and the user is unprotected until they reopen the app. This service holds a
 * foreground notification, which raises the process priority and makes the system restart
 * it (`START_STICKY`) if it is ever killed.
 *
 * It carries no data and does no work; it is a "keep me alive" anchor.
 */
class GuardForegroundService : Service() {

    companion object {
        private const val TAG = "GuardForegroundService"
        const val COMBINED_ID = 0x6702
        const val CHANNEL_ID = "guard_service"
        private const val NOTIFICATION_ID = COMBINED_ID

        fun start(context: Context) {
            val intent = Intent(context, GuardForegroundService::class.java)
            try {
                androidx.core.content.ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                Log.w(TAG, "Could not start the guard service", e)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, GuardForegroundService::class.java))
        }

        fun refresh(context: Context) {
            start(context)
        }

        /** Builds the single combined notification for both protection services. */
        fun buildCombinedNotification(context: Context): android.app.Notification {
            val openApp = PendingIntent.getActivity(
                context,
                0,
                appLaunchIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val language = currentLanguage(context)
            val stealth = com.aistudio.breakfree.recovr.stealth.StealthPreferences.isStealthEnabled(context) &&
                com.aistudio.breakfree.recovr.stealth.StealthPreferences.discreetNotifications(context)
            val vpnRunning = DnsFilterVpnService.isRunning()
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(if (stealth) R.drawable.ic_stat_neutral else R.drawable.ic_stat_shield)
                .setContentTitle(
                    Translations.t(if (stealth) "stealth_service_title" else "guard_service_title", language)
                )
                .setContentText(
                    if (vpnRunning) {
                        Translations.t(
                            if (stealth) "stealth_service_text_dns" else "guard_service_text_dns",
                            language
                        )
                    } else {
                        Translations.t(
                            if (stealth) "stealth_service_text" else "guard_service_text",
                            language
                        )
                    }
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(openApp)
            if (vpnRunning) {
                builder.addAction(
                    0,
                    Translations.t("dns_vpn_notification_stop", language),
                    DnsFilterVpnService.stopIntent(context)
                )
            }
            return builder.build()
        }

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val language = currentLanguage(context)
            val stealth = com.aistudio.breakfree.recovr.stealth.StealthPreferences.isStealthEnabled(context) &&
                com.aistudio.breakfree.recovr.stealth.StealthPreferences.discreetNotifications(context)
            val channel = NotificationChannel(
                CHANNEL_ID,
                Translations.t(
                    if (stealth) "stealth_notif_channel" else "guard_service_channel",
                    language
                ),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = Translations.t(
                    if (stealth) "stealth_notif_channel_desc" else "guard_service_channel_desc",
                    language
                )
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }

        fun currentLanguage(context: Context): AppLanguage {
            val code = context.getSharedPreferences("breakfree_prefs", Context.MODE_PRIVATE)
                .getString("selected_language", "en")
            return if (code == "ar") AppLanguage.ARABIC else AppLanguage.ENGLISH
        }
    }

    override fun onBind(intent: Intent?): android.os.IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Companion.createChannel(this)
        startInForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Re-post on every start so a Stealth Mode toggle (which changes the wording and
        // the icon) is reflected immediately instead of waiting for a process restart.
        Companion.createChannel(this)
        startInForeground()
        // START_STICKY: if the system kills this service, it is recreated with a null
        // intent and restarted.
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // When the user swipes the app away from recents, schedule a restart so the
        // protection comes back even on OEMs that do not honour START_STICKY.
        try {
            val restart = Intent(applicationContext, GuardForegroundService::class.java)
            val pi = PendingIntent.getService(
                applicationContext,
                1,
                restart,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val am = getSystemService(AlarmManager::class.java)
            am.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 2_000L,
                pi
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not schedule a guard restart", e)
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        // Detach (keep the notification) in case the DNS filter is still running; ask it
        // to re-post the combined notification under its own ownership.
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
        if (DnsFilterVpnService.isRunning()) {
            try {
                DnsFilterVpnService.refreshNotification(applicationContext)
            } catch (e: Exception) {
                Log.w(TAG, "Could not refresh the DNS notification", e)
            }
        }
        super.onDestroy()
        // Last-resort restart attempt.
        try {
            val restart = Intent(applicationContext, GuardForegroundService::class.java)
            val pi = PendingIntent.getService(
                applicationContext,
                2,
                restart,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val am = getSystemService(AlarmManager::class.java)
            am.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 3_000L,
                pi
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not schedule a guard restart on destroy", e)
        }
    }

    private fun startInForeground() {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, COMBINED_ID, Companion.buildCombinedNotification(this), type)
    }

}
