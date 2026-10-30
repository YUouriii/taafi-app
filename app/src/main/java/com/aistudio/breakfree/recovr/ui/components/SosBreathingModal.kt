package com.aistudio.breakfree.recovr.ui.components

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
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.ui.theme.BlackBorder
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.BackgroundBottom
import com.aistudio.breakfree.recovr.ui.theme.BackgroundTop
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmBorder
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmDark
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmGlow
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.OnGreenCalm
import com.aistudio.breakfree.recovr.ui.theme.PureBlack
import com.aistudio.breakfree.recovr.ui.theme.WhiteDim
import com.aistudio.breakfree.recovr.ui.theme.WhiteGlow
import com.aistudio.breakfree.recovr.ui.theme.WhiteGlowBorder
import com.aistudio.breakfree.recovr.ui.theme.WhiteGlowing
import com.aistudio.breakfree.recovr.ui.theme.WhiteMuted
import com.aistudio.breakfree.recovr.ui.theme.WhiteSubtleBorder
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
                .background(
                    Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))
                )
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CostumeBadge(
                            type = CostumeIconType.ALERT,
                            size = 36.dp,
                            isGlowing = true,
                            isCalmGreen = false
                        )
                        Text(
                            text = Translations.t("sos_modal_title", language),
                            color = WhiteGlowing,
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
                            tint = WhiteDim
                        )
                    }
                }

                // Tabs: 4-7-8 Breathing, 5-4-3-2-1 Grounding, Urge Surfing Rules
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BlackCard,
                    contentColor = GreenCalm,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GreenCalm
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = Translations.t("sos_breathing_tab", language),
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) GreenCalmLight else WhiteDim
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
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) GreenCalmLight else WhiteDim
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
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 2) GreenCalmLight else WhiteDim
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
                        containerColor = GreenCalm,
                        contentColor = OnGreenCalm
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
            color = GreenCalmLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Expanding breathing orb (3 colors: Black, White Glowing, Green Calm)
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
                            BreathPhase.INHALE -> GreenCalm.copy(alpha = 0.25f)
                            BreathPhase.HOLD -> WhiteGlowing.copy(alpha = 0.2f)
                            BreathPhase.EXHALE -> GreenCalmDark.copy(alpha = 0.35f)
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
                                BreathPhase.INHALE -> listOf(GreenCalmLight, GreenCalm, GreenCalmDark)
                                BreathPhase.HOLD -> listOf(WhiteGlowing, Color(0xAAFFFFFF), PureBlack)
                                BreathPhase.EXHALE -> listOf(GreenCalm, GreenCalmDark, PureBlack)
                            }
                        )
                    )
                    .border(
                        2.dp,
                        if (phase == BreathPhase.HOLD) WhiteGlowing else GreenCalmBorder,
                        CircleShape
                    )
                    .shadow(16.dp, CircleShape, spotColor = if (phase == BreathPhase.HOLD) WhiteGlowing else GreenCalm),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$countdownInPhase",
                        color = if (phase == BreathPhase.HOLD) PureBlack else WhiteGlowing,
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
            color = WhiteGlowing,
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
            color = WhiteDim,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun GroundingTechniquesView(language: AppLanguage) {
    val steps = listOf(
        Pair(CostumeIconType.SENSE_VISION, Translations.t("grounding_step5", language)),
        Pair(CostumeIconType.SENSE_TOUCH, Translations.t("grounding_step4", language)),
        Pair(CostumeIconType.SENSE_HEARING, Translations.t("grounding_step3", language)),
        Pair(CostumeIconType.SENSE_SCENT, Translations.t("grounding_step2", language)),
        Pair(CostumeIconType.SENSE_SIP, Translations.t("grounding_step1", language))
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        steps.forEachIndexed { index, (costume, step) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BlackCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CostumeBadge(
                        type = costume,
                        size = 34.dp,
                        isCalmGreen = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = step,
                        color = WhiteGlowing,
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
            colors = CardDefaults.cardColors(containerColor = BlackCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CostumeBadge(
                        type = CostumeIconType.WAVE,
                        size = 30.dp,
                        isCalmGreen = true
                    )
                    Text(
                        text = Translations.t("rule_15min_title", language),
                        color = WhiteGlowing,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Translations.t("rule_15min_desc", language),
                    color = WhiteDim,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = BlackCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CostumeBadge(
                        type = CostumeIconType.WATER,
                        size = 30.dp,
                        isCalmGreen = true
                    )
                    Text(
                        text = Translations.t("rule_cold_water", language),
                        color = GreenCalmLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Translations.t("rule_cold_desc", language),
                    color = WhiteDim,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }
    }
}
