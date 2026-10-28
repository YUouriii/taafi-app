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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkNavyBackground
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.OnEmerald
import com.example.ui.theme.RoseWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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

    val moods = listOf(
        Pair(1, "😞"),
        Pair(2, "😟"),
        Pair(3, "😐"),
        Pair(4, "🙂"),
        Pair(5, "🔥")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .background(NavySurface)
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
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
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = todayDateString,
                            color = CyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Mood Selection
                Text(
                    text = Translations.t("mood_question", language),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    moods.forEach { (score, emoji) ->
                        val isSelected = moodScore == score
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.25f) else NavySurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) EmeraldPrimary else NavyBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { moodScore = score }
                                .testTag("mood_option_$score"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Urge Intensity Slider
                val urgeInt = urgeFloat.toInt()
                val urgeColor = when {
                    urgeInt >= 7 -> RoseWarning
                    urgeInt >= 4 -> AmberAlert
                    urgeInt >= 1 -> CyanAccent
                    else -> EmeraldLight
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = Translations.t("urge_question", language),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(urgeColor.copy(alpha = 0.2f))
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
                        inactiveTrackColor = Color(0xFF1E2B54)
                    ),
                    modifier = Modifier.testTag("urge_intensity_slider")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Primary Emotions
                Text(
                    text = Translations.t("emotions_label", language),
                    color = TextPrimary,
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
                                .background(if (isSelected) CyanAccent.copy(alpha = 0.25f) else NavySurfaceVariant)
                                .border(1.dp, if (isSelected) CyanAccent else NavyBorder, RoundedCornerShape(50))
                                .clickable {
                                    if (isSelected) selectedEmotions.remove(emotion)
                                    else selectedEmotions.add(emotion)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = emotion,
                                color = if (isSelected) Color.White else TextSecondary,
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
                    color = TextPrimary,
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
                                .background(if (isSelected) RoseWarning.copy(alpha = 0.2f) else NavySurfaceVariant)
                                .border(1.dp, if (isSelected) RoseWarning else NavyBorder, RoundedCornerShape(50))
                                .clickable {
                                    if (isSelected) selectedTriggers.remove(trigger)
                                    else selectedTriggers.add(trigger)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = trigger,
                                color = if (isSelected) Color.White else TextSecondary,
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
                    color = TextPrimary,
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
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = NavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
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
                        .background(NavySurfaceVariant)
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
                            checkedThumbColor = EmeraldPrimary,
                            checkedTrackColor = Color(0xFF064E3B)
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
                        containerColor = EmeraldPrimary,
                        contentColor = OnEmerald
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
