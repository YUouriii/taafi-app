package com.aistudio.breakfree.recovr.monitoring

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.aistudio.breakfree.recovr.model.GracePeriodState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Owns the 120-second step-back window.
 *
 * The countdown is derived from a wall-clock timestamp instead of accumulated ticks, so
 * it cannot drift, and the whole window is persisted: if the process is killed while a
 * countdown is running, [init] restores the remaining time (or the resolution that was
 * reached while the app was dead).
 *
 * The manager never records a relapse by itself. When the window expires it emits
 * [GraceResolutionEvent.StreakResetDueToTimeout] and the UI asks the user what happened.
 */
object MonitoringManager {

    private const val TAG = "MonitoringManager"
    private const val TICK_MS = 250L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var countdownJob: Job? = null

    private val _gracePeriodState = MutableStateFlow(GracePeriodState())
    val gracePeriodState: StateFlow<GracePeriodState> = _gracePeriodState.asStateFlow()

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    sealed class GraceResolutionEvent {
        object StreakPreserved : GraceResolutionEvent()
        object StreakResetDueToTimeout : GraceResolutionEvent()
    }

    private val _resolutionEvents = MutableSharedFlow<GraceResolutionEvent>(extraBufferCapacity = 4)
    val resolutionEvents: SharedFlow<GraceResolutionEvent> = _resolutionEvents.asSharedFlow()

    private var prefs: SharedPreferences? = null

    /**
     * Idempotent. Called by the ViewModel and by the accessibility service, whichever
     * starts first after a process restart.
     */
    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext
            .getSharedPreferences(MonitoringPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        restore()
    }

    fun setServiceConnected(connected: Boolean) {
        _isServiceConnected.value = connected
    }

    /**
     * Starts the step-back window. [totalSeconds] of 0 means "no buffer configured":
     * the window expires immediately and the user is asked what happened.
     */
    fun triggerGracePeriod(detectedTarget: String, totalSeconds: Int = MonitoringPreferences.GRACE_BUFFER_SECONDS) {
        if (_gracePeriodState.value.isGraceActive) return

        countdownJob?.cancel()

        if (totalSeconds <= 0) {
            _gracePeriodState.value = GracePeriodState(
                isGraceActive = false,
                remainingSeconds = 0,
                totalSeconds = 0,
                detectedTarget = detectedTarget,
                triggerTimestamp = System.currentTimeMillis(),
                isExpiredAndRelapsed = true
            )
            persist(active = false, pending = PENDING_EXPIRED)
            emit(GraceResolutionEvent.StreakResetDueToTimeout)
            return
        }

        val now = System.currentTimeMillis()
        _gracePeriodState.value = GracePeriodState(
            isGraceActive = true,
            remainingSeconds = totalSeconds,
            totalSeconds = totalSeconds,
            detectedTarget = detectedTarget,
            triggerTimestamp = now,
            isResolvedBySelfControl = false,
            isExpiredAndRelapsed = false
        )
        persist(active = true, pending = null)
        startCountdown()
    }

    /**
     * User closed the site / tapped "Step back" / switched to another app.
     * Self-control reversal: the streak stays intact.
     */
    fun stepBackAndResolve() {
        if (!_gracePeriodState.value.isGraceActive) return
        countdownJob?.cancel()
        _gracePeriodState.update {
            it.copy(
                isGraceActive = false,
                isResolvedBySelfControl = true,
                isExpiredAndRelapsed = false,
                remainingSeconds = it.remainingSeconds.coerceAtLeast(0)
            )
        }
        persist(active = false, pending = PENDING_PRESERVED)
        emit(GraceResolutionEvent.StreakPreserved)
    }

    /** The window ran out without the user stepping back. */
    private fun expireGracePeriod() {
        countdownJob?.cancel()
        _gracePeriodState.update {
            it.copy(
                isGraceActive = false,
                isResolvedBySelfControl = false,
                isExpiredAndRelapsed = true,
                remainingSeconds = 0
            )
        }
        persist(active = false, pending = PENDING_EXPIRED)
        emit(GraceResolutionEvent.StreakResetDueToTimeout)
    }

