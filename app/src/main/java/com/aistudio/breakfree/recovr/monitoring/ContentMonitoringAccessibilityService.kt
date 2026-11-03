package com.aistudio.breakfree.recovr.monitoring

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.aistudio.breakfree.recovr.MainActivity
import com.aistudio.breakfree.recovr.R
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Inspects the foreground browser or opted-in social app and starts the step-back window
 * when explicit content is detected.
 *
 * Everything happens on-device: node text is matched in memory and never stored or
 * uploaded. The tree walk runs off the main thread and is rate-limited, because
 * accessibility events arrive many times per second while a page is loading.
 */
class ContentMonitoringAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "BreakFreeService"
        const val CHANNEL_ID = "breakfree_grace_period_channel"
        const val NOTIFICATION_ID = 1001

        /** Events from these packages say nothing about what the user is looking at. */
        private val IGNORED_PACKAGES = setOf(
            "android",
            "com.android.systemui",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
            "com.google.android.packageinstaller",
            "com.google.android.gms"
        )

        /** Skip anything that looks like an on-screen keyboard. */
        private fun isKeyboard(packageName: String): Boolean =
            packageName.contains("inputmethod") ||
                packageName.contains("keyboard") ||
                packageName.contains("gboard")

        private const val MIN_SCAN_INTERVAL_MS = 400L
        private const val MAX_NODES = 500
        private const val MAX_DEPTH = 12
        private const val MAX_CHILDREN = 25

        /**
         * How long a page stays quiet after the user left it. Without this, the page is
         * still in front when the countdown resolves (they switched apps rather than
         * closed the tab), and the very next scan starts a new window — a nag loop that
         * also inflates the saved-streak counter.
         */
        private const val RESOLVED_PAGE_COOLDOWN_MS = 60_000L

        /**
         * Shorter window used when the address bar cannot be read at all. Kept short
         * (5 s) so that closing and reopening the browser does not noticeably delay
         * detection — just long enough to swallow the tail of the accessibility-event
         * burst from the previous page.
         */
        private const val RESOLVED_UNKNOWN_PAGE_COOLDOWN_MS = 5_000L

        /** Same key [com.aistudio.breakfree.recovr.data.BreakFreeRepository] writes. */
        private const val KEY_LANGUAGE = "selected_language"

        /** Launcher aliases used by Stealth Mode; their labels appear in system screens. */
        private val STEALTH_ALIASES = listOf(
            "com.aistudio.breakfree.recovr.alias.CalculatorAlias",
            "com.aistudio.breakfree.recovr.alias.NotesAlias",
            "com.aistudio.breakfree.recovr.alias.WeatherAlias"
        )

        /** Rate limit for the uninstall-guard tree walk (Settings emits many events). */
        private const val GUARD_SCAN_INTERVAL_MS = 350L
        private const val GUARD_MAX_NODES = 200
        private const val GUARD_MAX_CHILDREN = 20
        private const val PROTECTION_ALIGN_TOLERANCE_PX = 150

        /**
         * How long to wait before re-checking a tentative protection-screen decision:
         * long enough for page-transition animations to finish so a merged node tree
         * (two pages briefly shown at once) cannot fake a guarded screen, short enough
         * that a real protected screen is still blocked almost immediately.
         */
        private const val PROTECTION_SETTLE_DELAY_MS = 450L

        /** Re-read the app's own labels periodically so a Stealth Mode switch is noticed. */
        private const val IDENTITIES_CACHE_TTL_MS = 60_000L
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var graceStateObservationJob: Job? = null
    private var notificationManager: NotificationManager? = null
    private var preferences: MonitoringPreferences? = null

    /** Draws the reminder over the flagged app when the overlay permission is granted. */
    private var overlayController: GraceOverlayController? = null

    private val scanInFlight = AtomicBoolean(false)
    private var lastScanAtMillis = 0L

    /**
     * Identity of the surface that was flagged: the address-bar text for a browser,
     * `app://<package>` for an app.
     */
    private var graceTriggerSurface: String? = null
    /** Package name of the app in which the current step-back window was triggered. */
    private var graceTriggerPackage: String? = null

    /**
     * The surface the user was on when the last countdown resolved, and when. A surface
     * that was just left stays quiet for a minute so a single navigation does not bounce
     * straight back into a new countdown; a different one is flagged immediately.
     */
    private var lastResolvedSurface: String? = null
    private var lastResolvedAtMillis = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        MonitoringManager.init(this)
        preferences = MonitoringPreferences(this)
        MonitoringManager.setServiceConnected(true)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        createNotificationChannel()
        overlayController = GraceOverlayController(this)

        observeGracePeriodState()
        Log.i(TAG, "ContentMonitoringAccessibilityService activated and guarding monitored apps.")
    }

    private fun observeGracePeriodState() {
        graceStateObservationJob?.cancel()
        graceStateObservationJob = serviceScope.launch {
            var wasActive = false
            MonitoringManager.gracePeriodState.collect { state ->
                // Remember the surface the user left, so the same page is not flagged
                // again a fraction of a second after the countdown resolves.
                if (wasActive && !state.isGraceActive) {
                    if (state.isResolvedBySelfControl) {
                        lastResolvedSurface = graceTriggerSurface
                        lastResolvedAtMillis = SystemClock.elapsedRealtime()
                    }
                    graceTriggerPackage = null
                }
                wasActive = state.isGraceActive

                // The overlay is the primary reminder when the user allowed it; the
                // notification is the fallback when the overlay cannot be drawn. Both
                // are driven by the same state, so they can never disagree.
                val overlayShowing =
                    overlayController?.syncWith(state, currentLanguage()) ?: false

                if (state.isGraceActive && !overlayShowing) {
                    showOrUpdateGraceNotification(
                        remainingSeconds = state.remainingSeconds,
                        totalSeconds = state.totalSeconds
                    )
                } else {
                    // Nothing to remind about, or the card on screen is doing it.
                    cancelGraceNotification()
                }
            }
        }
    }

    /**
     * The overlay is drawn by the service, outside the Compose tree, so it reads the
     * language straight from the same preference store the repository writes to.
     */
    private fun currentLanguage(): AppLanguage {
        val code = getSharedPreferences(MonitoringPreferences.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "en")
        return when (code) { "ar" -> AppLanguage.ARABIC; "fr" -> AppLanguage.FRENCH; else -> AppLanguage.ENGLISH }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return

        // Our own windows are never monitored content — including the reminder overlay
        // itself, which is an application overlay owned by this package. Without this
        // check, drawing the overlay looks exactly like "the user left the app", which
        // resolves the countdown the overlay is showing and makes it flicker.
        if (packageName == this.packageName) return

        // Guard against deactivation, uninstall, or settings tampering during 90-day lock.
        // The row-click check comes first: it is the precise "user just tapped the
        // BreakFree row in a Settings list" signal.
        if (checkGuardRowClick(event)) return
        if (checkAndGuardUninstallLock()) return

        if (packageName in IGNORED_PACKAGES || isKeyboard(packageName)) return

        val isBrowser = packageName in ExplicitContentDetector.monitoredBrowsers
        val isMonitoredApp = packageName in ExplicitContentDetector.monitoredApps

        // 1. Moving to a different app is the reversal we want to reward. It does not
        //    matter whether the new app is the launcher, a chat, or another monitored
        //    app: the user has left the surface that was flagged.
        if (MonitoringManager.gracePeriodState.value.isGraceActive) {
            val trigger = graceTriggerPackage
            val leftFlaggedApp = if (trigger != null) {
                packageName != trigger
            } else {
                // The countdown survived a process restart, so the trigger app is
                // unknown. Treat a move to an unmonitored app as a step-back.
                !isBrowser && !isMonitoredApp
            }
            if (leftFlaggedApp) {
                Log.i(TAG, "User left the flagged app. Step-back detected.")
                MonitoringManager.stepBackAndResolve()
                return
            }
        }

        // 2. Only inspect browsers and the apps the user explicitly opted into.
        if (!isBrowser && !isMonitoredApp) return

        val prefs = preferences ?: return
        if (!prefs.strictGuardEnabled) return
        // Reading another app's screen is a wider scope than browser monitoring and
        // has its own consent. Without it the Shield stays browser-only.
        if (isMonitoredApp && !prefs.appsMonitoringConsentGranted) return

        scheduleScan(packageName)
    }

    private var lastGuardToastMillis = 0L
    private var lastGuardScanAtMillis = 0L
    private var lastGuardWindowPackage: String? = null
    private var cachedAppIdentities: List<String>? = null
    private var cachedAppIdentitiesAtMillis = 0L
    private var pendingProtectionSettle: Job? = null

    private enum class UninstallAttempt {
        NONE, APP_INFO_SCREEN, CONFIRMATION_DIALOG, ADMIN_LIST_SCREEN, PROTECTION_SCREEN
    }

    /**
     * Precise row-level guard: when the user taps a row that names this app inside a
     * Settings surface (Accessibility list, Display-over list, app lists in the
     * security center…), they are heading for a protection toggle. The click is
     * intercepted directly — no screen-wide scan needed — so generic Settings pages
     * are not disturbed unless BreakFree itself was clicked.
     */
    private fun checkGuardRowClick(event: AccessibilityEvent?): Boolean {
        if (!UninstallLock.tamperLocked(this)) return false
        val e = event ?: return false
        if (e.eventType !in listOf(
                AccessibilityEvent.TYPE_VIEW_CLICKED,
                AccessibilityEvent.TYPE_VIEW_LONG_CLICKED
            )
        ) return false
        val pkg = e.packageName?.toString()?.lowercase(Locale.ROOT) ?: return false
        if (!isUninstallSurfacePackage(pkg)) return false

        val identities = appIdentities()

        // The clicked node's own subtree: rows usually place the app name as a sibling
        // TextView inside the row container. The scan is capped deliberately — nothing
        // outside the tapped row may be considered, so tapping OTHER rows (Google, Phone…)
        // that merely share a screen with BreakFree never triggers the guard. Big
        // scrollable containers (a whole list) are rejected even when they are the source.
        val source = e.source
        if (source != null && !source.isScrollable && source.childCount <= 12) {
            if (subtreeContainsIdentity(source, identities, 60)) {
                blockAndDismiss(closeSettings = false)
                return true
            }
        }

        // Fallback: the row name may sit on the tapped node's own text chain.
        var node = nodeParentOf(source)
        var hops = 0
        val ownText = StringBuilder()
        while (node != null && hops < 4) {
            node.text?.let { ownText.append(it.toString()).append(' ') }
            node.contentDescription?.let { ownText.append(it.toString()).append(' ') }
            node = nodeParentOf(node)
            hops++
        }
        val own = ownText.toString().lowercase(Locale.ROOT)
        if (identities.any { own.contains(it) }) {
            blockAndDismiss(closeSettings = false)
            return true
        }
        return false
    }

    private fun nodeParentOf(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? =
        try { node?.parent } catch (_: Exception) { null }

    /** Depth-limited check whether any text in the subtree names this app. */
    private fun subtreeContainsIdentity(
        start: AccessibilityNodeInfo,
        identities: List<String>,
        maxNodes: Int
    ): Boolean {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(start)
        var visited = 0
        while (stack.isNotEmpty() && visited < maxNodes) {
            val cur = stack.removeLast()
            visited++
            val text = buildString {
                cur.text?.let { append(it.toString()) }
                append(' ')
                cur.contentDescription?.let { append(it.toString()) }
            }.lowercase(Locale.ROOT)
            if (identities.any { text.contains(it) }) return true
            for (i in 0 until minOf(cur.childCount, 8)) {
                cur.getChild(i)?.let { stack.addLast(it) }
            }
        }
        return false
    }

    /**
     * When the 90-day commitment is active, stop the user on the one screen that can
     * actually remove this app: its App Info page (the Uninstall / Force stop / Clear
     * data controls) or the confirmation dialog that follows.
     *
     * Nothing else is touched. Earlier versions reacted to any Settings window that
     * merely mentioned the app plus words like "accessibility" or "device admin", which
     * closed the app while the user was only switching tasks or browsing Settings.
     *
     * @return true if an unauthorized deactivation/uninstall attempt was intercepted.
     */
    private fun checkAndGuardUninstallLock(): Boolean {
        // Only enforce while the lock is active and within the 90-day window
        if (!UninstallLock.tamperLocked(this)) {
            return false
        }

        // The event's package is only a hint: SystemUI and the launcher keep posting
        // events while Settings — or this app — is in front. What matters is the window
        // the user can actually see.
        val root = rootInActiveWindow ?: return false
        val windowPackage = root.packageName?.toString()?.lowercase(Locale.ROOT) ?: return false
        if (windowPackage == this.packageName) return false
        if (!isUninstallSurfacePackage(windowPackage)) return false

        val now = SystemClock.elapsedRealtime()
        // A window the user has not been on before must be scanned immediately: the
        // rate limit exists for repeated events on the same screen, not for letting a
        // freshly-opened deactivation screen slip by unguarded. Otherwise someone could
        // sprint through Settings and flip a protected switch before the first scan.
        if (windowPackage != lastGuardWindowPackage) {
            lastGuardWindowPackage = windowPackage
            lastGuardScanAtMillis = 0L
        }
        if (now - lastGuardScanAtMillis < GUARD_SCAN_INTERVAL_MS) return false
        lastGuardScanAtMillis = now

        val attempt = try {
            scanForUninstallAttempt(root)
        } catch (e: Exception) {
            Log.e(TAG, "Error inspecting settings node tree", e)
            UninstallAttempt.NONE
        }

        return when (attempt) {
            UninstallAttempt.NONE -> false
            UninstallAttempt.APP_INFO_SCREEN -> {
                blockAndDismiss(closeSettings = false)
                true
            }
            UninstallAttempt.CONFIRMATION_DIALOG -> {
                blockAndDismiss(closeSettings = true)
                true
            }
            UninstallAttempt.ADMIN_LIST_SCREEN -> {
                blockAndDismiss(closeSettings = false)
                true
            }
            UninstallAttempt.PROTECTION_SCREEN -> {
                // Transition animations momentarily merge two Settings pages into one
                // node tree: the outgoing detail (which owns the toggle) plus the
                // incoming list (which owns the app name) can fake a protection screen.
                // A settled tree shows only one page — so re-scan once the animation
                // stops and block only when the steady state is still a protected
                // screen naming this app.
                if (pendingProtectionSettle?.isActive != true) {
                    pendingProtectionSettle = serviceScope.launch {
                        delay(PROTECTION_SETTLE_DELAY_MS)
                        try {
                            val fresh = rootInActiveWindow ?: return@launch
                            val freshPkg = fresh.packageName?.toString()?.lowercase(Locale.ROOT)
                                ?: return@launch
                            if (!isUninstallSurfacePackage(freshPkg)) return@launch
                            val reattempt = try {
                                scanForUninstallAttempt(fresh)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error re-inspecting settings node tree", e)
                                UninstallAttempt.NONE
                            }
                            if (reattempt != UninstallAttempt.NONE) {
                                // The steady state is a guarded surface naming this app.
                                blockAndDismiss(closeSettings = false)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Protection re-scan failed", e)
                        }
                    }
                }
                true
            }
        }
    }

    /**
     * Windows that can host an uninstall: system Settings, the package installer, the
     * device-admin screens and the OEM security centers. The launcher, the recents
     * screen and SystemUI are deliberately absent — they are never where an app is
     * removed, and scanning them is what closed the app during ordinary use.
     */
    private fun isUninstallSurfacePackage(pkg: String): Boolean =
        pkg == "android" ||
            pkg.contains("settings") ||
            pkg.contains("packageinstaller") ||
            pkg.contains("security") ||
            pkg.contains("safecenter") ||
            pkg.contains("systemmanager")

    /**
     * Strings that identify this app on a system screen: the package name, the label the
     * system currently shows and the branded name. Cached for the life of the service.
     */
    private fun appIdentities(): List<String> {
        val now = SystemClock.elapsedRealtime()
        cachedAppIdentities?.let { cached ->
            if (now - cachedAppIdentitiesAtMillis < IDENTITIES_CACHE_TTL_MS) return cached
        }

        val identities = linkedSetOf(
            packageName.lowercase(Locale.ROOT),
            "breakfree",
            "break free",
            "تعافي"
        )

        try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info)?.toString()
                ?.lowercase(Locale.ROOT)
                ?.takeIf { it.isNotBlank() }
                ?.let(identities::add)
        } catch (e: Exception) {
            Log.w(TAG, "Could not read own application label", e)
        }

        STEALTH_ALIASES.forEach { alias ->
            try {
                val component = ComponentName(this, alias)
                if (packageManager.getComponentEnabledSetting(component) ==
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                ) {
                    packageManager.getActivityInfo(component, 0).loadLabel(packageManager)
                        ?.toString()
                        ?.lowercase(Locale.ROOT)
                        ?.takeIf { it.isNotBlank() }
                        ?.let(identities::add)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not read alias label for $alias", e)
            }
        }

        return identities.toList().also {
            cachedAppIdentities = it
            cachedAppIdentitiesAtMillis = now
        }
    }

    private fun blockAndDismiss(closeSettings: Boolean) {
        Log.w(TAG, "Intercepted attempt to deactivate or uninstall BreakFree during 90-day lock!")
        // A confirmation dialog is cancelled and the user is sent home, so the removal
        // cannot be completed by accident. The App Info screen is only dismissed one step
        // back: looking at the app's own settings is not a reason to throw the user out
        // of Settings.
        performGlobalAction(GLOBAL_ACTION_BACK)
        if (closeSettings) performGlobalAction(GLOBAL_ACTION_HOME)

        val now = SystemClock.elapsedRealtime()
        if (now - lastGuardToastMillis > 2500L) {
            lastGuardToastMillis = now
            val daysLeft = UninstallLock.daysRemaining(this)
            val msg = when (currentLanguage()) {
                AppLanguage.ARABIC -> "تعافي: قفل الـ 90 يوماً مفعّل (متبقي $daysLeft يوماً). تم منع إلغاء التنشيط أو الحذف لحماية تعافيك."
                AppLanguage.FRENCH -> "BreakFree : verrou de 90 jours actif ($daysLeft jours restants). Désactivation et désinstallation sont protégées."
                else -> "BreakFree: 90-day recovery lock is active ($daysLeft days remaining). Uninstallation and deactivation are guarded."
            }
            android.widget.Toast.makeText(applicationContext, msg, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    /** A control that can remove, force-stop or wipe this app. */
    private fun isUninstallControl(content: String): Boolean {
        if (content.isBlank()) return false

        val trimmed = content.trim()
        if (trimmed == "إزالة" || trimmed == "uninstall") return true

        return content.contains("uninstall") ||
            content.contains("deactivate") ||
            content.contains("force stop") ||
            content.contains("clear data") ||
            content.contains("clear storage") ||
            content.contains("إلغاء التثبيت") ||
            content.contains("إلغاء التنشيط") ||
            content.contains("إزالة التطبيق") ||
            content.contains("حذف التطبيق") ||
            content.contains("إيقاف إجباري") ||
            content.contains("مسح البيانات") ||
            content.contains("مسح التخزين")
    }

    /**
     * The standard system dialog shown when an active device administrator is removed:
     * "This app is an active device administrator and must be deactivated before
     * uninstalling." Only this app holds that role, so the text alone is unambiguous.
     */
    private fun isDeactivateDialogText(content: String): Boolean {
        if (content.isBlank()) return false
        return content.contains("deactivate & uninstall") ||
            content.contains("deactivate and uninstall") ||
            content.contains("active device administrator") ||
            content.contains("must be deactivated") ||
            content.contains("إلغاء التنشيط والإلغاء") ||
            content.contains("إلغاء التنشيط والإزالة") ||
            content.contains("مشرف نشط للجهاز") ||
            content.contains("مشرف الجهاز النشط") ||
            content.contains("يجب إلغاء تنشيطه")
    }

    /**
     * The system screen that lists device administrators ("Device admin apps" on stock
     * Android, the security center equivalent on MIUI). Visiting it while the 90-day
     * commitment is running means the user is going for the "Deactivate" button, so the
     * guard treats it like the App Info page: it is disarmed by pressing back, not by
     * being thrown out of Settings.
     */
    private fun isAdminListText(content: String): Boolean {
        if (content.isBlank()) return false
        return content.contains("device admin") ||
            content.contains("device administrator") ||
            content.contains("device managers") ||
            content.contains("إدارة الجهاز") ||
            content.contains("مسؤول الجهاز") ||
            content.contains("مسؤولي الجهاز") ||
            content.contains("مشرفي الجهاز") ||
            content.contains("مشرف هنا") ||
            content.contains("settings device admin")
    }

    /**
     * Permission screens that kill a protection surface while the 90-day commitment runs:
     * the per-app Accessibility service screen (kills the monitor), the app's notification
     * settings (kills the guard/grace notifications) and "Display over other apps" (kills
     * the step-back overlay). The wording alone is not enough — the screen-level rule
     * additionally requires a toggle switch to be visible, so the *list* pages (which
     * merely mention BreakFree) stay safe; a click on the actual row is caught by
     * [checkGuardRowClick] instead.
     */
    private fun isProtectionScreenText(content: String): Boolean {
        if (content.isBlank()) return false
        return content.contains("accessibility") ||
            content.contains("إمكانية الوصول") ||
            content.contains("ميزات الوصول") ||
            content.contains("قارئ الشاشة") ||
            content.contains("notification") ||
            content.contains("تنبيهات") ||
            content.contains("الإشعارات") ||
            content.contains("إدارة التنبيهات") ||
            // Overlay / display-over-apps wording: stock Android and MIUI.
            content.contains("display over") ||
            content.contains("over other apps") ||
            content.contains("appear on top") ||
            content.contains("draw over") ||
            content.contains("pop-up") ||
            content.contains("pop up") ||
            content.contains("popup") ||
            content.contains("floating window") ||
            content.contains("الظهور فوق") ||
            content.contains("فوق التطبيقات") ||
            content.contains("عرض فوق التطبيقات") ||
            content.contains("الظهور أثناء") ||
            content.contains("نافذة منبثقة") ||
            content.contains("نافذة منبثقه") ||
            content.contains("النوافذ المنبثقة") ||
            content.contains("النوافذ المنبثقه") ||
            content.contains("نوافذ منبثقة") ||
            content.contains("نوافذ منبثقه") ||
            content.contains("عرض نوافذ")
    }

    /**
     * The one screen that can only exist for this app: the Accessibility detail screen,
     * which renders this app's own service description. Matching our description text
     * (or the "Restricted setting" dialog Android shows when the service is toggled for
     * a sideloaded app) is unambiguous, so it can trigger without a switch requirement.
     */
    private fun isOwnAccessibilityDetailText(content: String): Boolean {
        if (content.isBlank()) return false
        return content.contains("visible text and address bar") ||
            content.contains("يقرأ هذا التطبيق النص الظاهر") ||
            content.contains("restricted setting") ||
            content.contains("الإعدادات المقيّدة")
    }

    /**
     * Walks the visible Settings/installer window and reports whether the user is on the
     * screen that removes this app. Two things must both be present: something that names
     * this app, and a control that removes it. A stray "Accessibility" or "Device admin"
     * row on the Settings home screen no longer matches, and neither does the recents
     * screen showing the app's card.
     */
    private fun scanForUninstallAttempt(root: AccessibilityNodeInfo): UninstallAttempt {
        val identities = appIdentities()
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(root)

        var visited = 0
        var mentionsApp = false
        var hasUninstallControl = false
        var hasDeactivateDialog = false
        var hasAdminList = false
        var hasProtectionScreen = false
        var hasOwnAccessibilityDetail = false
        var hasSwitchNode = false
        var mentionText = "?"
        var protectionText = "?"
        var switchSource = "?"
        var mentionBounds = android.graphics.Rect()
        var switchBounds = android.graphics.Rect()
        var hasMentionBounds = false
        var hasSwitchBounds = false

        while (stack.isNotEmpty() && visited < GUARD_MAX_NODES) {
            val node = stack.removeLast()
            visited++

            val text = node.text?.toString()?.lowercase(Locale.ROOT).orEmpty()
            val desc = node.contentDescription?.toString()?.lowercase(Locale.ROOT).orEmpty()
            val combined = "$text $desc".trim()

            if (combined.isNotEmpty()) {
                if (!mentionsApp && identities.any { combined.contains(it) }) {
                    mentionsApp = true
                    mentionText = "'${node.text}' bounds=${run { val r = android.graphics.Rect(); node.getBoundsInScreen(r); r.toShortString() }}"
                    if (!hasMentionBounds) { node.getBoundsInScreen(mentionBounds); hasMentionBounds = true }
                }
                if (!hasUninstallControl && isUninstallControl(combined)) {
                    hasUninstallControl = true
                }
                if (!hasDeactivateDialog && isDeactivateDialogText(combined)) {
                    hasDeactivateDialog = true
                }
                if (!hasAdminList && isAdminListText(combined)) {
                    hasAdminList = true
                }
                if (!hasProtectionScreen && isProtectionScreenText(combined)) {
                    hasProtectionScreen = true
                    protectionText = "'${node.text}' bounds=${run { val r = android.graphics.Rect(); node.getBoundsInScreen(r); r.toShortString() }}"
                }
                if (!hasOwnAccessibilityDetail && isOwnAccessibilityDetailText(combined)) {
                    hasOwnAccessibilityDetail = true
                }
            }
            val className = node.className?.toString() ?: ""
            val viewId = node.viewIdResourceName ?: ""
            // A Compose-rendered toggle (modern Settings pages) exposes no Switch class:
            // it is a plain clickable View marked checkable=true. Count both.
            if (!hasSwitchNode &&
                (node.isCheckable || className.contains("Switch") || className.contains("CheckBox") ||
                    viewId.contains("switch", ignoreCase = true) ||
                    viewId.contains("check_box", ignoreCase = true))
            ) {
                hasSwitchNode = true
                switchSource = "cls=$className checkable=${node.isCheckable} " +
                    "text='${node.text}' bounds=${run { val r = android.graphics.Rect(); node.getBoundsInScreen(r); r.toShortString() }}"
                if (!hasSwitchBounds) { node.getBoundsInScreen(switchBounds); hasSwitchBounds = true }
            }

            val childCount = minOf(node.childCount, GUARD_MAX_CHILDREN)
            for (i in 0 until childCount) {
                node.getChild(i)?.let { stack.addLast(it) }
            }
        }

        val alignedSwitch = !hasSwitchNode || !hasMentionBounds || !hasSwitchBounds ||
            kotlin.math.abs(mentionBounds.centerX() - switchBounds.centerX()) < PROTECTION_ALIGN_TOLERANCE_PX

        return when {
            hasDeactivateDialog -> UninstallAttempt.CONFIRMATION_DIALOG
            mentionsApp && hasUninstallControl -> UninstallAttempt.APP_INFO_SCREEN
            hasOwnAccessibilityDetail && mentionsApp -> UninstallAttempt.PROTECTION_SCREEN
            mentionsApp && hasAdminList -> UninstallAttempt.ADMIN_LIST_SCREEN
            // Protection-screen wording only counts when the page also shows a toggle,
            // which keeps the plain list pages out of the guard. During a page transition
            // the outgoing and incoming pages briefly coexist in one node tree: the app
            // name comes from one page and the toggle from the other (their horizontal
            // centers are far apart). Only accept a toggle whose horizontal center lines
            // up with the app name's (a genuine single page).
            mentionsApp && hasProtectionScreen && hasSwitchNode && alignedSwitch -> UninstallAttempt.PROTECTION_SCREEN
            else -> UninstallAttempt.NONE
        }.also { attempt ->
            if (attempt != UninstallAttempt.NONE) {
                Log.d(
                    TAG,
                    "GuardScan pkg=${root.packageName} attempt=$attempt mentionsApp=$mentionsApp " +
                        "uninstallControl=$hasUninstallControl adminList=$hasAdminList " +
                        "protection=$hasProtectionScreen switch=$hasSwitchNode " +
                        "ownDetail=$hasOwnAccessibilityDetail dialog=$hasDeactivateDialog " +
                        "mention=$mentionText protection=$protectionText switchSrc=$switchSource"
                )
            }
        }
    }

    /**
     * Captures the window on the main thread, then walks it on a background thread.
     * Overlapping events are dropped and scans are rate-limited.
     */
    private fun scheduleScan(packageName: String) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastScanAtMillis < MIN_SCAN_INTERVAL_MS) return
        if (!scanInFlight.compareAndSet(false, true)) return
        lastScanAtMillis = now

        val prefs = preferences

        serviceScope.launch {
            try {
                val root = rootInActiveWindow ?: return@launch
                val includeKeywords = prefs?.keywordFilterEnabled ?: true
                val outcome = withContext(Dispatchers.Default) {
                    scanWindow(root, packageName, includeKeywords)
                }

                if (outcome.matched != null) {
                    if (recentlyLeftAlone(outcome.surfaceKey)) {
                        Log.d(TAG, "Re-match on a surface the user just left; staying quiet")
                        return@launch
                    }
                    // The matched word is deliberately not logged: the service promises
                    // that page text never leaves memory, and logcat ends up in bug
                    // reports. The fact of a match is enough for diagnostics.
                    Log.w(TAG, "Explicit content detected. Starting step-back window.")
                    graceTriggerSurface = outcome.surfaceKey
                    graceTriggerPackage = packageName
                    MonitoringManager.triggerGracePeriod(
                        detectedTarget = outcome.matched,
                        totalSeconds = prefs?.graceBufferSeconds() ?: MonitoringPreferences.GRACE_BUFFER_SECONDS
                    )
                } else {
                    resolveIfNavigatedAway(outcome.addressBarText)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error while inspecting accessibility node tree", e)
            } finally {
                scanInFlight.set(false)
            }
        }
    }

    /**
     * True when this exact surface was just left. When it cannot be identified, only a
     * short grace window applies — long enough that one navigation cannot bounce
     * straight back into a new countdown, short enough that a different surface is
     * still flagged promptly.
     *
     * The cooldown only applies when the **same surface** is still showing. If the key
     * is null/blank (e.g. the browser was closed and reopened showing a different page
     * or new-tab page), the cooldown is skipped so detection is immediate.
     */
    private fun recentlyLeftAlone(currentSurface: String?): Boolean {
        if (lastResolvedAtMillis == 0L) return false
        val elapsed = SystemClock.elapsedRealtime() - lastResolvedAtMillis
        val resolved = lastResolvedSurface

        // When the surface cannot be identified (or the last resolved one is unknown),
        // we cannot tell if it is the same page or a brand-new session. Use a very short
        // cooldown just to prevent the same accessibility-event burst from re-triggering,
        // but not long enough to delay detection after a browser restart.
        if (resolved == null || currentSurface.isNullOrBlank()) {
            return elapsed <= RESOLVED_UNKNOWN_PAGE_COOLDOWN_MS
        }
        // Only suppress when it is literally the same surface that was just left.
        return elapsed <= RESOLVED_PAGE_COOLDOWN_MS && currentSurface == resolved
    }

    /**
     * If the address bar now points somewhere else and that page is not itself explicit,
     * the user has navigated away and the streak is safe. Comparing against the address
     * captured at trigger time avoids resolving on pages whose *content* matched while
     * the URL stayed the same.
     */
    private fun resolveIfNavigatedAway(currentAddress: String?) {
        if (!MonitoringManager.gracePeriodState.value.isGraceActive) return
        val triggerAddress = graceTriggerSurface ?: return
        // App surfaces carry an `app://` key and have no address to compare. Moving
        // between apps is handled by the package check in onAccessibilityEvent.
        if (triggerAddress.startsWith("app://")) return
        if (currentAddress.isNullOrBlank() || currentAddress == triggerAddress) return
        if (ExplicitContentDetector.isExplicitDomain(currentAddress).first) return

        // The address itself is not logged: it can contain a search query.
        Log.i(TAG, "Browser navigated to a safe page. Step-back detected.")
        MonitoringManager.stepBackAndResolve()
    }

    /**
     * [surfaceKey] identifies what was scanned so the cooldown can tell "the same page
     * again" from "a different page": the URL for a browser, `app://<package>` for an
     * app, which has no address bar.
     */
    private data class ScanOutcome(
        val matched: String?,
        val addressBarText: String?,
        val surfaceKey: String? = addressBarText
    )

    /**
     * Bounded depth-first walk. Runs on a background thread.
     *
     * The walk separates the **browser chrome** (address bar, toolbar, suggestion
     * drop-down, tab strip, new-tab surface) from **page content**. The chrome carries
     * text the user is not reading as a page: a focused omnibox lists past searches,
     * and a new-tab page shows "most visited" tiles built from browsing history. Either
     * can contain explicit words from an earlier session while the user is doing
     * nothing wrong, so neither may trigger the reminder.
     *
     * Three rules keep them out:
     *  1. A first pass reads the address bar, detects whether the omnibox has focus,
     *     and notices whether a suggestion drop-down is on screen.
     *  2. In search mode (focused omnibox or visible suggestions) only the address-bar
     *     host is checked. The user is typing, not viewing.
     *  3. Content is scanned only when the address bar holds a real, loaded address.
     *     A blank or placeholder bar means the browser is showing its own home screen.
     *
     * Chrome subtrees are skipped **whole** rather than node by node: its suggestion
     * rows are plain `line_1`/`line_2` TextViews whose own ids say nothing.
     */
    private fun scanWindow(root: AccessibilityNodeInfo, packageName: String, includeKeywords: Boolean): ScanOutcome {
        // Social/video apps have no address bar and no shared chrome ids, so they get
        // their own walk (see [scanAppWindow]).
        if (packageName !in ExplicitContentDetector.monitoredBrowsers) {
            return scanAppWindow(root, packageName, includeKeywords)
        }

        var addressBarText: String? = null
        var addressBarIsEditable = false
        var omniboxFocused = false
        var suggestionsVisible = false
        val stack = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
        stack.addLast(root to 0)
        var visited = 0

        // ── First pass: read the address bar, detect "search mode" ─────────────
        while (stack.isNotEmpty() && visited < MAX_NODES) {
            val (node, depth) = stack.removeLast()
            visited++

            val viewId = node.viewIdResourceName
            if (viewId != null) {
                if (BrowserChromeHeuristics.isAddressBarId(viewId)) {
                    val candidate = node.text?.toString()?.trim().orEmpty()
                    // Some browsers expose both a container and the editable field.
                    // The editable one carries the real address, so it wins.
                    if (candidate.isNotEmpty() &&
                        (addressBarText == null || (node.isEditable && !addressBarIsEditable))
                    ) {
                        addressBarText = candidate
                        addressBarIsEditable = node.isEditable
                    }
                    if (node.isFocused) omniboxFocused = true
                } else if (node.isFocused && node.isEditable && BrowserChromeHeuristics.isChromeId(viewId)) {
                    // Focus can land on a wrapper around the field, not the field itself.
                    omniboxFocused = true
                }

                // An open drop-down is proof of search mode even if focus is reported
                // late: Chrome builds it only while the omnibox is active.
                if (BrowserChromeHeuristics.isSuggestionAreaId(viewId)) suggestionsVisible = true
            }

            if (depth < MAX_DEPTH) {
                val childCount = minOf(node.childCount, MAX_CHILDREN)
                for (i in 0 until childCount) {
                    val child = node.getChild(i) ?: continue
                    stack.addLast(child to (depth + 1))
                }
            }
        }

        // The address-bar host is safe to check in every state: it is the page the user
        // asked for, and typing an adult domain is still an intention worth interrupting.
        val domainCheck = ExplicitContentDetector.isExplicitDomain(addressBarText)
        if (domainCheck.first) return ScanOutcome(domainCheck.second, addressBarText, addressBarText)

        // Search mode: the drop-down shows history from past sessions and the page
        // behind it is stale. Nothing here says the user is viewing explicit content.
        if (omniboxFocused || suggestionsVisible) return ScanOutcome(null, addressBarText, addressBarText)

        // No loaded page: the browser is on its new-tab/home screen, whose tiles are
        // the user's own browsing history. Scanning them would flag the past, not now.
        if (!BrowserChromeHeuristics.looksLikeLoadedAddress(addressBarText)) return ScanOutcome(null, addressBarText, addressBarText)

        // With keyword filtering off only the host matters, and that was checked above.
        if (!includeKeywords) return ScanOutcome(null, addressBarText, addressBarText)

        // ── Second pass: page content only ─────────────────────────────────────
        var matched: String? = null
        stack.clear()
        stack.addLast(root to 0)
        visited = 0

        while (stack.isNotEmpty() && visited < MAX_NODES) {
            val (node, depth) = stack.removeLast()
            visited++

            val viewId = node.viewIdResourceName
            if (viewId != null && BrowserChromeHeuristics.isChromeId(viewId)) {
                // Browser chrome is not page content: skip the whole subtree so the
                // suggestion rows inside it cannot be reached through their children.
                continue
            }

            val text = node.text?.toString()
            if (!text.isNullOrBlank()) {
                val check = ExplicitContentDetector.isExplicitContent(text, includeKeywords)
                if (check.first) {
                    matched = check.second
                    break
                }
            }

            val description = node.contentDescription?.toString()
            if (!description.isNullOrBlank()) {
                val check = ExplicitContentDetector.isExplicitContent(description, includeKeywords)
                if (check.first) {
                    matched = check.second
                    break
                }
            }

            if (depth < MAX_DEPTH) {
                val childCount = minOf(node.childCount, MAX_CHILDREN)
                for (i in 0 until childCount) {
                    val child = node.getChild(i) ?: continue
                    stack.addLast(child to (depth + 1))
                }
            }
        }

        return ScanOutcome(matched, addressBarText, addressBarText)
    }

    /**
     * Content walk for the social/video apps the user opted into.
     *
     * Apps expose no address bar and no shared resource-ids, so the rules are generic:
     *
     *  1. If any editable field has focus (search box, comment box, message), the window
     *     is not scanned at all. That is the app equivalent of browser search mode: the
     *     list under the keyboard is past searches, not content the user is viewing.
     *  2. Suggestion/autocomplete containers and editable fields are skipped whole as a
     *     second line of defence, so a stray history row cannot raise a reminder.
     *  3. Every other node that carries text is matched, on-device.
     *
     * Images and video frames are invisible to accessibility, so this is text-only:
     * titles, captions, tweets, post bodies and descriptions.
     */
    private fun scanAppWindow(
        root: AccessibilityNodeInfo,
        packageName: String,
        includeKeywords: Boolean
    ): ScanOutcome {
        // Apps have no URL, so the package is the closest equivalent of "the same
        // screen" for the post-resolution cooldown.
        val surfaceKey = "app://$packageName"
        val stack = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
        stack.addLast(root to 0)
        var visited = 0

        // ── First pass: is the user typing anywhere? ───────────────────────────
        while (stack.isNotEmpty() && visited < MAX_NODES) {
            val (node, depth) = stack.removeLast()
            visited++

            if (node.isFocused && isEditableNode(node)) {
                return ScanOutcome(null, null, surfaceKey)
            }

            if (depth < MAX_DEPTH) {
                val childCount = minOf(node.childCount, MAX_CHILDREN)
                for (i in 0 until childCount) {
                    val child = node.getChild(i) ?: continue
                    stack.addLast(child to (depth + 1))
                }
            }
        }

        // ── Second pass: on-screen text, minus search fields and suggestions ───
        stack.clear()
        stack.addLast(root to 0)
        visited = 0

        while (stack.isNotEmpty() && visited < MAX_NODES) {
            val (node, depth) = stack.removeLast()
            visited++

            val viewId = node.viewIdResourceName
            if (viewId != null && BrowserChromeHeuristics.isSuggestionAreaId(viewId)) {
                // Drop-down of past searches: skip the whole subtree.
                continue
            }
            if (isEditableNode(node)) {
                // A search field keeps showing the last query; that is not content.
                continue
            }

            val text = node.text?.toString()
            if (!text.isNullOrBlank()) {
                val check = ExplicitContentDetector.isExplicitContent(text, includeKeywords)
                if (check.first) return ScanOutcome(check.second, null, surfaceKey)
            }

            val description = node.contentDescription?.toString()
            if (!description.isNullOrBlank()) {
                val check = ExplicitContentDetector.isExplicitContent(description, includeKeywords)
                if (check.first) return ScanOutcome(check.second, null, surfaceKey)
            }

            if (depth < MAX_DEPTH) {
                val childCount = minOf(node.childCount, MAX_CHILDREN)
                for (i in 0 until childCount) {
                    val child = node.getChild(i) ?: continue
                    stack.addLast(child to (depth + 1))
                }
            }
        }

        return ScanOutcome(null, null, surfaceKey)
    }

    /** `isEditable` is not set by every app, so the class name is the fallback. */
    private fun isEditableNode(node: AccessibilityNodeInfo): Boolean =
        node.isEditable ||
            node.className?.toString()?.contains("EditText", ignoreCase = true) == true

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val language = currentLanguage()
            // In Stealth Mode even the channel name in system settings must not give the
            // app away, so the channel is re-created with neutral copy.
            val stealth = com.aistudio.breakfree.recovr.stealth.StealthPreferences.isStealthEnabled(this) &&
                com.aistudio.breakfree.recovr.stealth.StealthPreferences.discreetNotifications(this)
            val channel = NotificationChannel(
                CHANNEL_ID,
                Translations.t(
                    if (stealth) "stealth_notif_channel" else "grace_notification_channel",
                    language
                ),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = Translations.t(
                    if (stealth) "stealth_notif_channel_desc" else "grace_notification_channel_desc",
                    language
                )
                enableVibration(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * The detected address is deliberately not shown: it can contain a search query the
     * user would not want on a lock screen or in a notification shade.
     */
    private fun showOrUpdateGraceNotification(
        remainingSeconds: Int,
        totalSeconds: Int
    ) {
        // Resolves to the enabled launcher component, so tapping still opens the app when
        // Stealth Mode has disabled MainActivity in favour of an alias.
        val appIntent = appLaunchIntent(this)
        val appPendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        val timeString = String.format(Locale.US, "%02d:%02d", mins, secs)
        val language = currentLanguage()

        val remainingLine = Translations.t("grace_notification_remaining", language)
            .replace("{time}", timeString)
            .replace("{total}", totalSeconds.toString())

        val stealth = com.aistudio.breakfree.recovr.stealth.StealthPreferences.isStealthEnabled(this) &&
            com.aistudio.breakfree.recovr.stealth.StealthPreferences.discreetNotifications(this)

        // Nothing in the shade may mention recovery, urges or explicit content while
        // Stealth Mode is on: neutral copy is used end to end.
        val detectedLine = Translations.t(
            if (stealth) "grace_notification_detected_stealth" else "grace_notification_detected",
            language
        )
        val title = Translations.t(
            if (stealth) "stealth_grace_title" else "grace_notification_title",
            language
        )
        val bodyText = Translations.t(
            if (stealth) "stealth_grace_text" else "grace_notification_text",
            language
        )
        val staysLine = Translations.t(
            if (stealth) "stealth_grace_stays" else "grace_notification_stays",
            language
        )
        val notRecordedLine = Translations.t(
            if (stealth) "stealth_grace_not_recorded" else "grace_notification_not_recorded",
            language
        )

        val smallIcon = R.drawable.ic_stat_shield

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle("$title ($timeString)")
            .setContentText(bodyText)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    detectedLine + "\n" +
                        remainingLine + "\n" +
                        staysLine + "\n" +
                        notRecordedLine
                )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            // Ongoing + no clear action: the shade entry cannot be swiped away while the
            // countdown runs. It disappears only when the state resolves (tab closed,
            // browser left, or the window expired) because the service cancels it.
            .setOngoing(true)
            .setAutoCancel(false)
            // The countdown updates every second; never re-alert on those updates.
            .setOnlyAlertOnce(true)
            // Keep the detected target off the lock screen.
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(appPendingIntent)

        notificationManager?.notify(NOTIFICATION_ID, builder.build())
    }

    private fun cancelGraceNotification() {
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    override fun onInterrupt() {
        Log.w(TAG, "ContentMonitoringAccessibilityService interrupted.")
        // The countdown may still be running, but nothing is watching the screen any
        // more, so the window must not stay on screen as a stale reminder.
        overlayController?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        graceStateObservationJob?.cancel()
        overlayController?.destroy()
        overlayController = null
        serviceScope.cancel()
        MonitoringManager.setServiceConnected(false)
        cancelGraceNotification()
    }
}
