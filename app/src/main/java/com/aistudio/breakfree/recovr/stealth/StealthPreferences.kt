package com.aistudio.breakfree.recovr.stealth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Persisted switches for Stealth / Disguise Mode.
 *
 * The PIN (and its salt) are stored in [EncryptedSharedPreferences] so they are not
 * readable from a rooted device or a backup. Everything else lives in the ordinary
 * preference store, which is already excluded from backups (`allowBackup="false"`).
 */
class StealthPreferences(context: Context) {

    private val appContext = context.applicationContext

    private val plain: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val secure: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Whether Stealth Mode is currently active. */
    var stealthEnabled: Boolean
        get() = plain.getBoolean(KEY_STEALTH_ENABLED, false)
        set(value) = plain.edit().putBoolean(KEY_STEALTH_ENABLED, value).apply()

    /** Which neutral disguise is shown on the launcher. */
    var disguisePreset: DisguisePreset
        get() = DisguisePreset.fromKey(plain.getString(KEY_DISGUISE_PRESET, DisguisePreset.CALCULATOR.key))
        set(value) = plain.edit().putString(KEY_DISGUISE_PRESET, value.key).apply()

    /** Whether the real dashboard is locked behind biometric/PIN. */
    var appLockEnabled: Boolean
        get() = plain.getBoolean(KEY_APP_LOCK_ENABLED, true)
        set(value) = plain.edit().putBoolean(KEY_APP_LOCK_ENABLED, value).apply()

    /** Auto-lock after this many minutes in the background (0 = never). */
    var autoLockMinutes: Int
        get() = plain.getInt(KEY_AUTO_LOCK_MINUTES, 0)
        set(value) = plain.edit().putInt(KEY_AUTO_LOCK_MINUTES, value).apply()

    /** Whether notifications use neutral wording. */
    var discreetNotifications: Boolean
        get() = plain.getBoolean(KEY_DISCREET_NOTIFICATIONS, true)
        set(value) = plain.edit().putBoolean(KEY_DISCREET_NOTIFICATIONS, value).apply()

    /** Salted hash of the PIN, or null when no PIN is set. */
    var pinHash: String?
        get() = secure.getString(KEY_PIN_HASH, null)
        set(value) {
            if (value == null) secure.edit().remove(KEY_PIN_HASH).apply()
            else secure.edit().putString(KEY_PIN_HASH, value).apply()
        }

    /** Salt for the PIN hash. */
    var pinSalt: String?
        get() = secure.getString(KEY_PIN_SALT, null)
        set(value) {
            if (value == null) secure.edit().remove(KEY_PIN_SALT).apply()
            else secure.edit().putString(KEY_PIN_SALT, value).apply()
        }

    fun hasPin(): Boolean = !pinHash.isNullOrBlank()

    fun clearPin() {
        pinHash = null
        pinSalt = null
    }

    companion object {
        const val PREFS_NAME = "breakfree_stealth_prefs"
        private const val SECURE_PREFS_NAME = "breakfree_stealth_secure"

        /** Quick check usable from other components (services, receivers) without instantiating. */
        fun isStealthEnabled(context: Context): Boolean =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean("stealth_enabled", false)

        fun discreetNotifications(context: Context): Boolean =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean("discreet_notifications", true)

        private const val KEY_STEALTH_ENABLED = "stealth_enabled"
        private const val KEY_DISGUISE_PRESET = "disguise_preset"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_AUTO_LOCK_MINUTES = "auto_lock_minutes"
        private const val KEY_DISCREET_NOTIFICATIONS = "discreet_notifications"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
    }
}

enum class DisguisePreset(val key: String, val label: String, val aliasClass: String) {
    CALCULATOR("calculator", "Calculator", "com.aistudio.breakfree.recovr.alias.CalculatorAlias"),
    NOTES("notes", "Notes", "com.aistudio.breakfree.recovr.alias.NotesAlias"),
    WEATHER("weather", "Weather", "com.aistudio.breakfree.recovr.alias.WeatherAlias");

    companion object {
        fun fromKey(key: String?): DisguisePreset =
            entries.firstOrNull { it.key == key } ?: CALCULATOR
    }
}
