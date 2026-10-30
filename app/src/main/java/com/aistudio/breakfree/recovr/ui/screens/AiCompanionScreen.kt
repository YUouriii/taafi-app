package com.aistudio.breakfree.recovr.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.ChatMessage
import com.aistudio.breakfree.recovr.ui.components.CostumeBadge
import com.aistudio.breakfree.recovr.ui.components.CostumeEmoji
import com.aistudio.breakfree.recovr.ui.components.CostumeIconType
import com.aistudio.breakfree.recovr.ui.components.EmptyState
import com.aistudio.breakfree.recovr.ui.components.Pill
import com.aistudio.breakfree.recovr.ui.components.ScreenGutter
import com.aistudio.breakfree.recovr.ui.components.ScreenHeader
import com.aistudio.breakfree.recovr.ui.components.toVector
import com.aistudio.breakfree.recovr.ui.theme.AuroraTeal
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.OnGreenCalm
import com.aistudio.breakfree.recovr.ui.theme.SoftGold
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.ui.theme.WhiteSubtleBorder
import com.aistudio.breakfree.recovr.viewmodel.BreakFreeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UrgeActionChip(
    val title: String,
    val iconType: CostumeIconType,
    val isEmergency: Boolean = false,
    val isSosDirect: Boolean = false,
    val accentColor: Color = GreenCalm
)

