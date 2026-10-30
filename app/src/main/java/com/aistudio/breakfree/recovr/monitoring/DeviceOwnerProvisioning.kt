package com.aistudio.breakfree.recovr.monitoring

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/**
 * Builds the two honest ways of turning BreakFree into a Device Owner:
 *
 *  - the one-line `adb` command, run from a computer, and
 *  - the QR code Android reads during first-time setup after a factory reset.
 *
 * Android has no in-app "become Device Owner" button; these are the only provisioning
 * paths available to a normal APK. Everything here is pure data — nothing is enabled
 * until the system itself performs the provisioning.
 */
object DeviceOwnerProvisioning {

    private const val TAG = "DeviceOwnerProvisioning"
    private const val PREFS = "breakfree_prefs"
    private const val KEY_APK_URL = "device_owner_apk_url"

    /** The administrator component, in the `package/class` form both adb and QR expect. */
    fun adminComponent(context: Context): String =
        ComponentName(context, UninstallGuardReceiver::class.java).flattenToString()

    /** The exact command a computer has to run. */
    fun adbCommand(context: Context): String =
        "adb shell dpm set-device-owner ${adminComponent(context)}"

    /**
     * Where the APK that the QR points at is hosted. Kept in preferences because the
     * link is chosen by whoever publishes the build, not something we can guess.
     */
    fun savedApkUrl(context: Context): String =
        prefs(context).getString(KEY_APK_URL, "").orEmpty()

    fun saveApkUrl(context: Context, url: String) {
        prefs(context).edit().putString(KEY_APK_URL, url).apply()
    }

    /**
     * Base64 (URL-safe, unpadded) SHA-256 of the APK currently installed. The provisioning
     * payload carries it so Android refuses to install a different file from the link.
     */
    fun apkChecksum(context: Context): String? = try {
        val digest = MessageDigest.getInstance("SHA-256")
        File(context.applicationInfo.sourceDir).inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        Base64.encodeToString(
            digest.digest(),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    } catch (e: Exception) {
        Log.w(TAG, "Could not hash the installed APK", e)
        null
    }

    /**
     * The JSON payload Android's setup wizard expects when it scans the QR code.
     * Keys are the platform's own extra names; they are not ours to rename.
     */
    fun qrPayload(context: Context, apkUrl: String, checksum: String): String =
        JSONObject().apply {
            put(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                adminComponent(context)
            )
            put(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION,
                apkUrl
            )
            put(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM,
                checksum
            )
            // Leave the rest of the phone usable; BreakFree only wants the uninstall block.
            put(DevicePolicyManager.EXTRA_PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED, true)
            put(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION, true)
        }.toString()

    /** A link is only usable if the setup wizard can download the APK over TLS. */
    fun isUsableApkUrl(url: String): Boolean =
        url.startsWith("https://") && url.length > "https://".length

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
