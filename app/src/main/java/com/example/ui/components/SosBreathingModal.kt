package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.localization.Translations
import com.example.model.AppLanguage
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
import kotlinx.coroutines.delay

enum class BreathPhase {
    INHALE, HOLD, EXHALE
}

@Composable
fun SosBreathingModal(
    isVisible: Boolean,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    if (!isVisible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var selectedTab by remember { mutableIntStateOf(0) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavyBackground.copy(alpha = 0.98f))
                .padding(16.dp)
                .testTag("sos_breathing_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar with Close
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RoseWarning.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🚨", fontSize = 18.sp)
                        }
                        Text(
                            text = Translations.t("sos_modal_title", language),
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("sos_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                // Tabs: 4-7-8 Breathing, 5-4-3-2-1 Grounding, Urge Surfing Rules
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = NavySurface,
                    contentColor = EmeraldPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = EmeraldPrimary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = Translations.t("sos_breathing_tab", language),
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = Translations.t("sos_grounding_tab", language),
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = Translations.t("sos_rules_tab", language),
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (selectedTab) {
                    0 -> BreathingCycleView(language = language)
                    1 -> GroundingTechniquesView(language = language)
                    2 -> UrgeSurfingRulesView(language = language)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Completion Button
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = OnEmerald
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("sos_grounded_button")
                ) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Translations.t("i_am_calm_btn", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun BreathingCycleView(language: AppLanguage) {
    var phase by remember { mutableStateOf(BreathPhase.INHALE) }
    var cycleCount by remember { mutableIntStateOf(1) }
    var countdownInPhase by remember { mutableIntStateOf(4) }
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            // INHALE 4 seconds
            phase = BreathPhase.INHALE
            for (sec in 4 downTo 1) {
                countdownInPhase = sec
                scaleAnim.animateTo(
                    targetValue = 1.35f,
                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                )
            }

            // HOLD 7 seconds
            phase = BreathPhase.HOLD
            for (sec in 7 downTo 1) {
                countdownInPhase = sec
                delay(1000L)
            }

            // EXHALE 8 seconds
            phase = BreathPhase.EXHALE
            for (sec in 8 downTo 1) {
                countdownInPhase = sec
                scaleAnim.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                )
            }

            cycleCount++
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "${Translations.t("cycle", language)} $cycleCount",
            color = CyanAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Expanding breathing orb
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(scaleAnim.value),
            contentAlignment = Alignment.Center
        ) {
            // Outer aura
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        when (phase) {
                            BreathPhase.INHALE -> EmeraldPrimary.copy(alpha = 0.25f)
                            BreathPhase.HOLD -> CyanAccent.copy(alpha = 0.25f)
                            BreathPhase.EXHALE -> Color(0xFF6366F1).copy(alpha = 0.2f)
                        }
                    )
            )

            // Core sphere
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = when (phase) {
                                BreathPhase.INHALE -> listOf(EmeraldLight, EmeraldPrimary, Color(0xFF064E3B))
                                BreathPhase.HOLD -> listOf(Color(0xFF67E8F9), CyanAccent, Color(0xFF083344))
                                BreathPhase.EXHALE -> listOf(Color(0xFFA5B4FC), Color(0xFF6366F1), Color(0xFF1E1B4B))
                            }
                        )
                    )
                    .shadow(16.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$countdownInPhase",
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        val phaseText = when (phase) {
            BreathPhase.INHALE -> Translations.t("breathe_inhale", language)
            BreathPhase.HOLD -> Translations.t("breathe_hold", language)
            BreathPhase.EXHALE -> Translations.t("breathe_exhale", language)
        }

        Text(
            text = phaseText,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (language == AppLanguage.ARABIC)
                "يخفض ضغط الدم ويعيد توازن الجهاز العصبي السمبثاوي خلال 2-3 دورات."
            else
                "Slows heart rate & stimulates parasympathetic relaxation within 2-3 cycles.",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun GroundingTechniquesView(language: AppLanguage) {
    val steps = listOf(
        Translations.t("grounding_step5", language),
        Translations.t("grounding_step4", language),
        Translations.t("grounding_step3", language),
        Translations.t("grounding_step2", language),
        Translations.t("grounding_step1", language)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        steps.forEachIndexed { index, step ->
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${5 - index}",
                            color = EmeraldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = step,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UrgeSurfingRulesView(language: AppLanguage) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🌊 " + Translations.t("rule_15min_title", language),
                    color = CyanAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = Translations.t("rule_15min_desc", language),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "❄️ " + Translations.t("rule_cold_water", language),
                    color = EmeraldLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = Translations.t("rule_cold_desc", language),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }
    }
}
