package com.aistudio.breakfree.recovr.monitoring

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.GracePeriodState
import kotlin.math.roundToInt

/**
 * Puts the step-back reminder on top of whatever app is in front — normally the browser.
 *
 * Lifecycle is driven entirely by [MonitoringManager.gracePeriodState]: the window exists
 * only while the countdown is running, and it is removed the moment the state resolves.
 * That is what makes "navigate away and it disappears" work without any extra bookkeeping
 * in the browser: leaving the browser, closing the tab, navigating to a safe page and the
 * timer expiring all resolve the same state.
 *
 * There is no dismiss path on purpose. A button that clears the reminder while the page
 * is still open defeats the point of the reminder.
 *
 * Every [WindowManager] call is guarded. A service can be torn down, the permission can be
 * revoked from Settings, and some ROMs reject overlay windows outright; none of those may
 * crash the accessibility service, and none may leave a view attached after its state is
 * gone.
 *
 * All methods must be called on the main thread.
 */
class GraceOverlayController(private val context: Context) {

    private val windowManager: WindowManager? =
        context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    private var overlayView: GraceReminderOverlayView? = null
    private var isAttached = false
    private var lastPermissionCheckAt = 0L

    val isShowing: Boolean get() = isAttached

    /**
     * Reconciles the window with [state]. Idempotent and cheap: it is called on every
     * countdown tick (4×/s), and only touches the window when something changed.
     *
     * @return true when the reminder is on screen, so the caller can skip the
     *   notification instead of showing two reminders at once.
     */
    fun syncWith(state: GracePeriodState, language: AppLanguage): Boolean {
        if (!state.isGraceActive) {
            hide()
            return false
        }

        if (isAttached) {
            // The permission can be revoked while the window is up; Android usually
            // detaches it, but the flag must not go stale. Checking every tick would be a
            // binder round-trip 4×/s, so throttle it.
            val now = SystemClock.elapsedRealtime()
            if (now - lastPermissionCheckAt > PERMISSION_RECHECK_MS) {
                lastPermissionCheckAt = now
                if (!OverlayPermission.isGranted(context)) {
                    Log.i(TAG, "Overlay permission revoked; removing the reminder")
                    hide()
                    return false
                }
            }
            overlayView?.render(state, language)
            return true
        }

        show(state, language)
        return isAttached
    }

    private fun show(state: GracePeriodState, language: AppLanguage) {
        val wm = windowManager ?: return
        if (!OverlayPermission.isGranted(context)) {
            // Expected state, not an error: the notification is the fallback.
            Log.d(TAG, "Overlay permission not granted; keeping the notification fallback")
            return
        }

        val view = GraceReminderOverlayView(context)
        view.render(state, language)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayWindowType(),
            // Not focusable keeps the browser keyboard and input working; not touch modal
            // lets the area outside the card keep receiving taps, so the user can still
            // navigate away — which is exactly the reversal the app wants to reward.
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            // The window's origin is the physical screen top, so the card must be pushed
            // below the status bar itself; otherwise it covers the clock and the icons.
            y = statusBarInsetPx() + (8 * context.resources.displayMetrics.density).roundToInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
            }
        }

        try {
            wm.addView(view, params)
        } catch (e: RuntimeException) {
            // BadTokenException / SecurityException / IllegalStateException: the service
            // lost its token or the permission was pulled between the check and the add.
            Log.e(TAG, "Could not attach the overlay; using the notification instead", e)
            return
        }

        overlayView = view
        isAttached = true
        lastPermissionCheckAt = SystemClock.elapsedRealtime()
        view.alpha = 0f
        view.animate().alpha(1f).setDuration(160L).start()
    }

    /** Removes the window. Safe to call when nothing is attached, and safe twice. */
    fun hide() {
        val view = overlayView ?: return
        overlayView = null
        if (!isAttached) return
        isAttached = false
        try {
            windowManager?.removeViewImmediate(view)
        } catch (e: IllegalArgumentException) {
            // Already detached by the system (permission revoked, display change).
            Log.d(TAG, "Overlay was already detached")
        } catch (e: RuntimeException) {
            Log.w(TAG, "Failed to remove the overlay cleanly", e)
        }
    }

    /** Called from the service's onDestroy/onInterrupt: never leave a window behind. */
    fun destroy() {
        hide()
    }

    private fun overlayWindowType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            // API 24-25 have no APPLICATION_OVERLAY; TYPE_PHONE is the equivalent and is
            // still accepted for services that hold SYSTEM_ALERT_WINDOW.
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

    /**
     * Height of the status bar in px. Pre-API-30 has no window-metrics API, so the
     * framework dimension is the only portable source; a missing resource (rare OEM
     * ROMs) degrades to no offset rather than to a crash.
     */
    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    private fun statusBarInsetPx(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = windowManager ?: return 0
            return try {
                wm.currentWindowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())
                    .top
            } catch (e: RuntimeException) {
                Log.w(TAG, "Could not read the status bar inset", e)
                0
            }
        }
        val id = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) context.resources.getDimensionPixelSize(id) else 0
    }

    private companion object {
        private const val TAG = "GraceOverlay"
        private const val PERMISSION_RECHECK_MS = 5_000L
    }
}
