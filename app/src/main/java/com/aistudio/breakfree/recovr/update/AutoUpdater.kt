package com.aistudio.breakfree.recovr.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.aistudio.breakfree.recovr.BuildConfig
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Checks a remote source for a newer APK and installs it.
 *
 * ## Sources
 *
 *  - **GitHub releases** (default): the latest release of the configured repository. The
 *    release tag is expected to be the version code (`v2`, `v3`, …) so it can be compared
 *    numerically; the APK is taken from the release assets.
 *  - **A `version.json` on a website**: set [checkUrl] to an endpoint that returns
 *    `{ "versionCode": 2, "versionName": "1.1", "downloadUrl": "https://…/app.apk",
 *       "releaseNotes": "…" }`. Useful when the app is self-hosted.
 *
 * ## What it does NOT do
 *
 *  - It never installs silently. Android requires the user to confirm an install unless the
 *    app is the Device Owner *and* uses the PackageInstaller API, which is out of scope
 *    here. The user is taken to the system install screen.
 *  - It does not run in the background on a schedule. Checks happen on app start (at most
 *    once a day) and when the user taps "Check for updates".
 */
object AutoUpdater {

    private const val TAG = "AutoUpdater"

    /** Default source: the GitHub repository the app ships from. */
    const val DEFAULT_CHECK_URL =
        "https://api.github.com/repos/YUouriii/taafi-app/releases/latest"

    const val PREFS = "breakfree_prefs"
    private const val KEY_CHECK_URL = "updater_check_url"
    private const val KEY_LAST_CHECK = "updater_last_check_ms"
    private const val KEY_AUTO_CHECK = "updater_auto_check"

    data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val downloadUrl: String,
        val releaseNotes: String,
        val htmlUrl: String
    ) {
        /**
         * True only when the remote build is strictly newer. A release whose tag cannot be
         * read as a version code is compared by name, and an older name never counts as an
         * update — otherwise a stale tag would offer a downgrade.
         */
        fun isNewerThan(currentCode: Int, currentName: String): Boolean {
            if (versionCode > 0) {
                if (versionCode > currentCode) return true
                if (versionCode < currentCode) return false
            }
            if (versionName.isBlank()) return false
            return compareVersionNames(versionName, currentName) > 0
        }
    }

    /** Numeric dotted-version comparison ("1.10" is newer than "1.9"). */
    internal fun compareVersionNames(a: String, b: String): Int {
        fun parts(value: String): List<Int> = value
            .split('.', '-', '+')
            .map { segment -> segment.takeWhile { it.isDigit() }.toIntOrNull() ?: 0 }
        val left = parts(a)
        val right = parts(b)
        for (i in 0 until maxOf(left.size, right.size)) {
            val l = left.getOrElse(i) { 0 }
            val r = right.getOrElse(i) { 0 }
            if (l != r) return l.compareTo(r)
        }
        return 0
    }

    fun checkUrl(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_CHECK_URL, DEFAULT_CHECK_URL) ?: DEFAULT_CHECK_URL

    fun setCheckUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_CHECK_URL, url.trim()).apply()
    }

    fun autoCheckEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTO_CHECK, true)

    fun setAutoCheckEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_AUTO_CHECK, enabled).apply()
    }

    /** True when an automatic check is due (at most once per day). */
    fun isCheckDue(context: Context): Boolean {
        if (!autoCheckEnabled(context)) return false
        val last = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_CHECK, 0L)
        return System.currentTimeMillis() - last > 24L * 60L * 60L * 1000L
    }

    /**
     * Fetches the latest release. Calls [onResult] on a background thread with either an
     * [UpdateInfo] (when a newer version exists) or null (up to date / no APK found).
     * [onError] receives a human-readable message on failure.
     */
    fun check(
        context: Context,
        language: AppLanguage,
        onResult: (UpdateInfo?) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            try {
                val connection = (URL(checkUrl(context)).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "BreakFree-Updater")
                    connectTimeout = 15000
                    readTimeout = 15000
                }
                val code = connection.responseCode
                if (code !in 200..299) {
                    val msg = when (code) {
                        403 -> Translations.t("update_error_rate_limit", language)
                        404 -> Translations.t("update_error_no_release", language)
                        else -> Translations.t("update_error_server", language)
                            .replace("{code}", code.toString())
                    }
                    onError(msg)
                    connection.disconnect()
                    return@Thread
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()

                val info = parse(body) ?: run {
                    onError(Translations.t("update_error_parse", language))
                    return@Thread
                }

                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()

                if (info.isNewerThan(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)) {
                    onResult(info)
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Update check failed", e)
                onError(Translations.t("update_error_network", language))
            }
        }.start()
    }

    private fun parse(body: String): UpdateInfo? {
        return try {
            val json = JSONObject(body)

            // Website mode: a flat version.json.
            if (json.has("downloadUrl")) {
                return UpdateInfo(
                    versionCode = json.optInt("versionCode", 0),
                    versionName = json.optString("versionName", ""),
                    downloadUrl = json.optString("downloadUrl"),
                    releaseNotes = json.optString("releaseNotes", ""),
                    htmlUrl = json.optString("htmlUrl", "")
                )
            }

            // GitHub release mode.
            val tag = json.optString("tag_name", "").trim().removePrefix("v")
            val versionCode = tag.toIntOrNull() ?: 0
            val versionName = if (versionCode == 0) tag else ""
            val assets = json.optJSONArray("assets")
            var downloadUrl = ""
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.optJSONObject(i) ?: continue
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
            }
            if (downloadUrl.isBlank()) return null

            UpdateInfo(
                versionCode = versionCode,
                versionName = versionName,
                downloadUrl = downloadUrl,
                releaseNotes = json.optString("body", ""),
                htmlUrl = json.optString("html_url", "")
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not parse update JSON", e)
            null
        }
    }

    /**
     * Downloads the APK to the app's cache and launches the system installer.
     * Returns the downloaded file, or null on failure.
     */
    fun downloadAndInstall(
        context: Context,
        info: UpdateInfo,
        onProgress: (Int) -> Unit,
        onComplete: (File?) -> Unit
    ) {
        Thread {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(info.downloadUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 30000
                    readTimeout = 60000
                }
                val total = connection.contentLength.toLong()
                val dir = File(context.cacheDir, "updates").apply { mkdirs() }
                val file = File(dir, "breakfree-update.apk").apply { delete() }

                var downloaded = 0L
                connection.inputStream.use { input ->
                    file.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            downloaded += read
                            if (total > 0) {
                                onProgress(((downloaded * 100) / total).toInt())
                            }
                        }
                    }
                }

                if (file.length() <= 0) {
                    onComplete(null)
                    return@Thread
                }

                install(context, file)
                onComplete(file)
            } catch (e: Exception) {
                Log.w(TAG, "Download failed", e)
                onComplete(null)
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    /** Launches the system install screen for the downloaded APK. */
    fun install(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apk
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Could not launch the installer", e)
        }
    }

    /** True when the user has granted "install unknown apps" for this package. */
    fun canInstallPackages(context: Context): Boolean =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }

    /** Opens the system screen where the user grants "install unknown apps". */
    fun openInstallPermissionSettings(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Could not open install settings", e)
            }
        }
    }
}
