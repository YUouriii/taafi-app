package com.example.model

import com.example.ui.components.CostumeIconType

data class Badge(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val iconEmoji: String = "",
    val costumeType: CostumeIconType = CostumeIconType.SPROUT,
    val requiredDays: Int = 0,
    val isSpecial: Boolean = false,
    val unlocked: Boolean = false
)

object BadgesRepository {
    fun getAllBadges(streakDays: Long, graceSaves: Int, sosCompleted: Int): List<Badge> {
        return listOf(
            Badge(
                id = "spark",
                titleEn = "First Spark",
                titleAr = "الشرارة الأولى",
                descriptionEn = "Completed 24 clean hours. The journey of freedom begins.",
                descriptionAr = "إتمام 24 ساعة نقية. رحلة الحرية قد بدأت.",
                iconEmoji = "🔥",
                costumeType = CostumeIconType.FLAME,
                requiredDays = 1,
                unlocked = streakDays >= 1
            ),
            Badge(
                id = "clarity",
                titleEn = "Clarity Dawning",
                titleAr = "بزوغ الوضوح",
                descriptionEn = "3 days clean. Brain fog begins to lift.",
                descriptionAr = "3 أيام من النقاء. ضباب الدماغ يبدأ في التلاشي.",
                iconEmoji = "🌅",
                costumeType = CostumeIconType.SUNRISE,
                requiredDays = 3,
                unlocked = streakDays >= 3
            ),
            Badge(
                id = "fortress",
                titleEn = "One Week Fortress",
                titleAr = "حصن الأسبوع",
                descriptionEn = "7 days clean! Peak acute withdrawal conquered.",
                descriptionAr = "7 أيام نقية! تجاوز ذروة الانسحاب الحاد بنجاح.",
                iconEmoji = "🏰",
                costumeType = CostumeIconType.FORTRESS,
                requiredDays = 7,
                unlocked = streakDays >= 7
            ),
            Badge(
                id = "rewired",
                titleEn = "Neural Shift",
                titleAr = "التحول العصبي",
                descriptionEn = "14 days clean. Dopamine receptors multiplying.",
                descriptionAr = "14 يوماً من النقاء. مستقبلات الدوبامين تتضاعف وتتعافى.",
                iconEmoji = "🧠",
                costumeType = CostumeIconType.BRAIN,
                requiredDays = 14,
                unlocked = streakDays >= 14
            ),
            Badge(
                id = "iron_will",
                titleEn = "Iron Will",
                titleAr = "إرادة فولاذية",
                descriptionEn = "30 days of unyielding discipline and self-mastery.",
                descriptionAr = "30 يوماً من الانضباط الراسخ والتحكم بالذات.",
                iconEmoji = "🛡️",
                costumeType = CostumeIconType.SHIELD,
                requiredDays = 30,
                unlocked = streakDays >= 30
            ),
            Badge(
                id = "unshakeable",
                titleEn = "Unshakeable",
                titleAr = "ثبات لا يتزعزع",
                descriptionEn = "60 days of transformation. New identity cemented.",
                descriptionAr = "60 يوماً من التحول الحقيقي. ترسيخ الهوية الجديدة.",
                iconEmoji = "⚡",
                costumeType = CostumeIconType.LIGHTNING,
                requiredDays = 60,
                unlocked = streakDays >= 60
            ),
            Badge(
                id = "reborn",
                titleEn = "Reborn Free",
                titleAr = "ولادة الحرية",
                descriptionEn = "90 days milestone! Full neural baseline restoration.",
                descriptionAr = "إنجاز 90 يوماً! استعادة التوازن الكيميائي الدماغي الكامل.",
                iconEmoji = "👑",
                costumeType = CostumeIconType.CROWN,
                requiredDays = 90,
                unlocked = streakDays >= 90
            ),
            Badge(
                id = "grace_warrior",
                titleEn = "Mindful Reversal",
                titleAr = "محارب اليقظة",
                descriptionEn = "Defeated urge in the 120s Grace Period and stepped back.",
                descriptionAr = "الانتصار على الرغبة والتراجع الواعي خلال فترة السماح 120 ثانية.",
                iconEmoji = "⏳",
                costumeType = CostumeIconType.TIMER,
                isSpecial = true,
                unlocked = graceSaves >= 1
            ),
            Badge(
                id = "zen_breather",
                titleEn = "Zen Anchor",
                titleAr = "مرساة السكينة",
                descriptionEn = "Successfully grounded mind through SOS breathing session.",
                descriptionAr = "تهدئة الجهاز العصبي والرسو في السكينة عبر تمرين SOS.",
                iconEmoji = "🌊",
                costumeType = CostumeIconType.WAVE,
                isSpecial = true,
                unlocked = sosCompleted >= 1
            )
        )
    }
}
