package com.aistudio.breakfree.recovr.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.BuildConfig
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.monitoring.AccessibilityMonitor
import com.aistudio.breakfree.recovr.monitoring.DnsShield
import com.aistudio.breakfree.recovr.monitoring.ExplicitContentDetector
import com.aistudio.breakfree.recovr.monitoring.MonitoringManager
import com.aistudio.breakfree.recovr.monitoring.OverlayPermission
import com.aistudio.breakfree.recovr.monitoring.PrivateDnsShield
import com.aistudio.breakfree.recovr.monitoring.UninstallLock
import com.aistudio.breakfree.recovr.ui.components.CostumeBadge
import com.aistudio.breakfree.recovr.ui.components.CostumeIconType
import com.aistudio.breakfree.recovr.ui.components.GhostButton
import com.aistudio.breakfree.recovr.ui.components.IconTile
import com.aistudio.breakfree.recovr.ui.components.OverlayPermissionDialog
import com.aistudio.breakfree.recovr.ui.components.Pill
import com.aistudio.breakfree.recovr.ui.components.PrimaryButton
import com.aistudio.breakfree.recovr.ui.components.ScreenGutter
import com.aistudio.breakfree.recovr.ui.components.ScreenHeader
import com.aistudio.breakfree.recovr.ui.components.SectionHeader
import com.aistudio.breakfree.recovr.ui.components.SettingRow
import com.aistudio.breakfree.recovr.ui.components.SoftCard
import com.aistudio.breakfree.recovr.ui.components.SoftDivider
import com.aistudio.breakfree.recovr.ui.components.rememberOverlayPermissionGranted
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.DangerRed
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.OnGreenCalm
import com.aistudio.breakfree.recovr.ui.theme.SoftGold
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.ui.theme.WhiteMuted
import com.aistudio.breakfree.recovr.viewmodel.BreakFreeViewModel

/**
 * The Guard tab: everything that decides *how* the Shield behaves.
 *
 * App-level preferences (language, account, notifications) live on the Settings tab;
 * this screen is only the protection engine, so the two never duplicate each other.
 */
