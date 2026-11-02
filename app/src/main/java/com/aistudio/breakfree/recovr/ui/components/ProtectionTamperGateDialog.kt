package com.aistudio.breakfree.recovr.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import android.content.Context
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.stealth.StealthManager
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.SoftGold
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary

/**
 * Confirmation gate for weakening a protection surface while the 90-day commitment
 * is running: monitoring switches, the DNS shield and the notification stop target.
 *
 * When a Stealth PIN exists, the action requires it — that is the deliberate, slow
 * path the commitment is meant to enforce. Without a PIN it degrades to an explicit
 * acknowledgement dialog, which still stops the accidental/offhand tap.
 */
@Composable
fun ProtectionTamperGateDialog(
    context: Context,
    language: AppLanguage,
    daysRemaining: Int,
    onVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    val hasPin = StealthManager.prefs(context).hasPin()
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Translations.t("tamper_gate_title", language),
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Translations
                        .t(if (hasPin) "tamper_gate_body_pin" else "tamper_gate_body_nopin", language)
                        .replace("{days}", daysRemaining.toString()),
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (hasPin) {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
                        label = { Text(Translations.t("tamper_gate_pin_label", language)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = SoftGold, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (hasPin) {
                    if (StealthManager.verifyPin(context, pin)) {
                        onVerified()
                    } else {
                        error = Translations.t("stealth_lock_wrong", language)
                    }
                } else {
                    onVerified()
                }
            }) {
                Text(
                    text = Translations.t(
                        if (hasPin) "tamper_gate_confirm_pin" else "tamper_gate_turn_off",
                        language
                    ),
                    color = GreenCalmLight,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = Translations.t("cancel", language), color = TextSecondary)
            }
        },
        containerColor = BlackElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}
