package com.example.model

import com.example.ui.components.CostumeIconType

data class CommunityPost(
    val id: String,
    val authorAlias: String,
    val authorAvatarBg: Long = 0xFF10B981,
    val daysClean: Int,
    val category: CommunityCategory,
    val contentEn: String,
    val contentAr: String,
    val timestampMillis: Long,
    val fistBumps: Int = 0,
    val userCheered: Boolean = false
)

enum class CommunityCategory(
    val titleEn: String,
    val titleAr: String,
    val emoji: String,
    val costumeType: CostumeIconType
) {
    ALL("All", "الكل", "🌐", CostumeIconType.COMMUNITY),
    MILESTONES("Milestones", "إنجازات", "🏆", CostumeIconType.TROPHY),
    URGE_SURFING("Urge Surfing", "مواجهة الرغبات", "🌊", CostumeIconType.WAVE),
    DAILY_MOTIVATION("Daily Fuel", "تحفيز يومي", "🔥", CostumeIconType.FLAME),
    GRACE_VICTORIES("Grace Victories", "انتصارات الوعي", "⏳", CostumeIconType.TIMER)
}
