package com.example.model

import com.example.ui.components.CostumeIconType
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
    val moodCostumeType: CostumeIconType
        get() = when (moodScore) {
            1 -> CostumeIconType.MOOD_1
            2 -> CostumeIconType.MOOD_2
            3 -> CostumeIconType.MOOD_3
            4 -> CostumeIconType.MOOD_4
            5 -> CostumeIconType.MOOD_5
            else -> CostumeIconType.MOOD_3
        }

    val moodEmoji: String
        get() = when (moodScore) {
            1 -> "😞"
            2 -> "😟"
            3 -> "😐"
            4 -> "🙂"
            5 -> "🔥"
            else -> "😐"
        }

    // 3-color palette: Green Calm for low urges, White Glowing for elevated urges
    val urgeColorHex: Long
        get() = when {
            urgeIntensity >= 5 -> 0xFFFFFFFF // White Glowing for high urge vigilance
            else -> 0xFF10B981 // Calm Green
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
