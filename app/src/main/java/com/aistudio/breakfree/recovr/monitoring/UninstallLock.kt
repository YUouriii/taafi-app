package com.aistudio.breakfree.recovr.monitoring

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.aistudio.breakfree.recovr.R
import com.aistudio.breakfree.recovr.model.AppLanguage
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

/**
 * The optional 90-day "keep me installed" commitment.
 *
 * ## What this actually does
 *
 * Android has no API that lets an ordinary app forbid its own uninstall. What it does
 * have is the device-administrator mechanism: while an app is an *active* administrator,
 * the system refuses to uninstall it and tells the user to deactivate the administrator
 * first. That is a real, honest barrier — it turns a two-tap uninstall into a deliberate
 * trip through Settings — but it is not a lock, and the user can always remove it. This
 * class does not pretend otherwise, and the UI says so.
 *
 * Two rules keep it defensible:
 *  - It is strictly opt-in, behind a disclosure screen, and never enabled silently.
 *  - It asks for **no** device policies (see `res/xml/device_admin.xml`), so BreakFree
 *    gains no ability to lock, wipe or reconfigure the phone.
 *
 * The commitment ends by itself: an alarm plus a check on every app start calls
 * [releaseIfDue], which deactivates the administrator once the release date has passed.
 */
object UninstallLock {

    /** Length of the commitment in days. */
    const val COMMITMENT_DAYS = 90

    private const val TAG = "UninstallLock"
    private const val PREFS = "breakfree_prefs"
    private const val KEY_STARTED_AT = "uninstall_lock_started_at"
    private const val KEY_COMPLETED_AT = "uninstall_lock_completed_at"
    private const val RELEASE_REQUEST_CODE = 4711
    private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

    enum class State {
        /** Never enabled, or removed early by the user. */
        OFF,

        /** Administrator is active; the app cannot be uninstalled right now. */
        ACTIVE,

        /** The 90 days elapsed and the administrator was released automatically. */
        COMPLETED
    }

    fun state(context: Context): State {
        if (startedAt(context) <= 0L) return State.OFF
        if (isActive(context)) return State.ACTIVE
        return if (prefs(context).getLong(KEY_COMPLETED_AT, 0L) > 0L) {
            State.COMPLETED
        } else {
            State.OFF
        }
    }

    fun isActive(context: Context): Boolean = try {
        dpm(context).isAdminActive(adminComponent(context))
    } catch (e: Exception) {
        // A handful of ROMs throw instead of answering. "Cannot tell" means "not active",
        // so the UI never claims protection that may not exist.
        Log.w(TAG, "Could not read the device administrator state", e)
        false
    }

    /** When the commitment started, or 0 when there is none. */
    fun startedAt(context: Context): Long =
        prefs(context).getLong(KEY_STARTED_AT, 0L)

    /** When the administrator should release itself. */
    fun releaseAt(context: Context): Long {
        val started = startedAt(context)
        if (started <= 0L) return 0L
        return started + COMMITMENT_DAYS * MILLIS_PER_DAY
    }

    /** Whole days left, rounded up so "1 day left" never shows as 0 while still locked. */
    fun daysRemaining(context: Context): Int {
        val releaseAt = releaseAt(context)
        if (releaseAt <= 0L) return 0
        val left = releaseAt - System.currentTimeMillis()
        if (left <= 0L) return 0
        return ceil(left.toDouble() / MILLIS_PER_DAY).toInt()
    }

