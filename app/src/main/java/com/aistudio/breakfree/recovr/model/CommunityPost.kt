package com.aistudio.breakfree.recovr.model

import com.aistudio.breakfree.recovr.ui.components.CostumeIconType

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
    val titleFr: String,
    val costumeType: CostumeIconType
) {
    ALL("All", "الكل", "Tout", CostumeIconType.COMMUNITY),
    MILESTONES("Milestones", "إنجازات", "Étapes", CostumeIconType.TROPHY),
    URGE_SURFING("Urge Surfing", "مواجهة الرغبات", "Surf d'envie", CostumeIconType.WAVE),
    DAILY_MOTIVATION("Daily Fuel", "تحفيز يومي", "Carburant quotidien", CostumeIconType.FLAME),
    GRACE_VICTORIES("Grace Victories", "انتصارات الوعي", "Victoires de grâce", CostumeIconType.TIMER)
}
