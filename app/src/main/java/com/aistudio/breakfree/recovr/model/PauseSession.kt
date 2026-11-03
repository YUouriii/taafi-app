package com.aistudio.breakfree.recovr.model

/** How a 10-minute pause ended. */
enum class PauseOutcome {
    /** The user rode the wave out and said "I'm okay". */
    SURVIVED,

    /** The gate ended without a claim either way (closed, or the app was killed). */
    ENDED,

    /** The app never came back before the window closed. */
    ABANDONED
}

/**
 * One completed 10-minute pause. Stored locally only: this is the user's private
 * history of the moments they held on, and it feeds the "urges survived" counter.
 */
data class PauseSession(
    val id: String,
    val startedAtMs: Long,
    val durationMs: Long,
    val outcome: PauseOutcome
)

/** Everything the full-screen pause gate needs to draw itself. */
data class PauseGateUiState(
    val isVisible: Boolean = false,
    val startedAtMs: Long = 0L,
    val durationMs: Long = PAUSE_DURATION_MS,
    val remainingMs: Long = PAUSE_DURATION_MS,
    /** Three short notifications, written by the model when it is available. */
    val messages: List<String> = emptyList(),
    /** True while the model is still writing personalised messages. */
    val isPreparing: Boolean = false
) {
    companion object {
        /** The length of the gate. Ten minutes: long enough for an urge to peak and fall. */
        const val PAUSE_DURATION_MS = 10L * 60L * 1000L
    }
}
