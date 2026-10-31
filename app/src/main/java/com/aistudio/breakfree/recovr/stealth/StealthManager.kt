package com.aistudio.breakfree.recovr.stealth

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Applies and removes the Stealth / Disguise Mode.
 *
 * ## Disguise mechanism
 *
 * The launcher entry point is switched between the real [com.aistudio.breakfree.recovr.MainActivity]
 * and one of the neutral activity-aliases declared in the manifest, using
 * [android.content.pm.PackageManager.setComponentEnabledSetting]. The alias's `targetActivity`
 * is still MainActivity, so deep links and intents keep working.
 */
object StealthManager {

    private const val TAG = "StealthManager"
    private const val MAIN_ACTIVITY = "com.aistudio.breakfree.recovr.MainActivity"

    // Observable lock state. true = locked (dashboard hidden).
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    // When true, the dashboard shows a fake/empty view (duress PIN was used).
    private val _duressMode = MutableStateFlow(false)
    val duressMode: StateFlow<Boolean> = _duressMode.asStateFlow()

    fun setDuressMode(value: Boolean) { _duressMode.value = value }

    private var lastUnlockTimeMs: Long = 0L

    fun lock() { _isLocked.value = true }

    fun unlock(context: Context, duress: Boolean = false) {
        _isLocked.value = false
        _duressMode.value = duress
        prefs(context).lastUnlockTimeMs = System.currentTimeMillis()
        if (duress && prefs(context).duressWipeEnabled) {
            // Wipe recovery data. Guarded so a misconfigured toggle can't destroy data
            // unless the user explicitly opted in.
            wipeRecoveryData(context)
        }
    }

    private fun wipeRecoveryData(context: Context) {
        // Clear monitoring prefs and streak/check-in data for the current account.
        try {
            val plainPrefs = context.getSharedPreferences("breakfree_prefs", Context.MODE_PRIVATE)
            // The 90-day uninstall commitment is a separate, deliberate choice: wiping
            // recovery history must not leave the administrator active with no release
            // date (which would make the lock permanent).
            val lockStartedAt = plainPrefs.getLong("uninstall_lock_started_at", 0L)
            val lockCompletedAt = plainPrefs.getLong("uninstall_lock_completed_at", 0L)
            plainPrefs.edit().clear().apply()
            if (lockStartedAt > 0L || lockCompletedAt > 0L) {
                plainPrefs.edit()
                    .putLong("uninstall_lock_started_at", lockStartedAt)
                    .putLong("uninstall_lock_completed_at", lockCompletedAt)
                    .apply()
            }

            // The disguise and the PIN stay in place on purpose: a duress wipe must not
            // reveal the real app on the launcher or drop the user out of the decoy while
            // someone is watching. Only the recovery history is destroyed.
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Wipe failed", e)
        }
    }

