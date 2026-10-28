package com.example.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Translations
import com.example.model.AppLanguage
import com.example.model.GracePeriodState
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackElevated
import com.example.ui.theme.GreenCalm
import com.example.ui.theme.GreenCalmLight
import com.example.ui.theme.OnGreenCalm
import com.example.ui.theme.PureBlack
import com.example.ui.theme.WhiteDim
import com.example.ui.theme.WhiteGlow
import com.example.ui.theme.WhiteGlowBorder
import com.example.ui.theme.WhiteGlowing
import com.example.ui.theme.WhiteSubtleBorder

@Composable
fun GracePeriodOverlay(
    gracePeriodState: GracePeriodState,
    language: AppLanguage,
    onStepBackClicked: () -> Unit,
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
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(20.dp, RoundedCornerShape(20.dp), spotColor = WhiteGlowing)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            BlackElevated,
                            PureBlack
                        )
                    )
                )
                .border(
                    2.dp,
                    WhiteGlowing.copy(alpha = pulseAlpha),
                    RoundedCornerShape(20.dp)
                )
                .padding(18.dp)
                .testTag("grace_period_overlay")
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Header with Pulsing Warning Costume Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CostumeBadge(
                            type = CostumeIconType.TIMER,
                            size = 38.dp,
                            isGlowing = true,
                            isCalmGreen = false
                        )
                        Text(
                            text = Translations.t("grace_banner_title", language),
                            color = WhiteGlowing,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Countdown Timer Pill (Glowing White on Black)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(WhiteGlowing)
                            .shadow(8.dp, RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
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

                // Progress Bar (Glowing White on Dark Track)
                LinearProgressIndicator(
                    progress = { gracePeriodState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = WhiteGlowing,
                    trackColor = BlackBorder
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = Translations.t("grace_banner_desc", language),
                    color = WhiteDim,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Start
                )

                if (gracePeriodState.detectedTarget.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${Translations.t("grace_detected_target", language)}: ${gracePeriodState.detectedTarget}",
                        color = GreenCalmLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button: Step Back to Protect Streak (Green Calm)
                Button(
                    onClick = onStepBackClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenCalm,
                        contentColor = OnGreenCalm
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("grace_step_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Translations.t("grace_step_back_btn", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
