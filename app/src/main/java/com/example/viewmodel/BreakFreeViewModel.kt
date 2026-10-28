package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiRecoveryCounselor
import com.example.ai.OpenCodeCounselorService
import com.example.data.BreakFreeRepository
import com.example.data.FirestoreRecoveryRepository
import com.example.localization.Translations
import com.example.model.AppLanguage
import com.example.model.ChatMessage
import com.example.model.CommunityCategory
import com.example.model.CommunityPost
import com.example.model.DailyCheckIn
import com.example.model.GracePeriodState
import com.example.model.StreakState
import com.example.model.UserProfile
import com.example.monitoring.MonitoringManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BreakFreeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BreakFreeRepository(application)
    private val firestoreRepo = FirestoreRecoveryRepository(application)

    // Language state
    private val _language = MutableStateFlow(
        if (repository.getLanguageCode() == "ar") AppLanguage.ARABIC else AppLanguage.ENGLISH
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Real-time streak engine
    private val _streakState = MutableStateFlow(repository.getStreakState())
    val streakState: StateFlow<StreakState> = _streakState.asStateFlow()

    // Grace period state
    val gracePeriodState: StateFlow<GracePeriodState> = MonitoringManager.gracePeriodState

    // Authentication state
    private val _currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // Daily Mood & Urge Check-Ins
    private val _dailyCheckIns = MutableStateFlow<List<DailyCheckIn>>(emptyList())
    val dailyCheckIns: StateFlow<List<DailyCheckIn>> = _dailyCheckIns.asStateFlow()

    private val _isCheckInDialogOpen = MutableStateFlow(false)
    val isCheckInDialogOpen: StateFlow<Boolean> = _isCheckInDialogOpen.asStateFlow()

    // SOS Modal state
    private val _isSosModalVisible = MutableStateFlow(false)
    val isSosModalVisible: StateFlow<Boolean> = _isSosModalVisible.asStateFlow()

    // Dialog states
    private val _showVictoryDialog = MutableStateFlow(false)
    val showVictoryDialog: StateFlow<Boolean> = _showVictoryDialog.asStateFlow()

    private val _showRelapseDialog = MutableStateFlow(false)
    val showRelapseDialog: StateFlow<Boolean> = _showRelapseDialog.asStateFlow()

    private val _showSetDateDialog = MutableStateFlow(false)
    val showSetDateDialog: StateFlow<Boolean> = _showSetDateDialog.asStateFlow()

    private val _showResetConfirmationDialog = MutableStateFlow(false)
    val showResetConfirmationDialog: StateFlow<Boolean> = _showResetConfirmationDialog.asStateFlow()

    // Anonymous Community
    private val _communityPosts = MutableStateFlow(repository.getCommunityPosts())
    val communityPosts: StateFlow<List<CommunityPost>> = _communityPosts.asStateFlow()

    private val _selectedCategory = MutableStateFlow(CommunityCategory.ALL)
    val selectedCategory: StateFlow<CommunityCategory> = _selectedCategory.asStateFlow()

    private val _showNewPostDialog = MutableStateFlow(false)
    val showNewPostDialog: StateFlow<Boolean> = _showNewPostDialog.asStateFlow()

    // AI Companion Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Gemini AI Counselor with dynamic Daily Check-in context
    private val geminiCounselor = GeminiRecoveryCounselor(
        getRecentCheckIns = { _dailyCheckIns.value }
    )

    private var liveTimerJob: Job? = null
    private var checkInsObservationJob: Job? = null

    init {
        startLiveStreakEngine()
        observeGracePeriodResolutions()
        initInitialChatMessages()
        initAuthListener()
        seedInitialSampleCheckInsIfEmpty()
    }

    private fun initAuthListener() {
        Firebase.auth.addAuthStateListener { auth ->
            val user = auth.currentUser
            _currentUser.value = user
            if (user != null) {
                observeFirestoreCheckIns()
                syncProfileToCloud()
            }
        }
    }

    private fun seedInitialSampleCheckInsIfEmpty() {
        if (_dailyCheckIns.value.isEmpty()) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            _dailyCheckIns.value = listOf(
                DailyCheckIn(
                    id = "sample-1",
                    dateString = today,
                    moodScore = 4,
                    urgeIntensity = 2,
                    primaryEmotions = listOf("Peaceful", "Motivated"),
                    triggersEncountered = listOf("Work Stress"),
                    reflectionNotes = "Had a busy afternoon, stayed grounded with 4-7-8 breathing.",
                    streakProtected = true
                )
            )
        }
    }

    private fun observeFirestoreCheckIns() {
        checkInsObservationJob?.cancel()
        checkInsObservationJob = viewModelScope.launch {
            try {
                firestoreRepo.observeCheckIns()
                    .catch { e ->
                        Log.w("BreakFreeVM", "Check-ins observation error: ${e.message}")
                    }
                    .collect { list ->
                        if (list.isNotEmpty()) {
                            _dailyCheckIns.value = list
                        }
                    }
            } catch (e: Exception) {
                Log.w("BreakFreeVM", "Failed to start observing check-ins: ${e.message}")
            }
        }
    }

    private fun syncProfileToCloud() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val profile = UserProfile(
                    userId = user.uid,
                    displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Warrior",
                    cleanSinceTimestamp = _streakState.value.cleanSinceTimestamp,
                    longestStreakSeconds = _streakState.value.longestStreakSeconds,
                    gracePeriodSavesCount = _streakState.value.gracePeriodSavesCount,
                    sosCompletedCount = _streakState.value.sosCompletedCount,
                    relapsesCount = _streakState.value.relapsesCount
                )
                firestoreRepo.syncUserProfile(profile)
            } catch (e: Exception) {
                Log.w("BreakFreeVM", "Profile sync failed: ${e.message}")
            }
        }
    }

    private fun startLiveStreakEngine() {
        liveTimerJob?.cancel()
        liveTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                _streakState.update { it.copy(currentTimestamp = System.currentTimeMillis()) }
            }
        }
    }

    private fun observeGracePeriodResolutions() {
        viewModelScope.launch {
            MonitoringManager.resolutionEvents.collect { event ->
                when (event) {
                    is MonitoringManager.GraceResolutionEvent.StreakPreserved -> {
                        val newSaves = repository.recordGracePeriodSave()
                        _streakState.update { it.copy(gracePeriodSavesCount = newSaves) }
                        _showVictoryDialog.value = true
                        syncProfileToCloud()
                        triggerHaptic(longVibe = false)
                    }
                    is MonitoringManager.GraceResolutionEvent.StreakResetDueToTimeout -> {
                        repository.resetStreakDueToRelapse()
                        _streakState.value = repository.getStreakState()
                        _showRelapseDialog.value = true
                        syncProfileToCloud()
                        triggerHaptic(longVibe = true)
                    }
                }
            }
        }
    }

    fun toggleLanguage() {
        val next = if (_language.value == AppLanguage.ENGLISH) AppLanguage.ARABIC else AppLanguage.ENGLISH
        _language.value = next
        repository.saveLanguageCode(next.code)
    }

    // Daily Mood & Urge Check-in methods
    fun openCheckInDialog(open: Boolean) {
        _isCheckInDialogOpen.value = open
    }

    fun saveDailyCheckIn(checkIn: DailyCheckIn) {
        val user = _currentUser.value
        val enriched = if (user != null) checkIn.copy(userId = user.uid) else checkIn

        // Update local memory list immediately
        val currentList = _dailyCheckIns.value.toMutableList()
        val index = currentList.indexOfFirst { it.dateString == enriched.dateString }
        if (index >= 0) {
            currentList[index] = enriched
        } else {
            currentList.add(0, enriched)
        }
        _dailyCheckIns.value = currentList
        _isCheckInDialogOpen.value = false
        triggerHaptic(longVibe = false)

        // Save to Firestore if authenticated
        if (user != null) {
            viewModelScope.launch {
                try {
                    firestoreRepo.saveCheckIn(enriched)
                } catch (e: Exception) {
                    Log.w("BreakFreeVM", "Firestore check-in save error: ${e.message}")
                }
            }
        }

        // Add an immediate welcoming AI insight message in the chat companion
        val isAr = _language.value == AppLanguage.ARABIC
        val insightNote = if (isAr) {
            "📊 شكراً لتسجيل مراجعتك لليوم (${enriched.dateString})! رصدت تقييمك المزاجي (${enriched.moodScore}/5) وشدة الرغبة (${enriched.urgeIntensity}/10). سأستخدم هذه الأنماط لتخصيص استراتيجيات الصمود الأنسب لك."
        } else {
            "📊 Daily reflection logged for ${enriched.dateString}! Mood: ${enriched.moodScore}/5, Peak Urge: ${enriched.urgeIntensity}/10. I have updated my counseling model to incorporate these insights."
        }
        _chatMessages.update {
            it + ChatMessage(id = UUID.randomUUID().toString(), isFromUser = false, text = insightNote)
        }
    }

    fun getTodayCheckIn(): DailyCheckIn? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return _dailyCheckIns.value.firstOrNull { it.dateString == today }
    }

    fun onAuthSuccess() {
        _currentUser.value = Firebase.auth.currentUser
        observeFirestoreCheckIns()
        syncProfileToCloud()
        triggerHaptic(longVibe = false)
    }

    fun onSignOut() {
        _currentUser.value = null
        checkInsObservationJob?.cancel()
    }

    fun openSosModal() {
        _isSosModalVisible.value = true
        triggerHaptic(longVibe = false)
    }

    fun closeSosModal() {
        _isSosModalVisible.value = false
    }

    fun completeSosSession() {
        val newCount = repository.recordSosCompleted()
        _streakState.update { it.copy(sosCompletedCount = newCount) }
        _isSosModalVisible.value = false
        syncProfileToCloud()
        triggerHaptic(longVibe = false)
    }

    fun simulateGracePeriod() {
        MonitoringManager.triggerGracePeriod("adult-preview-example.com/explicit")
    }

    fun stepBackInGracePeriod() {
        MonitoringManager.stepBackAndResolve()
    }

    fun dismissVictoryDialog() {
        _showVictoryDialog.value = false
        MonitoringManager.dismissFeedback()
    }

    fun dismissRelapseDialog() {
        _showRelapseDialog.value = false
        MonitoringManager.dismissFeedback()
    }

    fun manualResetStreak() {
        repository.resetStreakDueToRelapse()
        _streakState.value = repository.getStreakState()
        _showResetConfirmationDialog.value = false
        _showRelapseDialog.value = true
        syncProfileToCloud()
    }

    fun showResetDialog(show: Boolean) {
        _showResetConfirmationDialog.value = show
    }

    fun showSetDateDialog(show: Boolean) {
        _showSetDateDialog.value = show
    }

    fun setCustomCleanDays(days: Int) {
        val newTimestamp = System.currentTimeMillis() - (days.toLong() * 24 * 3600 * 1000)
        repository.saveCleanSinceTimestamp(newTimestamp)
        _streakState.update {
            it.copy(
                cleanSinceTimestamp = newTimestamp,
                currentTimestamp = System.currentTimeMillis()
            )
        }
        _showSetDateDialog.value = false
        syncProfileToCloud()
    }

    // Community methods
    fun setCategory(category: CommunityCategory) {
        _selectedCategory.value = category
    }

    fun toggleFistBump(postId: String) {
        _communityPosts.value = repository.toggleFistBump(postId)
        triggerHaptic(longVibe = false)
    }

    fun openNewPostDialog(show: Boolean) {
        _showNewPostDialog.value = show
    }

    fun createPost(alias: String, content: String, category: CommunityCategory) {
        if (content.isBlank()) return
        val effectiveAlias = if (alias.isBlank()) {
            if (_language.value == AppLanguage.ARABIC) "صامد_مجهول #${(100..999).random()}" else "SoberWarrior #${(100..999).random()}"
        } else {
            alias
        }
        val newPost = CommunityPost(
            id = UUID.randomUUID().toString(),
            authorAlias = effectiveAlias,
            daysClean = _streakState.value.days.toInt(),
            category = category,
            contentEn = content,
            contentAr = content,
            timestampMillis = System.currentTimeMillis(),
            fistBumps = 1,
            userCheered = true
        )
        _communityPosts.value = repository.addCommunityPost(newPost)
        _showNewPostDialog.value = false
    }

    // AI Companion Chat with Gemini AI Logic
    private fun initInitialChatMessages() {
        val isAr = _language.value == AppLanguage.ARABIC
        val welcome = if (isAr) {
            "أهلاً بك يا بطل في مرشد الصمود الذكي (BreakFree AI مدعوماً بـ Gemini). أنا هنا لدعمك على مدار الساعة، ومربوط بسجل مشاعرك ورغباتك اليومية لتقديم إرشاد دقيق مبني على علوم الأعصاب والسلوك المعرفي."
        } else {
            "Welcome to the BreakFree Resilience Companion powered by Gemini AI. I am attuned to your daily mood logs and urge intensity to provide personalized CBT urge surfing guidance."
        }
        _chatMessages.value = listOf(
            ChatMessage(
                id = "welcome",
                isFromUser = false,
                text = welcome
            )
        )
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank() || _isAiLoading.value) return

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromUser = true,
            text = text.trim()
        )
        _chatMessages.update { it + userMsg }
        _isAiLoading.value = true

        viewModelScope.launch {
            val responseText = geminiCounselor.sendChatMessage(
                userMessage = text,
                cleanDays = _streakState.value.days,
                language = _language.value
            )
            val aiMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                isFromUser = false,
                text = responseText
            )
            _chatMessages.update { it + aiMsg }
            _isAiLoading.value = false
            triggerHaptic(longVibe = false)
        }
    }

    fun triggerHaptic(longVibe: Boolean) {
        try {
            val context = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                val duration = if (longVibe) 400L else 70L
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
            }
        } catch (e: Exception) {
            // Ignore if vibrator permission or hardware is unavailable
        }
    }
}