    /** Disables every decoy launcher entry (used when the disguise is removed). */
    private fun disableAllAliases(context: Context) {
        DisguisePreset.entries.forEach { preset ->
            setEnabled(context, preset.aliasClass, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
        }
    }

    /** Restores the lock state from prefs on app start. */
    fun restoreLockState(context: Context) {
        val p = prefs(context)
        if (!p.stealthEnabled || !(p.appLockEnabled && p.hasPin())) {
            _isLocked.value = false
            return
        }
        val autoLock = p.autoLockMinutes
        val last = p.lastUnlockTimeMs
        if (autoLock <= 0 || last == 0L) {
            _isLocked.value = true
        } else {
            val elapsed = (System.currentTimeMillis() - last) / 60_000L
            _isLocked.value = elapsed >= autoLock
        }
    }

    fun shouldAutoLock(context: Context): Boolean {
        val p = prefs(context)
        val autoLock = p.autoLockMinutes
        if (autoLock <= 0) return false
        val last = p.lastUnlockTimeMs
        if (last == 0L) return true
        val elapsed = (System.currentTimeMillis() - last) / 60_000L
        return elapsed >= autoLock
    }


    fun prefs(context: Context): StealthPreferences = StealthPreferences(context)

    fun isStealthActive(context: Context): Boolean = prefs(context).stealthEnabled

    /** True when the real dashboard must be unlocked before showing. */
    fun lockRequired(context: Context): Boolean {
        val p = prefs(context)
        return p.stealthEnabled && p.appLockEnabled && p.hasPin()
    }

    /**
     * Turns Stealth Mode on: stores the PIN, applies the disguise, and enables neutral
     * notifications. Returns false if the PIN is invalid.
     */
    fun enable(
        context: Context,
        preset: DisguisePreset,
        pin: String,
        discreetNotifications: Boolean = true
    ): Boolean {
        if (pin.length !in 4..6 || !pin.all { it.isDigit() }) return false

        val p = prefs(context)
        val salt = generateSalt()
        p.pinSalt = salt
        p.pinHash = hashPin(pin, salt)
        p.disguisePreset = preset
        p.discreetNotifications = discreetNotifications
        p.stealthEnabled = true
        p.appLockEnabled = true

        applyDisguise(context, preset)
        refreshNotificationAppearance(context)
        return true
    }

    /** Turns Stealth Mode off. Requires the PIN to have been verified by the caller. */
    fun disable(context: Context) {
        val p = prefs(context)
        val preset = p.disguisePreset
        removeDisguise(context, preset)
        p.stealthEnabled = false
        p.appLockEnabled = false
        p.clearPin()
        refreshNotificationAppearance(context)
    }

    /**
     * Rewrites the visible notification surface after a Stealth Mode toggle: the ongoing
     * notifications are re-posted with the right wording/icon, and the three channels are
     * renamed so system notification settings do not expose the app either.
     */
    private fun refreshNotificationAppearance(context: Context) {
        try {
            com.aistudio.breakfree.recovr.monitoring.GuardForegroundService.start(context)
        } catch (e: Exception) {
            Log.w(TAG, "Could not refresh the guard notification", e)
        }
        try {
            com.aistudio.breakfree.recovr.monitoring.DnsFilterVpnService.refreshNotification(context)
        } catch (e: Exception) {
            Log.w(TAG, "Could not refresh the DNS notification", e)
        }
        updateChannelNames(context)
    }

    private fun updateChannelNames(context: Context) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return
        val manager = context.getSystemService(android.app.NotificationManager::class.java) ?: return
        val p = prefs(context)
        val stealth = p.stealthEnabled && p.discreetNotifications
        val code = context.getSharedPreferences("breakfree_prefs", Context.MODE_PRIVATE)
            .getString("selected_language", "en")
        val language = if (code == "ar") {
            com.aistudio.breakfree.recovr.model.AppLanguage.ARABIC
        } else {
            com.aistudio.breakfree.recovr.model.AppLanguage.ENGLISH
        }
        val neutralName = com.aistudio.breakfree.recovr.localization.Translations
            .t("stealth_notif_channel", language)
        val neutralDesc = com.aistudio.breakfree.recovr.localization.Translations
            .t("stealth_notif_channel_desc", language)

        val channels = mapOf(
            "breakfree_grace_period_channel" to
                ("grace_notification_channel" to "grace_notification_channel_desc"),
            "guard_service" to
                ("guard_service_channel" to "guard_service_channel_desc"),
            "dns_shield" to
                ("dns_vpn_notification_channel" to "dns_vpn_notification_channel_desc")
        )
        channels.forEach { (id, keys) ->
            val existing = manager.getNotificationChannel(id) ?: return@forEach
            val name = if (stealth) neutralName else
                com.aistudio.breakfree.recovr.localization.Translations.t(keys.first, language)
            if (existing.name == name) return@forEach
            val updated = android.app.NotificationChannel(id, name, existing.importance).apply {
                description = if (stealth) neutralDesc else
                    com.aistudio.breakfree.recovr.localization.Translations.t(keys.second, language)
            }
            manager.createNotificationChannel(updated)
        }
    }

