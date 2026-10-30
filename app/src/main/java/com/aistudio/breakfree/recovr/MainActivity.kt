package com.aistudio.breakfree.recovr

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.monitoring.DnsShield
import com.aistudio.breakfree.recovr.monitoring.GuardForegroundService
import com.aistudio.breakfree.recovr.monitoring.UninstallLock
import com.aistudio.breakfree.recovr.ui.components.BreakFreeBackground
import com.aistudio.breakfree.recovr.ui.components.CostumeBadge
import com.aistudio.breakfree.recovr.ui.components.CostumeIconType
import com.aistudio.breakfree.recovr.ui.components.FirstRunShieldSetupDialog
import com.aistudio.breakfree.recovr.stealth.StealthManager
import com.aistudio.breakfree.recovr.ui.components.StealthLockScreen
import com.aistudio.breakfree.recovr.ui.components.SosBreathingModal
import com.aistudio.breakfree.recovr.update.AutoUpdater
import com.aistudio.breakfree.recovr.ui.screens.AiCompanionScreen
import com.aistudio.breakfree.recovr.ui.screens.CommunityScreen
import com.aistudio.breakfree.recovr.ui.screens.DashboardScreen
import com.aistudio.breakfree.recovr.ui.screens.MonitoringSettingsScreen
import com.aistudio.breakfree.recovr.ui.screens.SettingsScreen
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.DangerRed
import com.aistudio.breakfree.recovr.ui.theme.GlassBottomBarBg
import com.aistudio.breakfree.recovr.ui.theme.GlassBorderSubtle
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmContainer
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.MyApplicationTheme
import com.aistudio.breakfree.recovr.ui.theme.OnDanger
import com.aistudio.breakfree.recovr.ui.theme.SoftGold
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.util.configureAppCheck
import com.aistudio.breakfree.recovr.viewmodel.BreakFreeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : FragmentActivity() {

    private val viewModel: BreakFreeViewModel by viewModels()

    /**
     * Lint's `InvalidFragmentVersionForActivityResult` check assumes an old Fragment
     * dependency. This app has none — it is a plain [ComponentActivity], which is the
     * parent class of FragmentActivity, so no fragment code is involved and the
     * ActivityResult API is safe here.
     */
    @SuppressLint("InvalidFragmentVersionForActivityResult")
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // Nothing to undo: if the user declines, the in-app grace flow still works,
            // only the heads-up notification is unavailable.
            Log.d("MainActivity", "POST_NOTIFICATIONS granted=$granted")
        }

    private var notificationPermissionChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        // The 90-day uninstall commitment ends on its own. The alarm is the primary path,
        // but alarms can be dropped (reboot, battery saver), so every start re-checks.
        if (!UninstallLock.releaseIfDue(this)) {
            UninstallLock.ensureDeviceOwnerCommitment(this)
        }

        setContent {
            val language by viewModel.language.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme(darkTheme = darkTheme) {
                    BreakFreeApp(viewModel = viewModel, language = language)
                }
            }
        }
    }

    /**
     * The 120s grace warning is delivered as a notification, so without this permission
     * the user gets no warning at all on Android 13+. Ask once, from onResume (an
     * activity must be resumed before it can show a permission dialog), then stay quiet.
     */
    override fun onResume() {
        super.onResume()
        // Device Owner can be granted from a computer or the setup wizard while the app
        // is backgrounded; re-check on every resume so the commitment starts immediately.
        if (!UninstallLock.releaseIfDue(this)) {
            UninstallLock.ensureDeviceOwnerCommitment(this)
        }
        syncDnsShield()

        // Stealth Mode: keep the disguise in sync and protect the window.
        StealthManager.syncDisguise(this)
        if (StealthManager.isStealthActive(this)) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }

        // Keep the protection process alive even when the app is closed.
        GuardForegroundService.start(this)

        viewModel.checkForUpdate()
        if (!notificationPermissionChecked) {
            notificationPermissionChecked = true
            ensureNotificationPermission()
        }
    }

    /**
     * Re-applies the DNS shield when it is on: system Private DNS for a Device Owner, the
     * DNS-only VPN otherwise. The Device Owner path performs a DoT connectivity check,
     * which blocks, so the work runs off the main thread. The UI status is refreshed
     * before and after.
     */
    private fun syncDnsShield() {
        viewModel.refreshDnsShield()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                DnsShield.sync(applicationContext)
            }
            viewModel.refreshDnsShield()
        }
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return

        val prefs = getSharedPreferences("breakfree_prefs", MODE_PRIVATE)
        if (prefs.getBoolean(KEY_NOTIFICATION_PERMISSION_ASKED, false)) return
        prefs.edit().putBoolean(KEY_NOTIFICATION_PERMISSION_ASKED, true).apply()
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

