package com.aistudio.breakfree.recovr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.GracePeriodState
import com.aistudio.breakfree.recovr.ui.theme.BlackBorder
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.PureBlack
import com.aistudio.breakfree.recovr.ui.theme.SoftCoral
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary

/**
 * The 120-second step-back window, shown inline at the top of Home.
 *
 * Deliberately has **no button**. The window ends when the user actually leaves the page
 * (the accessibility service resolves it), or when it expires. A "I stepped back" button
 * here was reachable straight from the reminder notification, so it turned the reminder
 * into a one-tap escape hatch: open app, tap, go back to the page. The card now only
 * explains the rule, exactly like the system overlay.
 */
@Composable
fun GracePeriodOverlay(
    gracePeriodState: GracePeriodState,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = gracePeriodState.isGraceActive,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )

        val shape = RoundedCornerShape(26.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SoftCoral.copy(alpha = 0.16f),
                            BlackCard
                        )
                    )
                )
                .border(1.5.dp, SoftCoral.copy(alpha = pulseAlpha), shape)
                .padding(18.dp)
                .testTag("grace_period_overlay")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconTile(type = CostumeIconType.TIMER, tint = SoftCoral, size = 40.dp)
                    Text(
                        text = Translations.t("grace_banner_title", language),
                        color = TextPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SoftCoral)
                        .padding(horizontal = 13.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = gracePeriodState.formattedTime,
                        color = PureBlack,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.testTag("grace_countdown_timer")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { gracePeriodState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SoftCoral,
                trackColor = BlackBorder
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = Translations.t("grace_banner_desc", language),
                color = TextSecondary,
                fontSize = 12.5.sp,
                lineHeight = 19.sp
            )

            if (gracePeriodState.detectedTarget.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    CostumeEmoji(
                        type = CostumeIconType.SHIELD,
                        size = 12.dp,
                        tint = SoftCoral
                    )
                    Text(
                        text = "${Translations.t("grace_detected_target", language)}: ${gracePeriodState.detectedTarget}",
                        color = SoftCoral,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = Translations.t("overlay_stays", language),
                color = TextMuted,
                fontSize = 11.5.sp,
                lineHeight = 17.sp,
                modifier = Modifier.testTag("grace_stays_hint")
            )
        }
    }
}