@Composable
fun MonitoringSettingsScreen(
    viewModel: BreakFreeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val isServiceConnected by MonitoringManager.isServiceConnected.collectAsState()

    // Persisted, so the accessibility service sees the same values after a restart.
    val strictGuardMode by viewModel.strictGuardEnabled.collectAsState()
    val warningBufferEnabled by viewModel.warningBufferEnabled.collectAsState()
    val keywordFilterEnabled by viewModel.keywordFilterEnabled.collectAsState()
    val monitoringConsentGranted by viewModel.monitoringConsentGranted.collectAsState()
    val appsMonitoringConsentGranted by viewModel.appsMonitoringConsentGranted.collectAsState()

    // DNS shield. The system is the source of truth, so this is re-read on entry and on
    // resume (MainActivity calls refreshDnsShield after returning from Settings).
    val dnsShieldState by viewModel.dnsShieldState.collectAsState()
    val dnsShieldConsentGranted by viewModel.dnsShieldConsentGranted.collectAsState()
    val dnsShieldBusy by viewModel.dnsShieldBusy.collectAsState()
    val dnsShieldResult by viewModel.dnsShieldResult.collectAsState()

    // The system VPN consent dialog. The filter refuses to start without it, so the
    // launcher always comes first on a normal install.
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (viewModel.dnsShieldVpnPermissionIntent() == null) {
            viewModel.enableDnsShield()
        }
    }

    val startDnsShield: () -> Unit = {
        if (dnsShieldState.mechanism == DnsShield.Mechanism.PRIVATE_DNS) {
            viewModel.enableDnsShield()
        } else {
            val intent = viewModel.dnsShieldVpnPermissionIntent()
            if (intent != null) vpnPermissionLauncher.launch(intent) else viewModel.enableDnsShield()
        }
    }

    var showMonitoringConsentDialog by remember { mutableStateOf(false) }
    var showAppsConsentDialog by remember { mutableStateOf(false) }
    var showDnsConsentDialog by remember { mutableStateOf(false) }

    // Live state of the two system gates the content monitor depends on. Re-read on
    // resume, because both are flipped on a system screen.
    var accessibilityEnabled by remember { mutableStateOf(AccessibilityMonitor.isEnabled(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityEnabled = AccessibilityMonitor.isEnabled(context)
                viewModel.refreshDnsShield()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        accessibilityEnabled = AccessibilityMonitor.isEnabled(context)
        viewModel.refreshDnsShield()
    }

    val releaseDateLabel = remember(language) {
        UninstallLock.releaseDateLabel(context, language)
    }
    // Re-read on ON_RESUME: the overlay permission is granted on a system screen.
    val overlayAllowed = rememberOverlayPermissionGranted()
    var showOverlayDialog by remember { mutableStateOf(false) }

    val openAccessibilitySettings: () -> Unit = {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS)
            fallback.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(fallback)
        }
    }

    if (showMonitoringConsentDialog) {
        MonitoringConsentDialog(
            language = language,
            onAccept = {
                viewModel.grantMonitoringConsent()
                showMonitoringConsentDialog = false
                openAccessibilitySettings()
            },
            onDecline = { showMonitoringConsentDialog = false }
        )
    }

    if (showAppsConsentDialog) {
        AppsMonitoringConsentDialog(
            language = language,
            onAccept = {
                viewModel.grantAppsMonitoringConsent()
                showAppsConsentDialog = false
            },
            onDecline = { showAppsConsentDialog = false }
        )
    }

    if (showDnsConsentDialog) {
        DnsShieldConsentDialog(
            language = language,
            onAccept = {
                showDnsConsentDialog = false
                viewModel.grantDnsShieldConsent()
                startDnsShield()
            },
            onDecline = { showDnsConsentDialog = false }
        )
    }

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

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = Translations.t("shield_title", language),
            subtitle = Translations.t("guard_subtitle", language),
            iconType = CostumeIconType.SHIELD
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ScreenGutter,
                end = ScreenGutter,
                top = 4.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------------------------------------------------------------
            // Shield status + the one action that actually matters
            // ---------------------------------------------------------------
            item {
                ShieldStatusCard(
                    language = language,
                    isConnected = isServiceConnected,
                    onOpenSettings = {
                        // Play policy: the accessibility service is only opened
                        // after an explicit, in-app disclosure and consent.
                        if (monitoringConsentGranted) {
                            openAccessibilitySettings()
                        } else {
                            showMonitoringConsentDialog = true
                        }
                    }
                )
            }

            // ---------------------------------------------------------------
            // Wider scope: social/video apps (own consent, added after launch)
            // ---------------------------------------------------------------
            if (monitoringConsentGranted && !appsMonitoringConsentGranted) {
                item {
                    SoftCard(accent = SoftGold, contentPadding = 16.dp) {
                        Row(verticalAlignment = Alignment.Top) {
                            IconTile(type = CostumeIconType.SENSE_VISION, tint = SoftGold, size = 38.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = Translations.t("guard_apps_consent_title", language),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Translations.t("guard_apps_consent_body", language),
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = Translations.t("guard_apps_consent_note", language),
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        GhostButton(
                            text = Translations.t("guard_apps_consent_btn", language),
                            onClick = { showAppsConsentDialog = true },
                            icon = CostumeIconType.SHIELD_CHECK,
                            tint = SoftGold,
                            borderColor = SoftGold.copy(alpha = 0.35f),
                            modifier = Modifier.testTag("enable_apps_monitoring_btn")
                        )
                    }
                }
            }

            // ---------------------------------------------------------------
            // Overlay permission hint (only when the Shield runs without it)
            // ---------------------------------------------------------------
            if (isServiceConnected && !overlayAllowed) {
                item {
                    SoftCard(accent = SoftGold, contentPadding = 16.dp) {
                        Row(verticalAlignment = Alignment.Top) {
                            IconTile(type = CostumeIconType.SENSE_VISION, tint = SoftGold, size = 38.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = Translations.t("guard_overlay_hint_title", language),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Translations.t("guard_overlay_hint_body", language),
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        GhostButton(
                            text = Translations.t("guard_overlay_hint_btn", language),
                            onClick = { showOverlayDialog = true },
                            icon = CostumeIconType.SENSE_VISION,
                            tint = SoftGold,
                            borderColor = SoftGold.copy(alpha = 0.35f),
                            modifier = Modifier.testTag("allow_overlay_btn")
                        )
                    }
                }
            }

            // ---------------------------------------------------------------
            // Step-back window preview (debug only: it fabricates a detection)
            // ---------------------------------------------------------------
            if (BuildConfig.DEBUG) {
                item {
                    SoftCard(contentPadding = 16.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconTile(type = CostumeIconType.TIMER, tint = SoftGold, size = 38.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = Translations.t("grace_banner_title", language),
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = Translations.t("simulate_grace_desc", language),
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Pill(
                                text = Translations.t("buffer_label", language),
                                color = SoftGold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        PrimaryButton(
                            text = Translations.t("simulate_grace_btn", language),
                            onClick = { viewModel.simulateGracePeriod() },
                            icon = CostumeIconType.TIMER,
                            containerColor = SoftGold,
                            contentColor = Color(0xFF241A00),
                            modifier = Modifier.testTag("settings_simulate_grace_button")
                        )
                    }
                }
            }

            // ---------------------------------------------------------------
            // Protection controls
            // ---------------------------------------------------------------
            item {
                SectionHeader(
                    title = Translations.t("protection_controls", language),
                    subtitle = Translations.t("settings_protection_desc", language)
                )
            }
            item {
                SoftCard(contentPadding = 8.dp) {
                    SettingRow(
                        iconType = CostumeIconType.SHIELD_CHECK,
                        title = Translations.t("strict_guard_mode", language),
                        subtitle = Translations.t("strict_guard_desc", language),
                        tint = GreenCalmLight,
                        trailing = {
                            GuardSwitch(
                                checked = strictGuardMode,
                                onCheckedChange = { viewModel.setStrictGuardEnabled(it) },
                                testTag = "toggle_strict_guard_mode"
                            )
                        }
                    )
                    SoftDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    SettingRow(
                        iconType = CostumeIconType.TIMER,
                        title = Translations.t("grace_buffer_toggle", language),
                        subtitle = Translations.t("grace_buffer_desc", language),
                        tint = SoftGold,
                        trailing = {
                            GuardSwitch(
                                checked = warningBufferEnabled,
                                onCheckedChange = { viewModel.setWarningBufferEnabled(it) },
                                testTag = "toggle_warning_buffer"
                            )
                        }
                    )
                    SoftDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    SettingRow(
                        iconType = CostumeIconType.LOCKED,
                        title = Translations.t("keyword_filter_toggle", language),
                        subtitle = Translations.t("keyword_filter_desc", language),
                        tint = GreenCalmLight,
                        trailing = {
                            GuardSwitch(
                                checked = keywordFilterEnabled,
                                onCheckedChange = { viewModel.setKeywordFilterEnabled(it) },
                                testTag = "toggle_explicit_filter"
                            )
                        }
                    )
                }
            }

            // ---------------------------------------------------------------
            // Setup checklist — shown until the monitor can actually run
            // ---------------------------------------------------------------
            if (!accessibilityEnabled || !monitoringConsentGranted) {
                item {
                    SetupChecklistCard(
                        language = language,
                        accessibilityEnabled = accessibilityEnabled,
                        monitoringConsentGranted = monitoringConsentGranted,
                        dnsConsentGranted = dnsShieldConsentGranted,
                        dnsActive = dnsShieldState.status == DnsShield.Status.ACTIVE,
                        onAllowRestricted = { AccessibilityMonitor.openAppInfo(context) },
                        onEnableMonitor = {
                            if (monitoringConsentGranted) {
                                AccessibilityMonitor.openAccessibilitySettings(context)
                            } else {
                                showMonitoringConsentDialog = true
                            }
                        },
                        onEnableDns = {
                            if (dnsShieldConsentGranted) {
                                startDnsShield()
                            } else {
                                showDnsConsentDialog = true
                            }
                        }
                    )
                }
            }

            // ---------------------------------------------------------------
            // DNS shield (system Private DNS)
            // ---------------------------------------------------------------
            item {
                SectionHeader(
                    title = Translations.t("dns_shield_section_title", language),
                    subtitle = Translations.t("dns_shield_section_desc", language)
                )
            }
            item {
                DnsShieldCard(
                    language = language,
                    state = dnsShieldState,
                    consentGranted = dnsShieldConsentGranted,
                    busy = dnsShieldBusy,
                    result = dnsShieldResult,
                    releaseDateLabel = releaseDateLabel,
                    onEnable = {
                        if (dnsShieldConsentGranted) {
                            startDnsShield()
                        } else {
                            showDnsConsentDialog = true
                        }
                    },
                    onOpenSettings = {
                        // Opening the system screen is not consent: the resolver only
                        // becomes BreakFree-managed after the disclosure is accepted.
                        PrivateDnsShield.openSettings(context)
                    },
                    onCopyHost = {
                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                        clipboard?.setPrimaryClip(
                            ClipData.newPlainText(
                                "Private DNS hostname",
                                PrivateDnsShield.PROVIDER_HOST
                            )
                        )
                        Toast.makeText(
                            context,
                            Translations.t("dns_shield_copied", language),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onCheck = { viewModel.refreshDnsShield() },
                    onDisable = { viewModel.disableDnsShield() }
                )
            }

            // ---------------------------------------------------------------
            // Detection rules
            // ---------------------------------------------------------------
            item {
                SoftCard(contentPadding = 16.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconTile(type = CostumeIconType.CHART, tint = GreenCalmLight, size = 38.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = Translations.t("scanner_title", language),
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Pill(
                            text = "${ExplicitContentDetector.getTotalBlockedRulesCount()} " +
                                Translations.t("scanner_rules", language),
                            color = GreenCalmLight,
                            icon = CostumeIconType.CHECK
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = Translations.t("test_scanner_desc", language),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ShieldStatusCard(
    language: AppLanguage,
    isConnected: Boolean,
    onOpenSettings: () -> Unit
) {
    SoftCard(accent = if (isConnected) GreenCalm else SoftGold, contentPadding = 20.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CostumeBadge(
                type = if (isConnected) CostumeIconType.SHIELD_CHECK else CostumeIconType.SHIELD,
                size = 52.dp,
                isCalmGreen = isConnected,
                isGlowing = isConnected,
                isFilled = true
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isConnected) {
                        Translations.t("shield_running", language)
                    } else {
                        Translations.t("shield_stopped", language)
                    },
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Pill(
                    text = if (isConnected) {
                        Translations.t("service_active", language)
                    } else {
                        Translations.t("service_inactive", language)
                    },
                    color = if (isConnected) GreenCalmLight else SoftGold,
                    icon = if (isConnected) CostumeIconType.CHECK else CostumeIconType.INFO
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (language == AppLanguage.ARABIC) {
                "يقرأ درع المراقبة شريط العنوان والنص الظاهر في المتصفحات التي تفتحها، والنص الظاهر في تطبيقات التواصل المدعومة، على جهازك فقط، لرصد المحتوى الإباحي ومنحك نافذة تراجع. لا يُخزَّن هذا النص ولا يُرسَل، ولا يُسجَّل أي انتكاسة دون تأكيدك."
            } else {
                "The Shield reads the address bar and visible text of the browsers you open, and the visible text of the supported social apps, on this device only, to spot explicit content and offer you a step-back window. That text is never stored or uploaded, and nothing is recorded unless you confirm it."
            },
            color = TextSecondary,
            fontSize = 12.5.sp,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isConnected) {
            GhostButton(
                text = Translations.t("shield_manage", language),
                onClick = onOpenSettings,
                icon = CostumeIconType.SETTINGS,
                tint = GreenCalmLight,
                borderColor = GreenCalm.copy(alpha = 0.35f),
                modifier = Modifier.testTag("open_accessibility_settings_btn")
            )
        } else {
            PrimaryButton(
                text = Translations.t("shield_open_settings", language),
                onClick = onOpenSettings,
                icon = CostumeIconType.SHIELD,
                modifier = Modifier.testTag("open_accessibility_settings_btn")
            )
        }
    }
}

@Composable
private fun GuardSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = OnGreenCalm,
            checkedTrackColor = GreenCalm,
            uncheckedThumbColor = WhiteMuted,
            uncheckedTrackColor = BlackElevated
        ),
        modifier = Modifier.testTag(testTag)
    )
}

/**
 * The DNS layer: adult domains are refused before a connection is opened.
 *
 * Two mechanisms can back it and the user does not choose: a Device Owner gets Android's
 * system Private DNS, everyone else gets a VPN that carries DNS queries only. The card
 * says which one is running, because "protected" is not a feeling — it is a system state
 * that can be read and shown.
 */
@Composable
private fun DnsShieldCard(
    language: AppLanguage,
    state: DnsShield.State,
    consentGranted: Boolean,
    busy: Boolean,
    result: PrivateDnsShield.ApplyResult?,
    releaseDateLabel: String,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onOpenSettings: () -> Unit,
    onCopyHost: () -> Unit,
    onCheck: () -> Unit
) {
    val isActive = state.status == DnsShield.Status.ACTIVE
    val isPrivateDns = state.mechanism == DnsShield.Mechanism.PRIVATE_DNS
    val accent = if (isActive) GreenCalm else SoftGold

    SoftCard(accent = accent, contentPadding = 16.dp) {
        Row(verticalAlignment = Alignment.Top) {
            IconTile(
                type = CostumeIconType.LOCKED,
                tint = if (isActive) GreenCalmLight else SoftGold,
                size = 38.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Translations.t("dns_shield_title", language),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Translations.t("dns_shield_body", language),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Pill(
                text = when (state.status) {
                    DnsShield.Status.ACTIVE ->
                        Translations.t("dns_shield_status_active", language)
                    DnsShield.Status.OTHER_HOST ->
                        Translations.t("dns_shield_status_other", language)
                    DnsShield.Status.OFF ->
                        Translations.t("dns_shield_status_off", language)
                },
                color = when (state.status) {
                    DnsShield.Status.ACTIVE -> GreenCalmLight
                    DnsShield.Status.OTHER_HOST -> SoftGold
                    DnsShield.Status.OFF -> TextMuted
                },
                icon = if (isActive) CostumeIconType.CHECK else CostumeIconType.INFO
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isPrivateDns) {
                Translations.t("dns_shield_mechanism_private", language)
            } else {
                Translations.t("dns_shield_mechanism_filter", language)
            },
            color = if (isActive) GreenCalmLight else TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = Translations.t("dns_shield_privacy", language),
            color = TextMuted,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )

        val detail: String? = when {
            state.status == DnsShield.Status.OTHER_HOST ->
                Translations.t("dns_shield_other_host", language)
                    .replace("{host}", state.activeHost ?: "—")

            isActive && state.locked ->
                if (releaseDateLabel.isBlank()) {
                    Translations.t("dns_shield_status_locked", language)
                } else {
                    Translations.t("dns_shield_locked_until", language)
                        .replace("{date}", releaseDateLabel)
                }

            isActive && isPrivateDns -> Translations.t("dns_shield_active_unlocked", language)

            isActive -> Translations.t("dns_filter_active", language)

            consentGranted && isPrivateDns -> Translations.t("dns_shield_do_hint", language)

            consentGranted -> Translations.t("dns_filter_hint", language)

            else -> null
        }

        if (detail != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = detail,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        val errorKey = when (result) {
            PrivateDnsShield.ApplyResult.HOST_NOT_SERVING -> "dns_shield_error_host"
            PrivateDnsShield.ApplyResult.NOT_DEVICE_OWNER -> "dns_shield_error_not_owner"
            PrivateDnsShield.ApplyResult.UNSUPPORTED -> "dns_shield_apply_unsupported"
            PrivateDnsShield.ApplyResult.FAILED -> "dns_shield_error_generic"
            else -> null
        }
        if (errorKey != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Translations.t(errorKey, language),
                color = DangerRed,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when {
            busy -> Text(
                text = Translations.t("dns_shield_busy", language),
                color = SoftGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            // The lock is the point; the detail line says until when.
            isActive && state.locked -> Unit

            isActive -> GhostButton(
                text = Translations.t("dns_shield_disable", language),
                onClick = onDisable,
                icon = CostumeIconType.LOCKED,
                tint = SoftGold,
                borderColor = SoftGold.copy(alpha = 0.35f),
                modifier = Modifier.testTag("dns_shield_disable_btn")
            )

            !consentGranted -> PrimaryButton(
                text = Translations.t("dns_shield_enable", language),
                onClick = onEnable,
                icon = CostumeIconType.SHIELD_CHECK,
                modifier = Modifier.testTag("dns_shield_enable_btn")
            )

            // Device Owner: one tap applies the resolver and locks it for the commitment.
            isPrivateDns -> {
                PrimaryButton(
                    text = Translations.t("dns_shield_enable_lock", language),
                    onClick = onEnable,
                    icon = CostumeIconType.LOCKED,
                    modifier = Modifier.testTag("dns_shield_enable_lock_btn")
                )
                Spacer(modifier = Modifier.height(8.dp))
                GhostButton(
                    text = Translations.t("dns_shield_open_settings", language),
                    onClick = onOpenSettings,
                    icon = CostumeIconType.SETTINGS,
                    tint = TextSecondary,
                    borderColor = WhiteMuted.copy(alpha = 0.25f),
                    modifier = Modifier.testTag("dns_shield_open_settings_btn")
                )
            }

            else -> PrimaryButton(
                text = Translations.t("dns_shield_turn_on", language),
                onClick = onEnable,
                icon = CostumeIconType.SHIELD_CHECK,
                modifier = Modifier.testTag("dns_shield_enable_btn")
            )
        }

        if (state.status == DnsShield.Status.OTHER_HOST) {
            Spacer(modifier = Modifier.height(8.dp))
            GhostButton(
                text = Translations.t("dns_shield_copy_host", language),
                onClick = onCopyHost,
                icon = CostumeIconType.COPY,
                tint = SoftGold,
                borderColor = SoftGold.copy(alpha = 0.35f),
                modifier = Modifier.testTag("dns_shield_copy_btn")
            )
            Spacer(modifier = Modifier.height(8.dp))
            GhostButton(
                text = Translations.t("dns_shield_check", language),
                onClick = onCheck,
                icon = CostumeIconType.REFRESH,
                tint = TextSecondary,
                borderColor = WhiteMuted.copy(alpha = 0.25f),
                modifier = Modifier.testTag("dns_shield_check_btn")
            )
        }
    }
}

/**
 * Disclosure for the DNS shield. It is a third-party network flow, not an on-device
 * check, so it needs its own consent separate from the accessibility monitor.
 */
@Composable
private fun DnsShieldConsentDialog(
    language: AppLanguage,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDecline,
        icon = {
            CostumeBadge(
                type = CostumeIconType.LOCKED,
                size = 44.dp,
                isCalmGreen = true,
                isFilled = true
            )
        },
        title = {
            Text(
                text = Translations.t("dns_shield_consent_title", language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = Translations.t("dns_shield_consent_body", language),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(
                    text = Translations.t("dns_shield_consent_accept", language),
                    color = GreenCalmLight,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(
                    text = Translations.t("dns_shield_consent_cancel", language),
                    color = TextSecondary
                )
            }
        },
        containerColor = BlackElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

/**
 * A compact checklist shown until the content monitor can actually run. Each row either
 * shows a green tick or a button that takes the user to the exact system screen that
 * unblocks it, so nobody has to guess where "Allow restricted settings" lives.
 */
@Composable
private fun SetupChecklistCard(
    language: AppLanguage,
    accessibilityEnabled: Boolean,
    monitoringConsentGranted: Boolean,
    dnsConsentGranted: Boolean,
    dnsActive: Boolean,
    onAllowRestricted: () -> Unit,
    onEnableMonitor: () -> Unit,
    onEnableDns: () -> Unit
) {
    SoftCard(accent = SoftGold, contentPadding = 16.dp) {
        Text(
            text = Translations.t("setup_checklist_title", language),
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = Translations.t("setup_checklist_desc", language),
            color = TextMuted,
            fontSize = 11.5.sp,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = Translations.t("setup_identity_hint", language),
            color = SoftGold,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Step 1: restricted settings (Android 13+, sideloaded installs only).
        if (AccessibilityMonitor.restrictedSettingsApply() && !accessibilityEnabled) {
            ChecklistRow(
                language = language,
                done = false,
                titleKey = "setup_step_restricted",
                actionKey = "setup_open_app_info",
                testTag = "checklist_restricted_btn",
                onAction = onAllowRestricted
            )
            ChecklistDivider()
        }

        // Step 1b: autostart on OEMs that block background services by default (Xiaomi, ...).
        if (AccessibilityMonitor.isOemThatBlocksAutostart()) {
            ChecklistRow(
                language = language,
                done = false,
                titleKey = "setup_step_autostart",
                actionKey = "setup_open_app_info",
                testTag = "checklist_autostart_btn",
                onAction = onAllowRestricted
            )
            ChecklistDivider()
        }

        // Step 2: content monitor.
        ChecklistRow(
            language = language,
            done = accessibilityEnabled && monitoringConsentGranted,
            titleKey = "setup_step_monitor",
            actionKey = if (monitoringConsentGranted) "setup_open_accessibility" else "setup_grant_consent",
            testTag = "checklist_monitor_btn",
            onAction = onEnableMonitor
        )

        ChecklistDivider()

        // Step 3: DNS shield.
        ChecklistRow(
            language = language,
            done = dnsActive,
            titleKey = "setup_step_dns",
            actionKey = if (dnsConsentGranted) "setup_turn_on" else "setup_grant_consent",
            testTag = "checklist_dns_btn",
            onAction = onEnableDns
        )
    }
}

@Composable
private fun ChecklistRow(
    language: AppLanguage,
    done: Boolean,
    titleKey: String,
    actionKey: String,
    testTag: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (done) {
            IconTile(type = CostumeIconType.CHECK, tint = GreenCalmLight, size = 30.dp)
        } else {
            IconTile(type = CostumeIconType.INFO, tint = SoftGold, size = 30.dp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = Translations.t(titleKey, language),
            color = if (done) TextSecondary else TextPrimary,
            fontSize = 13.sp,
            fontWeight = if (done) FontWeight.Normal else FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (!done) {
            TextButton(onClick = onAction, modifier = Modifier.testTag(testTag)) {
                Text(
                    text = Translations.t(actionKey, language),
                    color = SoftGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        } else {
            Pill(
                text = Translations.t("setup_on", language),
                color = GreenCalmLight,
                icon = CostumeIconType.CHECK
            )
        }
    }
}

@Composable
private fun ChecklistDivider() {
    Spacer(modifier = Modifier.height(10.dp))
}

/**
 * Play-policy disclosure shown before the accessibility settings screen is opened.
 * The monitor can see the text of pages the user visits, so it must be consented to
 * explicitly rather than switched on implicitly by installing the app.
 */
@Composable
private fun MonitoringConsentDialog(
    language: AppLanguage,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val isAr = language == AppLanguage.ARABIC
    AlertDialog(
        onDismissRequest = onDecline,
        icon = {
            CostumeBadge(
                type = CostumeIconType.PRIVACY,
                size = 44.dp,
                isCalmGreen = true,
                isFilled = true
            )
        },
        title = {
            Text(
                text = if (isAr) "قبل تفعيل درع المراقبة" else "Before enabling the Shield",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = if (isAr) {
                    "لكي يعمل درع المراقبة، يحتاج التطبيق إلى خدمة إمكانية الوصول. " +
                        "ستقرأ الخدمة النص الظاهر وشريط العنوان في المتصفحات التي تفتحها، " +
                        "والنص الظاهر في يوتيوب وX وإنستغرام وتيك توك وفيسبوك وريديت، " +
                        "على جهازك فقط، لرصد المحتوى الإباحي وتنبيهك. " +
                        "لا يتم تخزين هذا النص ولا تسجيله ولا إرساله إلى أي خادم، " +
                        "ولا تُقرأ تطبيقات أخرى، ولا تُلتقط كلمات المرور. " +
                        "يمكنك إيقاف الخدمة في أي وقت من إعدادات النظام."
                } else {
                    "The Shield needs Android's accessibility service to work. It reads the visible text " +
                        "and address bar of pages in the browsers you open, and the visible text of YouTube, X, " +
                        "Instagram, TikTok, Facebook and Reddit, on this device only, to spot explicit content " +
                        "and offer you a step-back reminder. " +
                        "That text is never stored, logged or uploaded, no other apps are read, and passwords " +
                        "are never captured. You can switch the service off at any time in Android Settings."
                },
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(
                    text = if (isAr) "أوافق، افتح الإعدادات" else "I agree, open settings",
                    color = GreenCalmLight,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(
                    text = if (isAr) "إلغاء" else "Cancel",
                    color = TextSecondary
                )
            }
        },
        containerColor = BlackElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

/**
 * Disclosure for the wider app scope. Browser consent was granted before app coverage
 * existed, so extending the Shield to other apps needs its own explicit agreement.
 * Until it is given, the service keeps reading browsers only.
 */
@Composable
private fun AppsMonitoringConsentDialog(
    language: AppLanguage,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDecline,
        icon = {
            CostumeBadge(
                type = CostumeIconType.SENSE_VISION,
                size = 44.dp,
                isCalmGreen = true,
                isFilled = true
            )
        },
        title = {
            Text(
                text = Translations.t("guard_apps_consent_title", language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = Translations.t("guard_apps_consent_body", language),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = Translations.t("guard_apps_consent_note", language),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = TextMuted
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(
                    text = Translations.t("guard_apps_consent_btn", language),
                    color = GreenCalmLight,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(
                    text = Translations.t("guard_apps_consent_later", language),
                    color = TextSecondary
                )
            }
        },
        containerColor = BlackElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}
