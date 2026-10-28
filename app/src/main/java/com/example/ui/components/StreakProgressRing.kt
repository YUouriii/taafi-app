package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Translations
import com.example.model.AppLanguage
import com.example.model.StreakState
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackElevated
import com.example.ui.theme.BlackSurface
import com.example.ui.theme.GreenCalm
import com.example.ui.theme.GreenCalmBorder
import com.example.ui.theme.GreenCalmContainer
import com.example.ui.theme.GreenCalmDark
import com.example.ui.theme.GreenCalmLight
import com.example.ui.theme.PureBlack
import com.example.ui.theme.WhiteDim
import com.example.ui.theme.WhiteGlow
import com.example.ui.theme.WhiteGlowing
import com.example.ui.theme.WhiteMuted
import com.example.ui.theme.WhiteSubtleBorder

@Composable
fun StreakProgressRing(
    streakState: StreakState,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = streakState.milestoneProgress,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "milestoneProgress"
    )

    val animatedDopamine by animateFloatAsState(
        targetValue = streakState.dopamineRecoveryScore / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "dopamineProgress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(BlackCard)
            .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(24.dp))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Recovery Phase Pill with Costume Emoji
        val phase = streakState.recoveryPhase
        val phaseTitle = if (language == AppLanguage.ARABIC) phase.titleAr else phase.titleEn
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(GreenCalmContainer)
                .border(1.dp, GreenCalmBorder, RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CostumeEmoji(type = phase.costumeType, size = 16.dp, tint = GreenCalmLight)
            Text(
                text = phaseTitle,
                color = GreenCalmLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center Vibrant Progress Circle (Black, White Glowing, Green Calm)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .testTag("streak_progress_ring")
        ) {
            Canvas(modifier = Modifier.size(225.dp)) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2

                // Outer Background Track (Pure Black / Deep)
                drawCircle(
                    color = BlackBorder,
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )

                // Outer Milestone Active Gradient Arc (Green Calm & Glowing White)
                val sweepAngle = animatedProgress * 360f
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            GreenCalm,
                            WhiteGlowing,
                            GreenCalmLight,
                            GreenCalm
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Inner Dopamine Arc Track
                val innerStroke = 6.dp.toPx()
                val innerRadius = radius - 18.dp.toPx()
                drawCircle(
                    color = BlackElevated,
                    radius = innerRadius,
                    style = Stroke(width = innerStroke)
                )

                // Inner Dopamine Progress Arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            GreenCalmLight,
                            GreenCalm,
                            GreenCalmLight
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedDopamine * 360f,
                    useCenter = false,
                    style = Stroke(width = innerStroke, cap = StrokeCap.Round)
                )
            }

            // Inside Center Content (Days, Hours, Mins, Secs)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${streakState.days}",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    color = WhiteGlowing,
                    lineHeight = 58.sp
                )
                Text(
                    text = Translations.t("days", language).uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenCalm,
                    letterSpacing = 2.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time ticking hours : mins : secs pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(PureBlack)
                        .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = String.format("%02d", streakState.hours),
                        color = WhiteGlowing,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = ":", color = GreenCalmLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%02d", streakState.minutes),
                        color = WhiteGlowing,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = ":", color = GreenCalmLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%02d", streakState.seconds),
                        color = GreenCalmLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Dopamine & Milestone Progress Card
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Translations.t("dopamine_score", language),
                    color = WhiteDim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${streakState.dopamineRecoveryScore}%",
                    color = WhiteGlowing,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${Translations.t("next_milestone", language)}: ${streakState.nextMilestoneDays} ${Translations.t("days", language)}",
                    color = GreenCalmLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${(streakState.milestoneProgress * 100).toInt()}% " +
                            if (language == AppLanguage.ARABIC) "مكتمل" else "Complete",
                    color = WhiteMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
