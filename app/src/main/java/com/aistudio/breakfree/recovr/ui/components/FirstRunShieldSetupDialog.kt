package com.aistudio.breakfree.recovr.ui.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.monitoring.UninstallLock
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.SoftGold
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.ui.theme.WhiteMuted

/**
 * First-run setup for the three protection layers.
 *
 * It is shown once, after the first sign-in, because the Shield is only useful if the
 * system permissions behind it actually exist. Every row is a real action with its own
 * disclosure; nothing is enabled by opening this dialog.
 *
 * The 90-day uninstall lock is deliberately **not** offered here. It is a commitment, not
 * a setup step, and it stays a separate, explicit choice on the Guard screen.
 */
@Composable
fun FirstRunShieldSetupDialog(
    language: AppLanguage,
    dnsActive: Boolean,
    dnsNeedsVpn: Boolean,
    vpnPermissionIntent: () -> Intent?,
    monitorConsentGranted: Boolean,
    onEnableDns: () -> Unit,
    onSetupMonitor: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    var adminActive by remember { mutableStateOf(UninstallLock.isActive(context)) }

    // The administrator is activated on a system screen, so the state has to be re-read
    // when the user comes back.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                adminActive = UninstallLock.isActive(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val vpnLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (vpnPermissionIntent() == null) onEnableDns()
    }

    val enableDns = {
        if (dnsNeedsVpn) {
            val intent = vpnPermissionIntent()
            if (intent != null) vpnLauncher.launch(intent) else onEnableDns()
        } else {
            onEnableDns()
        }
    }

    AlertDialog(
        onDismissRequest = onDone,
        title = {
            Text(
                text = Translations.t("setup_title", language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = Translations.t("setup_intro", language),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Translations.t("setup_identity_hint", language),
                    color = SoftGold,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                SetupRow(
                    language = language,
                    icon = CostumeIconType.SHIELD_CHECK,
                    tint = GreenCalmLight,
                    titleKey = "setup_dns_title",
                    descriptionKey = "setup_dns_desc",
                    done = dnsActive,
                    actionKey = "setup_enable",
                    testTag = "setup_enable_dns_btn",
                    onAction = enableDns
                )

                SetupDivider()

                SetupRow(
                    language = language,
                    icon = CostumeIconType.SENSE_VISION,
                    tint = SoftGold,
                    titleKey = "setup_monitor_title",
                    descriptionKey = "setup_monitor_desc",
                    done = monitorConsentGranted,
                    actionKey = "setup_set_up",
                    testTag = "setup_monitor_btn",
                    onAction = onSetupMonitor
                )

                SetupDivider()

                SetupRow(
                    language = language,
                    icon = CostumeIconType.LOCKED,
                    tint = TextSecondary,
                    titleKey = "setup_admin_title",
                    descriptionKey = "setup_admin_desc",
                    done = adminActive,
                    actionKey = "setup_activate",
                    testTag = "setup_admin_btn",
                    onAction = { UninstallLock.activate(context) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = Translations.t("setup_note", language),
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDone,
                modifier = Modifier.testTag("setup_done_btn")
            ) {
                Text(
                    text = Translations.t("setup_start", language),
                    color = GreenCalmLight,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = BlackElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

@Composable
private fun SetupRow(
    language: AppLanguage,
    icon: CostumeIconType,
    tint: androidx.compose.ui.graphics.Color,
    titleKey: String,
    descriptionKey: String,
    done: Boolean,
    actionKey: String,
    testTag: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        IconTile(type = icon, tint = tint, size = 34.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = Translations.t(titleKey, language),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = Translations.t(descriptionKey, language),
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        if (done) {
            Pill(
                text = Translations.t("setup_on", language),
                color = GreenCalmLight,
                icon = CostumeIconType.CHECK
            )
        } else {
            TextButton(
                onClick = onAction,
                modifier = Modifier.testTag(testTag)
            ) {
                Text(
                    text = Translations.t(actionKey, language),
                    color = SoftGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SetupDivider() {
    Box(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(WhiteMuted.copy(alpha = 0.12f))
    )
}

/** Opens Android's accessibility screen, where the content monitor is switched on. */
fun openAccessibilitySettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    } catch (e: Exception) {
        // Some ROMs hide the screen; the Guard tab repeats the instructions.
    }
}
