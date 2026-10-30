package com.aistudio.breakfree.recovr.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.monitoring.OverlayPermission
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary

/**
 * `SYSTEM_ALERT_WINDOW` can only be granted on a system screen, so the value has to be
 * re-read when the app comes back to the foreground — there is no permission callback
 * for special access.
 */
@Composable
fun rememberOverlayPermissionGranted(): Boolean {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(OverlayPermission.isGranted(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = OverlayPermission.isGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return granted
}

/**
 * Disclosure shown before the system overlay screen is opened. Same reasoning as the
 * accessibility-service consent: drawing over another app is only acceptable when the
 * person using the phone asked for it and knows what will be drawn.
 */
@Composable
fun OverlayPermissionDialog(
    language: AppLanguage,
    onContinue: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                text = Translations.t("overlay_permission_title", language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = Translations.t("overlay_permission_body", language),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text(
                    text = Translations.t("overlay_permission_continue", language),
                    color = GreenCalmLight,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = Translations.t("close", language),
                    color = TextSecondary
                )
            }
        },
        containerColor = BlackElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}