    /**
     * Localised date of the automatic release, in the app's own language rather than the
     * system one — an Arabic screen showing "Jan 26, 2027" looks like a bug.
     */
    fun releaseDateLabel(context: Context, language: AppLanguage): String {
        val releaseAtMillis = releaseAt(context)
        if (releaseAtMillis <= 0L) return ""
        return try {
            val formatter = if (language.isRtl) {
                // ar-MA is the Arabic locale whose default numbering system is Latin, so the
                // date reads "26 يناير 2027" — matching the Latin digits used by the day
                // count beside it. The pattern is explicit because ar-MA's MEDIUM style is
                // the bare "2027/01/26".
                SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("ar-MA"))
            } else {
                DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH)
            }
            formatter.format(Date(releaseAtMillis))
        } catch (e: Exception) {
            Log.w(TAG, "Could not format the release date", e)
            ""
        }
    }

    /**
     * Opens the system screen that activates the administrator. The user still has to
     * confirm there; nothing is enabled by this call alone.
     */
    fun activate(context: Context): Boolean = try {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent(context))
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                context.getString(R.string.device_admin_explanation)
            )
        }
        // The system's DeviceAdminAdd screen refuses FLAG_ACTIVITY_NEW_TASK ("Cannot start
        // ADD_DEVICE_ADMIN as a new task"), so only set it when the caller has no task of
        // its own — i.e. when this is started from the application context.
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        Log.e(TAG, "No device administrator screen available", e)
        false
    }

    /**
     * Opens the system screen where the commitment can be turned off.
     *
     * There is no public "device admin list" action — `android.settings.DEVICE_ADMIN_SETTINGS`
     * exists on some ROMs but is not resolvable on stock Android, so the app opens Security
     * settings (where "Device admin apps" lives) and falls back to the top-level Settings.
     */
    fun openAdminSettings(context: Context): Boolean {
        for (action in ADMIN_SETTINGS_ACTIONS) {
            val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (context.packageManager.resolveActivity(intent, 0) == null) continue
            try {
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Could not open $action", e)
            }
        }
        Log.e(TAG, "No settings screen could be opened")
        return false
    }

    /**
     * Security settings is the documented screen; the hidden admin action is tried first
     * because a few OEM builds expose the admin list directly.
     */
    private val ADMIN_SETTINGS_ACTIONS = listOf(
        "android.settings.DEVICE_ADMIN_SETTINGS",
        Settings.ACTION_SECURITY_SETTINGS,
        Settings.ACTION_SETTINGS
    )

    /** Called by [UninstallGuardReceiver] once the user confirmed activation. */
    fun onEnabled(context: Context) {
        // A previous commitment that already ran out (or a stale start time) must not
        // make the new one expire immediately: the clock restarts from this activation.
        val existingStart = startedAt(context)
        val expired = existingStart > 0L &&
            System.currentTimeMillis() >= existingStart + COMMITMENT_DAYS * MILLIS_PER_DAY
        if (existingStart <= 0L || expired) {
            prefs(context).edit()
                .putLong(KEY_STARTED_AT, System.currentTimeMillis())
                .remove(KEY_COMPLETED_AT)
                .apply()
        }
        scheduleRelease(context, releaseAt(context))
        applyDeviceOwnerPolicies(context)
        Log.i(TAG, "Uninstall lock active for $COMMITMENT_DAYS days")
    }

    /**
     * Keeps the Device Owner path coherent.
     *
     * A Device Owner is provisioned outside the app (adb or the QR setup wizard), so the
     * first time the app sees the role it starts the same 90-day commitment the disclosure
     * screen would have started. Without this, [applyDeviceOwnerPolicies] would block
     * uninstallation with no release date at all — a lock that never ends.
     */
    fun ensureDeviceOwnerCommitment(context: Context) {
        if (!isDeviceOwner(context)) return

        if (startedAt(context) <= 0L) {
            prefs(context).edit()
                .putLong(KEY_STARTED_AT, System.currentTimeMillis())
                .remove(KEY_COMPLETED_AT)
                .apply()
            scheduleRelease(context, releaseAt(context))
            Log.i(TAG, "Device Owner detected; 90-day commitment started")
        }
        applyDeviceOwnerPolicies(context)
    }

    /**
     * Checks if BreakFree has been provisioned as Device Owner (e.g. via ADB).
     */
    fun isDeviceOwner(context: Context): Boolean = try {
        dpm(context).isDeviceOwnerApp(context.packageName)
    } catch (e: Exception) {
        false
    }

    /**
     * When the app holds Device Owner status (via ADB command or provisioning),
     * this permanently disables the Uninstall button across the entire Android OS
     * and disables Factory Reset in Settings.
     */
    fun applyDeviceOwnerPolicies(context: Context) {
        try {
            val dpm = dpm(context)
            val admin = adminComponent(context)
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                dpm.setUninstallBlocked(admin, context.packageName, true)
                dpm.addUserRestriction(admin, android.os.UserManager.DISALLOW_FACTORY_RESET)
                dpm.addUserRestriction(admin, android.os.UserManager.DISALLOW_SAFE_BOOT)
                Log.i(TAG, "Device Owner policies enforced: OS-level uninstall & factory reset are disabled")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not apply Device Owner policies", e)
        }
    }

    /**
     * Lifts the OS-level uninstall block and factory reset restriction when the 90 days are up.
     */
    fun clearDeviceOwnerPolicies(context: Context) {
        try {
            val dpm = dpm(context)
            val admin = adminComponent(context)
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                dpm.setUninstallBlocked(admin, context.packageName, false)
                dpm.clearUserRestriction(admin, android.os.UserManager.DISALLOW_FACTORY_RESET)
                dpm.clearUserRestriction(admin, android.os.UserManager.DISALLOW_SAFE_BOOT)
                Log.i(TAG, "Device Owner policies cleared")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not clear Device Owner policies", e)
        }
    }

    /**
     * Called when the administrator is deactivated — either by [releaseIfDue] at the end
     * of the commitment, or by the user in Settings. Only the first case is "completed".
     */
    fun onDisabled(context: Context) {
        cancelRelease(context)
        if (startedAt(context) <= 0L) return

        val due = releaseAt(context)
        if (due > 0L && System.currentTimeMillis() >= due) {
            prefs(context).edit()
                .putLong(KEY_COMPLETED_AT, System.currentTimeMillis())
                .apply()
            Log.i(TAG, "Commitment finished; administrator released")
        } else {
            // Removed early. Do not keep claiming a commitment that is no longer there.
            prefs(context).edit()
                .remove(KEY_STARTED_AT)
                .remove(KEY_COMPLETED_AT)
                .apply()
            Log.i(TAG, "Uninstall lock removed before the commitment ended")
        }
    }

    /**
     * Deactivates the administrator once the 90 days are up. Safe to call on every app
     * start: it is a no-op while the commitment is still running.
     *
     * @return true when the administrator was released by this call.
     */
    fun releaseIfDue(context: Context): Boolean {
        if (!isActive(context)) return false
        val due = releaseAt(context)
        if (due <= 0L || System.currentTimeMillis() < due) return false

        return try {
            // The DNS shield's lock must be lifted while the app still holds Device
            // Owner; after clearDeviceOwnerApp it no longer has the authority to do so.
            PrivateDnsShield.onCommitmentEnded(context)
            clearDeviceOwnerPolicies(context)
            if (isDeviceOwner(context)) {
                // A device owner cannot be removed with removeActiveAdmin (the system
                // throws "Cannot remove a device owner"); the app has to give the role up
                // itself, which also drops the administrator. Without this branch the lock
                // would never lift and the device would keep its factory-reset block.
                @Suppress("DEPRECATION")
                dpm(context).clearDeviceOwnerApp(context.packageName)
            } else {
                dpm(context).removeActiveAdmin(adminComponent(context))
            }
            cancelRelease(context)
            // Record completion here as well: onDisabled may not arrive on every ROM.
            prefs(context).edit()
                .putLong(KEY_COMPLETED_AT, System.currentTimeMillis())
                .apply()
            Log.i(TAG, "Release date reached; lock released")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Could not release the administrator", e)
            false
        }
    }

    private fun scheduleRelease(context: Context, at: Long) {
        if (at <= 0L) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return
        try {
            // Inexact on purpose: an exact alarm would need SCHEDULE_EXACT_ALARM and this
            // is a 90-day deadline, not an appointment.
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                at,
                releasePendingIntent(context)
            )
        } catch (e: RuntimeException) {
            // The check on app start still covers this case.
            Log.w(TAG, "Could not schedule the release check", e)
        }
    }

    private fun cancelRelease(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return
        try {
            alarmManager.cancel(releasePendingIntent(context))
        } catch (e: RuntimeException) {
            Log.w(TAG, "Could not cancel the release check", e)
        }
    }

    private fun releasePendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            RELEASE_REQUEST_CODE,
            Intent(context, UninstallLockReleaseReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun adminComponent(context: Context): ComponentName =
        ComponentName(context, UninstallGuardReceiver::class.java)

    private fun dpm(context: Context): DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
