package com.example.model

data class StreakState(
    val cleanSinceTimestamp: Long = System.currentTimeMillis() - (5L * 24 * 3600 * 1000 + 4 * 3600 * 1000 + 32 * 60 * 1000), // Default starting streak for realistic feel
    val currentTimestamp: Long = System.currentTimeMillis(),
    val longestStreakSeconds: Long = 14L * 24 * 3600,
    val relapsesCount: Int = 0,
    val gracePeriodSavesCount: Int = 3,
    val sosCompletedCount: Int = 2
) {
    val totalSecondsClean: Long
        get() = maxOf(0L, (currentTimestamp - cleanSinceTimestamp) / 1000L)

    val days: Long
        get() = totalSecondsClean / 86400L

    val hours: Long
        get() = (totalSecondsClean % 86400L) / 3600L

    val minutes: Long
        get() = (totalSecondsClean % 3600L) / 60L

    val seconds: Long
        get() = totalSecondsClean % 60L

    /**
     * Dopamine Recovery Score (0-100%):
     * Based on neuroplasticity recovery curve:
     * - Days 0-7: 0 - 25% (Acute detoxification & receptor upregulation beginning)
     * - Days 8-14: 26 - 50% (Dopamine sensitivity recovery & baseline restabilization)
     * - Days 15-30: 51 - 75% (Neural rewiring & urge peak flattening)
     * - Days 31-90: 76 - 95% (Prefrontal cortex executive control restoration)
     * - Days 90+: 96 - 100% (Substantial neural liberation & mastery)
     * Plus bonus points for grace period mindful reversals (up to +5%)
     */
    val dopamineRecoveryScore: Int
        get() {
            val daysFloat = totalSecondsClean.toFloat() / 86400f
            val baseScore = when {
                daysFloat <= 0f -> 0
                daysFloat < 7f -> (daysFloat / 7f * 25f).toInt()
                daysFloat < 14f -> (25f + (daysFloat - 7f) / 7f * 25f).toInt()
                daysFloat < 30f -> (50f + (daysFloat - 14f) / 16f * 25f).toInt()
                daysFloat < 90f -> (75f + (daysFloat - 30f) / 60f * 20f).toInt()
                else -> 95 + minOf(5, ((daysFloat - 90f) / 30f * 5f).toInt())
            }
            val bonusFromSaves = minOf(5, gracePeriodSavesCount)
            return minOf(100, baseScore + bonusFromSaves)
        }

    val recoveryPhase: RecoveryPhase
        get() = when {
            days < 7 -> RecoveryPhase.ACUTE_DETOX
            days < 14 -> RecoveryPhase.RECEPTOR_AWAKENING
            days < 30 -> RecoveryPhase.NEURAL_REWIRING
            days < 90 -> RecoveryPhase.EXECUTIVE_CONTROL
            else -> RecoveryPhase.TRUE_MASTERY
        }

    val nextMilestoneDays: Int
        get() = when {
            days < 1 -> 1
            days < 3 -> 3
            days < 7 -> 7
            days < 14 -> 14
            days < 30 -> 30
            days < 60 -> 60
            days < 90 -> 90
            days < 180 -> 180
            else -> 365
        }

    val milestoneProgress: Float
        get() {
            val prevMilestone = when {
                days < 1 -> 0f
                days < 3 -> 1f
                days < 7 -> 3f
                days < 14 -> 7f
                days < 30 -> 14f
                days < 60 -> 30f
                days < 90 -> 60f
                days < 180 -> 90f
                else -> 180f
            }
            val target = nextMilestoneDays.toFloat()
            val current = days.toFloat() + (hours.toFloat() / 24f)
            val span = target - prevMilestone
            return if (span <= 0f) 1f else ((current - prevMilestone) / span).coerceIn(0f, 1f)
        }
}

enum class RecoveryPhase(
    val titleEn: String,
    val titleAr: String,
    val descEn: String,
    val descAr: String,
    val icon: String
) {
    ACUTE_DETOX(
        "Acute Detox",
        "إزالة السموم الحادة",
        "Clearing dopamine overload; initial brain receptor stabilization.",
        "تطهير فرط الدوبامين واستقرار مستقبلات الدماغ الأولى.",
        "⚡"
    ),
    RECEPTOR_AWAKENING(
        "Receptor Awakening",
        "استيقاظ المستقبلات",
        "Dopamine D2 receptors multiplying; natural joys start returning.",
        "تضاعف مستقبلات الدوبامين D2 وبداية عودة المتعة للأشياء الطبيعية.",
        "🌱"
    ),
    NEURAL_REWIRING(
        "Neural Rewiring",
        "إعادة التشكيل العصبي",
        "DeltaFosB buildup dissipating; automatic trigger habits weakening.",
        "تلاشي بروتين دلتا-فوس-بي وضعف روابط العادات التلقائية السابقة.",
        "🧠"
    ),
    EXECUTIVE_CONTROL(
        "Executive Control",
        "تعزيز التحكم التنفيذي",
        "Prefrontal cortex grey matter thickening; high resilience.",
        "تكثيف المادة الرمادية في القشرة الجبهية ومقاومة عالية للإغراءات.",
        "🛡️"
    ),
    TRUE_MASTERY(
        "True Freedom & Mastery",
        "الحرية والسيادة الكاملة",
        "Complete baseline transformation; peaceful, empowered mental clarity.",
        "تحول كامل لخط الأساس الذهني؛ وضوح عقلي وسكينة وقوة داخلية.",
        "👑"
    )
}
