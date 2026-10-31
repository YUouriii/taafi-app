package com.aistudio.breakfree.recovr.ui.components

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.BlackSurface
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.ui.theme.WhiteGlowing
import com.aistudio.breakfree.recovr.stealth.DisguisePreset
import com.aistudio.breakfree.recovr.stealth.StealthManager
import kotlin.math.abs

/**
 * The decoy shown while Stealth Mode is locked. Calculator, Notes and Weather all look
 * like ordinary apps, and none of them prints an "unlock" hint — that would defeat the
 * disguise. The way back in is explained once, at setup time, and repeated in Settings:
 *
 *  - Calculator: type the PIN and press `=`, or long-press the display.
 *  - Notes: long-press any note.
 *  - Weather: long-press the temperature.
 */
@Composable
fun StealthEntryScreen(
    language: AppLanguage,
    preset: DisguisePreset = DisguisePreset.CALCULATOR,
    onUnlockRequested: () -> Unit,
    onUnlocked: () -> Unit = onUnlockRequested
) {
    when (preset) {
        DisguisePreset.CALCULATOR -> CalculatorDecoy(
            onUnlockRequested = onUnlockRequested,
            onUnlocked = onUnlocked
        )
        DisguisePreset.NOTES -> NotesDecoy(onUnlockRequested = onUnlockRequested)
        DisguisePreset.WEATHER -> WeatherDecoy(onUnlockRequested = onUnlockRequested)
    }
}

@Composable
private fun CalculatorDecoy(
    onUnlockRequested: () -> Unit,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    var display by remember { mutableStateOf("0") }
    var accumulator by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var fresh by remember { mutableStateOf(true) }

    fun applyDigit(d: String) {
        if (fresh) {
            display = if (d == ".") "0." else d
            fresh = false
        } else {
            if (d == "." && display.contains(".")) return
            display = if (display == "0" && d != ".") d else display + d
        }
    }

    fun applyOp(op: String) {
        val v = display.toDoubleOrNull() ?: 0.0
        if (accumulator == null) accumulator = v
        else if (pendingOp != null && !fresh) {
            val r = compute(accumulator!!, v, pendingOp!!)
            accumulator = r
            display = format(r)
            fresh = true
        }
        pendingOp = op
        fresh = true
    }

    fun equals() {
        val v = display.toDoubleOrNull() ?: 0.0
        val acc = accumulator
        if (acc != null && pendingOp != null) {
            val r = compute(acc, v, pendingOp!!)
            accumulator = null
            pendingOp = null
            display = format(r)
            fresh = true
        }
    }

    fun clear() {
        display = "0"; accumulator = null; pendingOp = null; fresh = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackSurface)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onUnlockRequested() })
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Display — long-press to unlock.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = { onUnlockRequested() })
                    },
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = display,
                    color = WhiteGlowing,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val rows = listOf(
                listOf("C", "÷", "×", "⌫"),
                listOf("7", "8", "9", "-"),
                listOf("4", "5", "6", "+"),
                listOf("1", "2", "3", "="),
                listOf("0", ".", "", "")
            )
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.weight(1f).height(64.dp))
                        } else {
                            CalcButton(
                                label = key,
                                modifier = Modifier.weight(1f),
                                isOp = key in setOf("÷", "×", "-", "+", "="),
                                isClear = key == "C",
                                onClick = {
                                    when (key) {
                                        "C" -> clear()
                                        "⌫" -> if (display.length > 1) display = display.dropLast(1) else display = "0"
                                        "÷", "×", "-", "+" -> applyOp(key)
                                        "=" -> {
                                            // A 4-6 digit "calculation" that matches the
                                            // PIN unlocks the real app. Anything else
                                            // behaves like a normal calculator.
                                            val candidate = display
                                            if (candidate.length in 4..6 &&
                                                candidate.all { it.isDigit() } &&
                                                StealthManager.verifyPin(context, candidate)
                                            ) {
                                                onUnlocked()
                                            } else {
                                                equals()
                                            }
                                        }
                                        else -> applyDigit(key)
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun NotesDecoy(onUnlockRequested: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackSurface)
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onUnlockRequested() }) }
            .padding(16.dp)
    ) {
        Text(
            text = "Notes",
            color = WhiteGlowing,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        listOf("Shopping list", "Meeting notes", "Ideas", "Reminders").forEach { title ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(BlackCard, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(text = title, color = TextPrimary, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "4 notes",
            color = TextMuted.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WeatherDecoy(onUnlockRequested: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackSurface)
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onUnlockRequested() }) }
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "24°C", color = WhiteGlowing, fontSize = 64.sp, fontWeight = FontWeight.Light)
        Text(text = "Partly Cloudy", color = TextSecondary, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "New York", color = TextMuted, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Now" to "24°", "1 PM" to "25°", "2 PM" to "25°", "3 PM" to "23°")
                .forEach { (hour, temp) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = hour, color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = temp, color = TextSecondary, fontSize = 15.sp)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Updated just now",
            color = TextMuted.copy(alpha = 0.5f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CalcButton(
    label: String,
    modifier: Modifier = Modifier,
    isOp: Boolean = false,
    isClear: Boolean = false,
    onClick: () -> Unit
) {
    val bg = when {
        isClear -> com.aistudio.breakfree.recovr.ui.theme.SoftCoral.copy(alpha = 0.25f)
        isOp -> com.aistudio.breakfree.recovr.ui.theme.GreenCalm.copy(alpha = 0.25f)
        else -> BlackCard
    }
    val fg = when {
        isClear -> com.aistudio.breakfree.recovr.ui.theme.SoftCoral
        isOp -> com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
        else -> WhiteGlowing
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .height(64.dp)
            .background(bg, RoundedCornerShape(16.dp))
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = fg, fontSize = 22.sp, fontWeight = FontWeight.Medium)
    }
}

private fun compute(a: Double, b: Double, op: String): Double = when (op) {
    "+" -> a + b
    "-" -> a - b
    "×" -> a * b
    "÷" -> if (b == 0.0) Double.NaN else a / b
    else -> b
}

private fun format(v: Double): String {
    if (v.isNaN() || v.isInfinite()) return "Error"
    val l = v.toLong()
    return if (v == l.toDouble()) l.toString() else "%.6g".format(v)
}
