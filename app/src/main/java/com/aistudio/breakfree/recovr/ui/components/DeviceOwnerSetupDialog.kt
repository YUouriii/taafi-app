package com.aistudio.breakfree.recovr.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.monitoring.DeviceOwnerProvisioning
import com.aistudio.breakfree.recovr.monitoring.UninstallLock
import com.aistudio.breakfree.recovr.ui.theme.BlackBorder
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.SoftGold
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Device Owner is the only state in which Android disables the uninstall button, and it
 * cannot be granted from inside the app. This dialog documents the two provisioning
 * paths that exist — adb and QR setup — and builds the QR from the real installed APK.
 */
@Composable
fun DeviceOwnerSetupDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isOwner = rememberDeviceOwnerActive()
    val dateLabel = remember(isOwner) {
        UninstallLock.releaseDateLabel(context, language)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(if (isOwner) 0.42f else 0.9f),
            shape = RoundedCornerShape(26.dp),
            color = BlackElevated
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CostumeBadge(
                        type = CostumeIconType.SHIELD_CHECK,
                        size = 44.dp,
                        isCalmGreen = true,
                        isFilled = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = Translations.t("do_setup_title", language),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Translations.t("do_setup_subtitle", language),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isOwner) {
                    DeviceOwnerActiveCard(language = language, dateLabel = dateLabel)
                } else {
                    Text(
                        text = Translations.t("do_setup_intro", language),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    AdbOption(language = language)

                    Spacer(modifier = Modifier.height(16.dp))
                    QrOption(language = language)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = Translations.t("do_close", language),
                            color = GreenCalmLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceOwnerActiveCard(language: AppLanguage, dateLabel: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GreenCalm.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = BlackCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🛡️ " + Translations.t("do_owner_active_title", language),
                color = GreenCalmLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = Translations.t("do_owner_active_body", language)
                    .replace("%date%", dateLabel.ifBlank { "—" }),
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun AdbOption(language: AppLanguage) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val command = remember { DeviceOwnerProvisioning.adbCommand(context) }
    var copied by remember { mutableStateOf(false) }

    OptionCard(title = Translations.t("do_pc_title", language)) {
        StepText(Translations.t("do_pc_step1", language))
        StepText(Translations.t("do_pc_step2", language))
        StepText(Translations.t("do_pc_step3", language))

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlackBorder, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = BlackCard
        ) {
            Text(
                text = command,
                color = GreenCalmLight,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(
            onClick = {
                clipboard.setText(AnnotatedString(command))
                copied = true
            }
        ) {
            Text(
                text = Translations.t(if (copied) "do_copied" else "do_copy", language),
                color = SoftGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        StepText(Translations.t("do_pc_note", language))
    }
}

@Composable
private fun QrOption(language: AppLanguage) {
    val context = LocalContext.current
    var url by remember { mutableStateOf(DeviceOwnerProvisioning.savedApkUrl(context)) }
    var requestedUrl by remember { mutableStateOf<String?>(null) }
    var requestId by remember { mutableStateOf(0) }
    var qr by remember { mutableStateOf<ImageBitmap?>(null) }
    var checksum by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        checksum = withContext(Dispatchers.IO) { DeviceOwnerProvisioning.apkChecksum(context) }
    }

    LaunchedEffect(requestedUrl, requestId) {
        val link = requestedUrl ?: return@LaunchedEffect
        busy = true
        error = null
        val hash = withContext(Dispatchers.IO) {
            checksum ?: DeviceOwnerProvisioning.apkChecksum(context)
        }
        if (hash == null) {
            error = Translations.t("do_qr_failed", language)
            busy = false
            return@LaunchedEffect
        }
        checksum = hash
        val payload = DeviceOwnerProvisioning.qrPayload(context, link, hash)
        qr = withContext(Dispatchers.Default) { renderQr(payload, 720) }
        if (qr == null) {
            error = Translations.t("do_qr_failed", language)
        } else {
            DeviceOwnerProvisioning.saveApkUrl(context, link)
        }
        busy = false
    }

    OptionCard(title = Translations.t("do_qr_title", language)) {
        Text(
            text = "⚠️ " + Translations.t("do_qr_warning", language),
            color = SoftGold,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        StepText(Translations.t("do_qr_step1", language))
        StepText(Translations.t("do_qr_step2", language))
        StepText(Translations.t("do_qr_step3", language))
        StepText(Translations.t("do_qr_step4", language))
        StepText(Translations.t("do_qr_no_account", language))

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = url,
            onValueChange = { url = it.trim() },
            label = {
                Text(
                    text = Translations.t("do_qr_link_label", language),
                    fontSize = 11.sp
                )
            },
            placeholder = { Text("https://…/breakfree.apk", fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        TextButton(
            onClick = {
                val link = url.trim()
                if (!DeviceOwnerProvisioning.isUsableApkUrl(link)) {
                    error = Translations.t("do_qr_invalid", language)
                    qr = null
                } else {
                    requestedUrl = link
                    requestId += 1
                }
            },
            enabled = !busy
        ) {
            Text(
                text = Translations.t("do_qr_generate", language),
                color = if (busy) TextMuted else SoftGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        error?.let {
            Text(text = it, color = SoftGold, fontSize = 11.sp, lineHeight = 16.sp)
        }

        qr?.let { bitmap ->
            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(220.dp)
                    )
                }
            }
            checksum?.let { hash ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = Translations.t("do_qr_checksum", language).replace("%hash%", hash),
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    lineHeight = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = Translations.t("do_qr_note", language),
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun OptionCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = BlackCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = SoftGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun StepText(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

/**
 * The same "read it again on resume" pattern as the admin state: the role is granted on
 * another screen (or another device), so there is no callback to listen to.
 */
@Composable
fun rememberDeviceOwnerActive(): Boolean {
    val context = LocalContext.current
    var active by remember { mutableStateOf(UninstallLock.isDeviceOwner(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                active = UninstallLock.isDeviceOwner(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return active
}

private fun renderQr(content: String, size: Int): androidx.compose.ui.graphics.ImageBitmap? = try {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val pixels = IntArray(size * size)
    for (y in 0 until size) {
        for (x in 0 until size) {
            pixels[y * size + x] = if (matrix[x, y]) {
                android.graphics.Color.BLACK
            } else {
                android.graphics.Color.WHITE
            }
        }
    }
    Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888).asImageBitmap()
} catch (e: Exception) {
    null
}
