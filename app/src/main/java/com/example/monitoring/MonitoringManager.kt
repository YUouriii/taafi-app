package com.example.monitoring

import com.example.model.GracePeriodState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object MonitoringManager {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var countdownJob: Job? = null

    private val _gracePeriodState = MutableStateFlow(GracePeriodState())
    val gracePeriodState: StateFlow<GracePeriodState> = _gracePeriodState.asStateFlow()

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    // Events emitted when grace period concludes
    sealed class GraceResolutionEvent {
        object StreakPreserved : GraceResolutionEvent()
        object StreakResetDueToTimeout : GraceResolutionEvent()
    }

    private val _resolutionEvents = MutableSharedFlow<GraceResolutionEvent>()
    val resolutionEvents: SharedFlow<GraceResolutionEvent> = _resolutionEvents.asSharedFlow()

    fun setServiceConnected(connected: Boolean) {
        _isServiceConnected.value = connected
    }

    /**
     * Called when explicit content is detected in browser or when simulated by user.
     * Starts the 120-second mindfulness grace period countdown.
     */
    fun triggerGracePeriod(detectedTarget: String) {
        // If already in grace period for the same target, do not reset countdown
        if (_gracePeriodState.value.isGraceActive) {
            return
        }

        countdownJob?.cancel()

        _gracePeriodState.value = GracePeriodState(
            isGraceActive = true,
            remainingSeconds = 120,
            totalSeconds = 120,
            detectedTarget = detectedTarget,
            triggerTimestamp = System.currentTimeMillis(),
            isResolvedBySelfControl = false,
            isExpiredAndRelapsed = false
        )

        countdownJob = scope.launch {
            while (_gracePeriodState.value.isGraceActive && _gracePeriodState.value.remainingSeconds > 0) {
                delay(1000L)
                val newSeconds = _gracePeriodState.value.remainingSeconds - 1
                if (newSeconds <= 0) {
                    // 120 full seconds elapsed without stepping back -> Streak reset
                    handleTimeoutRelapse()
                    break
                } else {
                    _gracePeriodState.update { it.copy(remainingSeconds = newSeconds) }
                }
            }
        }
    }

    /**
     * User closed the site or tapped "I stepped back / Safe now".
     * Acknowledges self-control reversal: streak remains intact!
     */
    fun stepBackAndResolve() {
        if (!_gracePeriodState.value.isGraceActive) return

        countdownJob?.cancel()
        _gracePeriodState.update {
            it.copy(
                isGraceActive = false,
                isResolvedBySelfControl = true,
                isExpiredAndRelapsed = false
            )
        }
        scope.launch {
            _resolutionEvents.emit(GraceResolutionEvent.StreakPreserved)
        }
    }

    /**
     * 120 seconds expired -> Relapse recorded.
     */
    private fun handleTimeoutRelapse() {
        countdownJob?.cancel()
        _gracePeriodState.update {
            it.copy(
                isGraceActive = false,
                isResolvedBySelfControl = false,
                isExpiredAndRelapsed = true,
                remainingSeconds = 0
            )
        }
        scope.launch {
            _resolutionEvents.emit(GraceResolutionEvent.StreakResetDueToTimeout)
        }
    }

    fun dismissFeedback() {
        _gracePeriodState.update {
            it.copy(
                isResolvedBySelfControl = false,
                isExpiredAndRelapsed = false
            )
        }
    }
}
