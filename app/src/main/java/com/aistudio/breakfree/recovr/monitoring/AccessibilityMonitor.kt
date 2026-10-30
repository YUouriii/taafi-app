package com.aistudio.breakfree.recovr.monitoring

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.aistudio.breakfree.recovr.monitoring.ContentMonitoringAccessibilityService

/**
 * Helpers for the two system permissions the content monitor depends on:
 *
 *  - **Accessibility service enabled** — the actual switch the user flips in Settings.
 *  - **Restricted settings allowed** — on Android 13+ a sideloaded app cannot enable an
 *    accessibility service until the user explicitly approves it from the app's info page.
 *    There is no public API for "is it allowed", so the service being enabled is the
 *    observable proof that both gates have been cleared.
 */
object AccessibilityMonitor {

    fun isEnabled(context: Context): Boolean {
        val expected = ComponentName(context, ContentMonitoringAccessibilityService::class.java)
            .flattenToString()
        val enabled = try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
        } catch (e: Exception) {
            null
        } ?: return false
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    fun openAccessibilitySettings(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            // Some ROMs hide the screen; the Guard tab repeats the instructions.
        }
    }

    fun openAppInfo(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:${context.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            // Fall back to the top-level settings screen.
            try {
                context.startActivity(
                    Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: Exception) {
            }
        }
    }

    /** True on Android 13+, where restricted settings exist. */
    fun restrictedSettingsApply(): Boolean =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU

    /** True on OEMs that kill background services unless the user enables "Autostart". */
    fun isOemThatBlocksAutostart(): Boolean {
        val manufacturer = android.os.Build.MANUFACTURER.lowercase()
        return manufacturer.contains("xiaomi") || manufacturer.contains("redmi") ||
            manufacturer.contains("poco") || manufacturer.contains("oppo") ||
            manufacturer.contains("vivo") || manufacturer.contains("huawei") ||
            manufacturer.contains("honor") || manufacturer.contains("oneplus")
    }
}
