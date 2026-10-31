package com.aistudio.breakfree.recovr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.stealth.DisguisePreset
import com.aistudio.breakfree.recovr.stealth.StealthManager
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.SoftGold
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary

/**
 * Dialog for enabling Stealth Mode: pick a disguise and set a 4-6 digit PIN.
 * The PIN is mandatory — it protects the real dashboard once the app is disguised.
 */
@Composable
fun StealthSetupDialog(
    language: AppLanguage,
    onConfirm: (DisguisePreset, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPreset by remember { mutableStateOf(DisguisePreset.CALCULATOR) }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Translations.t("stealth_enable", language),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = Translations.t("stealth_preset", language),
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DisguisePreset.entries.forEach { preset ->
                        val selected = preset == selectedPreset
                        TextButton(onClick = { selectedPreset = preset }) {
                            Text(
                                text = preset.label,
                                color = if (selected) GreenCalmLight else TextMuted,
                                fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold
                                else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text(Translations.t("stealth_set_pin", language)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) confirmPin = it },
                    label = { Text(Translations.t("stealth_confirm_pin", language)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = Translations.t("stealth_unlock_title", language),
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = Translations.t(
                        when (selectedPreset) {
                            DisguisePreset.CALCULATOR -> "stealth_unlock_calc_hint"
                            DisguisePreset.NOTES -> "stealth_unlock_notes_hint"
                            DisguisePreset.WEATHER -> "stealth_unlock_weather_hint"
                        },
                        language
                    ),
                    color = TextMuted,
                    fontSize = 11.5.sp
                )

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = SoftGold, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (pin.length !in 4..6) {
                    error = Translations.t("stealth_pin_required", language)
                    return@TextButton
                }
                if (pin != confirmPin) {
                    error = Translations.t("stealth_pin_mismatch", language)
                    return@TextButton
                }
                onConfirm(selectedPreset, pin)
            }) {
                Text(
                    text = Translations.t("stealth_enable", language),
                    color = GreenCalmLight,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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
