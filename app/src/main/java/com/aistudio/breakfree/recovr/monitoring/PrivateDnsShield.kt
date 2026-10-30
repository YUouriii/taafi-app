package com.aistudio.breakfree.recovr.monitoring

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.UserManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast
import java.util.Locale

/**
 * The DNS layer of the Shield: a system-wide Private DNS (DNS over TLS) resolver that
 * blocks adult domains before a connection is ever opened.
 *
 * ## Why Private DNS and not a VPN
 *
 * A local VPN could filter queries with our own list, but it takes the phone's only VPN
 * slot, needs a permanent foreground notification, and is bypassed by browsers that use
 * their own encrypted DNS. Android's Private DNS is enforced by the system for every app,
 * costs no battery, and — when BreakFree is Device Owner — can be set and locked through
 * [DeviceOwnerProvisioning]'s role using
 * [DeviceOwnerProvisioning] / `setGlobalPrivateDnsModeSpecifiedHost`.
 *
 * ## What it can and cannot do
 *
 * It blocks **domain names** (pornhub.com, xvideos.com, …) via the resolver's maintained
 * family filter. It cannot inspect a page, an image or a video, and a determined user can
 * still bypass it with a VPN or an app that hard-codes its own DoH server. The UI says so;
 * this is a speed bump for the impulse, not a content filter.
 *
 * ## Privacy
 *
 * DNS lookups go to the resolver (Cloudflare for Families), not to BreakFree. The app
 * never sees the query, the answer or the browsing history. That is disclosed before the
 * shield can be enabled.
 */
object PrivateDnsShield {

    private const val TAG = "PrivateDnsShield"

    /** The resolver host. Cloudflare's family filter blocks adult content and malware. */
    const val PROVIDER_HOST = "family.cloudflare-dns.com"

    /** Human-readable provider name used in the UI and documentation. */
    const val PROVIDER_NAME = "Cloudflare for Families"

    /** Hidden but stable since Android 9; some OEMs rename or omit it. */
    private const val ACTION_PRIVATE_DNS_SETTINGS = "android.settings.PRIVATE_DNS_SETTINGS"

    /**
     * `Settings.Global` keeps these two keys hidden, so they are addressed by their
     * stable string values. The values are part of Android's settings provider contract
     * and are read by every Private DNS UI, including AOSP's own Settings app.
     */
    private const val SETTING_PRIVATE_DNS_MODE = "private_dns_mode"
    private const val SETTING_PRIVATE_DNS_SPECIFIER = "private_dns_specifier"