private const val KEY_NOTIFICATION_PERMISSION_ASKED = "notification_permission_requested"

private enum class Tab { HOME, GUARD, AI, COMMUNITY, SETTINGS }

private data class NavItem(
    val titleKey: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
    val tab: Tab
)

@Composable
fun BreakFreeApp(
    viewModel: BreakFreeViewModel,
    language: AppLanguage
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val isSosVisible by viewModel.isSosModalVisible.collectAsState()
    // Shown on whatever tab the user is on: a pending streak decision must not be hidden
    // behind the Home tab.
    val showRelapseConfirmation by viewModel.showRelapseConfirmation.collectAsState()

    // First-run Shield setup: shown once, when the app opens for the first time.
    val firstRunSetupDone by viewModel.firstRunSetupDone.collectAsState()
    val dnsShieldState by viewModel.dnsShieldState.collectAsState()
    val monitoringConsentGranted by viewModel.monitoringConsentGranted.collectAsState()

    // Stealth Mode lock gate.
    val context = LocalContext.current
    var stealthUnlocked by remember { mutableStateOf(!StealthManager.lockRequired(context)) }

    val updateInfo by viewModel.updateInfo.collectAsState()
    val updateDownloadProgress by viewModel.updateDownloadProgress.collectAsState()
    val updateError by viewModel.updateError.collectAsState()

    val navItems = listOf(
        NavItem("tab_home", Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home", Tab.HOME),
        NavItem("tab_guard", Icons.Filled.Shield, Icons.Outlined.Shield, "nav_tab_guard", Tab.GUARD),
        NavItem("tab_ai_chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline, "nav_tab_ai", Tab.AI),
        NavItem("tab_community", Icons.Filled.Groups, Icons.Outlined.Groups, "nav_tab_community", Tab.COMMUNITY),
        NavItem("tab_settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_tab_settings", Tab.SETTINGS)
    )

    BreakFreeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val forward = targetState > initialState
                    val offset = if (forward) 1 else -1
                    (fadeIn(tween(220)) + slideInHorizontally(tween(260)) { it / 14 * offset })
                        .togetherWith(
                            fadeOut(tween(160)) + slideOutHorizontally(tween(220)) { -it / 14 * offset }
                        )
                },
                label = "tab_transition",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { tab ->
                when (Tab.entries[tab]) {
                    Tab.HOME -> DashboardScreen(viewModel = viewModel)
                    Tab.GUARD -> MonitoringSettingsScreen(viewModel = viewModel)
                    Tab.AI -> AiCompanionScreen(viewModel = viewModel)
                    Tab.COMMUNITY -> CommunityScreen(viewModel = viewModel)
                    Tab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onOpenGuard = { selectedTab = Tab.GUARD.ordinal }
                    )
                }
            }

            FloatingNavBar(
                items = navItems,
                selectedIndex = selectedTab,
                language = language,
                onSelect = { selectedTab = it }
            )
        }

        // Stealth Mode lock gate: blocks the dashboard until authenticated.
        if (!stealthUnlocked && StealthManager.lockRequired(context)) {
            StealthLockScreen(
                language = language,
                onUnlock = { stealthUnlocked = true }
            )
        }

        // Full screen emergency SOS modal
        SosBreathingModal(
            isVisible = isSosVisible,
            language = language,
            onDismiss = { viewModel.closeSosModal() },
            onComplete = { viewModel.completeSosSession() }
        )

        // First run: the Shield only works if the system permissions behind it exist, so
        // the three layers are offered once, up front. Nothing is enabled by opening it.
        if (!firstRunSetupDone) {
            FirstRunShieldSetupDialog(
                language = language,
                dnsActive = dnsShieldState.status == DnsShield.Status.ACTIVE,
                dnsNeedsVpn = dnsShieldState.mechanism == DnsShield.Mechanism.ON_DEVICE_FILTER,
                vpnPermissionIntent = { viewModel.dnsShieldVpnPermissionIntent() },
                monitorConsentGranted = monitoringConsentGranted,
                onEnableDns = {
                    viewModel.grantDnsShieldConsent()
                    viewModel.enableDnsShield()
                },
                onSetupMonitor = {
                    // The full accessibility disclosure lives on the Guard screen; this
                    // only takes the user there rather than granting consent silently.
                    viewModel.completeFirstRunSetup()
                    selectedTab = Tab.GUARD.ordinal
                },
                onDone = { viewModel.completeFirstRunSetup() }
            )
        }

        // Update available dialog
        if (updateInfo != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdate() },
                title = { Text(Translations.t("update_available_title", language)) },
                text = {
                    Column {
                        Text(
                            text = Translations.t("update_available_body", language)
                                .replace("{version}", updateInfo!!.versionName.ifBlank { updateInfo!!.versionCode.toString() }),
                            color = TextSecondary
                        )
                        if (updateInfo!!.releaseNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = updateInfo!!.releaseNotes, color = TextMuted, fontSize = 12.sp)
                        }
                        updateDownloadProgress?.let { progress ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "$progress%", color = SoftGold, fontWeight = FontWeight.Bold)
                        }
                        updateError?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = err, color = DangerRed, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.downloadAndInstallUpdate() },
                        enabled = updateDownloadProgress == null
                    ) {
                        Text(Translations.t("update_download_install", language), color = GreenCalmLight, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUpdate() }) {
                        Text(Translations.t("update_later", language), color = TextSecondary)
                    }
                },
                containerColor = BlackElevated,
                titleContentColor = TextPrimary,
                textContentColor = TextSecondary
            )
        }

        // The step-back window expired. Nothing is recorded until the user says what
        // happened: a timer is not proof of a relapse.
        if (showRelapseConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissRelapseConfirmation() },
                icon = {
                    CostumeBadge(
                        type = CostumeIconType.TIMER,
                        size = 46.dp,
                        isGlowing = true,
                        isCalmGreen = false,
                        isFilled = true
                    )
                },
                title = {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "انتهت نافذة التراجع" else "Your step-back window ended",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = if (language == AppLanguage.ARABIC) {
                            "لم نسجّل أي شيء بعد. هل حدثت انتكاسة فعلاً، أم أنك ما زلت نظيفاً؟ اختيارك يحدد ما نكتبه، لا المؤقّت."
                        } else {
                            "Nothing has been recorded yet. Did you actually slip, or are you still clean? Your answer decides what we write — not the timer."
                        },
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmRelapse() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = OnDanger
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "نعم، حدثت انتكاسة" else "Yes, I slipped",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissRelapseConfirmation() }) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "لا، ما زلت نظيفاً" else "No, I'm still clean",
                            color = GreenCalmLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = BlackElevated,
                titleContentColor = TextPrimary
            )
        }
    }
}

/**
 * Floating bottom bar with an animated selection pill. Five destinations is the
 * practical maximum for a bar like this — beyond that, labels stop fitting.
 */
@Composable
private fun FloatingNavBar(
    items: List<NavItem>,
    selectedIndex: Int,
    language: AppLanguage,
    onSelect: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(GlassBottomBarBg)
                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(28.dp))
                .padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                val itemBg by animateColorAsState(
                    targetValue = if (isSelected) GreenCalmContainer else Color.Transparent,
                    animationSpec = tween(240),
                    label = "navBg"
                )
                val tint by animateColorAsState(
                    targetValue = if (isSelected) GreenCalmLight else TextMuted,
                    animationSpec = tween(240),
                    label = "navTint"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(itemBg)
                        .border(
                            1.dp,
                            if (isSelected) GreenCalm.copy(alpha = 0.30f) else Color.Transparent,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onSelect(index) }
                        .padding(vertical = 7.dp)
                        .testTag(item.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = Translations.t(item.titleKey, language),
                        tint = tint,
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = Translations.t(item.titleKey, language),
                        fontSize = 9.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = tint,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
