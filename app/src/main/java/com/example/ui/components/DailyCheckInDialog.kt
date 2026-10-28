package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.localization.Translations
import com.example.model.AppLanguage
import com.example.model.DailyCheckIn
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackElevated
import com.example.ui.theme.GreenCalm
import com.example.ui.theme.GreenCalmBorder
import com.example.ui.theme.GreenCalmContainer
import com.example.ui.theme.GreenCalmDark
import com.example.ui.theme.GreenCalmLight
import com.example.ui.theme.OnGreenCalm
import com.example.ui.theme.PureBlack
import com.example.ui.theme.WhiteDim
import com.example.ui.theme.WhiteGlow
import com.example.ui.theme.WhiteGlowBorder
import com.example.ui.theme.WhiteGlowing
import com.example.ui.theme.WhiteMuted
import com.example.ui.theme.WhiteSubtleBorder
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
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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

    // Custom Costume Emojis for 1-5 Moods
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
                .clip(RoundedCornerShape(24.dp))
                .background(BlackCard)
                .border(1.dp, GreenCalmBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("daily_checkin_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Translations.t("daily_checkin_title", language),
                            color = WhiteGlowing,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = todayDateString,
                            color = GreenCalmLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WhiteDim)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Mood Selection with Costume Emojis
                Text(
                    text = Translations.t("mood_question", language),
                    color = WhiteGlowing,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    moods.forEach { (score, costume) ->
                        val isSelected = moodScore == score
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) GreenCalmContainer else BlackElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) GreenCalm else WhiteSubtleBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { moodScore = score }
                                .testTag("mood_option_$score"),
                            contentAlignment = Alignment.Center
                        ) {
                            CostumeEmoji(
                                type = costume,
                                size = 26.dp,
                                tint = if (isSelected) GreenCalmLight else WhiteDim
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Urge Intensity Slider (Strictly Black, White Glowing, Green Calm)
                val urgeInt = urgeFloat.toInt()
                val urgeColor = if (urgeInt >= 5) WhiteGlowing else GreenCalmLight

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = Translations.t("urge_question", language),
                        color = WhiteGlowing,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (urgeInt >= 5) Color(0x33FFFFFF) else GreenCalmContainer)
                            .border(1.dp, if (urgeInt >= 5) WhiteGlowBorder else GreenCalmBorder, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$urgeInt / 10",
                            color = urgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
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

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Primary Emotions
                Text(
                    text = Translations.t("emotions_label", language),
                    color = WhiteGlowing,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emotionOptions.forEach { emotion ->
                        val isSelected = selectedEmotions.contains(emotion)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) GreenCalmContainer else BlackElevated)
                                .border(1.dp, if (isSelected) GreenCalm else WhiteSubtleBorder, RoundedCornerShape(50))
                                .clickable {
                                    if (isSelected) selectedEmotions.remove(emotion)
                                    else selectedEmotions.add(emotion)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = emotion,
                                color = if (isSelected) WhiteGlowing else WhiteDim,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Triggers Encountered
                Text(
                    text = Translations.t("triggers_label", language),
                    color = WhiteGlowing,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    triggerOptions.forEach { trigger ->
                        val isSelected = selectedTriggers.contains(trigger)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) Color(0x33FFFFFF) else BlackElevated)
                                .border(1.dp, if (isSelected) WhiteGlowBorder else WhiteSubtleBorder, RoundedCornerShape(50))
                                .clickable {
                                    if (isSelected) selectedTriggers.remove(trigger)
                                    else selectedTriggers.add(trigger)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = trigger,
                                color = if (isSelected) WhiteGlowing else WhiteDim,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Reflection Notes
                Text(
                    text = Translations.t("reflection_label", language),
                    color = WhiteGlowing,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = reflectionText,
                    onValueChange = { reflectionText = it },
                    placeholder = {
                        Text(
                            text = Translations.t("reflection_placeholder", language),
                            color = WhiteMuted,
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenCalm,
                        unfocusedBorderColor = WhiteSubtleBorder,
                        focusedTextColor = WhiteGlowing,
                        unfocusedTextColor = WhiteGlowing
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("reflection_notes_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 6. Streak Protected Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BlackElevated)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = Translations.t("streak_safe_toggle", language),
                        color = WhiteGlowing,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )

                    Switch(
                        checked = streakKeptClean,
                        onCheckedChange = { streakKeptClean = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WhiteGlowing,
                            checkedTrackColor = GreenCalmDark,
                            uncheckedThumbColor = WhiteMuted,
                            uncheckedTrackColor = BlackBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                Button(
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
                            streakProtected = streakKeptClean
                        )
                        onSaveCheckIn(checkIn)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenCalm,
                        contentColor = OnGreenCalm
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_checkin_button")
                ) {
                    Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Translations.t("save_checkin_btn", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