    /** Private DNS itself was introduced in Android 9 (API 28). */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
    fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    /** Device Owner can apply the host automatically from Android 10 (API 29). */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
    fun canApplyAutomatically(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    enum class Status {
        /** Private DNS is set to [PROVIDER_HOST]. */
        ACTIVE,

        /** Private DNS is in hostname mode, but pointed at a different resolver. */
        OTHER_HOST,

        /** Android's automatic (opportunistic) mode; nothing is filtered by us. */
        AUTOMATIC,

        /** Private DNS is off, or cannot be read. */
        OFF,

        /** Android is older than 9, so Private DNS does not exist. */
        UNSUPPORTED
    }

    enum class ApplyResult {
        APPLIED,
        NOT_DEVICE_OWNER,
        UNSUPPORTED,
        HOST_NOT_SERVING,
        FAILED
    }

    /**
     * Reads the configured Private DNS mode. Returns null on old Android versions or on
     * ROMs that hide the setting; callers fall back to the live network state.
     */
    fun configuredMode(context: Context): String? {
        if (!isSupported()) return null
        return try {
            Settings.Global.getString(
                context.contentResolver,
                SETTING_PRIVATE_DNS_MODE
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not read the Private DNS mode", e)
            null
        }
    }

    /** The configured hostname, or null when the mode is not "hostname". */
    fun configuredHost(context: Context): String? {
        if (!isSupported()) return null
        return try {
            Settings.Global.getString(
                context.contentResolver,
                SETTING_PRIVATE_DNS_SPECIFIER
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not read the Private DNS host", e)
            null
        }
    }

    /**
     * Current shield state.
     *
     * The stored mode is the source of truth because it survives being offline; the live
     * [android.net.LinkProperties] is only a fallback for ROMs that do not expose it.
     */
    fun status(context: Context): Status {
        if (!isSupported()) return Status.UNSUPPORTED

        val mode = configuredMode(context)
        if (mode == "hostname") {
            return if (isProviderHost(configuredHost(context))) {
                Status.ACTIVE
            } else {
                Status.OTHER_HOST
            }
        }

        // Some OEM builds return null for the global setting. Ask the active network.
        val liveHost = livePrivateDnsHost(context)
        if (liveHost != null) {
            return if (isProviderHost(liveHost)) Status.ACTIVE else Status.OTHER_HOST
        }

        return if (mode == "opportunistic") Status.AUTOMATIC else Status.OFF
    }

    /** The resolver currently in use, if any. Shown to the user when it is not ours. */
    fun activeHost(context: Context): String? =
        configuredHost(context)?.takeIf { it.isNotBlank() } ?: livePrivateDnsHost(context)

    /** Case-insensitive comparison that tolerates a trailing DNS root dot. */
    fun isProviderHost(host: String?): Boolean {
        val value = host?.trim()?.trimEnd('.')?.lowercase(Locale.ROOT) ?: return false
        return value == PROVIDER_HOST
    }

    fun isDeviceOwner(context: Context): Boolean = UninstallLock.isDeviceOwner(context)

    /** True when BreakFree holds the Device Owner role and has locked Private DNS. */
    fun isLocked(context: Context): Boolean {
        // The restriction constant itself was added in Android 10, and a lock is only
        // meaningful on versions where the app can also set the resolver.
        if (!canApplyAutomatically() || !isDeviceOwner(context)) return false
        return try {
            val restrictions = dpm(context).getUserRestrictions(admin(context))
            restrictions.getBoolean(UserManager.DISALLOW_CONFIG_PRIVATE_DNS, false)
        } catch (e: Exception) {
            Log.w(TAG, "Could not read the Private DNS restriction", e)
            false
        }
    }

    /**
     * Applies [PROVIDER_HOST] and prevents the user from changing Private DNS while the
     * 90-day commitment is running.
     *
     * **Blocking.** The platform performs a DNS-over-TLS connectivity check here, which can
     * take several seconds. Call this from [kotlinx.coroutines.Dispatchers.IO], never from
     * the main thread.
     */
    fun applyAndLock(context: Context): ApplyResult {
        if (!canApplyAutomatically()) return ApplyResult.UNSUPPORTED
        if (!isDeviceOwner(context)) return ApplyResult.NOT_DEVICE_OWNER

        return try {
            val result = dpm(context).setGlobalPrivateDnsModeSpecifiedHost(
                admin(context),
                PROVIDER_HOST
            )
            when (result) {
                DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR -> {
                    // Only lock after the resolver is confirmed reachable; otherwise the
                    // phone could be left unable to resolve anything with no way out.
                    lock(context)
                    ApplyResult.APPLIED
                }

                DevicePolicyManager.PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING ->
                    ApplyResult.HOST_NOT_SERVING

                else -> ApplyResult.FAILED
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not apply the Private DNS host", e)
            ApplyResult.FAILED
        }
    }

    /** Adds the user restriction that greys out Private DNS in system Settings. */
    fun lock(context: Context): Boolean {
        if (!canApplyAutomatically() || !isDeviceOwner(context)) return false
        return try {
            dpm(context).addUserRestriction(
                admin(context),
                UserManager.DISALLOW_CONFIG_PRIVATE_DNS
            )
            Log.i(TAG, "Private DNS locked to $PROVIDER_HOST")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Could not lock Private DNS", e)
            false
        }
    }

    /** Removes the restriction. Does not change the resolver. */
    fun clearLock(context: Context): Boolean {
        if (!canApplyAutomatically() || !isDeviceOwner(context)) return false
        return try {
            dpm(context).clearUserRestriction(
                admin(context),
                UserManager.DISALLOW_CONFIG_PRIVATE_DNS
            )
            Log.i(TAG, "Private DNS lock cleared")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Could not clear the Private DNS lock", e)
            false
        }
    }

    /**
     * Restores Android's automatic Private DNS. **Blocking**; call from a background
     * thread. Used when the user turns the shield off while BreakFree still holds the
     * Device Owner role.
     */
    fun restoreAutomatic(context: Context): Boolean {
        if (!canApplyAutomatically() || !isDeviceOwner(context)) return false
        return try {
            val result = dpm(context).setGlobalPrivateDnsModeOpportunistic(admin(context))
            result == DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR
        } catch (e: Exception) {
            Log.w(TAG, "Could not restore automatic Private DNS", e)
            false
        }
    }

    /**
     * Called at the end of the 90-day commitment, **before** Device Owner is released.
     * The resolver may stay in place; what must not stay is the lock. The user can then
     * change or remove it in Settings.
     */
    fun onCommitmentEnded(context: Context) {
        clearLock(context)
    }

    /**
     * Re-applies the shield on app start when the user opted in and BreakFree is Device
     * Owner. Cheap when everything is already in place. **Blocking**; call from a
     * background thread.
     */
    fun ensureAppliedIfEnabled(context: Context) {
        val preferences = MonitoringPreferences(context)
        if (!preferences.dnsShieldEnabled || !preferences.dnsShieldConsentGranted) return
        if (!isDeviceOwner(context)) return

        if (status(context) == Status.ACTIVE) {
            // A previous run may have applied the host but failed to add the restriction.
            if (!isLocked(context)) lock(context)
            return
        }

        val result = applyAndLock(context)
        Log.i(TAG, "DNS shield re-applied on start: $result")
    }

    /** Opens Android's Private DNS screen, falling back to wireless/top-level settings. */
    fun openSettings(context: Context): Boolean {
        val actions = listOf(
            ACTION_PRIVATE_DNS_SETTINGS,
            Settings.ACTION_WIRELESS_SETTINGS,
            Settings.ACTION_SETTINGS
        )
        for (action in actions) {
            val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Could not open $action", e)
            }
        }
        Log.e(TAG, "No Private DNS settings screen could be opened")
        return false
    }

    private fun livePrivateDnsHost(context: Context): String? {
        if (!isSupported()) return null
        return try {
            val manager = context.getSystemService(ConnectivityManager::class.java) ?: return null
            val network = manager.activeNetwork ?: return null
            val properties = manager.getLinkProperties(network) ?: return null
            if (!properties.isPrivateDnsActive) return null
            properties.privateDnsServerName
        } catch (e: Exception) {
            Log.w(TAG, "Could not read the active Private DNS host", e)
            null
        }
    }

    private fun admin(context: Context): ComponentName =
        ComponentName(context, UninstallGuardReceiver::class.java)

    private fun dpm(context: Context): DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
}
