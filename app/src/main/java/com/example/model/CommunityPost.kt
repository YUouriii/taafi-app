package com.example.model

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
    val emoji: String
) {
    ALL("All", "الكل", "🌐"),
    MILESTONES("Milestones", "إنجازات", "🏆"),
    URGE_SURFING("Urge Surfing", "مواجهة الرغبات", "🌊"),
    DAILY_MOTIVATION("Daily Fuel", "تحفيز يومي", "🔥"),
    GRACE_VICTORIES("Grace Victories", "انتصارات الوعي", "⏳")
}
