package com.aistudio.breakfree.recovr.ui.components

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.stealth.StealthManager
import com.aistudio.breakfree.recovr.ui.theme.BlackSurface
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.ui.theme.WhiteGlowing

/**
 * Lock screen shown when Stealth Mode is on and the app is locked. The real dashboard
 * is only revealed after a correct PIN or a successful biometric prompt.
 */
@Composable
fun StealthLockScreen(
    language: AppLanguage,
    onUnlock: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var enteredPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val biometricAvailable = remember {
        if (activity == null) false
        else {
            val bm = BiometricManager.from(context)
            bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) ==
                BiometricManager.BIOMETRIC_SUCCESS
        }
    }

    // Auto-trigger biometric on first show if available.
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (biometricAvailable && activity != null) {
            showBiometric(activity, language) { success ->
                if (success) {
                    StealthManager.unlock(activity)
                    onUnlock()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackSurface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = Translations.t("stealth_lock_title", language),
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Translations.t("stealth_lock_subtitle", language),
                color = TextMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN dots
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(6) { i ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (i < enteredPin.length) GreenCalm else TextMuted.copy(alpha = 0.3f))
                    )
                }
            }

            if (error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Translations.t("stealth_lock_wrong", language),
                    color = com.aistudio.breakfree.recovr.ui.theme.SoftCoral,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Keypad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "⌫")
            )
            keys.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.size(64.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(BlackSurface)
                                    .border(1.dp, TextMuted.copy(alpha = 0.3f), CircleShape)
                                    .clickable {
                                        when (key) {
                                            "⌫" -> if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                                error = false
                                            }
                                            else -> if (enteredPin.length < 6) {
                                                enteredPin += key
                                                error = false
                                                if (enteredPin.length >= 4) {
                                                    val isDuress = StealthManager.verifyDuressPin(context, enteredPin)
                                                    val isNormal = StealthManager.verifyPin(context, enteredPin)
                                                    if (isDuress || isNormal) {
                                                        StealthManager.unlock(context, duress = isDuress)
                                                        onUnlock()
                                                    } else if (enteredPin.length == 6) {
                                                        error = true
                                                        enteredPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    color = TextPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (biometricAvailable) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                    if (activity != null) {
                        showBiometric(activity, language) { success ->
                            if (success) {
                                StealthManager.unlock(activity)
                                onUnlock()
                            }
                        }
                    }
                }) {
                    Text(
                        text = Translations.t("stealth_lock_biometric", language),
                        color = GreenCalmLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun showBiometric(
    activity: FragmentActivity,
    language: AppLanguage,
    onResult: (Boolean) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onResult(true)
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            // User cancelled or error — don't unlock.
        }
        override fun onAuthenticationFailed() {
            // Wrong biometric — don't unlock.
        }
    })
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(Translations.t("stealth_lock_biometric_title", language))
        .setSubtitle(Translations.t("stealth_lock_biometric_subtitle", language))
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()
    prompt.authenticate(info)
}
