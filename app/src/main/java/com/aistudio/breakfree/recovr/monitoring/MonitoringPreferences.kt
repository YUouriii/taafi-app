package com.aistudio.breakfree.recovr.monitoring

import android.content.Context
import android.content.SharedPreferences

/**
 * User-facing monitoring/AI switches, persisted so they survive a restart and so the
 * accessibility service (a separate component, possibly in another process lifetime)
 * sees exactly the same values as the settings screen.
 */
class MonitoringPreferences(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Master switch for the content monitor (browsers and opted-in apps). */
    var strictGuardEnabled: Boolean
        get() = prefs.getBoolean(KEY_STRICT_GUARD, true)
        set(value) = prefs.edit().putBoolean(KEY_STRICT_GUARD, value).apply()

    /** Whether the 120-second step-back window is offered before anything is recorded. */
    var warningBufferEnabled: Boolean
        get() = prefs.getBoolean(KEY_WARNING_BUFFER, true)
        set(value) = prefs.edit().putBoolean(KEY_WARNING_BUFFER, value).apply()

    /** Whether text/search keywords are matched in addition to the domain list. */
    var keywordFilterEnabled: Boolean
        get() = prefs.getBoolean(KEY_KEYWORD_FILTER, true)
        set(value) = prefs.edit().putBoolean(KEY_KEYWORD_FILTER, value).apply()

    /** Explicit consent to run the accessibility monitor (Play policy requirement). */
    var monitoringConsentGranted: Boolean
        get() = prefs.getBoolean(KEY_MONITORING_CONSENT, false)
        set(value) = prefs.edit().putBoolean(KEY_MONITORING_CONSENT, value).apply()

    /**
     * Separate consent to read the visible text of social/video apps.
     *
     * Browser consent predates app coverage, so an existing user keeps browser-only
     * monitoring until they accept this expanded scope.
     */
    var appsMonitoringConsentGranted: Boolean
        get() = prefs.getBoolean(KEY_APPS_MONITORING_CONSENT, false)
        set(value) = prefs.edit().putBoolean(KEY_APPS_MONITORING_CONSENT, value).apply()

    /**
     * Whether the DNS shield is on.
     *
     * It defaults to on: the shield is part of the protection the app offers, not an extra
     * the user has to find. Nothing is enforced until [dnsShieldConsentGranted] is set,
     * because the lookups leave the device for a third-party resolver.
     */
    var dnsShieldEnabled: Boolean
        get() = prefs.getBoolean(KEY_DNS_SHIELD_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DNS_SHIELD_ENABLED, value).apply()

    /**
     * Explicit consent to send DNS lookups to the third-party family resolver.
     *
     * This is separate from the accessibility consent: it is a network data flow, and
     * the resolver — not BreakFree — sees the domain names the user looks up.
     */
    var dnsShieldConsentGranted: Boolean
        get() = prefs.getBoolean(KEY_DNS_SHIELD_CONSENT, false)
        set(value) = prefs.edit().putBoolean(KEY_DNS_SHIELD_CONSENT, value).apply()

    /** Explicit consent to send chat text to Gemini via Firebase AI Logic. */
    var aiConsentGranted: Boolean
        get() = prefs.getBoolean(KEY_AI_CONSENT, false)
        set(value) = prefs.edit().putBoolean(KEY_AI_CONSENT, value).apply()

    /** "dark" (default, current look), "light" (white), or "system" (follow OS). */
    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "dark") ?: "dark"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    /** True once the first-run Shield setup has been shown (whether or not it was accepted). */
    var firstRunSetupDone: Boolean
        get() = prefs.getBoolean(KEY_FIRST_RUN_SETUP, false)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_RUN_SETUP, value).apply()

    /** Length of the step-back window in seconds (0 when the buffer is disabled). */
    fun graceBufferSeconds(): Int = if (warningBufferEnabled) GRACE_BUFFER_SECONDS else 0

    companion object {
        const val PREFS_NAME = "breakfree_prefs"
        const val GRACE_BUFFER_SECONDS = 120

        private const val KEY_STRICT_GUARD = "monitor_strict_guard"
        private const val KEY_WARNING_BUFFER = "monitor_warning_buffer"
        private const val KEY_KEYWORD_FILTER = "monitor_keyword_filter"
        private const val KEY_MONITORING_CONSENT = "monitor_consent_granted"
        private const val KEY_APPS_MONITORING_CONSENT = "monitor_apps_consent_granted"
        private const val KEY_DNS_SHIELD_ENABLED = "dns_shield_enabled"
        private const val KEY_DNS_SHIELD_CONSENT = "dns_shield_consent_granted"
        private const val KEY_FIRST_RUN_SETUP = "first_run_setup_done"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_AI_CONSENT = "ai_consent_granted"
    }
}
