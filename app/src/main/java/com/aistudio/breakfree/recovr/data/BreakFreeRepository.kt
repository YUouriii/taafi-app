package com.aistudio.breakfree.recovr.data

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aistudio.breakfree.recovr.ai.AiProviderConfig
import com.aistudio.breakfree.recovr.ai.AiProviderKind
import com.aistudio.breakfree.recovr.model.DailyCheckIn
import com.aistudio.breakfree.recovr.model.PauseGateUiState
import com.aistudio.breakfree.recovr.model.PauseOutcome
import com.aistudio.breakfree.recovr.model.PauseSession
import com.aistudio.breakfree.recovr.model.StreakState
import com.aistudio.breakfree.recovr.model.UserProfile
import com.google.firebase.Timestamp
import org.json.JSONArray
import org.json.JSONObject

class BreakFreeRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = context.getSharedPreferences("breakfree_prefs", Context.MODE_PRIVATE)

    /**
     * API keys live in [EncryptedSharedPreferences]: a key is a credential, and the
     * ordinary preference file is readable by anything with access to the app's data.
     * Null when the keystore is unavailable (corrupted key, restored backup): the app
     * then simply behaves as if no key were configured instead of crashing at startup.
     */
    private val securePrefs: SharedPreferences? by lazy {
        runCatching {
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
        }.getOrNull()
    }

    companion object {
        private const val KEY_CLEAN_SINCE = "clean_since_timestamp"
        private const val KEY_LONGEST_STREAK = "longest_streak_seconds"
        private const val KEY_GRACE_SAVES = "grace_saves_count"
        private const val KEY_SOS_COUNT = "sos_completed_count"
        private const val KEY_RELAPSES_COUNT = "relapses_count"
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_COMMUNITY_CHEERED = "community_cheered_ids"
        private const val KEY_CHECK_INS_PREFIX = "checkins_json_"
        private const val KEY_PENDING_SYNC_PREFIX = "pending_sync_dates_"

        // The Pause: the user's promise, the live gate, and the local session history.
        private const val KEY_MY_PROMISE = "my_promise_text"
        private const val KEY_PAUSE_ACTIVE_START = "pause_active_start"
        private const val KEY_PAUSE_SESSIONS = "pause_sessions_json"

        // Rewire 30 micro-lessons
        private const val KEY_REWIRE_COMPLETED = "rewire_completed_ids"
        private const val KEY_REWIRE_CURRENT = "rewire_current_index"

        // Weekly insight card
        private const val KEY_LAST_INSIGHT_WEEK = "last_insight_week"

        // AI chat: whether the suggestion chips tray is collapsed.
        private const val KEY_AI_SUGGESTIONS_HIDDEN = "ai_suggestions_hidden"

        /** Keep the local history small; it is a counter and a pattern, not an archive. */
        private const val MAX_PAUSE_SESSIONS = 500

        // AI provider settings. The key itself is kept in the encrypted store.
        private const val KEY_AI_PROVIDER = "ai_provider_kind"
        private const val KEY_AI_BASE_URL = "ai_base_url"
        private const val KEY_AI_MODEL = "ai_model"
        private const val KEY_AI_API_KEY = "ai_api_key"

        // Pre-1.3 single-provider key, migrated on first read.
        private const val LEGACY_KEY_GEMINI_API_KEY = "gemini_api_key"

        private const val SECURE_PREFS_NAME = "breakfree_ai_secure"

        /** Local storage bucket used while the user is signed out. */
        const val LOCAL_OWNER = "local"
    }

    /**
     * The AI provider the user configured. This is always Google Gemini: either the
     * user's own free AI Studio key, or the key-less Firebase AI Logic route when they
     * are signed in with Google. A missing key means "no direct model": the counselor
     * falls back to its offline guidance instead of pretending to call something.
     */
    fun getAiConfig(): AiProviderConfig {
        migrateLegacyGeminiKey()
        return AiProviderConfig(
            kind = AiProviderKind.fromKey(prefs.getString(KEY_AI_PROVIDER, null)),
            baseUrl = prefs.getString(KEY_AI_BASE_URL, null).orEmpty(),
            model = prefs.getString(KEY_AI_MODEL, null).orEmpty(),
            apiKey = securePrefs?.getString(KEY_AI_API_KEY, null).orEmpty()
        )
    }

    fun saveAiConfig(config: AiProviderConfig) {
        prefs.edit()
            .putString(KEY_AI_PROVIDER, config.kind.key)
            .putString(KEY_AI_BASE_URL, config.baseUrl.trim())
            .putString(KEY_AI_MODEL, config.model.trim())
            .apply()

        val key = config.apiKey.trim()
        val secure = securePrefs ?: return
        if (key.isBlank()) {
            secure.edit().remove(KEY_AI_API_KEY).apply()
        } else {
            secure.edit().putString(KEY_AI_API_KEY, key).apply()
        }
    }

    fun clearAiApiKey() {
        securePrefs?.edit()?.remove(KEY_AI_API_KEY)?.apply()
    }

    /** Moves a key saved by an older build out of the plain preference file. */
    private fun migrateLegacyGeminiKey() {
        val secure = securePrefs ?: return
        if (secure.contains(KEY_AI_API_KEY)) {
            prefs.edit().remove(LEGACY_KEY_GEMINI_API_KEY).apply()
            return
        }
        val legacy = prefs.getString(LEGACY_KEY_GEMINI_API_KEY, null)?.trim()
        if (legacy.isNullOrBlank()) return
        secure.edit().putString(KEY_AI_API_KEY, legacy).apply()
        prefs.edit()
            .remove(LEGACY_KEY_GEMINI_API_KEY)
            .putString(KEY_AI_PROVIDER, AiProviderKind.GEMINI.key)
            .apply()
    }

    /** True when a validated network is available right now. */
    fun isOnline(): Boolean {
        return try {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            false
        }
    }

    fun getStreakState(): StreakState {
        val now = System.currentTimeMillis()

        // Never invent progress. A fresh install starts at zero and the clock starts
        // running from the very first launch. Previously this defaulted to a fake
        // "5 days, 4 hours clean" streak, which is misleading in a recovery app.
        val cleanSince = if (prefs.contains(KEY_CLEAN_SINCE)) {
            prefs.getLong(KEY_CLEAN_SINCE, now)
        } else {
            prefs.edit().putLong(KEY_CLEAN_SINCE, now).apply()
            now
        }

        return StreakState(
            cleanSinceTimestamp = cleanSince,
            currentTimestamp = now,
            longestStreakSeconds = prefs.getLong(KEY_LONGEST_STREAK, 0L),
            relapsesCount = prefs.getInt(KEY_RELAPSES_COUNT, 0),
            gracePeriodSavesCount = prefs.getInt(KEY_GRACE_SAVES, 0),
            sosCompletedCount = prefs.getInt(KEY_SOS_COUNT, 0)
        )
    }

    fun saveCleanSinceTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_CLEAN_SINCE, timestamp).apply()
    }

    fun recordGracePeriodSave(): Int {
        val current = prefs.getInt(KEY_GRACE_SAVES, 0) + 1
        prefs.edit().putInt(KEY_GRACE_SAVES, current).apply()
        return current
    }

    fun recordSosCompleted(): Int {
        val current = prefs.getInt(KEY_SOS_COUNT, 0) + 1
        prefs.edit().putInt(KEY_SOS_COUNT, current).apply()
        return current
    }

    fun resetStreakDueToRelapse() {
        val currentStreak = getStreakState().totalSecondsClean
        val longest = prefs.getLong(KEY_LONGEST_STREAK, 0L)
        if (currentStreak > longest) {
            prefs.edit().putLong(KEY_LONGEST_STREAK, currentStreak).apply()
        }
        val relapses = prefs.getInt(KEY_RELAPSES_COUNT, 0) + 1
        prefs.edit()
            .putLong(KEY_CLEAN_SINCE, System.currentTimeMillis())
            .putInt(KEY_RELAPSES_COUNT, relapses)
            .apply()
    }

    // ---------------------------------------------------------------------------------
    // The Pause: a ten-minute gate offered before a relapse.
    //
    // Everything here is local-only. The promise is the sentence the user wants to read
    // in that moment; the session history feeds the "urges survived" counter and, later,
    // the weekly insight card. Nothing is uploaded.
    // ---------------------------------------------------------------------------------

    fun getMyPromise(): String = prefs.getString(KEY_MY_PROMISE, null).orEmpty()

    fun saveMyPromise(text: String) {
        prefs.edit().putString(KEY_MY_PROMISE, text.trim()).apply()
    }

    /** Start time of a gate that is currently open, or 0 when there is none. */
    fun getActivePauseStart(): Long = prefs.getLong(KEY_PAUSE_ACTIVE_START, 0L)

    fun setActivePauseStart(startedAtMs: Long) {
        prefs.edit().putLong(KEY_PAUSE_ACTIVE_START, startedAtMs).apply()
    }

    fun getPauseSessions(): List<PauseSession> {
        val raw = prefs.getString(KEY_PAUSE_SESSIONS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<PauseSession>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(
                    PauseSession(
                        id = obj.optString("id"),
                        startedAtMs = obj.optLong("startedAtMs", 0L),
                        durationMs = obj.optLong("durationMs", PauseGateUiState.PAUSE_DURATION_MS),
                        outcome = runCatching {
                            PauseOutcome.valueOf(obj.optString("outcome"))
                        }.getOrDefault(PauseOutcome.ENDED)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun recordPauseSession(session: PauseSession): List<PauseSession> {
        val updated = (listOf(session) + getPauseSessions()).take(MAX_PAUSE_SESSIONS)
        val arr = JSONArray()
        for (s in updated) {
            arr.put(
                JSONObject().apply {
                    put("id", s.id)
                    put("startedAtMs", s.startedAtMs)
                    put("durationMs", s.durationMs)
                    put("outcome", s.outcome.name)
                }
            )
        }
        prefs.edit().putString(KEY_PAUSE_SESSIONS, arr.toString()).apply()
        return updated
    }

    /** How many waves the user has ridden out through the gate. */
    fun getUrgesSurvivedCount(): Int =
        getPauseSessions().count { it.outcome == PauseOutcome.SURVIVED }

    // ---------------------------------------------------------------------------------
    // Rewire 30: micro-lessons. Progress is local-only.
    // ---------------------------------------------------------------------------------

    fun getRewireCompletedIds(): Set<Int> {
        val raw = prefs.getString(KEY_REWIRE_COMPLETED, null) ?: return emptySet()
        return raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    fun markRewireLessonComplete(id: Int) {
        val updated = getRewireCompletedIds() + id
        prefs.edit().putString(KEY_REWIRE_COMPLETED, updated.joinToString(",")).apply()
    }

    fun getRewireCurrentIndex(): Int = prefs.getInt(KEY_REWIRE_CURRENT, 1).coerceIn(1, 30)

    fun setRewireCurrentIndex(index: Int) {
        prefs.edit().putInt(KEY_REWIRE_CURRENT, index.coerceIn(1, 30)).apply()
    }

    // ---------------------------------------------------------------------------------
    // Weekly insight card: track which ISO week was last acknowledged.
    // ---------------------------------------------------------------------------------

    fun getLastInsightWeek(): Long = prefs.getLong(KEY_LAST_INSIGHT_WEEK, 0L)

    fun setLastInsightWeek(weekStartMs: Long) {
        prefs.edit().putLong(KEY_LAST_INSIGHT_WEEK, weekStartMs).apply()
    }

    // ---------------------------------------------------------------------------------
    // AI chat: suggestion tray visibility survives tab switches and restarts.
    // ---------------------------------------------------------------------------------

    fun getAiSuggestionsHidden(): Boolean = prefs.getBoolean(KEY_AI_SUGGESTIONS_HIDDEN, false)

    fun setAiSuggestionsHidden(hidden: Boolean) {
        prefs.edit().putBoolean(KEY_AI_SUGGESTIONS_HIDDEN, hidden).apply()
    }

    /**
     * Merge a profile pulled from Firestore into the local store.
     *
     * Policy, in order of precedence:
     *  1. A relapse recorded on this device but not yet uploaded wins - otherwise an
     *     offline relapse would be undone by the longer cloud streak.
     *  2. A relapse recorded in the cloud but not seen here wins - a second device must
     *     not resurrect a streak that was already reset.
     *  3. Same history on both sides: the longer current streak wins, so a fresh
     *     install adopts the account's streak.
     *
     * Counters only ever move forward.
     *
     * @return true when the local profile is at least as advanced as the cloud copy and
     *         is therefore safe to push back up.
     */
    fun applyCloudProfile(profile: UserProfile): Boolean {
        val now = System.currentTimeMillis()
        val localCleanSince = prefs.getLong(KEY_CLEAN_SINCE, now)
        val localLongest = prefs.getLong(KEY_LONGEST_STREAK, 0L)
        val localRelapses = prefs.getInt(KEY_RELAPSES_COUNT, 0)

        val localStreakSeconds = ((now - localCleanSince) / 1000L).coerceAtLeast(0L)
        val cloudStreakSeconds = if (profile.cleanSinceTimestamp > 0L) {
            ((now - profile.cleanSinceTimestamp) / 1000L).coerceAtLeast(0L)
        } else {
            0L
        }

        val adoptCloudStreak = when {
            // Nothing usable in the cloud: keep the local clock.
            profile.cleanSinceTimestamp <= 0L -> false
            // Local relapse the cloud has not seen yet: the local reset is newer news.
            localRelapses > profile.relapsesCount -> false
            // Cloud relapse this device has not seen: adopt the reset clock.
            profile.relapsesCount > localRelapses -> true
            else -> cloudStreakSeconds > localStreakSeconds
        }

        if (adoptCloudStreak) {
            prefs.edit().putLong(KEY_CLEAN_SINCE, profile.cleanSinceTimestamp).apply()
        }

        prefs.edit()
            .putLong(KEY_LONGEST_STREAK, maxOf(localLongest, profile.longestStreakSeconds))
            .putInt(KEY_GRACE_SAVES, maxOf(prefs.getInt(KEY_GRACE_SAVES, 0), profile.gracePeriodSavesCount))
            .putInt(KEY_SOS_COUNT, maxOf(prefs.getInt(KEY_SOS_COUNT, 0), profile.sosCompletedCount))
            .putInt(KEY_RELAPSES_COUNT, maxOf(prefs.getInt(KEY_RELAPSES_COUNT, 0), profile.relapsesCount))
            .apply()

        return !adoptCloudStreak
    }

    fun getLanguageCode(): String {
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun saveLanguageCode(code: String) {
        prefs.edit().putString(KEY_LANGUAGE, code).apply()
    }

    // ---------------------------------------------------------------------------------
    // Daily check-ins: stored locally per account so they survive restarts and work
    // offline. Firestore is a sync target, not the source of truth.
    // ---------------------------------------------------------------------------------

    fun getCheckIns(owner: String): List<DailyCheckIn> {
        val raw = prefs.getString(KEY_CHECK_INS_PREFIX + owner, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<DailyCheckIn>()
            for (i in 0 until arr.length()) {
                list.add(parseCheckIn(arr.getJSONObject(i)))
            }
            list.sortedByDescending { it.dateString }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCheckIns(owner: String, checkIns: List<DailyCheckIn>) {
        val arr = JSONArray()
        for (c in checkIns) {
            arr.put(serializeCheckIn(c))
        }
        prefs.edit().putString(KEY_CHECK_INS_PREFIX + owner, arr.toString()).apply()
    }

    fun clearCheckIns(owner: String) {
        prefs.edit().remove(KEY_CHECK_INS_PREFIX + owner).apply()
    }

    // ---------------------------------------------------------------------------------
    // Pending sync markers.
    //
    // A check-in is written locally first, then mirrored to Firestore. If that mirror
    // write fails, the date is remembered here so a later cloud snapshot cannot revert
    // the local edit and so the upload can be retried on the next sign-in.
    // ---------------------------------------------------------------------------------

    fun getPendingSyncDates(owner: String): Set<String> {
        val raw = prefs.getString(KEY_PENDING_SYNC_PREFIX + owner, null) ?: return emptySet()
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun markPendingSync(owner: String, dateString: String) {
        if (dateString.isBlank()) return
        val updated = getPendingSyncDates(owner) + dateString
        prefs.edit().putString(KEY_PENDING_SYNC_PREFIX + owner, updated.joinToString(",")).apply()
    }

    fun clearPendingSync(owner: String, dateString: String) {
        val updated = getPendingSyncDates(owner) - dateString
        prefs.edit().putString(KEY_PENDING_SYNC_PREFIX + owner, updated.joinToString(",")).apply()
    }

    private fun serializeCheckIn(checkIn: DailyCheckIn): JSONObject = JSONObject().apply {
        put("id", checkIn.id)
        put("userId", checkIn.userId)
        put("dateString", checkIn.dateString)
        put("moodScore", checkIn.moodScore)
        put("urgeIntensity", checkIn.urgeIntensity)
        put("primaryEmotions", JSONArray(checkIn.primaryEmotions))
        put("triggersEncountered", JSONArray(checkIn.triggersEncountered))
        put("reflectionNotes", checkIn.reflectionNotes)
        put("streakProtected", checkIn.streakProtected)
        put("createdAtMillis", checkIn.createdAt?.toDate()?.time ?: JSONObject.NULL)
    }

    private fun parseCheckIn(obj: JSONObject): DailyCheckIn {
        val createdAt = if (obj.isNull("createdAtMillis")) {
            null
        } else {
            val millis = obj.optLong("createdAtMillis", 0L)
            if (millis > 0L) {
                Timestamp(millis / 1000L, ((millis % 1000L) * 1_000_000L).toInt())
            } else {
                null
            }
        }

        return DailyCheckIn(
            id = obj.optString("id"),
            userId = obj.optString("userId"),
            dateString = obj.optString("dateString"),
            moodScore = obj.optInt("moodScore", 3),
            urgeIntensity = obj.optInt("urgeIntensity", 0),
            primaryEmotions = obj.optJSONArray("primaryEmotions").toStringList(),
            triggersEncountered = obj.optJSONArray("triggersEncountered").toStringList(),
            reflectionNotes = obj.optString("reflectionNotes"),
            streakProtected = obj.optBoolean("streakProtected", true),
            createdAt = createdAt
        )
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        val out = mutableListOf<String>()
        for (i in 0 until length()) {
            val value = optString(i)
            if (value.isNotBlank()) out.add(value)
        }
        return out
    }


    // ---------------------------------------------------------------------------------
    // Anonymous community: the feed is public and cloud-only. Nothing is written to the
    // device, so a post either reaches every user or does not exist. Only the "I bumped
    // this" flags are local, because they describe this device's own taps.
    // ---------------------------------------------------------------------------------

    fun getCheeredPostIds(): Set<String> {
        val raw = prefs.getString(KEY_COMMUNITY_CHEERED, null) ?: return emptySet()
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun setPostCheered(postId: String, cheered: Boolean) {
        if (postId.isBlank()) return
        val current = getCheeredPostIds()
        val updated = if (cheered) current + postId else current - postId
        prefs.edit().putString(KEY_COMMUNITY_CHEERED, updated.joinToString(",")).apply()
    }

    /** Drops posts cached by older builds that stored the feed on-device. */
    fun clearLegacyCommunityCache() {
        prefs.edit().remove("community_posts_json").apply()
    }
}
