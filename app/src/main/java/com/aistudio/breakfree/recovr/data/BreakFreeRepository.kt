package com.aistudio.breakfree.recovr.data

import android.content.Context
import android.content.SharedPreferences
import com.aistudio.breakfree.recovr.model.CommunityCategory
import com.aistudio.breakfree.recovr.model.CommunityPost
import com.aistudio.breakfree.recovr.model.DailyCheckIn
import com.aistudio.breakfree.recovr.model.StreakState
import com.aistudio.breakfree.recovr.model.UserProfile
import com.google.firebase.Timestamp
import org.json.JSONArray
import org.json.JSONObject

class BreakFreeRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("breakfree_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CLEAN_SINCE = "clean_since_timestamp"
        private const val KEY_LONGEST_STREAK = "longest_streak_seconds"
        private const val KEY_GRACE_SAVES = "grace_saves_count"
        private const val KEY_SOS_COUNT = "sos_completed_count"
        private const val KEY_RELAPSES_COUNT = "relapses_count"
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_COMMUNITY_POSTS = "community_posts_json"
        private const val KEY_CHECK_INS_PREFIX = "checkins_json_"
        private const val KEY_PENDING_SYNC_PREFIX = "pending_sync_dates_"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"

        /** Local storage bucket used while the user is signed out. */
        const val LOCAL_OWNER = "local"
    }

    fun getGeminiApiKey(): String? {
        val stored = prefs.getString(KEY_GEMINI_API_KEY, null)?.trim()
        if (!stored.isNullOrBlank()) return stored
        return try {
            val field = com.aistudio.breakfree.recovr.BuildConfig::class.java.getField("GEMINI_API_KEY")
            val buildKey = field.get(null) as? String
            buildKey?.trim()?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    fun saveGeminiApiKey(key: String?) {
        if (key.isNullOrBlank()) {
            prefs.edit().remove(KEY_GEMINI_API_KEY).apply()
        } else {
            prefs.edit().putString(KEY_GEMINI_API_KEY, key.trim()).apply()
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

    /**
     * Merge a profile pulled from Firestore into the local store.
     *
     * Policy, in order of precedence:
     *  1. A relapse recorded on this device but not yet uploaded wins — otherwise an
     *     offline relapse would be undone by the longer cloud streak.
     *  2. A relapse recorded in the cloud but not seen here wins — a second device must
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
    // Anonymous community: on-device only, and empty for a new install (no fake users).
    // ---------------------------------------------------------------------------------

    fun getCommunityPosts(): List<CommunityPost> {
        val raw = prefs.getString(KEY_COMMUNITY_POSTS, null)
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val list = mutableListOf<CommunityPost>()
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    CommunityPost(
                        id = obj.getString("id"),
                        authorAlias = obj.getString("authorAlias"),
                        authorAvatarBg = obj.optLong("authorAvatarBg", 0xFF10B981),
                        daysClean = obj.getInt("daysClean"),
                        category = try {
                            CommunityCategory.valueOf(obj.getString("category"))
                        } catch (e: Exception) {
                            CommunityCategory.DAILY_MOTIVATION
                        },
                        contentEn = obj.getString("contentEn"),
                        contentAr = obj.getString("contentAr"),
                        timestampMillis = obj.getLong("timestampMillis"),
                        fistBumps = obj.getInt("fistBumps"),
                        userCheered = obj.optBoolean("userCheered", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCommunityPosts(posts: List<CommunityPost>) {
        val arr = JSONArray()
        for (p in posts) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("authorAlias", p.authorAlias)
            obj.put("authorAvatarBg", p.authorAvatarBg)
            obj.put("daysClean", p.daysClean)
            obj.put("category", p.category.name)
            obj.put("contentEn", p.contentEn)
            obj.put("contentAr", p.contentAr)
            obj.put("timestampMillis", p.timestampMillis)
            obj.put("fistBumps", p.fistBumps)
            obj.put("userCheered", p.userCheered)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_COMMUNITY_POSTS, arr.toString()).apply()
    }

    fun addCommunityPost(post: CommunityPost): List<CommunityPost> {
        val existing = getCommunityPosts().toMutableList()
        existing.add(0, post)
        saveCommunityPosts(existing)
        return existing
    }

    fun toggleFistBump(postId: String): List<CommunityPost> {
        val list = getCommunityPosts().map { post ->
            if (post.id == postId) {
                val newCheered = !post.userCheered
                val newCount = if (newCheered) post.fistBumps + 1 else maxOf(0, post.fistBumps - 1)
                post.copy(fistBumps = newCount, userCheered = newCheered)
            } else {
                post
            }
        }
        saveCommunityPosts(list)
        return list
    }
}
