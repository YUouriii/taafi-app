package com.aistudio.breakfree.recovr.model

/**
 * The slow-moving projection of [StreakState] that screens actually display.
 *
 * [StreakState.currentTimestamp] ticks every second; anything derived from it changes
 * every second too. Screens that only show days, counters and badges collect this
 * instead, so the per-second tick does not recompose them.
 */
data class StreakSummary(
    val days: Long,
    val longestStreakSeconds: Long,
    val gracePeriodSavesCount: Int,
    val sosCompletedCount: Int,
    val relapsesCount: Int,
    val recoveryPhase: RecoveryPhase,
    val nextMilestoneDays: Int,
    val cleanSinceTimestamp: Long
) {
    companion object {
        fun from(state: StreakState): StreakSummary = StreakSummary(
            days = state.days,
            longestStreakSeconds = state.longestStreakSeconds,
            gracePeriodSavesCount = state.gracePeriodSavesCount,
            sosCompletedCount = state.sosCompletedCount,
            relapsesCount = state.relapsesCount,
            recoveryPhase = state.recoveryPhase,
            nextMilestoneDays = state.nextMilestoneDays,
            cleanSinceTimestamp = state.cleanSinceTimestamp
        )
    }
}
