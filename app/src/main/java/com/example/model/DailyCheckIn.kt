package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class DailyCheckIn(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateString: String = "",
    val moodScore: Int = 3,
    val urgeIntensity: Int = 0,
    val primaryEmotions: List<String> = emptyList(),
    val triggersEncountered: List<String> = emptyList(),
    val reflectionNotes: String = "",
    val streakProtected: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val moodEmoji: String
        get() = when (moodScore) {
            1 -> "😞" // Struggling / Low
            2 -> "😟" // Stressed / Vulnerable
            3 -> "😐" // Neutral / Stable
            4 -> "🙂" // Good / Clear
            5 -> "🔥" // Empowered / Victorious
            else -> "😐"
        }

    val urgeColorHex: Long
        get() = when {
            urgeIntensity >= 7 -> 0xFFF43F5E // Rose warning
            urgeIntensity >= 4 -> 0xFFF59E0B // Amber
            urgeIntensity >= 1 -> 0xFF06B6D4 // Cyan
            else -> 0xFF10B981 // Emerald calm
        }
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val cleanSinceTimestamp: Long = 0L,
    val longestStreakSeconds: Long = 0L,
    val gracePeriodSavesCount: Int = 0,
    val sosCompletedCount: Int = 0,
    val relapsesCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
