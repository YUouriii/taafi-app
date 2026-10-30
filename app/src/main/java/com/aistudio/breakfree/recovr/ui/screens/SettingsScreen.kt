package com.aistudio.breakfree.recovr.ui.screens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aistudio.breakfree.recovr.BuildConfig
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.monitoring.MonitoringManager
import com.aistudio.breakfree.recovr.monitoring.OverlayPermission
import com.aistudio.breakfree.recovr.monitoring.UninstallLock
import com.aistudio.breakfree.recovr.ui.components.AuthCard
import com.aistudio.breakfree.recovr.ui.components.CostumeEmoji
import com.aistudio.breakfree.recovr.ui.components.CostumeIconType
import com.aistudio.breakfree.recovr.ui.components.GhostButton
import com.aistudio.breakfree.recovr.ui.components.IconTile
import com.aistudio.breakfree.recovr.ui.components.OverlayPermissionDialog
import com.aistudio.breakfree.recovr.ui.components.Pill
import com.aistudio.breakfree.recovr.ui.components.ScreenGutter
import com.aistudio.breakfree.recovr.ui.components.ScreenHeader
import com.aistudio.breakfree.recovr.ui.components.SectionHeader
import com.aistudio.breakfree.recovr.ui.components.SegmentedControl
import com.aistudio.breakfree.recovr.ui.components.SettingRow
import com.aistudio.breakfree.recovr.ui.components.SoftCard
import com.aistudio.breakfree.recovr.ui.components.SoftDivider
import com.aistudio.breakfree.recovr.ui.components.DeviceOwnerSetupDialog
import com.aistudio.breakfree.recovr.ui.components.UninstallLockActiveDialog
import com.aistudio.breakfree.recovr.ui.components.UninstallLockDialog
import com.aistudio.breakfree.recovr.ui.components.rememberOverlayPermissionGranted
import com.aistudio.breakfree.recovr.ui.components.rememberUninstallLockState
import com.aistudio.breakfree.recovr.ui.components.uninstallLockSubtitle
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.DangerRed
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.OnDanger
import com.aistudio.breakfree.recovr.ui.theme.RoseAlert
import com.aistudio.breakfree.recovr.ui.theme.SoftGold
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.viewmodel.BreakFreeViewModel

/**
 * App-level preferences. The Shield's own controls stay on the Guard tab;
 * this screen is about the person using the app — account, language, system
 * permissions and the small print.
 */