@Composable
fun AiCompanionScreen(
    viewModel: BreakFreeViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val showAiConsentDialog by viewModel.showAiConsentDialog.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    if (showAiConsentDialog) {
        AiConsentDialog(
            language = language,
            onAccept = { viewModel.confirmAiConsentAndSend() },
            onDecline = { viewModel.dismissAiConsent() }
        )
    }

    if (showApiKeyDialog) {
        GeminiApiKeyDialog(
            language = language,
            currentKey = geminiApiKey.orEmpty(),
            onSave = { key ->
                viewModel.setGeminiApiKey(key)
                showApiKeyDialog = false
            },
            onClear = {
                viewModel.setGeminiApiKey(null)
                showApiKeyDialog = false
            },
            onDismiss = { showApiKeyDialog = false }
        )
    }

    val actionChips = listOf(
        UrgeActionChip(
            title = Translations.t("ai_chip_intense_urge", language),
            iconType = CostumeIconType.ALERT,
            isEmergency = true,
            accentColor = Color(0xFFFB7185)
        ),
        UrgeActionChip(
            title = Translations.t("ai_chip_halt", language),
            iconType = CostumeIconType.INFO,
            accentColor = SoftGold
        ),
        UrgeActionChip(
            title = Translations.t("ai_chip_night_lonely", language),
            iconType = CostumeIconType.WAVE,
            accentColor = AuroraTeal
        ),
        UrgeActionChip(
            title = Translations.t("ai_chip_why_started", language),
            iconType = CostumeIconType.SPROUT,
            accentColor = GreenCalmLight
        ),
        UrgeActionChip(
            title = Translations.t("breathing_sos_chip", language),
            iconType = CostumeIconType.MEDITATION,
            isSosDirect = true,
            accentColor = Color(0xFFFB7185)
        )
    )

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = Translations.t("ai_title", language),
            subtitle = Translations.t("ai_subtitle", language),
            iconType = CostumeIconType.BRAIN,
            onSosClick = { viewModel.openSosModal() }
        )

        // Model status & API key configuration row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenGutter, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (geminiApiKey?.isNotBlank() == true) {
                Pill(
                    text = Translations.t("ai_api_key_active", language),
                    color = GreenCalmLight,
                    icon = CostumeIconType.CHECK
                )
            } else {
                Pill(
                    text = Translations.t("ai_api_key_active", language),
                    color = GreenCalmLight,
                    icon = CostumeIconType.BRAIN
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { showApiKeyDialog = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = CostumeIconType.SETTINGS.toVector(),
                    contentDescription = null,
                    tint = if (geminiApiKey?.isNotBlank() == true) GreenCalmLight else TextSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Translations.t("ai_api_key_btn", language),
                    color = if (geminiApiKey?.isNotBlank() == true) GreenCalmLight else TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Guided starting points
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            contentPadding = PaddingValues(horizontal = ScreenGutter),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(actionChips) { chip ->
                QuickActionChip(
                    chip = chip,
                    onClick = {
                        if (chip.isSosDirect) {
                            viewModel.openSosModal()
                        } else {
                            viewModel.sendChatMessage(chip.title)
                        }
                    }
                )
            }
        }

        // Message list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = ScreenGutter),
            contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    EmptyState(
                        iconType = CostumeIconType.CHAT,
                        title = Translations.t("ai_title", language),
                        description = Translations.t("ai_subtitle", language)
                    )
                }
            }

            items(chatMessages) { message ->
                ChatBubble(message = message, language = language)
            }

            if (isAiLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = GreenCalm,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = Translations.t("ai_thinking", language),
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Chat composer (always available — no sign-in required)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = ScreenGutter, end = ScreenGutter, bottom = 12.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(BlackElevated)
                .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(22.dp))
                .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = Translations.t("ai_input_placeholder", language),
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = GreenCalmLight
                ),
                shape = RoundedCornerShape(18.dp),
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_message_input")
            )

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val text = inputText
                        inputText = ""
                        viewModel.sendChatMessage(text)
                    }
                },
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) GreenCalm else BlackCard)
                    .testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = Translations.t("ai_send", language),
                    tint = if (inputText.isNotBlank()) OnGreenCalm else TextMuted,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionChip(chip: UrgeActionChip, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(50))
            .background(chip.accentColor.copy(alpha = if (chip.isEmergency) 0.16f else 0.08f))
            .border(1.dp, chip.accentColor.copy(alpha = 0.32f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp)
            .testTag("ai_quick_chip"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        CostumeEmoji(
            type = chip.iconType,
            size = 15.dp,
            tint = chip.accentColor
        )
        Text(
            text = chip.title,
            color = chip.accentColor,
            fontSize = 12.sp,
            fontWeight = if (chip.isEmergency) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, language: AppLanguage) {
    val isUser = message.isFromUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            CostumeBadge(
                type = CostumeIconType.SPROUT,
                size = 30.dp,
                isCalmGreen = true,
                isFilled = true
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.84f)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 6.dp,
                        bottomEnd = if (isUser) 6.dp else 20.dp
                    )
                )
                .background(if (isUser) GreenCalm else BlackCard)
                .border(
                    1.dp,
                    if (isUser) GreenCalm else WhiteSubtleBorder,
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            if (message.isFallback) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    CostumeEmoji(
                        type = CostumeIconType.INFO,
                        size = 12.dp,
                        tint = SoftGold
                    )
                    Text(
                        text = Translations.t("ai_offline_note", language),
                        color = SoftGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                text = message.text,
                color = if (isUser) OnGreenCalm else TextPrimary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(Date(message.timestampMillis)),
                color = if (isUser) OnGreenCalm.copy(alpha = 0.6f) else TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun GeminiApiKeyDialog(
    language: AppLanguage,
    currentKey: String,
    onSave: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var keyInput by remember { mutableStateOf(currentKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CostumeBadge(
                type = CostumeIconType.BRAIN,
                size = 44.dp,
                isCalmGreen = true,
                isFilled = true
            )
        },
        title = {
            Text(
                text = Translations.t("ai_api_key_dialog_title", language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = Translations.t("ai_api_key_dialog_desc", language),
                    color = TextSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(browserIntent)
                    },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text(
                        text = Translations.t("ai_api_key_get_free", language),
                        color = GreenCalmLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    placeholder = { Text("AIzaSy...", color = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(keyInput) }) {
                Text(
                    text = Translations.t("ai_api_key_save", language),
                    color = GreenCalmLight,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Row {
                if (currentKey.isNotBlank()) {
                    TextButton(onClick = onClear) {
                        Text(Translations.t("ai_api_key_clear", language), color = SoftGold)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(Translations.t("close", language), color = TextSecondary)
                }
            }
        },
        containerColor = BlackElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

@Composable
private fun AiConsentDialog(
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
                text = if (isAr) "قبل أن تبدأ المحادثة" else "Before you start chatting",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = if (isAr) {
                    "رسائلك في المحادثة تُرسل إلى Google (Gemini 1.5 Flash) لتوليد ردود التعافي والدعم المعرفي السلوكي. " +
                        "لا تُرسل سجلات مشاعرك اليومية كاملة، ولا يُرسل أي شيء من متصفحك أو من بيانات المراقبة. " +
                        "لا تكتب اسمك الكامل أو معلومات تعريفية. " +
                        "هذا مرشد دعم وليس بديلاً عن طبيب أو خط طوارئ، وإن كنت في خطر فاتصل بالطوارئ فوراً."
                } else {
                    "Your chat messages are sent to Google (Gemini 1.5 Flash) to generate personalized CBT recovery coaching. " +
                        "Your full daily mood logs are not sent, and nothing from your browser or the content monitor is ever sent. " +
                        "Please don't include your full name or identifying details. " +
                        "This is a support companion, not a clinician or a crisis line — if you are in danger, contact emergency services."
                },
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(
                    text = if (isAr) "أوافق، أرسل" else "I agree, send",
                    color = GreenCalmLight,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(
                    text = if (isAr) "ليس الآن" else "Not now",
                    color = TextSecondary
                )
            }
        },
        containerColor = BlackElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}
