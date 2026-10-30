package com.aistudio.breakfree.recovr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.DailyCheckIn
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmContainer
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.SoftCoral
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.ui.theme.WhiteSubtleBorder

@Composable
fun DailyCheckInProgressCard(
    language: AppLanguage,
    todayCheckIn: DailyCheckIn?,
    recentCheckIns: List<DailyCheckIn>,
    onOpenCheckInDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftCard(
        modifier = modifier.testTag("daily_checkin_progress_card"),
        contentPadding = 18.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CostumeBadge(
                type = CostumeIconType.CHART,
                size = 38.dp,
                isCalmGreen = true,
                isFilled = true
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Translations.t("daily_checkin_title", language),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = Translations.t("daily_checkin_subtitle", language),
                    color = TextMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
            if (todayCheckIn != null) {
                CostumeEmoji(
                    type = todayCheckIn.moodCostumeType,
                    size = 26.dp,
                    tint = GreenCalmLight
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (todayCheckIn != null) {
            TodayStatusBanner(
                checkIn = todayCheckIn,
                language = language,
                onEdit = onOpenCheckInDialog
            )
        } else {
            PrimaryButton(
                text = Translations.t("log_today_btn", language),
                onClick = onOpenCheckInDialog,
                icon = CostumeIconType.CHECK,
                modifier = Modifier.testTag("log_today_button")
            )
        }

        if (recentCheckIns.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = Translations.t("recent_logs_title", language),
                color = TextSecondary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recentCheckIns) { log ->
                    RecentCheckInPill(log = log, language = language)
                }
            }
        }
    }
}

@Composable
private fun TodayStatusBanner(
    checkIn: DailyCheckIn,
    language: AppLanguage,
    onEdit: () -> Unit
) {
    val isAr = language == AppLanguage.ARABIC
    val emotions = checkIn.primaryEmotions.take(2).joinToString(" · ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GreenCalmContainer)
            .border(1.dp, GreenCalm.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isAr) "تم تسجيل مراجعة اليوم" else "Today's check-in is logged",
                color = GreenCalmLight,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = buildString {
                    append(if (isAr) "شدة الرغبة" else "Peak urge")
                    append(" ${checkIn.urgeIntensity}/10")
                    if (emotions.isNotBlank()) {
                        append(" · ")
                        append(emotions)
                    }
                },
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .heightIn(min = 38.dp)
                .clip(RoundedCornerShape(50))
                .background(BlackElevated)
                .border(1.dp, GreenCalm.copy(alpha = 0.35f), RoundedCornerShape(50))
                .clickable(onClick = onEdit)
                .padding(horizontal = 13.dp, vertical = 8.dp)
                .testTag("update_checkin_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = Translations.t("edit_today_log", language),
                color = GreenCalmLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RecentCheckInPill(
    log: DailyCheckIn,
    language: AppLanguage
) {
    val isAr = language == AppLanguage.ARABIC
    val urgeColor = if (log.urgeIntensity >= 5) SoftCoral else GreenCalmLight

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BlackElevated)
            .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CostumeEmoji(
            type = log.moodCostumeType,
            size = 17.dp,
            tint = GreenCalmLight
        )
        Column {
            Text(
                text = log.dateString.substringAfter("-"),
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${if (isAr) "رغبة" else "Urge"} ${log.urgeIntensity}/10",
                color = urgeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
