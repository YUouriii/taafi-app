package com.example.model

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
        get() = (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)

    val formattedTime: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }
}
