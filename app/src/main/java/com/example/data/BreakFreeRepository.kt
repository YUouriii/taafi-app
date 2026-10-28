package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ChatMessage
import com.example.model.CommunityCategory
import com.example.model.CommunityPost
import com.example.model.StreakState
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
    }

    fun getStreakState(): StreakState {
        val now = System.currentTimeMillis()
        // Default to 5 days, 4 hours clean if first time launch to showcase non-empty rich stats immediately
        val defaultCleanSince = now - (5L * 24 * 3600 * 1000 + 4 * 3600 * 1000 + 32 * 60 * 1000)
        val cleanSince = prefs.getLong(KEY_CLEAN_SINCE, defaultCleanSince)
        val longest = prefs.getLong(KEY_LONGEST_STREAK, 14L * 24 * 3600)
        val graceSaves = prefs.getInt(KEY_GRACE_SAVES, 3)
        val sosCount = prefs.getInt(KEY_SOS_COUNT, 2)
        val relapses = prefs.getInt(KEY_RELAPSES_COUNT, 0)

        return StreakState(
            cleanSinceTimestamp = cleanSince,
            currentTimestamp = now,
            longestStreakSeconds = longest,
            relapsesCount = relapses,
            gracePeriodSavesCount = graceSaves,
            sosCompletedCount = sosCount
        )
    }

    fun saveCleanSinceTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_CLEAN_SINCE, timestamp).apply()
    }

    fun recordGracePeriodSave(): Int {
        val current = prefs.getInt(KEY_GRACE_SAVES, 3) + 1
        prefs.edit().putInt(KEY_GRACE_SAVES, current).apply()
        return current
    }

    fun recordSosCompleted(): Int {
        val current = prefs.getInt(KEY_SOS_COUNT, 2) + 1
        prefs.edit().putInt(KEY_SOS_COUNT, current).apply()
        return current
    }

    fun resetStreakDueToRelapse() {
        val currentStreak = getStreakState().totalSecondsClean
        val longest = prefs.getLong(KEY_LONGEST_STREAK, 14L * 24 * 3600)
        if (currentStreak > longest) {
            prefs.edit().putLong(KEY_LONGEST_STREAK, currentStreak).apply()
        }
        val relapses = prefs.getInt(KEY_RELAPSES_COUNT, 0) + 1
        prefs.edit()
            .putLong(KEY_CLEAN_SINCE, System.currentTimeMillis())
            .putInt(KEY_RELAPSES_COUNT, relapses)
            .apply()
    }

    fun getLanguageCode(): String {
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun saveLanguageCode(code: String) {
        prefs.edit().putString(KEY_LANGUAGE, code).apply()
    }

    fun getCommunityPosts(): List<CommunityPost> {
        val raw = prefs.getString(KEY_COMMUNITY_POSTS, null)
        if (raw.isNullOrBlank()) {
            val initial = getInitialCommunityPosts()
            saveCommunityPosts(initial)
            return initial
        }
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
            getInitialCommunityPosts()
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

    private fun getInitialCommunityPosts(): List<CommunityPost> {
        val now = System.currentTimeMillis()
        return listOf(
            CommunityPost(
                id = "post-1",
                authorAlias = "SoberSpartan #204",
                authorAvatarBg = 0xFF10B981,
                daysClean = 42,
                category = CommunityCategory.GRACE_VICTORIES,
                contentEn = "Yesterday I opened a site impulsively. The 120-second grace timer popped up—that red countdown gave me the exact shock of awareness I needed. I closed the tab at 45 seconds. Streak saved, but more importantly, self-respect saved!",
                contentAr = "أمس فتحت موقعاً باندفاع. ظهر عداد الـ 120 ثانية التحذيري—ذلك العد التنازلي كان الصدمة الواعية التي احتجتها بالضبط. أغلقت الصفحة عند الثانية 45. لم أحفظ عدادي فحسب، بل حفظت احترامي لذاتي!",
                timestampMillis = now - 3 * 3600 * 1000,
                fistBumps = 29
            ),
            CommunityPost(
                id = "post-2",
                authorAlias = "نور_التعافي",
                authorAvatarBg = 0xFF06B6D4,
                daysClean = 19,
                category = CommunityCategory.URGE_SURFING,
                contentEn = "Remember the 15-minute neuro rule! When an urge strikes, it screams that it will last forever. It won't. I did the 4-7-8 breathing twice, splashed icy water, and by minute 12 my heart rate was back to normal.",
                contentAr = "تذكروا دائماً قاعدة الـ 15 دقيقة العصبية! عندما تهاجمك الرغبة يخدعك عقلك بأنها لن تنتهي. الحقيقة أنها مجرد موجة. قمت بتمرين تنفس 4-7-8 مرتين وغسلت وجهي بماء مثلج، وبحلول الدقيقة 12 عادت نبضاتي طبيعية تماماً.",
                timestampMillis = now - 8 * 3600 * 1000,
                fistBumps = 41
            ),
            CommunityPost(
                id = "post-3",
                authorAlias = "IronPhoenix",
                authorAvatarBg = 0xFF8B5CF6,
                daysClean = 94,
                category = CommunityCategory.MILESTONES,
                contentEn = "Hit 90+ days clean! For anyone on Day 3 or Day 7 thinking their brain will never enjoy normal life again: IT DOES. My energy, eye contact, and deep sleep are unrecognizably better. Keep pushing!",
                contentAr = "تجاوزت حاجز الـ 90 يوماً! لكل أخ في اليوم الثالث أو السابع يشعر أن عقله لن يستمتع بالحياة الطبيعية مجدداً: أقسم لك أن المتعة تعود أضعافاً. نومي، نظرات عيني، وطاقتي اليومية تحولت 180 درجة. اثبتوا!",
                timestampMillis = now - 22 * 3600 * 1000,
                fistBumps = 87
            ),
            CommunityPost(
                id = "post-4",
                authorAlias = "Al-Musafer #77",
                authorAvatarBg = 0xFFF59E0B,
                daysClean = 8,
                category = CommunityCategory.DAILY_MOTIVATION,
                contentEn = "Halting triggers before they start: whenever I feel an urge, I ask myself HALT (Am I Hungry, Angry, Lonely, or Tired?). 90% of the time, I'm just sleep-deprived or lonely.",
                contentAr = "إيقاف المحفزات قبل اشتعالها: كلما شعرت برغبة أسأل نفسي قاعدة HALT (هل أنا جائع، غاضب، وحيد، أم مرهق؟). في 90% من الحالات أكتشف أنني مجرد متعب بحاجة للنوم أو محادثة صديق.",
                timestampMillis = now - 36 * 3600 * 1000,
                fistBumps = 33
            )
        )
    }
}