    /** Applies the neutral launcher icon/name for [preset]. */
    fun applyDisguise(context: Context, preset: DisguisePreset) {
        // Clear any stale decoy first, then enable the chosen alias and disable the real
        // launcher entry, so the app never disappears from the launcher mid-switch.
        disableAllAliases(context)
        setEnabled(context, preset.aliasClass, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
        setEnabled(context, MAIN_ACTIVITY, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
        Log.i(TAG, "Disguise applied: ${preset.label}")
    }

    /** Restores the real launcher icon/name. */
    fun removeDisguise(context: Context, preset: DisguisePreset) {
        setEnabled(context, MAIN_ACTIVITY, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
        disableAllAliases(context)
        Log.i(TAG, "Disguise removed (${preset.label})")
    }

    /**
     * Re-applies the disguise on boot / app start if it drifted (e.g. after an update
     * re-enabled the main activity).
     */
    fun syncDisguise(context: Context) {
        val p = prefs(context)
        if (p.stealthEnabled) {
            // Only the chosen alias may be enabled; a preset changed while stealth was on
            // must not leave a second decoy icon on the launcher.
            DisguisePreset.entries
                .filter { it != p.disguisePreset }
                .forEach { stale ->
                    setEnabled(
                        context,
                        stale.aliasClass,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    )
                }
            applyDisguise(context, p.disguisePreset)
        } else {
            // Make sure the real entry is enabled and no decoy is left behind (updates can
            // re-enable aliases, and a previous toggle may have been interrupted).
            setEnabled(context, MAIN_ACTIVITY, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
            disableAllAliases(context)
        }
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val p = prefs(context)
        val salt = p.pinSalt ?: return false
        val hash = p.pinHash ?: return false
        return hashPin(pin, salt) == hash
    }

    fun verifyDuressPin(context: Context, pin: String): Boolean {
        val p = prefs(context)
        val salt = p.duressSalt ?: return false
        val hash = p.duressPinHash ?: return false
        return hashPin(pin, salt) == hash
    }

    fun setDuressPin(context: Context, pin: String): Boolean {
        if (pin.length !in 4..6 || !pin.all { it.isDigit() }) return false
        val p = prefs(context)
        val salt = generateSalt()
        p.duressSalt = salt
        p.duressPinHash = hashPin(pin, salt)
        return true
    }

    fun clearDuressPin(context: Context) {
        val p = prefs(context)
        p.duressPinHash = null
        p.duressSalt = null
    }

    fun hasDuressPin(context: Context): Boolean = prefs(context).hasDuressPin()

    fun duressWipeEnabled(context: Context): Boolean = prefs(context).duressWipeEnabled

    fun setRecoveryQuestion(context: Context, question: String, answer: String): Boolean {
        if (question.isBlank() || answer.isBlank()) return false
        val p = prefs(context)
        val salt = generateSalt()
        p.recoveryQuestion = question
        p.recoverySalt = salt
        p.recoveryAnswerHash = hashPin(answer.trim().lowercase(), salt)
        return true
    }

    fun verifyRecoveryAnswer(context: Context, answer: String): Boolean {
        val p = prefs(context)
        val salt = p.recoverySalt ?: return false
        val hash = p.recoveryAnswerHash ?: return false
        return hashPin(answer.trim().lowercase(), salt) == hash
    }

    fun resetPinWithRecovery(context: Context, answer: String, newPin: String): Boolean {
        if (!verifyRecoveryAnswer(context, answer)) return false
        if (newPin.length !in 4..6 || !newPin.all { it.isDigit() }) return false
        val p = prefs(context)
        val salt = generateSalt()
        p.pinSalt = salt
        p.pinHash = hashPin(newPin, salt)
        return true
    }

    fun changePin(context: Context, newPin: String): Boolean {
        if (newPin.length !in 4..6 || !newPin.all { it.isDigit() }) return false
        val p = prefs(context)
        val salt = generateSalt()
        p.pinSalt = salt
        p.pinHash = hashPin(newPin, salt)
        return true
    }

    private fun setEnabled(context: Context, className: String, state: Int) {
        try {
            context.packageManager.setComponentEnabledSetting(
                ComponentName(context.packageName, className),
                state,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not toggle $className", e)
        }
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(Base64.decode(salt, Base64.DEFAULT))
        md.update(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
    }
}
