package com.aistudio.breakfree.recovr.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.breakfree.recovr.ai.CrisisSupport
import com.aistudio.breakfree.recovr.ai.GeminiRecoveryCounselor
import com.aistudio.breakfree.recovr.data.BreakFreeRepository
import com.aistudio.breakfree.recovr.data.FirestoreRecoveryRepository
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.ChatMessage
import com.aistudio.breakfree.recovr.model.CommunityCategory
import com.aistudio.breakfree.recovr.model.CommunityPost
import com.aistudio.breakfree.recovr.model.DailyCheckIn
import com.aistudio.breakfree.recovr.model.GracePeriodState
import com.aistudio.breakfree.recovr.model.StreakState
import com.aistudio.breakfree.recovr.model.StreakSummary
import com.aistudio.breakfree.recovr.model.UserProfile
import com.aistudio.breakfree.recovr.monitoring.DnsShield
import com.aistudio.breakfree.recovr.monitoring.MonitoringManager
import com.aistudio.breakfree.recovr.monitoring.MonitoringPreferences
import com.aistudio.breakfree.recovr.update.AutoUpdater
import com.aistudio.breakfree.recovr.monitoring.PrivateDnsShield
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BreakFreeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BreakFreeRepository(application)
    private val firestoreRepo = FirestoreRecoveryRepository(application)
    private val monitoringPreferences = MonitoringPreferences(application)

    // Language state
    private val _language = MutableStateFlow(
        if (repository.getLanguageCode() == "ar") AppLanguage.ARABIC else AppLanguage.ENGLISH
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Real-time streak engine
    private val _streakState = MutableStateFlow(repository.getStreakState())
    val streakState: StateFlow<StreakState> = _streakState.asStateFlow()

    /**
     * The parts of [streakState] that change at most once a day (or when a counter
     * moves). Screens that only show days/counters/badges should collect this instead of
     * the per-second state, so a ticking clock does not recompose the whole dashboard.
     */
    val streakSummary: StateFlow<StreakSummary> = _streakState
        .map { StreakSummary.from(it) }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, StreakSummary.from(_streakState.value))

    // Grace period state
    val gracePeriodState: StateFlow<GracePeriodState> = MonitoringManager.gracePeriodState

    // Monitoring settings (persisted, shared with the accessibility service)
    private val _strictGuardEnabled = MutableStateFlow(monitoringPreferences.strictGuardEnabled)
    val strictGuardEnabled: StateFlow<Boolean> = _strictGuardEnabled.asStateFlow()

    private val _warningBufferEnabled = MutableStateFlow(monitoringPreferences.warningBufferEnabled)
    val warningBufferEnabled: StateFlow<Boolean> = _warningBufferEnabled.asStateFlow()

    private val _keywordFilterEnabled = MutableStateFlow(monitoringPreferences.keywordFilterEnabled)
    val keywordFilterEnabled: StateFlow<Boolean> = _keywordFilterEnabled.asStateFlow()

    private val _monitoringConsentGranted = MutableStateFlow(monitoringPreferences.monitoringConsentGranted)
    val monitoringConsentGranted: StateFlow<Boolean> = _monitoringConsentGranted.asStateFlow()

    private val _appsMonitoringConsentGranted =
        MutableStateFlow(monitoringPreferences.appsMonitoringConsentGranted)
    val appsMonitoringConsentGranted: StateFlow<Boolean> = _appsMonitoringConsentGranted.asStateFlow()

    private val _aiConsentGranted = MutableStateFlow(monitoringPreferences.aiConsentGranted)
    val aiConsentGranted: StateFlow<Boolean> = _aiConsentGranted.asStateFlow()

    private val _themeMode = MutableStateFlow(monitoringPreferences.themeMode)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // In-app updater (GitHub releases or a self-hosted version.json).
    private val _updateInfo = MutableStateFlow<AutoUpdater.UpdateInfo?>(null)
    val updateInfo: StateFlow<AutoUpdater.UpdateInfo?> = _updateInfo.asStateFlow()

    private val _updateCheckInProgress = MutableStateFlow(false)
    val updateCheckInProgress: StateFlow<Boolean> = _updateCheckInProgress.asStateFlow()

    private val _updateError = MutableStateFlow<String?>(null)
    val updateError: StateFlow<String?> = _updateError.asStateFlow()

    private val _updateStatusMessage = MutableStateFlow<String?>(null)
    val updateStatusMessage: StateFlow<String?> = _updateStatusMessage.asStateFlow()

    private val _updateDownloadProgress = MutableStateFlow<Int?>(null)
    val updateDownloadProgress: StateFlow<Int?> = _updateDownloadProgress.asStateFlow()

    // DNS shield. There are two mechanisms (system Private DNS for a Device Owner, and a
    // DNS-only VPN for everyone else), so the live state is read from the system and
    // re-read on every resume.
    private val _dnsShieldState = MutableStateFlow(DnsShield.state(getApplication()))
    val dnsShieldState: StateFlow<DnsShield.State> = _dnsShieldState.asStateFlow()

    private val _dnsShieldConsentGranted =
        MutableStateFlow(monitoringPreferences.dnsShieldConsentGranted)
    val dnsShieldConsentGranted: StateFlow<Boolean> = _dnsShieldConsentGranted.asStateFlow()

    private val _dnsShieldBusy = MutableStateFlow(false)
    val dnsShieldBusy: StateFlow<Boolean> = _dnsShieldBusy.asStateFlow()

    private val _dnsShieldResult =
        MutableStateFlow<PrivateDnsShield.ApplyResult?>(null)
    val dnsShieldResult: StateFlow<PrivateDnsShield.ApplyResult?> = _dnsShieldResult.asStateFlow()

    private val _firstRunSetupDone = MutableStateFlow(monitoringPreferences.firstRunSetupDone)
    val firstRunSetupDone: StateFlow<Boolean> = _firstRunSetupDone.asStateFlow()

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

    /**
     * Shown when the step-back window expires. Nothing is recorded until the user says
     * what actually happened — a timer alone must never reset someone's streak.
     */
    private val _showRelapseConfirmation = MutableStateFlow(false)
    val showRelapseConfirmation: StateFlow<Boolean> = _showRelapseConfirmation.asStateFlow()

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

    /** Consent dialog shown before the first message is sent to a model. */
    private val _showAiConsentDialog = MutableStateFlow(false)
    val showAiConsentDialog: StateFlow<Boolean> = _showAiConsentDialog.asStateFlow()

    /** The message typed while consent was still pending. */
    private val _pendingChatMessage = MutableStateFlow<String?>(null)
    val pendingChatMessage: StateFlow<String?> = _pendingChatMessage.asStateFlow()

    private val _geminiApiKey = MutableStateFlow<String?>(repository.getGeminiApiKey())
    val geminiApiKey: StateFlow<String?> = _geminiApiKey.asStateFlow()

    // Gemini AI Counselor with dynamic Daily Check-in context and API key support
    private val geminiCounselor = GeminiRecoveryCounselor(
        getRecentCheckIns = { _dailyCheckIns.value },
        getApiKey = { repository.getGeminiApiKey() }
    )

    private var liveTimerJob: Job? = null
    private var checkInsObservationJob: Job? = null

    // Local storage bucket the visible check-ins belong to ("local" or a Firebase uid).
    private var currentOwner: String = BreakFreeRepository.LOCAL_OWNER

    // Server-assigned creation timestamp of the cloud profile, kept so profile
    // updates never rewrite it (the security rules require it to stay stable).
    private var cloudProfileCreatedAt: Timestamp? = null

    // True once this session has successfully read the cloud profile (or confirmed it
    // does not exist). Pushing before that could overwrite a longer streak from another
    // device, so every push is gated on it.
    private var profileRestored = false

    init {
        MonitoringManager.init(application)
        startLiveStreakEngine()
        observeGracePeriodResolutions()
        initInitialChatMessages()
        _dailyCheckIns.value = repository.getCheckIns(currentOwner)
        initAuthListener()
    }

    private fun initAuthListener() {
        Firebase.auth.addAuthStateListener { auth ->
            handleAuthUserChanged(auth.currentUser)
        }
    }

    /**
     * Single entry point for "who is using the app right now". Everything that is
     * account-scoped is swapped here, so data from a previous account can never leak
     * into the next session.
     */
    private fun handleAuthUserChanged(user: FirebaseUser?) {
        val owner = user?.uid ?: BreakFreeRepository.LOCAL_OWNER
        currentOwner = owner
        _currentUser.value = user
        profileRestored = false

        checkInsObservationJob?.cancel()
        checkInsObservationJob = null

        // Show this account's own on-device logs immediately, even while offline.
        _dailyCheckIns.value = repository.getCheckIns(owner)

        if (user == null) {
            cloudProfileCreatedAt = null
            return
        }

        observeFirestoreCheckIns(user.uid)
        viewModelScope.launch {
            // Pull first, push second: a new device adopts the cloud streak instead of
            // overwriting it with local zeros, and only a locally-advanced state is
            // pushed back up.
            val restored = restoreProfileFromCloud(user.uid)
            if (currentOwner == user.uid) {
                profileRestored = restored
                if (restored) pushProfileToCloud(user)
                retryPendingCheckInUploads(user.uid)
            }
        }
    }

    /**
     * Upload check-ins whose Firestore write failed earlier (offline, rules not
     * deployed, …). Each one is independent, so a single failure never blocks the rest.
     */
    private suspend fun retryPendingCheckInUploads(uid: String) {
        val pending = repository.getPendingSyncDates(uid)
        if (pending.isEmpty()) return

        for (dateString in pending) {
            if (currentOwner != uid) return
            val entry = repository.getCheckIns(uid).firstOrNull { it.dateString == dateString } ?: continue
            try {
                val storedCreatedAt = firestoreRepo.saveCheckIn(entry.copy(userId = uid))
                repository.clearPendingSync(uid, dateString)
                if (storedCreatedAt != null) {
                    val updated = upsertCheckIn(
                        repository.getCheckIns(uid),
                        entry.copy(userId = uid, createdAt = storedCreatedAt)
                    )
                    repository.saveCheckIns(uid, updated)
                    if (currentOwner == uid) _dailyCheckIns.value = updated
                }
            } catch (e: Exception) {
                Log.w("BreakFreeVM", "Pending check-in upload failed for $dateString: ${e.message}")
            }
        }
    }

    private fun observeFirestoreCheckIns(uid: String) {
        checkInsObservationJob?.cancel()
        checkInsObservationJob = viewModelScope.launch {
            try {
                firestoreRepo.observeCheckIns()
                    .catch { e ->
                        Log.w("BreakFreeVM", "Check-ins observation error: ${e.message}")
                    }
                    .collect { cloudCheckIns ->
                        // Ignore late emissions if the account changed mid-stream.
                        if (currentOwner != uid) return@collect
                        val merged = mergeCheckIns(
                            cloud = cloudCheckIns,
                            local = repository.getCheckIns(uid),
                            pendingDates = repository.getPendingSyncDates(uid)
                        )
                        _dailyCheckIns.value = merged
                        repository.saveCheckIns(uid, merged)
                    }
            } catch (e: Exception) {
                Log.w("BreakFreeVM", "Failed to start observing check-ins: ${e.message}")
            }
        }
    }

    /**
     * Firestore is authoritative for the dates it knows about; entries written
     * locally while offline are preserved. This is why the list is never simply
     * overwritten with an empty snapshot.
     *
     * Dates that failed to upload are listed in [pendingDates] and always keep their
     * local copy, so a retry cannot be undone by a stale cloud snapshot.
     */
    private fun mergeCheckIns(
        cloud: List<DailyCheckIn>,
        local: List<DailyCheckIn>,
        pendingDates: Set<String>
    ): List<DailyCheckIn> {
        val byDate = LinkedHashMap<String, DailyCheckIn>()
        cloud.forEach { byDate[it.dateString] = it }
        local.forEach { entry ->
            val cloudEntry = byDate[entry.dateString]
            if (cloudEntry == null || entry.dateString in pendingDates) {
                byDate[entry.dateString] = entry
            }
        }
        return byDate.values.sortedByDescending { it.dateString }
    }

    private fun upsertCheckIn(list: List<DailyCheckIn>, checkIn: DailyCheckIn): List<DailyCheckIn> {
        val updated = list.filterNot { it.dateString == checkIn.dateString }.toMutableList()
        updated.add(checkIn)
        return updated.sortedByDescending { it.dateString }
    }

    /**
     * @return true when the local state is at least as advanced as the cloud copy and
     *         is safe to push back up.
     */
    private suspend fun restoreProfileFromCloud(uid: String): Boolean {
        return try {
            val cloudProfile = firestoreRepo.getUserProfile() ?: return true
            if (cloudProfile.userId != uid) return true
            cloudProfileCreatedAt = cloudProfile.createdAt
            val localIsAhead = repository.applyCloudProfile(cloudProfile)
            _streakState.value = repository.getStreakState()
            localIsAhead
        } catch (e: Exception) {
            // Offline or denied: keep everything local and try again on the next
            // explicit sync trigger instead of clobbering the cloud copy.
            Log.w("BreakFreeVM", "Profile restore failed: ${e.message}")
            false
        }
    }

    private suspend fun pushProfileToCloud(user: FirebaseUser) {
        try {
            val profile = UserProfile(
                userId = user.uid,
                displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Warrior",
                cleanSinceTimestamp = _streakState.value.cleanSinceTimestamp,
                longestStreakSeconds = _streakState.value.longestStreakSeconds,
                gracePeriodSavesCount = _streakState.value.gracePeriodSavesCount,
                sosCompletedCount = _streakState.value.sosCompletedCount,
                relapsesCount = _streakState.value.relapsesCount,
                createdAt = cloudProfileCreatedAt
            )
            firestoreRepo.syncUserProfile(profile)
        } catch (e: Exception) {
            Log.w("BreakFreeVM", "Profile sync failed: ${e.message}")
        }
    }

    private fun syncProfileToCloud() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            // If the initial pull failed (offline, rules not deployed, …) retry it before
            // pushing. Blindly writing local state here could erase a longer streak that
            // exists on another device.
            if (!profileRestored) {
                val restored = restoreProfileFromCloud(user.uid)
                if (currentOwner != user.uid) return@launch
                profileRestored = restored
            }
            if (profileRestored && currentOwner == user.uid) {
                pushProfileToCloud(user)
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
            MonitoringManager.resolutionEvents.collect { event -> handleGraceResolution(event) }
        }
        // A resolution reached while the process was dead (e.g. the window expired with
        // the app closed) is persisted; pick it up exactly once.
        MonitoringManager.consumePendingResolution()?.let { handleGraceResolution(it) }
    }

    private fun handleGraceResolution(event: MonitoringManager.GraceResolutionEvent) {
        MonitoringManager.clearPendingResolution()
        when (event) {
            is MonitoringManager.GraceResolutionEvent.StreakPreserved -> {
                val newSaves = repository.recordGracePeriodSave()
                _streakState.update { it.copy(gracePeriodSavesCount = newSaves) }
                _showVictoryDialog.value = true
                syncProfileToCloud()
                triggerHaptic(longVibe = false)
            }
            is MonitoringManager.GraceResolutionEvent.StreakResetDueToTimeout -> {
                // Ask before recording anything: a timer is not evidence of a relapse.
                _showRelapseConfirmation.value = true
                triggerHaptic(longVibe = true)
            }
        }
    }

    /** The user confirmed the slip: record it honestly and start over. */
    fun confirmRelapse() {
        _showRelapseConfirmation.value = false
        repository.resetStreakDueToRelapse()
        _streakState.value = repository.getStreakState()
        _showRelapseDialog.value = true
        syncProfileToCloud()
    }

    /** The user is still clean: count it as a save and protect the streak. */
    fun dismissRelapseConfirmation() {
        _showRelapseConfirmation.value = false
        val newSaves = repository.recordGracePeriodSave()
        _streakState.update { it.copy(gracePeriodSavesCount = newSaves) }
        MonitoringManager.dismissFeedback()
        syncProfileToCloud()
    }

    fun toggleLanguage() {
        val next = if (_language.value == AppLanguage.ENGLISH) AppLanguage.ARABIC else AppLanguage.ENGLISH
        _language.value = next
        repository.saveLanguageCode(next.code)
    }

    // ---------------------------------------------------------------------------------
    // Monitoring settings. These are persisted in MonitoringPreferences, which the
    // accessibility service reads, so the switches actually change behaviour.
    // ---------------------------------------------------------------------------------

    fun setStrictGuardEnabled(enabled: Boolean) {
        monitoringPreferences.strictGuardEnabled = enabled
        _strictGuardEnabled.value = enabled
        if (!enabled) MonitoringManager.dismissFeedback()
        triggerHaptic(longVibe = false)
    }

    fun setWarningBufferEnabled(enabled: Boolean) {
        monitoringPreferences.warningBufferEnabled = enabled
        _warningBufferEnabled.value = enabled
    }

    fun setKeywordFilterEnabled(enabled: Boolean) {
        monitoringPreferences.keywordFilterEnabled = enabled
        _keywordFilterEnabled.value = enabled
    }

    fun grantMonitoringConsent() {
        monitoringPreferences.monitoringConsentGranted = true
        _monitoringConsentGranted.value = true
        // The current disclosure covers browsers *and* the supported apps, so a new
        // consent grants both scopes. Existing users grant the app scope separately.
        grantAppsMonitoringConsent()
    }

    fun grantAppsMonitoringConsent() {
        monitoringPreferences.appsMonitoringConsentGranted = true
        _appsMonitoringConsentGranted.value = true
    }

    // ---------------------------------------------------------------------------------
    // DNS shield (Private DNS for a Device Owner, DNS-only VPN otherwise)
    // ---------------------------------------------------------------------------------

    /** Re-reads the live system state. Called on resume and after every action. */
    fun refreshDnsShield() {
        _dnsShieldConsentGranted.value = monitoringPreferences.dnsShieldConsentGranted
        _dnsShieldState.value = DnsShield.state(getApplication())
    }

    fun grantDnsShieldConsent() {
        monitoringPreferences.dnsShieldConsentGranted = true
        _dnsShieldConsentGranted.value = true
    }

    /** Null when the VPN permission is already granted; otherwise the consent dialog. */
    fun dnsShieldVpnPermissionIntent(): Intent? =
        DnsShield.vpnPermissionIntent(getApplication())

    /**
     * Enables the shield with the strongest mechanism the app is allowed to use. Call only
     * after the disclosure is accepted; the VPN permission must already be granted (the
     * screen launches the system dialog first when it is not).
     *
     * The Device Owner path blocks on a DoT connectivity check, so it runs on IO.
     */
    fun enableDnsShield() {
        if (_dnsShieldBusy.value) return
        _dnsShieldBusy.value = true
        _dnsShieldResult.value = null
        viewModelScope.launch {
            val app = getApplication<Application>()
            val result = withContext(Dispatchers.IO) { DnsShield.enable(app) }
            _dnsShieldResult.value = result
            _dnsShieldBusy.value = false
            refreshDnsShield()
        }
    }

    fun disableDnsShield() {
        val app = getApplication<Application>()
        // While the 90-day commitment is running the shield is locked on purpose. The card
        // hides this action; the guard keeps a stale screen from bypassing it.
        if (DnsShield.isLockedByCommitment(app)) return
        if (_dnsShieldBusy.value) return
        _dnsShieldBusy.value = true
        _dnsShieldResult.value = null
        viewModelScope.launch {
            withContext(Dispatchers.IO) { DnsShield.disable(app) }
            _dnsShieldBusy.value = false
            refreshDnsShield()
        }
    }

    fun clearDnsShieldResult() {
        _dnsShieldResult.value = null
    }

    /** Marks the first-run Shield setup as seen, so it is not shown again. */
    fun completeFirstRunSetup() {
        monitoringPreferences.firstRunSetupDone = true
        _firstRunSetupDone.value = true
    }

    fun grantAiConsent() {
        monitoringPreferences.aiConsentGranted = true
        _aiConsentGranted.value = true
    }

    fun setThemeMode(mode: String) {
        monitoringPreferences.themeMode = mode
        _themeMode.value = mode
    }

    // ---------------------------------------------------------------------------------
    // In-app updater
    // ---------------------------------------------------------------------------------

    /** Manual or automatic check. Automatic checks are skipped when disabled or not due. */
    fun checkForUpdate(manual: Boolean = false) {
        val app = getApplication<Application>()
        if (!manual && !AutoUpdater.isCheckDue(app)) return
        if (_updateCheckInProgress.value) return
        _updateCheckInProgress.value = true
        _updateError.value = null
        _updateStatusMessage.value = null
        AutoUpdater.check(
            app,
            onResult = { info ->
                _updateInfo.value = info
                _updateCheckInProgress.value = false
                if (info == null) {
                    _updateStatusMessage.value = "You're up to date."
                }
            },
            onError = { message ->
                if (manual) _updateError.value = message
                _updateCheckInProgress.value = false
                if (manual) _updateStatusMessage.value = null
            }
        )
    }

    /** Downloads the available update and launches the system installer. */
    fun downloadAndInstallUpdate() {
        val info = _updateInfo.value ?: return
        val app = getApplication<Application>()
        if (!AutoUpdater.canInstallPackages(app)) {
            AutoUpdater.openInstallPermissionSettings(app)
            _updateError.value = "Grant \"Install unknown apps\" for BreakFree, then try again."
            return
        }
        _updateDownloadProgress.value = 0
        AutoUpdater.downloadAndInstall(
            app,
            info,
            onProgress = { _updateDownloadProgress.value = it },
            onComplete = { file ->
                _updateDownloadProgress.value = null
                if (file == null) {
                    _updateError.value = "Download failed. Check your connection and try again."
                }
            }
        )
    }

    fun dismissUpdate() {
        _updateInfo.value = null
    }

    fun clearUpdateError() {
        _updateError.value = null
    }

    fun clearUpdateStatusMessage() {
        _updateStatusMessage.value = null
    }

    // Daily Mood & Urge Check-in methods
    fun openCheckInDialog(open: Boolean) {
        _isCheckInDialogOpen.value = open
    }

    fun saveDailyCheckIn(checkIn: DailyCheckIn) {
        val user = _currentUser.value
        val owner = currentOwner
        val enriched = checkIn.copy(userId = user?.uid ?: checkIn.userId)

        // 1. Local-first: always persist, signed in or not. This used to live only in
        // memory, so every log was lost on process death.
        val localList = upsertCheckIn(repository.getCheckIns(owner), enriched)
        repository.saveCheckIns(owner, localList)
        _dailyCheckIns.value = localList
        _isCheckInDialogOpen.value = false
        triggerHaptic(longVibe = false)

        // 2. Best-effort cloud write. A failure here never loses the local entry: the
        // date stays marked as pending so the cloud snapshot cannot revert it and the
        // upload is retried on the next sign-in.
        if (user != null) {
            repository.markPendingSync(owner, enriched.dateString)
            viewModelScope.launch {
                try {
                    val storedCreatedAt = firestoreRepo.saveCheckIn(enriched)
                    repository.clearPendingSync(owner, enriched.dateString)
                    if (storedCreatedAt != null && currentOwner == owner) {
                        val withTimestamp = enriched.copy(createdAt = storedCreatedAt)
                        val updated = upsertCheckIn(repository.getCheckIns(owner), withTimestamp)
                        repository.saveCheckIns(owner, updated)
                        _dailyCheckIns.value = updated
                    }
                } catch (e: Exception) {
                    Log.w("BreakFreeVM", "Firestore check-in save error: ${e.message}")
                }
            }
        }

        // Add an immediate welcoming AI insight message in the chat companion
        val isAr = _language.value == AppLanguage.ARABIC
        val insightNote = if (isAr) {
" شكراً لتسجيل مراجعتك لليوم (${enriched.dateString})! رصدت تقييمك المزاجي (${enriched.moodScore}/5) وشدة الرغبة (${enriched.urgeIntensity}/10). سأستخدم هذه الأنماط لتخصيص استراتيجيات الصمود الأنسب لك."
        } else {
" Daily reflection logged for ${enriched.dateString}! Mood: ${enriched.moodScore}/5, Peak Urge: ${enriched.urgeIntensity}/10. I have updated my counseling model to incorporate these insights."
        }
        _chatMessages.update {
            it + ChatMessage(id = UUID.randomUUID().toString(), isFromUser = false, text = insightNote)
        }
    }

    fun getTodayCheckIn(): DailyCheckIn? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return _dailyCheckIns.value.firstOrNull { it.dateString == today }
    }

    fun onAuthSuccess() {
        val user = Firebase.auth.currentUser
        // The auth listener normally handles this, but sign-in can complete before it
        // fires; keep this idempotent.
        if (user == null || user.uid != currentOwner || checkInsObservationJob == null) {
            handleAuthUserChanged(user)
        } else {
            _currentUser.value = user
        }
        triggerHaptic(longVibe = false)
    }

    fun onSignOut() {
        handleAuthUserChanged(null)
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
        MonitoringManager.triggerGracePeriod(
            detectedTarget = "adult-preview-example.com/explicit",
            totalSeconds = monitoringPreferences.graceBufferSeconds()
        )
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

    fun setGeminiApiKey(key: String?) {
        repository.saveGeminiApiKey(key)
        _geminiApiKey.value = repository.getGeminiApiKey()
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank() || _isAiLoading.value) return

        // The first message asks for consent instead of silently sending text to a model.
        // Crisis messages are the exception: they are answered locally by CrisisSupport
        // before any network call, so consent must not stand between a user and help.
        if (!_aiConsentGranted.value && !CrisisSupport.isCrisisMessage(text)) {
            _pendingChatMessage.value = text.trim()
            _showAiConsentDialog.value = true
            return
        }

        deliverChatMessage(text.trim())
    }

    /** The user accepted the disclosure: remember it and send the message they typed. */
    fun confirmAiConsentAndSend() {
        grantAiConsent()
        _showAiConsentDialog.value = false
        val pending = _pendingChatMessage.value
        _pendingChatMessage.value = null
        if (!pending.isNullOrBlank()) deliverChatMessage(pending)
    }

    /** The user declined: drop the pending text and leave the chat untouched. */
    fun dismissAiConsent() {
        _showAiConsentDialog.value = false
        _pendingChatMessage.value = null
    }

    private fun deliverChatMessage(text: String) {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromUser = true,
            text = text
        )
        _chatMessages.update { it + userMsg }
        _isAiLoading.value = true

        viewModelScope.launch {
            val reply = geminiCounselor.sendChatMessage(
                userMessage = text,
                cleanDays = _streakState.value.days,
                language = _language.value
            )
            val aiMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                isFromUser = false,
                text = reply.text,
                isFallback = reply.isFallback
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
