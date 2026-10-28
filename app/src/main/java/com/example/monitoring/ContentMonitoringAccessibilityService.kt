package com.example.monitoring

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ContentMonitoringAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "BreakFreeService"
        const val CHANNEL_ID = "breakfree_grace_period_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STEP_BACK = "com.example.monitoring.ACTION_STEP_BACK"
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var graceStateObservationJob: Job? = null
    private var notificationManager: NotificationManager? = null

    private val stepBackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_STEP_BACK) {
                Log.d(TAG, "Step back triggered from system notification action")
                MonitoringManager.stepBackAndResolve()
                cancelGraceNotification()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        MonitoringManager.setServiceConnected(true)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        createNotificationChannel()

        val filter = IntentFilter(ACTION_STEP_BACK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stepBackReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stepBackReceiver, filter)
        }

        observeGracePeriodState()
        Log.i(TAG, "ContentMonitoringAccessibilityService activated and guarding browser traffic.")
    }

    private fun observeGracePeriodState() {
        graceStateObservationJob?.cancel()
        graceStateObservationJob = serviceScope.launch {
            MonitoringManager.gracePeriodState.collect { state ->
                if (state.isGraceActive) {
                    showOrUpdateGraceNotification(state.remainingSeconds, state.detectedTarget)
                } else {
                    cancelGraceNotification()
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        // 1. Check if user stepped back into BreakFree or home launcher while grace period is active
        if (MonitoringManager.gracePeriodState.value.isGraceActive) {
            val isSafeApp = packageName == applicationContext.packageName ||
                    packageName.contains("launcher") ||
                    packageName.contains("home")

            if (isSafeApp) {
                Log.i(TAG, "User exited explicit content into $packageName. Self-control reversal achieved!")
                MonitoringManager.stepBackAndResolve()
                return
            }
        }

        // 2. Only inspect recognized browsers to optimize performance and privacy
        if (!ExplicitContentDetector.monitoredBrowsers.contains(packageName)) {
            return
        }

        // 3. Inspect active window hierarchy for explicit domains or adult search terms
        val rootNode = rootInActiveWindow ?: return
        try {
            val detectionResult = scanNodeForExplicitContent(rootNode)
            if (detectionResult.first) {
                val detected = detectionResult.second
                Log.w(TAG, "Explicit content detected: '$detected'. Triggering 120s grace period!")
                MonitoringManager.triggerGracePeriod(detected)
            } else if (MonitoringManager.gracePeriodState.value.isGraceActive) {
                // If user navigated away inside the browser to a known safe domain, acknowledge reversal
                val urlText = findAddressBarText(rootNode)
                if (!urlText.isNullOrBlank() && !ExplicitContentDetector.isExplicitContent(urlText).first) {
                    if (urlText.contains("google.com") || urlText.contains("wikipedia.org") || urlText.contains("about:blank")) {
                        Log.i(TAG, "Browser navigated to safe page: $urlText. Grace period resolved successfully.")
                        MonitoringManager.stepBackAndResolve()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while inspecting accessibility node tree", e)
        }
    }

    private fun scanNodeForExplicitContent(node: AccessibilityNodeInfo?): Pair<Boolean, String> {
        if (node == null) return Pair(false, "")

        // Check text
        val text = node.text?.toString()
        val textCheck = ExplicitContentDetector.isExplicitContent(text)
        if (textCheck.first) return textCheck

        // Check content description
        val desc = node.contentDescription?.toString()
        val descCheck = ExplicitContentDetector.isExplicitContent(desc)
        if (descCheck.first) return descCheck

        // Check specific browser address bar view IDs
        val viewId = node.viewIdResourceName
        if (viewId != null && isBrowserAddressBarId(viewId)) {
            if (textCheck.first) return textCheck
            if (descCheck.first) return descCheck
        }

        // Recursively inspect children up to depth 25 to balance thoroughness with CPU efficiency
        val childCount = node.childCount
        for (i in 0 until minOf(childCount, 25)) {
            val child = node.getChild(i)
            if (child != null) {
                val childResult = scanNodeForExplicitContent(child)
                if (childResult.first) return childResult
            }
        }

        return Pair(false, "")
    }

    private fun findAddressBarText(node: AccessibilityNodeInfo?): String? {
        if (node == null) return null
        val viewId = node.viewIdResourceName
        if (viewId != null && isBrowserAddressBarId(viewId)) {
            return node.text?.toString()
        }
        val childCount = node.childCount
        for (i in 0 until minOf(childCount, 15)) {
            val child = node.getChild(i)
            val found = findAddressBarText(child)
            if (!found.isNullOrBlank()) return found
        }
        return null
    }

    private fun isBrowserAddressBarId(viewId: String): Boolean {
        val lower = viewId.lowercase()
        return lower.contains("url") ||
                lower.contains("omnibox") ||
                lower.contains("location_bar") ||
                lower.contains("search_box") ||
                lower.contains("toolbar_url")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BreakFree Mindfulness Shield",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts during 120-second mindfulness grace period countdown"
                enableVibration(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun showOrUpdateGraceNotification(remainingSeconds: Int, detectedTarget: String) {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stepBackIntent = Intent(ACTION_STEP_BACK)
        val stepBackPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            stepBackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        val timeString = String.format("%02d:%02d", mins, secs)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚨 Mindfulness Check: Step Back! ($timeString)")
            .setContentText("120s Grace Period active. Close the site now to preserve your streak!")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Explicit content was detected ($detectedTarget).\n" +
                            "You have $timeString remaining to close this tab and protect your streak."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setContentIntent(appPendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Step Back (Protect Streak)",
                stepBackPendingIntent
            )

        notificationManager?.notify(NOTIFICATION_ID, builder.build())
    }

    private fun cancelGraceNotification() {
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    override fun onInterrupt() {
        Log.w(TAG, "ContentMonitoringAccessibilityService interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        graceStateObservationJob?.cancel()
        MonitoringManager.setServiceConnected(false)
        cancelGraceNotification()
        try {
            unregisterReceiver(stepBackReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }
}
