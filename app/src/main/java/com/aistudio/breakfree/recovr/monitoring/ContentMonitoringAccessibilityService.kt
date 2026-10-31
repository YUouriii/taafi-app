package com.aistudio.breakfree.recovr.monitoring

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
        return if (code == "ar") AppLanguage.ARABIC else AppLanguage.ENGLISH
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return

        // Our own windows are never monitored content — including the reminder overlay
        // itself, which is an application overlay owned by this package. Without this
        // check, drawing the overlay looks exactly like "the user left the app", which
        // resolves the countdown the overlay is showing and makes it flicker.
        if (packageName == this.packageName) return

        // Guard against deactivation, uninstall, or settings tampering during 90-day lock
        if (checkAndGuardUninstallLock(packageName, event)) return

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

    /**
     * When the 90-day commitment is active, prevent the user from deactivating device admin,
     * clicking "Deactivate & uninstall", force-stopping, clearing data, uninstalling BreakFree,
     * or resetting the phone via Settings.
     *
     * @return true if an unauthorized deactivation/uninstall attempt was intercepted.
     */
    private fun checkAndGuardUninstallLock(packageName: String, event: AccessibilityEvent?): Boolean {
        // Only enforce while the lock is active and within the 90-day window
        if (!UninstallLock.isActive(this) || UninstallLock.daysRemaining(this) <= 0) {
            return false
        }

        val pkgLower = packageName.lowercase(Locale.ROOT)

        // 1. Check event text directly (fast path for system alert dialogs like "Deactivate & uninstall")
        if (event != null) {
            val eventTexts = event.text.joinToString(" ") { it.toString() }.lowercase(Locale.ROOT)
            if (isTamperContent(eventTexts)) {
                blockAndDismiss()
                return true
            }
        }

        // 2. Filter relevant packages where tamper actions, package managers or dialogs live
        val isTargetPackage = pkgLower == "android" ||
            pkgLower.contains("systemui") ||
            pkgLower.contains("settings") ||
            pkgLower.contains("packageinstaller") ||
            pkgLower.contains("security") ||
            pkgLower.contains("safecenter") ||
            pkgLower.contains("systemmanager")

        if (!isTargetPackage) return false

        val root = rootInActiveWindow ?: return false
        val shouldBlock = try {
            scanForTampering(root)
        } catch (e: Exception) {
            Log.e(TAG, "Error inspecting settings node tree", e)
            false
        }

        if (shouldBlock) {
            blockAndDismiss()
            return true
        }

        return false
    }

    private fun blockAndDismiss() {
        Log.w(TAG, "Intercepted attempt to deactivate or uninstall BreakFree during 90-day lock!")
        // Dismiss dialog/screen and return to safety
        performGlobalAction(GLOBAL_ACTION_BACK)
        performGlobalAction(GLOBAL_ACTION_HOME)

        val now = SystemClock.elapsedRealtime()
        if (now - lastGuardToastMillis > 2500L) {
            lastGuardToastMillis = now
            val daysLeft = UninstallLock.daysRemaining(this)
            val msg = if (currentLanguage() == AppLanguage.ARABIC) {
                "تعافي: قفل الـ 90 يوماً مفعّل (متبقي $daysLeft يوماً). تم منع إلغاء التنشيط أو الحذف لحماية تعافيك."
            } else {
                "BreakFree: 90-day recovery lock is active ($daysLeft days remaining). Uninstallation and deactivation are guarded."
            }
            android.widget.Toast.makeText(applicationContext, msg, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    /** Fast check on string content to identify deactivation/uninstallation dialogs. */
    private fun isTamperContent(content: String): Boolean {
        if (content.isBlank()) return false

        // Standard Android system dialog when user taps "Uninstall" on an active device admin:
        // "This app is an active device administrator and must be deactivated before uninstalling."
        val isDeactivateDialog = content.contains("deactivate & uninstall") ||
            content.contains("deactivate and uninstall") ||
            content.contains("active device administrator") ||
            content.contains("must be deactivated") ||
            content.contains("إلغاء التنشيط والإلغاء") ||
            content.contains("مشرف نشط للجهاز") ||
            content.contains("إلغاء تنشيط") ||
            content.contains("الغاء تنشيط")

        val mentionsBreakFree = content.contains("breakfree") ||
            content.contains("تعافي") ||
            content.contains("com.aistudio.breakfree")

        val isTamperVerb = content.contains("uninstall") ||
            content.contains("إلغاء التثبيت") ||
            content.contains("إزالة") ||
            content.contains("force stop") ||
            content.contains("إيقاف إجباري") ||
            content.contains("clear data") ||
            content.contains("مسح البيانات")

        val isFactoryReset = content.contains("factory reset") ||
            content.contains("erase all data") ||
            content.contains("إعادة ضبط المصنع") ||
            content.contains("محو جميع البيانات")

        return isDeactivateDialog || (mentionsBreakFree && isTamperVerb) || isFactoryReset
    }

    /**
     * Inspects the active window node tree to see if the user is attempting to deactivate BreakFree,
     * uninstall it, clear data, or perform a factory reset.
     */
    private fun scanForTampering(root: AccessibilityNodeInfo): Boolean {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(root)
        var visited = 0
        val maxNodes = 150

        var mentionsBreakFree = false
        var mentionsTamperAction = false

        while (stack.isNotEmpty() && visited < maxNodes) {
            val node = stack.removeLast()
            visited++

            val text = node.text?.toString()?.lowercase(Locale.ROOT)
            val desc = node.contentDescription?.toString()?.lowercase(Locale.ROOT)
            val combined = (text ?: "") + " " + (desc ?: "")

            if (combined.isNotBlank()) {
                if (isTamperContent(combined)) {
                    return true
                }

                if (combined.contains("breakfree") ||
                    combined.contains("تعافي") ||
                    combined.contains("com.aistudio.breakfree")
                ) {
                    mentionsBreakFree = true
                }

                if (combined.contains("uninstall") ||
                    combined.contains("إلغاء التثبيت") ||
                    combined.contains("إزالة") ||
                    combined.contains("deactivate") ||
                    combined.contains("إلغاء التنشيط") ||
                    combined.contains("force stop") ||
                    combined.contains("إيقاف إجباري") ||
                    combined.contains("clear data") ||
                    combined.contains("clear storage") ||
                    combined.contains("مسح البيانات") ||
                    combined.contains("device admin") ||
                    combined.contains("مشرفو الجهاز") ||
                    combined.contains("accessibility") ||
                    combined.contains("إمكانية الوصول")
                ) {
                    mentionsTamperAction = true
                }
            }

            if (mentionsBreakFree && mentionsTamperAction) {
                return true
            }

            val childCount = minOf(node.childCount, 20)
            for (i in 0 until childCount) {
                val child = node.getChild(i) ?: continue
                stack.addLast(child)
            }
        }

        return false
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

        val smallIcon = if (stealth) R.drawable.ic_stat_neutral else R.drawable.ic_stat_shield

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
