package com.aistudio.breakfree.recovr.monitoring

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log

/**
 * Chooses *how* the DNS layer is enforced, and reports the live result.
 *
 * There are exactly two mechanisms Android allows, and which one is available depends on
 * the role the app holds:
 *
 *  - **Device Owner** → system Private DNS (see [PrivateDnsShield]). Enforced for every
 *    app, no VPN slot, and lockable for the 90-day commitment.
 *  - **Everyone else** → the on-device filter (see [DnsFilterVpnService]). A VPN that
 *    carries only DNS queries, because a normal app cannot write the system setting.
 *
 * The user never chooses between them: the app picks the strongest one it is allowed to
 * use. The UI only shows which one is active, and why.
 */
object DnsShield {

    private const val TAG = "DnsShield"

    enum class Mechanism {
        /** Android's Private DNS, set and locked by the Device Owner. */
        PRIVATE_DNS,

        /** A VPN that carries DNS queries only. */
        ON_DEVICE_FILTER
    }

    enum class Status {
        /** Lookups are being filtered right now. */
        ACTIVE,

        /** Enabled in the app, but nothing is filtering (VPN off or permission missing). */
        OFF,

        /** Private DNS points at a different resolver. */
        OTHER_HOST
    }

    data class State(
        val status: Status,
        val mechanism: Mechanism,
        val activeHost: String?,
        val locked: Boolean,
        val deviceOwner: Boolean,
        val vpnRunning: Boolean
    )

    /**
     * Reads the current state. Cheap and side-effect free, so screens can call it on every
     * resume.
     */
    fun state(context: Context): State {
        val deviceOwner = UninstallLock.isDeviceOwner(context)
        val privateDnsUsable = deviceOwner && PrivateDnsShield.canApplyAutomatically()
        val vpnRunning = DnsFilterVpnService.isRunning()

        if (privateDnsUsable) {
            val privateStatus = PrivateDnsShield.status(context)
            val host = PrivateDnsShield.activeHost(context)
            val locked = PrivateDnsShield.isLocked(context)

            return when (privateStatus) {
                PrivateDnsShield.Status.ACTIVE -> State(
                    Status.ACTIVE, Mechanism.PRIVATE_DNS, host, locked, true, vpnRunning
                )

                PrivateDnsShield.Status.OTHER_HOST -> if (vpnRunning) {
                    // A second, user-installed resolver is configured; the filter is what
                    // is actually protecting this device.
                    State(Status.ACTIVE, Mechanism.ON_DEVICE_FILTER, null, false, true, true)
                } else {
                    State(Status.OTHER_HOST, Mechanism.PRIVATE_DNS, host, locked, true, false)
                }

                else -> if (vpnRunning) {
                    State(Status.ACTIVE, Mechanism.ON_DEVICE_FILTER, null, false, true, true)
                } else {
                    State(Status.OFF, Mechanism.PRIVATE_DNS, host, locked, true, false)
                }
            }
        }

        val status = if (vpnRunning) Status.ACTIVE else Status.OFF
        return State(
            status = status,
            mechanism = Mechanism.ON_DEVICE_FILTER,
            activeHost = null,
            locked = false,
            deviceOwner = deviceOwner,
            vpnRunning = vpnRunning
        )
    }

    /** The system consent dialog for a VPN, or null when it was already granted. */
    fun vpnPermissionIntent(context: Context): Intent? = try {
        VpnService.prepare(context)
    } catch (e: Exception) {
        Log.w(TAG, "Could not read the VPN permission state", e)
        null
    }

    /**
     * Turns the shield on with the strongest mechanism available.
     *
     * **Blocking** on the Device Owner path, because the platform performs a DoT
     * connectivity check. Call from [kotlinx.coroutines.Dispatchers.IO].
     */
    fun enable(context: Context): PrivateDnsShield.ApplyResult? {
        // Consent is recorded by the caller, after the disclosure is accepted.
        val preferences = MonitoringPreferences(context)
        preferences.dnsShieldEnabled = true

        if (UninstallLock.isDeviceOwner(context) && PrivateDnsShield.canApplyAutomatically()) {
            val result = PrivateDnsShield.applyAndLock(context)
            if (result == PrivateDnsShield.ApplyResult.APPLIED) {
                // Two filters would be redundant; the system one is strictly stronger.
                DnsFilterVpnService.stop(context)
            }
            return result
        }

        startFilterIfAllowed(context)
        return null
    }

    /**
     * Turns the shield off. **Blocking** on the Device Owner path.
     *
     * While the 90-day commitment is running the shield is locked on purpose, so this
     * refuses to act and returns false.
     */
    fun disable(context: Context): Boolean {
        if (isLockedByCommitment(context)) return false

        val preferences = MonitoringPreferences(context)
        preferences.dnsShieldEnabled = false
        DnsFilterVpnService.stop(context)

        if (UninstallLock.isDeviceOwner(context) && PrivateDnsShield.canApplyAutomatically()) {
            PrivateDnsShield.clearLock(context)
            PrivateDnsShield.restoreAutomatic(context)
        }
        return true
    }

    /** True when the shield is locked because the 90-day commitment is still running. */
    fun isLockedByCommitment(context: Context): Boolean =
        UninstallLock.isActive(context) && PrivateDnsShield.isLocked(context)

    /**
     * Re-applies the shield on app start / resume. Called from the foreground, which is
     * also the only place Android allows a foreground service to be started.
     *
     * **Blocking** on the Device Owner path.
     */
    fun sync(context: Context) {
        val preferences = MonitoringPreferences(context)
        if (!preferences.dnsShieldEnabled || !preferences.dnsShieldConsentGranted) {
            DnsFilterVpnService.stop(context)
            return
        }

        if (UninstallLock.isDeviceOwner(context) && PrivateDnsShield.canApplyAutomatically()) {
            PrivateDnsShield.ensureAppliedIfEnabled(context)
            if (PrivateDnsShield.status(context) == PrivateDnsShield.Status.ACTIVE) {
                DnsFilterVpnService.stop(context)
                return
            }
        }

        startFilterIfAllowed(context)
    }

    /**
     * Starts the VPN only when the user has already granted the system permission.
     * Without it, the screen has to show the consent dialog first.
     */
    private fun startFilterIfAllowed(context: Context) {
        if (DnsFilterVpnService.isRunning()) return
        if (vpnPermissionIntent(context) != null) {
            Log.i(TAG, "The VPN permission is missing; the screen will ask for it")
            return
        }
        try {
            DnsFilterVpnService.start(context)
        } catch (e: Exception) {
            Log.w(TAG, "Could not start the DNS filter", e)
        }
    }
}
