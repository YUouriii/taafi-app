package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Translations
import com.example.model.ChatMessage
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElevatedNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.OnEmerald
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.BreakFreeViewModel

data class UrgeActionChip(
    val title: String,
    val isEmergency: Boolean = false,
    val isSosDirect: Boolean = false,
    val accentColor: Color = EmeraldGreen
)

@Composable
fun AiCompanionScreen(
    viewModel: BreakFreeViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size, isAiLoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val actionChips = listOf(
        UrgeActionChip(
            title = Translations.t("ai_chip_intense_urge", language),
            isEmergency = true,
            accentColor = RoseAlert
        ),
        UrgeActionChip(
            title = Translations.t("ai_chip_halt", language),
            accentColor = AmberGold
        ),
        UrgeActionChip(
            title = Translations.t("ai_chip_night_lonely", language),
            accentColor = CyanAccent
        ),
        UrgeActionChip(
            title = Translations.t("ai_chip_why_started", language),
            accentColor = EmeraldGreen
        ),
        UrgeActionChip(
            title = if (language.isRtl) "🧘 تنفس 4-7-8 العاجل" else "🧘 4-7-8 Breathing SOS",
            isSosDirect = true,
            accentColor = RoseAlert
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
    ) {
        // Chat Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateNavy)
                .border(width = 1.dp, color = GlassBorderSubtle)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.2f))
                        .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🧠", fontSize = 18.sp)
                }
                Column {
                    Text(
                        text = Translations.t("ai_title", language),
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = Translations.t("ai_subtitle", language),
                        color = EmeraldLight,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Urge Control Action Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(actionChips) { chip ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (chip.isEmergency) chip.accentColor.copy(alpha = 0.15f)
                            else SlateNavy
                        )
                        .border(
                            1.dp,
                            if (chip.isEmergency) chip.accentColor.copy(alpha = 0.6f)
                            else GlassBorderSubtle,
                            RoundedCornerShape(50)
                        )
                        .clickable {
                            if (chip.isSosDirect) {
                                viewModel.openSosModal()
                            } else {
                                viewModel.sendChatMessage(chip.title)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("ai_quick_chip")
                ) {
                    Text(
                        text = chip.title,
                        color = if (chip.isEmergency) chip.accentColor else chip.accentColor,
                        fontSize = 12.sp,
                        fontWeight = if (chip.isEmergency) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(chatMessages) { message ->
                ChatBubble(message = message)
            }

            if (isAiLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = EmeraldGreen,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (language.isRtl) "المرشد يفكر ويجهز نصيحة سلوكية ملهمة..." else "AI counselor is formulating guidance...",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateNavy)
                .border(width = 1.dp, color = GlassBorderSubtle)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    focusedBorderColor = EmeraldGreen,
                    unfocusedBorderColor = GlassBorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_message_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val text = inputText
                        inputText = ""
                        viewModel.sendChatMessage(text)
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) EmeraldGreen else ElevatedNavy)
                    .testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = Translations.t("ai_send", language),
                    tint = if (inputText.isNotBlank()) OnEmerald else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isFromUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(EmeraldGreen.copy(alpha = 0.2f))
                    .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌱", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(
                    if (isUser) EmeraldGreen.copy(alpha = 0.88f) else SlateNavy
                )
                .border(
                    1.dp,
                    if (isUser) EmeraldGreen else GlassBorderSubtle,
                    RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
                .fillMaxWidth(0.85f)
        ) {
            Text(
                text = message.text,
                color = if (isUser) Color.White else TextPrimary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

