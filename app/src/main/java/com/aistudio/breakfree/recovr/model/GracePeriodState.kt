package com.aistudio.breakfree.recovr.model

import java.util.Locale

data class GracePeriodState(
    val isGraceActive: Boolean = false,
    val remainingSeconds: Int = 120,
    val totalSeconds: Int = 120,
    val detectedTarget: String = "",
    val triggerTimestamp: Long = 0L,
    val isResolvedBySelfControl: Boolean = false,
    val isExpiredAndRelapsed: Boolean = false
) {
    val progress: Float
        get() = if (totalSeconds <= 0) 0f
        else (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)

    val formattedTime: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            // Western digits in every language, so the countdown matches the rest of the
            // UI instead of switching to Arabic-Indic numerals on an ar-* device.
            return String.format(Locale.US, "%02d:%02d", mins, secs)
        }
}
