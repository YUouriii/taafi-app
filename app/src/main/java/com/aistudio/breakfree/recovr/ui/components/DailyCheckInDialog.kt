package com.aistudio.breakfree.recovr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.DailyCheckIn
import com.aistudio.breakfree.recovr.ui.theme.BlackBorder
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmContainer
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.OnGreenCalm
import com.aistudio.breakfree.recovr.ui.theme.SoftCoral
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.ui.theme.WhiteSubtleBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyCheckInDialog(
    language: AppLanguage,
    existingCheckIn: DailyCheckIn?,
    onDismiss: () -> Unit,
    onSaveCheckIn: (DailyCheckIn) -> Unit
) {
    val isAr = language == AppLanguage.ARABIC
    val todayDateString = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    var moodScore by remember { mutableIntStateOf(existingCheckIn?.moodScore ?: 3) }
    var urgeFloat by remember { mutableFloatStateOf((existingCheckIn?.urgeIntensity ?: 2).toFloat()) }
    var reflectionText by remember { mutableStateOf(existingCheckIn?.reflectionNotes ?: "") }
    var streakKeptClean by remember { mutableStateOf(existingCheckIn?.streakProtected ?: true) }

    val selectedEmotions = remember {
        mutableStateListOf<String>().apply {
            existingCheckIn?.primaryEmotions?.let { addAll(it) }
        }
    }

    val selectedTriggers = remember {
        mutableStateListOf<String>().apply {
            existingCheckIn?.triggersEncountered?.let { addAll(it) }
        }
    }

    val emotionOptions = if (isAr) {
        listOf("هادئ", "قلق", "وحيد", "ملل", "مجهد", "متحفز", "ممتن", "مضغوط", "واثق")
    } else {
        listOf("Peaceful", "Anxious", "Lonely", "Bored", "Tired", "Motivated", "Grateful", "Stressed", "Confident")
    }

    val triggerOptions = if (isAr) {
        listOf("سهر متأخر", "تواصل اجتماعي", "ضغط العمل", "فراغ", "عزلة", "خلافات", "لا يوجد محفزات")
    } else {
        listOf("Late Night", "Social Media", "Work Stress", "Boredom", "Isolation", "Conflicts", "None")
    }

    val moods = listOf(
        Pair(1, CostumeIconType.MOOD_1),
        Pair(2, CostumeIconType.MOOD_2),
        Pair(3, CostumeIconType.MOOD_3),
        Pair(4, CostumeIconType.MOOD_4),
        Pair(5, CostumeIconType.MOOD_5)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(28.dp))
                .background(BlackCard)
                .border(1.dp, GreenCalm.copy(alpha = 0.22f), RoundedCornerShape(28.dp))
                .padding(20.dp)
                .testTag("daily_checkin_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Translations.t("daily_checkin_title", language),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = todayDateString,
                            color = GreenCalmLight,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Translations.t("close", language),
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Mood
                FieldLabel(Translations.t("mood_question", language))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moods.forEach { (score, costume) ->
                        val isSelected = moodScore == score
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) GreenCalmContainer else BlackElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) GreenCalm.copy(alpha = 0.6f) else WhiteSubtleBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { moodScore = score }
                                .testTag("mood_option_$score"),
                            contentAlignment = Alignment.Center
                        ) {
                            CostumeEmoji(
                                type = costume,
                                size = 25.dp,
                                tint = if (isSelected) GreenCalmLight else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Urge intensity
                val urgeInt = urgeFloat.toInt()
                val urgeColor = if (urgeInt >= 5) SoftCoral else GreenCalmLight

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = Translations.t("urge_question", language),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Pill(
                        text = "$urgeInt / 10",
                        color = urgeColor,
                        filled = true,
                        modifier = Modifier.defaultMinSize(minWidth = 64.dp)
                    )
                }

                Slider(
                    value = urgeFloat,
                    onValueChange = { urgeFloat = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = urgeColor,
                        activeTrackColor = urgeColor,
                        inactiveTrackColor = BlackBorder
                    ),
                    modifier = Modifier.testTag("urge_intensity_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Emotions
                FieldLabel(Translations.t("emotions_label", language))
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emotionOptions.forEach { emotion ->
                        val isSelected = selectedEmotions.contains(emotion)
                        SelectableChip(
                            text = emotion,
                            isSelected = isSelected,
                            accent = GreenCalm,
                            onClick = {
                                if (isSelected) selectedEmotions.remove(emotion)
                                else selectedEmotions.add(emotion)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Triggers
                FieldLabel(Translations.t("triggers_label", language))
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    triggerOptions.forEach { trigger ->
                        val isSelected = selectedTriggers.contains(trigger)
                        SelectableChip(
                            text = trigger,
                            isSelected = isSelected,
                            accent = SoftCoral,
                            onClick = {
                                if (isSelected) selectedTriggers.remove(trigger)
                                else selectedTriggers.add(trigger)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Reflection
                FieldLabel(Translations.t("reflection_label", language))
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = reflectionText,
                    onValueChange = { reflectionText = it },
                    placeholder = {
                        Text(
                            text = Translations.t("reflection_placeholder", language),
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenCalm,
                        unfocusedBorderColor = WhiteSubtleBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = GreenCalmLight
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("reflection_notes_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 6. Streak protection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BlackElevated)
                        .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = Translations.t("streak_safe_toggle", language),
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )

                    Switch(
                        checked = streakKeptClean,
                        onCheckedChange = { streakKeptClean = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnGreenCalm,
                            checkedTrackColor = GreenCalm,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = BlackBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                PrimaryButton(
                    text = Translations.t("save_checkin_btn", language),
                    onClick = {
                        val checkIn = DailyCheckIn(
                            id = existingCheckIn?.id ?: todayDateString,
                            userId = existingCheckIn?.userId ?: "",
                            dateString = todayDateString,
                            moodScore = moodScore,
                            urgeIntensity = urgeInt,
                            primaryEmotions = selectedEmotions.toList(),
                            triggersEncountered = selectedTriggers.toList(),
                            reflectionNotes = reflectionText,
                            streakProtected = streakKeptClean,
                            // Carry the original creation timestamp forward: the
                            // Firestore rules require it to stay stable on update.
                            createdAt = existingCheckIn?.createdAt
                        )
                        onSaveCheckIn(checkIn)
                    },
                    icon = CostumeIconType.CLOUD,
                    modifier = Modifier.testTag("save_checkin_button")
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SelectableChip(
    text: String,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) accent.copy(alpha = 0.16f) else BlackElevated)
            .border(
                1.dp,
                if (isSelected) accent.copy(alpha = 0.55f) else WhiteSubtleBorder,
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) accent else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
