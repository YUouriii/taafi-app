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
            context.getSharedPreferences("breakfree_prefs", Context.MODE_PRIVATE).edit().clear().apply()
            // Reset stealth lock state so the app doesn't stay in duress mode forever.
            prefs(context).stealthEnabled = false
            _duressMode.value = false
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Wipe failed", e)
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
    }

    /** Applies the neutral launcher icon/name for [preset]. */
    fun applyDisguise(context: Context, preset: DisguisePreset) {
        val pm = context.packageManager
        // Enable the alias first, then disable the real launcher entry, so the app
        // never disappears from the launcher mid-switch.
        setEnabled(context, preset.aliasClass, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
        setEnabled(context, MAIN_ACTIVITY, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
        Log.i(TAG, "Disguise applied: ${preset.label}")
    }

    /** Restores the real launcher icon/name. */
    fun removeDisguise(context: Context, preset: DisguisePreset) {
        val pm = context.packageManager
        setEnabled(context, MAIN_ACTIVITY, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
        setEnabled(context, preset.aliasClass, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
        Log.i(TAG, "Disguise removed")
    }

    /**
     * Re-applies the disguise on boot / app start if it drifted (e.g. after an update
     * re-enabled the main activity).
     */
    fun syncDisguise(context: Context) {
        val p = prefs(context)
        if (p.stealthEnabled) {
            applyDisguise(context, p.disguisePreset)
        } else {
            // Make sure the real entry is enabled (updates can re-enable aliases).
            setEnabled(context, MAIN_ACTIVITY, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
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