@Composable
fun SettingsScreen(
    viewModel: BreakFreeViewModel,
    onOpenGuard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val updateCheckInProgress by viewModel.updateCheckInProgress.collectAsState()
    val updateError by viewModel.updateError.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val streakSummary by viewModel.streakSummary.collectAsState()
    val isServiceConnected by MonitoringManager.isServiceConnected.collectAsState()
    val showSetDateDialog by viewModel.showSetDateDialog.collectAsState()
    val showResetConfirmationDialog by viewModel.showResetConfirmationDialog.collectAsState()

    var notificationsAllowed by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    // Re-read on ON_RESUME: the overlay permission is granted on a system screen.
    val overlayAllowed = rememberOverlayPermissionGranted()
    var showOverlayDialog by remember { mutableStateOf(false) }

    // The device-administrator state also only changes on a system screen.
    val lockState = rememberUninstallLockState()
    var showLockDialog by remember { mutableStateOf(false) }
    var showActiveLockDialog by remember { mutableStateOf(false) }
    var showDeviceOwnerDialog by remember { mutableStateOf(false) }

    // Permissions can change in Android Settings while the app is backgrounded.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val openNotificationSettings: () -> Unit = {
        // ACTION_APP_NOTIFICATION_SETTINGS only exists from API 26. On API 24-25 the
        // per-app notification screen is not reachable, so fall back to the app's own
        // details page instead of throwing.
        val opened = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
                true
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }

        if (!opened) {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = Translations.t("settings_title", language),
            subtitle = Translations.t("settings_subtitle", language),
            iconType = CostumeIconType.SETTINGS
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ScreenGutter,
                end = ScreenGutter,
                top = 4.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ---------------------------------------------------------------
            // Account
            // ---------------------------------------------------------------
            item {
                SectionHeader(title = Translations.t("settings_account", language))
            }
            item {
                AuthCard(
                    currentUser = currentUser,
                    language = language,
                    onAuthSuccess = { viewModel.onAuthSuccess() },
                    onSignOut = { viewModel.onSignOut() }
                )
            }

            // ---------------------------------------------------------------
            // Appearance
            // ---------------------------------------------------------------
            item {
                SectionHeader(title = Translations.t("settings_appearance", language))
            }
            item {
                SoftCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(type = CostumeIconType.LANGUAGE, tint = GreenCalmLight, size = 40.dp)
                        Spacer(modifier = Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Translations.t("language_toggle", language),
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = Translations.t("settings_language_desc", language),
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    // The two options name languages, not directions, so keep them in a
                    // fixed order (English | العربية) instead of mirroring with the UI.
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        SegmentedControl(
                            options = listOf("English", "العربية"),
                            selectedIndex = if (language == AppLanguage.ARABIC) 1 else 0,
                            onSelect = { index ->
                                val wantsArabic = index == 1
                                if ((language == AppLanguage.ARABIC) != wantsArabic) {
                                    viewModel.toggleLanguage()
                                }
                            },
                            modifier = Modifier.testTag("language_segmented_control")
                        )
                    }
                }
            }

            item {
                SoftCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(type = CostumeIconType.SETTINGS, tint = GreenCalmLight, size = 40.dp)
                        Spacer(modifier = Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Translations.t("settings_theme", language),
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = Translations.t("settings_theme_desc", language),
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        SegmentedControl(
                            options = listOf(
                                Translations.t("theme_dark", language),
                                Translations.t("theme_light", language),
                                Translations.t("theme_system", language)
                            ),
                            selectedIndex = when (themeMode) {
                                "light" -> 1
                                "system" -> 2
                                else -> 0
                            },
                            onSelect = { index ->
                                val mode = when (index) {
                                    1 -> "light"
                                    2 -> "system"
                                    else -> "dark"
                                }
                                if (mode != themeMode) viewModel.setThemeMode(mode)
                            },
                            modifier = Modifier.testTag("theme_segmented_control")
                        )
                    }
                }
            }

            // ---------------------------------------------------------------
            // Updates
            // ---------------------------------------------------------------
            item {
                SectionHeader(title = Translations.t("settings_updates", language))
            }
            item {
                SoftCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(type = CostumeIconType.CLOUD, tint = GreenCalmLight, size = 40.dp)
                        Spacer(modifier = Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Translations.t("settings_current_version", language),
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    GhostButton(
                        text = if (updateCheckInProgress) {
                            Translations.t("settings_checking_updates", language)
                        } else {
                            Translations.t("settings_check_updates", language)
                        },
                        onClick = { viewModel.checkForUpdate(manual = true) },
                        icon = CostumeIconType.REFRESH,
                        tint = GreenCalmLight,
                        borderColor = GreenCalm.copy(alpha = 0.35f),
                        modifier = Modifier.testTag("check_updates_btn")
                    )
                    updateError?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = err, color = DangerRed, fontSize = 11.5.sp)
                    }
                }
            }

            // ---------------------------------------------------------------
            // Protection & permissions
            // ---------------------------------------------------------------
            item {
                SectionHeader(title = Translations.t("settings_protection", language))
            }
            item {
                SoftCard(contentPadding = 8.dp) {
                    SettingRow(
                        iconType = CostumeIconType.SHIELD,
                        title = Translations.t("shield_title", language),
                        subtitle = if (isServiceConnected) {
                            Translations.t("service_active", language)
                        } else {
                            Translations.t("service_inactive", language)
                        },
                        tint = if (isServiceConnected) GreenCalmLight else RoseAlert,
                        onClick = onOpenGuard,
                        trailing = { ArrowAffordance() }
                    )
                    SoftDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    SettingRow(
                        iconType = CostumeIconType.NOTIFICATIONS,
                        title = Translations.t("settings_notifications", language),
                        subtitle = if (notificationsAllowed) {
                            Translations.t("settings_notifications_ok", language)
                        } else {
                            Translations.t("settings_notifications_off", language)
                        },
                        tint = if (notificationsAllowed) GreenCalmLight else RoseAlert,
                        onClick = openNotificationSettings,
                        trailing = { ArrowAffordance() }
                    )
                    SoftDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    SettingRow(
                        iconType = CostumeIconType.SENSE_VISION,
                        title = Translations.t("settings_overlay", language),
                        subtitle = if (overlayAllowed) {
                            Translations.t("settings_overlay_ok", language)
                        } else {
                            Translations.t("settings_overlay_off", language)
                        },
                        tint = if (overlayAllowed) GreenCalmLight else SoftGold,
                        onClick = {
                            // Already granted: go straight to the system switch so it can
                            // also be turned off. Otherwise explain first.
                            if (overlayAllowed) {
                                OverlayPermission.openSettings(context)
                            } else {
                                showOverlayDialog = true
                            }
                        },
                        trailing = { ArrowAffordance() }
                    )
                    SoftDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    SettingRow(
                        iconType = CostumeIconType.LOCKED,
                        title = Translations.t("settings_uninstall_lock", language),
                        subtitle = uninstallLockSubtitle(
                            state = lockState,
                            language = language,
                            dateLabel = UninstallLock.releaseDateLabel(context, language),
                            daysLeft = UninstallLock.daysRemaining(context)
                        ),
                        tint = if (lockState == UninstallLock.State.ACTIVE) SoftGold else TextMuted,
                        onClick = {
                            if (lockState == UninstallLock.State.ACTIVE) {
                                showActiveLockDialog = true
                            } else {
                                showLockDialog = true
                            }
                        },
                        trailing = { ArrowAffordance() }
                    )
                }
            }

            // ---------------------------------------------------------------
            // Recovery data
            // ---------------------------------------------------------------
            item {
                SectionHeader(title = Translations.t("settings_data", language))
            }
            item {
                SoftCard(contentPadding = 8.dp) {
                    SettingRow(
                        iconType = CostumeIconType.CALENDAR,
                        title = Translations.t("adjust_start_date", language),
                        subtitle = "${streakSummary.days} " + Translations.t("days", language),
                        onClick = { viewModel.showSetDateDialog(true) },
                        trailing = { ArrowAffordance() }
                    )
                    SoftDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    SettingRow(
                        iconType = CostumeIconType.DELETE,
                        title = Translations.t("reset_streak", language),
                        subtitle = Translations.t("settings_data_desc", language),
                        tint = RoseAlert,
                        onClick = { viewModel.showResetDialog(true) },
                        trailing = { ArrowAffordance(tint = RoseAlert) }
                    )
                }
            }

            // ---------------------------------------------------------------
            // About & privacy
            // ---------------------------------------------------------------
            item {
                SectionHeader(title = Translations.t("settings_about", language))
            }
            item {
                SoftCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile(type = CostumeIconType.INFO, tint = GreenCalmLight, size = 40.dp)
                            Spacer(modifier = Modifier.size(12.dp))
                            Column {
                                Text(
                                    text = "BreakFree / تعافي",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = Translations.t("settings_footer", language),
                                    color = TextMuted,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                        Pill(text = "v${BuildConfig.VERSION_NAME}", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SoftDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        IconTile(type = CostumeIconType.PRIVACY, tint = GreenCalmLight, size = 36.dp)
                        Spacer(modifier = Modifier.size(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Translations.t("settings_privacy", language),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = Translations.t("settings_privacy_desc", language),
                                color = TextMuted,
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            // Crisis note: deliberately generic. Inventing a hotline number would be
            // worse than saying nothing.
            item {
                SoftCard(accent = RoseAlert) {
                    Row(verticalAlignment = Alignment.Top) {
                        IconTile(type = CostumeIconType.HEART, tint = RoseAlert, size = 36.dp)
                        Spacer(modifier = Modifier.size(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Translations.t("settings_crisis", language),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = Translations.t("settings_crisis_desc", language),
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            item {
                GhostButton(
                    text = Translations.t("settings_guard_shortcut", language),
                    onClick = onOpenGuard,
                    icon = CostumeIconType.SHIELD,
                    tint = GreenCalmLight,
                    borderColor = GreenCalm.copy(alpha = 0.35f)
                )
            }
        }
    }

    // Overlay ("display over other apps") disclosure, shown before the system screen.
    if (showOverlayDialog) {
        OverlayPermissionDialog(
            language = language,
            onContinue = {
                showOverlayDialog = false
                OverlayPermission.openSettings(context)
            },
            onDismiss = { showOverlayDialog = false }
        )
    }

    // 90-day uninstall commitment disclosure, shown before the system admin screen.
    if (showLockDialog) {
        UninstallLockDialog(
            language = language,
            onContinue = {
                showLockDialog = false
                // Opens the system screen; the user still confirms there, and the state is
                // only recorded once Android reports the administrator as enabled.
                UninstallLock.activate(context)
            },
            onShowDeviceOwner = {
                showLockDialog = false
                showDeviceOwnerDialog = true
            },
            onDismiss = { showLockDialog = false }
        )
    }

    // Maximum protection: Device Owner via adb or QR provisioning.
    if (showDeviceOwnerDialog) {
        DeviceOwnerSetupDialog(
            language = language,
            onDismiss = { showDeviceOwnerDialog = false }
        )
    }

    // Active 90-day lock reminder dialog: encourages the user to stay strong and blocks easy removal
    if (showActiveLockDialog) {
        UninstallLockActiveDialog(
            language = language,
            dateLabel = UninstallLock.releaseDateLabel(context, language),
            daysLeft = UninstallLock.daysRemaining(context),
            onShowDeviceOwner = {
                showActiveLockDialog = false
                showDeviceOwnerDialog = true
            },
            onDismiss = { showActiveLockDialog = false }
        )
    }

    // Adjust start date
    if (showSetDateDialog) {
        var daysInput by remember { mutableStateOf("${streakSummary.days}") }
        AlertDialog(
            onDismissRequest = { viewModel.showSetDateDialog(false) },
            title = {
                Text(
                    text = Translations.t("adjust_start_date", language),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (language == AppLanguage.ARABIC)
                            "أدخل عدد الأيام النظيفة التي حققتها:"
                        else
                            "Enter how many clean days you have already achieved:",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = daysInput,
                        onValueChange = { daysInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text(Translations.t("days", language)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = daysInput.toIntOrNull() ?: 0
                        viewModel.setCustomCleanDays(num)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenCalm,
                        contentColor = com.aistudio.breakfree.recovr.ui.theme.OnGreenCalm
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = if (language == AppLanguage.ARABIC) "حفظ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showSetDateDialog(false) }) {
                    Text(text = Translations.t("close", language), color = TextSecondary)
                }
            },
            containerColor = BlackElevated,
            titleContentColor = TextPrimary
        )
    }

    // Manual reset confirmation
    if (showResetConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showResetDialog(false) },
            icon = {
                IconTile(type = CostumeIconType.DELETE, tint = RoseAlert, size = 44.dp)
            },
            title = {
                Text(
                    text = Translations.t("reset_streak", language),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == AppLanguage.ARABIC)
                        "هل أنت متأكد من رغبتك في تصفير العداد؟ التعافي مسار شجاع، والاعتراف بالانتكاسة هو الخطوة الأولى لبداية أقوى."
                    else
                        "Are you sure you want to reset your streak? Recovery takes courage, and acknowledging a slip is the first step toward greater strength.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.manualResetStreak() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRed,
                        contentColor = OnDanger
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "تأكيد وإعادة البدء" else "Reset now",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showResetDialog(false) }) {
                    Text(text = Translations.t("close", language), color = TextSecondary)
                }
            },
            containerColor = BlackElevated,
            titleContentColor = TextPrimary
        )
    }
}

@Composable
private fun ArrowAffordance(tint: Color = TextMuted) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(BlackElevated),
        contentAlignment = Alignment.Center
    ) {
        CostumeEmoji(type = CostumeIconType.ARROW, size = 15.dp, tint = tint)
    }
}
