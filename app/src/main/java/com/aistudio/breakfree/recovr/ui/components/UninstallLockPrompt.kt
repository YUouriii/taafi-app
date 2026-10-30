package com.aistudio.breakfree.recovr.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.monitoring.UninstallLock
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.SoftGold
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary

/**
 * The device-administrator state lives in the system, not in Compose, so it is re-read
 * whenever the app returns to the foreground — the user toggles it on a system screen and
 * there is no callback to listen to.
 */
@Composable
fun rememberUninstallLockState(): UninstallLock.State {
    val context = LocalContext.current
    var state by remember { mutableStateOf(UninstallLock.state(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                state = UninstallLock.state(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return state
}

/**
 * Disclosure shown before the system administrator screen.
 *
 * Explains clearly that the 90-day commitment locks BreakFree as an active Device
 * Administrator and guards against deactivation or removal until the 90 days are complete.
 */
@Composable
fun UninstallLockDialog(
    language: AppLanguage,
    onContinue: () -> Unit,
    onShowDeviceOwner: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CostumeBadge(
                type = CostumeIconType.LOCKED,
                size = 44.dp,
                isCalmGreen = false,
                isFilled = true
            )
        },
        title = {
            Text(
                text = Translations.t("uninstall_lock_title", language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = Translations.t("uninstall_lock_body", language),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = onShowDeviceOwner) {
                    Text(
                        text = Translations.t("do_setup_open", language),
                        color = SoftGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text(
                    text = Translations.t("uninstall_lock_confirm", language),
                    color = SoftGold,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = Translations.t("uninstall_lock_later", language),
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
 * Dialog shown when the user taps the Uninstall Lock row while it is ALREADY ACTIVE.
 * Reminds them of their commitment and does not give an easy deactivation shortcut.
 */
@Composable
fun UninstallLockActiveDialog(
    language: AppLanguage,
    dateLabel: String,
    daysLeft: Int,
    onShowDeviceOwner: () -> Unit,
    onDismiss: () -> Unit
) {
    val isOwner = rememberDeviceOwnerActive()

    AlertDialog(
        onDismissRequest = onDismiss,
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
                text = Translations.t("uninstall_lock_active_title", language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = Translations.t("uninstall_lock_active_body", language)
                        .replace("%date%", dateLabel)
                        .replace("%days%", daysLeft.toString()),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (isOwner) {
                    Text(
                        text = "🛡️ " + Translations.t("do_owner_active_title", language),
                        color = com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    TextButton(onClick = onShowDeviceOwner) {
                        Text(
                            text = "💡 " + Translations.t("do_setup_open", language),
                            color = SoftGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = Translations.t("uninstall_lock_stay_strong", language),
                    color = com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = BlackElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

/** Reused by the settings row so the "N days left" line stays in one place. */
fun uninstallLockSubtitle(
    state: UninstallLock.State,
    language: AppLanguage,
    dateLabel: String,
    daysLeft: Int
): String = when (state) {
    UninstallLock.State.ACTIVE ->
        Translations.t("settings_lock_until", language)
            .replace("%date%", dateLabel)
            .replace("%days%", daysLeft.toString())

    UninstallLock.State.COMPLETED -> Translations.t("settings_lock_done", language)
    UninstallLock.State.OFF -> Translations.t("settings_lock_off", language)
}
