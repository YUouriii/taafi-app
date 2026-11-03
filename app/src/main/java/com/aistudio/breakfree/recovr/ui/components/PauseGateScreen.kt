package com.aistudio.breakfree.recovr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.PauseGateUiState
import com.aistudio.breakfree.recovr.model.PauseOutcome
import com.aistudio.breakfree.recovr.ui.theme.BackgroundBottom
import com.aistudio.breakfree.recovr.ui.theme.BackgroundTop
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.OnGreenCalm
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.WhiteDim
import com.aistudio.breakfree.recovr.ui.theme.WhiteGlowing
import com.aistudio.breakfree.recovr.ui.theme.WhiteMuted
import com.aistudio.breakfree.recovr.ui.theme.WhiteSubtleBorder

/** Each notification covers a third of the gate: 0-3, 3-6 and 6-9 minutes. */
private const val PAUSE_STAGE_MS = 3L * 60L * 1000L

/**
 * The Pause: a full-screen ten-minute gate offered instead of a relapse.
 * Deliberately quiet — no confetti, no score, no pressure. It shows the user's own
 * promise, their numbers and a countdown, then lets them choose. "I still want to" is
 * allowed and is not punished; the app only records what actually happened.
 */
@Composable
fun PauseGateScreen(
    state: PauseGateUiState,
    promise: String,
    cleanDays: Long,
    bestDays: Long,
    urgesSurvived: Int,
    language: AppLanguage,
    onSavePromise: (String) -> Unit,
    onFinish: (PauseOutcome) -> Unit
) {
    if (!state.isVisible) return

    Dialog(
        onDismissRequest = { onFinish(PauseOutcome.ENDED) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var editingPromise by remember { mutableStateOf(false) }
        var promiseInput by remember { mutableStateOf(promise) }

        LaunchedEffect(promise) {
            promiseInput = promise
            if (promise.isNotBlank()) editingPromise = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
                .testTag("pause_gate")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CostumeBadge(
                            type = CostumeIconType.WAVE,
                            size = 38.dp,
                            isCalmGreen = true,
                            isFilled = true
                        )
                        Text(
                            text = Translations.t("pause_gate_title", language),
                            color = WhiteGlowing,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = { onFinish(PauseOutcome.ENDED) },
                        modifier = Modifier.testTag("pause_gate_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Translations.t("close", language),
                            tint = WhiteDim
                        )
                    }
                }

                Text(
                    text = Translations.t("pause_gate_subtitle", language),
                    color = WhiteMuted,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
                )

                Spacer(modifier = Modifier.height(22.dp))

                CountdownRing(state = state, language = language)

                Spacer(modifier = Modifier.height(14.dp))

                // The same wording the notifications carry, shown in the gate too: if
                // notifications are switched off, the reminders still reach the user.
                val elapsed = (state.durationMs - state.remainingMs).coerceAtLeast(0L)
                val stageIndex = (elapsed / PAUSE_STAGE_MS).toInt().coerceIn(0, 2)
                val stageMessage = state.messages.getOrNull(stageIndex).orEmpty()
                if (stageMessage.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = BlackCard),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = stageMessage,
                            color = WhiteGlowing,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 13.dp)
                                .testTag("pause_gate_message")
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Text(
                    text = if (state.remainingMs > 0L) {
                        Translations.t("pause_gate_note", language)
                    } else {
                        Translations.t("pause_gate_done", language)
                    },
                    color = if (state.remainingMs > 0L) WhiteMuted else GreenCalmLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                if (state.isPreparing) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = GreenCalmLight,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = Translations.t("pause_gate_preparing", language),
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                StatsRow(
                    cleanDays = cleanDays,
                    bestDays = bestDays,
                    urgesSurvived = urgesSurvived,
                    language = language
                )

                Spacer(modifier = Modifier.height(20.dp))

                PromiseCard(
                    promise = promise,
                    promiseInput = promiseInput,
                    isEditing = editingPromise || promise.isBlank(),
                    language = language,
                    onInputChange = { promiseInput = it },
                    onStartEditing = { editingPromise = true },
                    onSave = {
                        onSavePromise(promiseInput)
                        editingPromise = false
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onFinish(PauseOutcome.SURVIVED) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenCalm,
                        contentColor = OnGreenCalm
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("pause_gate_ok_button")
                ) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Translations.t("pause_gate_im_ok", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = { onFinish(PauseOutcome.ENDED) },
                    modifier = Modifier.testTag("pause_gate_still_button")
                ) {
                    Text(
                        text = Translations.t("pause_gate_still_want", language),
                        color = WhiteMuted,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = Translations.t("pause_gate_footer", language),
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun CountdownRing(state: PauseGateUiState, language: AppLanguage) {
    val fraction = if (state.durationMs <= 0L) {
        0f
    } else {
        (state.remainingMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
    }
    val totalSeconds = (state.remainingMs / 1000L).coerceAtLeast(0L)
    val timeText = String.format(java.util.Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)

    Box(
        modifier = Modifier.size(224.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 11.dp.toPx()
            drawArc(
                color = WhiteSubtleBorder,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = GreenCalm,
                startAngle = -90f,
                sweepAngle = 360f * fraction,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                color = WhiteGlowing,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (state.remainingMs > 0L) {
                    Translations.t("pause_gate_time_left", language)
                } else {
                    Translations.t("pause_gate_done_short", language)
                },
                color = WhiteMuted,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatsRow(
    cleanDays: Long,
    bestDays: Long,
    urgesSurvived: Int,
    language: AppLanguage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCell(
            value = cleanDays.toString(),
            label = Translations.t("pause_gate_days", language),
            modifier = Modifier.weight(1f)
        )
        StatCell(
            value = bestDays.toString(),
            label = Translations.t("pause_gate_best", language),
            modifier = Modifier.weight(1f)
        )
        StatCell(
            value = urgesSurvived.toString(),
            label = Translations.t("pause_gate_survived", language),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = GreenCalmLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = WhiteMuted,
                fontSize = 10.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PromiseCard(
    promise: String,
    promiseInput: String,
    isEditing: Boolean,
    language: AppLanguage,
    onInputChange: (String) -> Unit,
    onStartEditing: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Translations.t("pause_gate_promise_label", language),
                    color = GreenCalmLight,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (!isEditing && promise.isNotBlank()) {
                    TextButton(onClick = onStartEditing) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = WhiteMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Translations.t("pause_gate_edit", language),
                            color = WhiteMuted,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }

            if (isEditing) {
                OutlinedTextField(
                    value = promiseInput,
                    onValueChange = onInputChange,
                    placeholder = {
                        Text(
                            text = Translations.t("pause_gate_promise_hint", language),
                            color = TextMuted,
                            fontSize = 12.5.sp
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pause_promise_field")
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onSave,
                    enabled = promiseInput.isNotBlank(),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = Translations.t("pause_gate_promise_save", language),
                        color = if (promiseInput.isNotBlank()) GreenCalmLight else TextMuted,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "\u201C$promise\u201D",
                    color = WhiteGlowing,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}
