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
import androidx.compose.foundation.layout.width
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
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElevatedNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GlassBorderEmerald
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
            .background(SlateNavy)
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(24.dp))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Recovery Phase Pill
        val phase = streakState.recoveryPhase
        val phaseTitle = if (language == AppLanguage.ARABIC) phase.titleAr else phase.titleEn
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(EmeraldGreen.copy(alpha = 0.15f))
                .border(1.dp, GlassBorderEmerald, RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = phase.icon, fontSize = 14.sp)
            Text(
                text = phaseTitle,
                color = EmeraldLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center Vibrant Progress Circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .testTag("streak_progress_ring")
        ) {
            Canvas(modifier = Modifier.size(225.dp)) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2

                // Outer Background Track
                drawCircle(
                    color = MidnightNavy,
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )

                // Outer Milestone Active Gradient Arc
                val sweepAngle = animatedProgress * 360f
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            EmeraldGreen,
                            AmberGold,
                            CyanAccent,
                            EmeraldGreen
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
                    color = ElevatedNavy.copy(alpha = 0.5f),
                    radius = innerRadius,
                    style = Stroke(width = innerStroke)
                )

                // Inner Dopamine Progress Arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            AmberGold,
                            EmeraldGreen,
                            AmberGold
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
                    color = TextPrimary,
                    lineHeight = 58.sp
                )
                Text(
                    text = Translations.t("days", language).uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen,
                    letterSpacing = 2.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time ticking hours : mins : secs pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MidnightNavy.copy(alpha = 0.8f))
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = String.format("%02d", streakState.hours),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = ":", color = AmberGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%02d", streakState.minutes),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = ":", color = AmberGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%02d", streakState.seconds),
                        color = EmeraldLight,
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
                .background(ElevatedNavy)
                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Translations.t("dopamine_score", language),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${streakState.dopamineRecoveryScore}%",
                    color = AmberGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${Translations.t("next_milestone", language)}: ${streakState.nextMilestoneDays} ${Translations.t("days", language)}",
                    color = EmeraldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${(streakState.milestoneProgress * 100).toInt()}% " +
                            if (language == AppLanguage.ARABIC) "مكتمل" else "Complete",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
