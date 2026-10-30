package com.aistudio.breakfree.recovr.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.StreakState
import com.aistudio.breakfree.recovr.ui.theme.BlackBorder
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.HeroGradientBottom
import com.aistudio.breakfree.recovr.ui.theme.HeroGradientTop
import com.aistudio.breakfree.recovr.ui.theme.PureBlack
import com.aistudio.breakfree.recovr.ui.theme.SoftGold
import com.aistudio.breakfree.recovr.ui.theme.WhiteDim
import com.aistudio.breakfree.recovr.ui.theme.WhiteGlowing
import com.aistudio.breakfree.recovr.ui.theme.WhiteSubtleBorder
import java.util.Locale

/**
 * The hero of the Home tab: one calm card that answers "how long have I been free?".
 *
 * The outer arc tracks the next milestone, the inner arc tracks dopamine recovery, and
 * the live hh:mm:ss pill makes the passage of time visible without a ticking number
 * dominating the screen.
 */
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

    val phase = streakState.recoveryPhase
    val phaseTitle = if (language == AppLanguage.ARABIC) phase.titleAr else phase.titleEn

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.verticalGradient(listOf(HeroGradientTop, HeroGradientBottom))
            )
            .border(1.dp, GreenCalm.copy(alpha = 0.22f), RoundedCornerShape(30.dp))
            .padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Pill(
            text = phaseTitle,
            color = GreenCalmLight,
            icon = phase.costumeType,
            filled = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(232.dp)
                .testTag("streak_progress_ring")
        ) {
            Canvas(modifier = Modifier.size(212.dp)) {
                val strokeWidth = 12.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2

                drawCircle(
                    color = BlackBorder,
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )

                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(GreenCalm, GreenCalmLight, GreenCalm)
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Inner dopamine track
                val innerStroke = 5.dp.toPx()
                val innerRadius = radius - 16.dp.toPx()
                drawCircle(
                    color = BlackElevated,
                    radius = innerRadius,
                    style = Stroke(width = innerStroke)
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(GreenCalmLight, GreenCalm, GreenCalmLight)
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedDopamine * 360f,
                    useCenter = false,
                    style = Stroke(width = innerStroke, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${streakState.days}",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    color = WhiteGlowing,
                    lineHeight = 58.sp
                )
                Text(
                    text = Translations.t("days", language).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenCalmLight,
                    letterSpacing = headingTracking(3.sp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(PureBlack.copy(alpha = 0.65f))
                        .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%02d", streakState.hours),
                        color = WhiteGlowing,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TimeSeparator()
                    Text(
                        text = String.format(Locale.US, "%02d", streakState.minutes),
                        color = WhiteGlowing,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TimeSeparator()
                    Text(
                        text = String.format(Locale.US, "%02d", streakState.seconds),
                        color = GreenCalmLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                label = Translations.t("dopamine_score", language),
                value = "${streakState.dopamineRecoveryScore}%",
                tint = GreenCalmLight,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "${Translations.t("next_milestone", language)} · ${streakState.nextMilestoneDays} " +
                    Translations.t("days", language),
                value = "${(streakState.milestoneProgress * 100).toInt()}%",
                tint = SoftGold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TimeSeparator() {
    Text(
        text = ":",
        color = GreenCalmLight.copy(alpha = 0.7f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x14000000))
            .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = WhiteDim,
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = tint,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
