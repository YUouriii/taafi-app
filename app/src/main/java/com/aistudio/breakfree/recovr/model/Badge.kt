package com.aistudio.breakfree.recovr.model

import com.aistudio.breakfree.recovr.ui.components.CostumeIconType

data class Badge(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val titleFr: String = titleEn,
    val descriptionEn: String,
    val descriptionAr: String,
    val descriptionFr: String = descriptionEn,
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
                titleFr = "Première étincelle",
                descriptionEn = "Completed 24 clean hours. The journey of freedom begins.",
                descriptionAr = "إتمام 24 ساعة نقية. رحلة الحرية قد بدأت.",
                descriptionFr = "24 heures propres accomplies. Le voyage vers la liberté commence.",
                costumeType = CostumeIconType.FLAME,
                requiredDays = 1,
                unlocked = streakDays >= 1
            ),
            Badge(
                id = "clarity",
                titleEn = "Clarity Dawning",
                titleAr = "بزوغ الوضوح",
                titleFr = "Aube de la clarté",
                descriptionEn = "3 days clean. Brain fog begins to lift.",
                descriptionAr = "3 أيام من النقاء. ضباب الدماغ يبدأ في التلاشي.",
                descriptionFr = "3 jours propres. Le brouillard mental commence à se dissiper.",
                costumeType = CostumeIconType.SUNRISE,
                requiredDays = 3,
                unlocked = streakDays >= 3
            ),
            Badge(
                id = "fortress",
                titleEn = "One Week Fortress",
                titleAr = "حصن الأسبوع",
                titleFr = "Forteresse d'une semaine",
                descriptionEn = "7 days clean! Peak acute withdrawal conquered.",
                descriptionAr = "7 أيام نقية! تجاوز ذروة الانسحاب الحاد بنجاح.",
                descriptionFr = "7 jours propres ! Le pic de sevrage aigu est surmonté.",
                costumeType = CostumeIconType.FORTRESS,
                requiredDays = 7,
                unlocked = streakDays >= 7
            ),
            Badge(
                id = "rewired",
                titleEn = "Neural Shift",
                titleAr = "التحول العصبي",
                titleFr = "Bascule neuronale",
                descriptionEn = "14 days clean. Dopamine receptors multiplying.",
                descriptionAr = "14 يوماً من النقاء. مستقبلات الدوبامين تتضاعف وتتعافى.",
                descriptionFr = "14 jours propres. Les récepteurs de dopamine se multiplient.",
                costumeType = CostumeIconType.BRAIN,
                requiredDays = 14,
                unlocked = streakDays >= 14
            ),
            Badge(
                id = "iron_will",
                titleEn = "Iron Will",
                titleAr = "إرادة فولاذية",
                titleFr = "Volonté de fer",
                descriptionEn = "30 days of unyielding discipline and self-mastery.",
                descriptionAr = "30 يوماً من الانضباط الراسخ والتحكم بالذات.",
                descriptionFr = "30 jours de discipline inébranlable et de maîtrise de soi.",
                costumeType = CostumeIconType.SHIELD,
                requiredDays = 30,
                unlocked = streakDays >= 30
            ),
            Badge(
                id = "unshakeable",
                titleEn = "Unshakeable",
                titleAr = "ثبات لا يتزعزع",
                titleFr = "Inébranlable",
                descriptionEn = "60 days of transformation. New identity cemented.",
                descriptionAr = "60 يوماً من التحول الحقيقي. ترسيخ الهوية الجديدة.",
                descriptionFr = "60 jours de transformation. Nouvelle identité cimentée.",
                costumeType = CostumeIconType.LIGHTNING,
                requiredDays = 60,
                unlocked = streakDays >= 60
            ),
            Badge(
                id = "reborn",
                titleEn = "Reborn Free",
                titleAr = "ولادة الحرية",
                titleFr = "Renaissance libre",
                descriptionEn = "90 days milestone! Full neural baseline restoration.",
                descriptionAr = "إنجاز 90 يوماً! استعادة التوازن الكيميائي الدماغي الكامل.",
                descriptionFr = "Jalon de 90 jours ! Restauration complète de la ligne de base neuronale.",
                costumeType = CostumeIconType.CROWN,
                requiredDays = 90,
                unlocked = streakDays >= 90
            ),
            Badge(
                id = "grace_warrior",
                titleEn = "Mindful Reversal",
                titleAr = "محارب اليقظة",
                titleFr = "Guerrier conscient",
                descriptionEn = "Defeated urge in the 120s Grace Period and stepped back.",
                descriptionAr = "الانتصار على الرغبة والتراجع الواعي خلال فترة السماح 120 ثانية.",
                descriptionFr = "Envie vaincue pendant la période de grâce de 120 s et recul effectué.",
                costumeType = CostumeIconType.TIMER,
                isSpecial = true,
                unlocked = graceSaves >= 1
            ),
            Badge(
                id = "zen_breather",
                titleEn = "Zen Anchor",
                titleAr = "مرساة السكينة",
                titleFr = "Ancre zen",
                descriptionEn = "Successfully grounded mind through SOS breathing session.",
                descriptionAr = "تهدئة الجهاز العصبي والرسو في السكينة عبر تمرين SOS.",
                descriptionFr = "Esprit ancré avec succès via une session de respiration SOS.",
                costumeType = CostumeIconType.WAVE,
                isSpecial = true,
                unlocked = sosCompleted >= 1
            )
        )
    }
}
