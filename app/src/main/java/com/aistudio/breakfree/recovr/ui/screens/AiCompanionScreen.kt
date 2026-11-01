package com.aistudio.breakfree.recovr.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import android.util.Log
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.ai.AiModelFetcher
import com.aistudio.breakfree.recovr.ai.AiProviderConfig
import com.aistudio.breakfree.recovr.ai.AiProviderKind
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
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmContainer
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
    val aiConfig by viewModel.aiConfig.collectAsState()
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
        AiProviderDialog(
            language = language,
            current = aiConfig,
            onSave = { config ->
                viewModel.setAiConfig(config)
                showApiKeyDialog = false
            },
            onClear = {
                viewModel.clearAiApiKey()
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

        // Model status & API key configuration row (compact, single line)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenGutter, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (aiConfig.isConfigured) {
                Pill(
                    text = aiConfig.label(language),
                    color = GreenCalmLight,
                    icon = CostumeIconType.CHECK
                )
            } else {
                Pill(
                    text = Translations.t("ai_offline_mode", language),
                    color = TextSecondary,
                    icon = CostumeIconType.BRAIN
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { showApiKeyDialog = true }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = CostumeIconType.SETTINGS.toVector(),
                    contentDescription = null,
                    tint = if (aiConfig.isConfigured) GreenCalmLight else TextSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = Translations.t("ai_api_key_btn", language),
                    color = if (aiConfig.isConfigured) GreenCalmLight else TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Guided starting points (collapsible tray to reclaim chat space)
        var showSuggestions by remember { mutableStateOf(true) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenGutter, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = Translations.t("ai_suggestions", language),
                color = TextMuted,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = { showSuggestions = !showSuggestions }) {
                Text(
                    text = if (showSuggestions)
                        Translations.t("ai_hide_suggestions", language)
                    else
                        Translations.t("ai_show_suggestions", language),
                    color = GreenCalmLight,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showSuggestions) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
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
                item {
                    Spacer(modifier = Modifier.width(ScreenGutter))
                }
            }
        } else {
            Spacer(modifier = Modifier.height(2.dp))
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
                text = parseBoldMarkdown(message.text),
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

/**
 * Renders `**bold**` markdown spans as bold text, which Compose's Text does not do
 * automatically. Works for both LTR and RTL scripts.
 */
private fun parseBoldMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    val regex = Regex("""\*\*(.+?)\*\*""", setOf(RegexOption.DOT_MATCHES_ALL))
    var lastIndex = 0
    for (match in regex.findAll(text)) {
        if (match.range.first > lastIndex) {
            append(text.substring(lastIndex, match.range.first))
        }
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(match.groupValues[1])
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AiProviderDialog(
    language: AppLanguage,
    current: AiProviderConfig,
    onSave: (AiProviderConfig) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var kind by remember { mutableStateOf(current.kind) }
    var modelInput by remember { mutableStateOf(current.model) }
    var baseUrlInput by remember { mutableStateOf(current.baseUrl) }
    var keyInput by remember { mutableStateOf(current.apiKey) }

    var availableModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingModels by remember { mutableStateOf(false) }
    var modelsError by remember { mutableStateOf<String?>(null) }
    var modelsExpanded by remember { mutableStateOf(false) }

    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }

    fun loadModels() {
        val config = AiProviderConfig(
            kind = kind,
            baseUrl = baseUrlInput,
            model = modelInput,
            apiKey = keyInput
        )
        if (keyInput.isBlank() && kind != AiProviderKind.GEMINI) {
            modelsError = Translations.t("ai_key_required", language)
            return
        }
        isLoadingModels = true
        modelsError = null
        scope.launch {
            try {
                val models = AiModelFetcher.fetchModels(config)
                availableModels = models
                if (models.isNotEmpty() && modelInput.isBlank()) {
                    modelInput = models.first()
                }
                if (models.isEmpty()) {
                    modelsError = Translations.t("ai_models_none", language)
                }
            } catch (e: Exception) {
                Log.w("AiProviderDialog", "Model fetch failed: ${e.message}")
                availableModels = emptyList()
                modelsError = Translations.t("ai_models_failed", language)
            } finally {
                isLoadingModels = false
            }
        }
    }

    // Auto-load when the provider changes (including the first composition).
    LaunchedEffect(kind) {
        if (keyInput.isNotBlank() || kind == AiProviderKind.GEMINI) {
            loadModels()
        }
    }

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
                text = Translations.t("ai_provider_dialog_title", language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = Translations.t("ai_provider_dialog_desc", language),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = Translations.t("ai_provider_label", language),
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AiProviderKind.entries.forEach { option ->
                        val selected = option == kind
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) GreenCalmContainer else BlackCard)
                                .border(
                                    1.dp,
                                    if (selected) GreenCalm.copy(alpha = 0.45f) else WhiteSubtleBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { kind = option }
                                .padding(horizontal = 12.dp, vertical = 9.dp)
                                .testTag("ai_provider_${option.key}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.title(language),
                                color = if (selected) GreenCalmLight else TextSecondary,
                                fontSize = 12.5.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (availableModels.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = modelsExpanded,
                        onExpandedChange = { modelsExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = modelInput,
                            onValueChange = { modelInput = it },
                            label = { Text(Translations.t("ai_model_label", language)) },
                            placeholder = { Text(kind.defaultModel, color = TextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelsExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = modelsExpanded,
                            onDismissRequest = { modelsExpanded = false }
                        ) {
                            availableModels.forEach { model ->
                                Text(
                                    text = model,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            modelInput = model
                                            modelsExpanded = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = modelInput,
                        onValueChange = { modelInput = it },
                        label = { Text(Translations.t("ai_model_label", language)) },
                        placeholder = { Text(kind.defaultModel, color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isLoadingModels) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = GreenCalmLight,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Translations.t("ai_loading_models", language),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        TextButton(onClick = { loadModels() }) {
                            Text(
                                text = Translations.t("ai_load_models", language),
                                color = GreenCalmLight,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (modelsError != null) {
                        Text(
                            text = modelsError!!,
                            color = SoftGold,
                            fontSize = 10.5.sp,
                            lineHeight = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = baseUrlInput,
                    onValueChange = { baseUrlInput = it },
                    label = { Text(Translations.t("ai_base_url_label", language)) },
                    placeholder = { Text(kind.defaultBaseUrl, color = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text(Translations.t("ai_api_key_label", language)) },
                    placeholder = { Text("sk-...", color = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_api_key_field")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(kind.keyPageUrl))
                            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(browserIntent)
                        }
                    ) {
                        Text(
                            text = Translations.t("ai_get_key", language),
                            color = GreenCalmLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = {
                            val config = AiProviderConfig(
                                kind = kind,
                                baseUrl = baseUrlInput,
                                model = modelInput,
                                apiKey = keyInput
                            )
                            isTesting = true
                            testResult = null
                            scope.launch {
                                try {
                                    val msg = AiModelFetcher.testConnection(config)
                                    testResult = "✅ $msg"
                                } catch (e: Exception) {
                                    Log.w("AiProviderDialog", "Connection test failed: ${e.message}")
                                    testResult = "❌ ${e.message ?: "Connection failed"}"
                                } finally {
                                    isTesting = false
                                }
                            }
                        },
                        enabled = !isTesting
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                color = GreenCalmLight,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = Translations.t("ai_test_connection", language),
                                color = GreenCalmLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (testResult != null) {
                    Text(
                        text = testResult!!,
                        color = if (testResult!!.startsWith("✅")) GreenCalmLight else SoftGold,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        AiProviderConfig(
                            kind = kind,
                            baseUrl = baseUrlInput,
                            model = modelInput,
                            apiKey = keyInput
                        )
                    )
                }
            ) {
                Text(
                    text = Translations.t("ai_api_key_save", language),
                    color = GreenCalmLight,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Row {
                if (current.apiKey.isNotBlank()) {
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
                    "رسائلك في المحادثة تُرسل إلى مزوّد الذكاء الاصطناعي الذي اخترته في الإعدادات (Gemini أو OpenAI أو غيرها) لتوليد ردود التعافي والدعم المعرفي السلوكي. " +
                        "لا تُرسل سجلات مشاعرك اليومية كاملة، ولا يُرسل أي شيء من متصفحك أو من بيانات المراقبة. " +
                        "لا تكتب اسمك الكامل أو معلومات تعريفية. " +
                        "هذا مرشد دعم وليس بديلاً عن طبيب أو خط طوارئ، وإن كنت في خطر فاتصل بالطوارئ فوراً."
                } else {
                    "Your chat messages are sent to the AI provider you configured in Settings (Gemini, OpenAI, or any other endpoint) to generate personalized CBT recovery coaching. " +
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