    fun dismissFeedback() {
        _gracePeriodState.update {
            it.copy(isResolvedBySelfControl = false, isExpiredAndRelapsed = false)
        }
        persist(active = false, pending = null)
    }

    /**
     * Returns (and clears) a resolution that happened while nobody was collecting, e.g.
     * the window expired after the process was killed. Call after subscribing to
     * [resolutionEvents] so no event is handled twice.
     */
    fun consumePendingResolution(): GraceResolutionEvent? {
        val stored = prefs?.getString(KEY_PENDING_RESOLUTION, null) ?: return null
        prefs?.edit()?.remove(KEY_PENDING_RESOLUTION)?.apply()
        return when (stored) {
            PENDING_PRESERVED -> GraceResolutionEvent.StreakPreserved
            PENDING_EXPIRED -> GraceResolutionEvent.StreakResetDueToTimeout
            else -> null
        }
    }

    fun clearPendingResolution() {
        prefs?.edit()?.remove(KEY_PENDING_RESOLUTION)?.apply()
    }

    private fun startCountdown() {
        countdownJob = scope.launch {
            while (isActive) {
                val state = _gracePeriodState.value
                if (!state.isGraceActive) break

                val elapsed = (System.currentTimeMillis() - state.triggerTimestamp) / 1000L
                val remaining = (state.totalSeconds - elapsed).toInt()
                if (remaining <= 0) {
                    expireGracePeriod()
                    break
                }
                if (remaining != state.remainingSeconds) {
                    _gracePeriodState.update { it.copy(remainingSeconds = remaining) }
                }
                delay(TICK_MS)
            }
        }
    }

    private fun restore() {
        val store = prefs ?: return
        val active = store.getBoolean(KEY_ACTIVE, false)
        if (!active) {
            // A resolution may still be waiting to be shown.
            return
        }

        val total = store.getInt(KEY_TOTAL, MonitoringPreferences.GRACE_BUFFER_SECONDS)
        val triggerTs = store.getLong(KEY_TRIGGER_TS, 0L)
        val target = store.getString(KEY_TARGET, "").orEmpty()
        val elapsed = ((System.currentTimeMillis() - triggerTs) / 1000L).toInt()
        val remaining = (total - elapsed).coerceAtLeast(0)

        if (remaining <= 0) {
            Log.i(TAG, "Restored an expired step-back window")
            _gracePeriodState.value = GracePeriodState(
                isGraceActive = false,
                remainingSeconds = 0,
                totalSeconds = total,
                detectedTarget = target,
                triggerTimestamp = triggerTs,
                isExpiredAndRelapsed = true
            )
            persist(active = false, pending = PENDING_EXPIRED)
            return
        }

        Log.i(TAG, "Restored step-back window with ${remaining}s left")
        _gracePeriodState.value = GracePeriodState(
            isGraceActive = true,
            remainingSeconds = remaining,
            totalSeconds = total,
            detectedTarget = target,
            triggerTimestamp = triggerTs
        )
        startCountdown()
    }

    private fun persist(active: Boolean, pending: String?) {
        val store = prefs ?: return
        val state = _gracePeriodState.value
        store.edit().apply {
            putBoolean(KEY_ACTIVE, active)
            putInt(KEY_TOTAL, state.totalSeconds)
            putLong(KEY_TRIGGER_TS, state.triggerTimestamp)
            putString(KEY_TARGET, state.detectedTarget)
            if (pending == null) remove(KEY_PENDING_RESOLUTION) else putString(KEY_PENDING_RESOLUTION, pending)
        }.apply()
    }

    private fun emit(event: GraceResolutionEvent) {
        // extraBufferCapacity keeps this non-suspending; the event is also persisted, so
        // a missing collector cannot lose it.
        _resolutionEvents.tryEmit(event)
    }

    private const val KEY_ACTIVE = "grace_active"
    private const val KEY_TOTAL = "grace_total_seconds"
    private const val KEY_TRIGGER_TS = "grace_trigger_ts"
    private const val KEY_TARGET = "grace_target"
    private const val KEY_PENDING_RESOLUTION = "grace_pending_resolution"
    private const val PENDING_PRESERVED = "preserved"
    private const val PENDING_EXPIRED = "expired"
}
