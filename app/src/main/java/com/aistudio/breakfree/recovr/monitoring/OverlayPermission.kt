package com.aistudio.breakfree.recovr.monitoring

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri

/**
 * `SYSTEM_ALERT_WINDOW` is a special permission: `requestPermissions()` cannot grant it
 * on any Android version. The only way in is the system screen behind
 * [Settings.ACTION_MANAGE_OVERLAY_PERMISSION], and the result must be re-checked with
 * [Settings.canDrawOverlays] when the app comes back to the foreground.
 *
 * The app never depends on this permission: without it the step-back reminder still
 * arrives as an ongoing notification, so declining costs convenience, not protection.
 */
object OverlayPermission {

    private const val TAG = "OverlayPermission"

    /** True when the user allowed this app to draw over other apps. */
    fun isGranted(context: Context): Boolean = try {
        Settings.canDrawOverlays(context)
    } catch (e: Exception) {
        // canDrawOverlays is documented from API 23, but a few heavily modified ROMs
        // throw instead of returning false. Treat "cannot tell" as "not granted" so the
        // overlay is never added without a confirmed permission.
        Log.w(TAG, "Could not read overlay permission state", e)
        false
    }

    /**
     * Opens the per-app "Display over other apps" screen.
     *
     * @return true when a system screen was opened. Some OEM ROMs ship without the
     *   overlay screen, so this falls back to the app's own details page instead of
     *   crashing or silently doing nothing.
     */
    fun openSettings(context: Context): Boolean {
        val direct = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:${context.packageName}".toUri()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(direct)
            return true
        } catch (e: Exception) {
            Log.w(TAG, "Overlay permission screen unavailable, falling back", e)
        }

        return try {
            context.startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    "package:${context.packageName}".toUri()
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "No settings screen could be opened", e)
            false
        }
    }
}
