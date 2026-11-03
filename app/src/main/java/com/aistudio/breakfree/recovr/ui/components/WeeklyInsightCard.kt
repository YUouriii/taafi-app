package com.aistudio.breakfree.recovr.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.ui.theme.WhiteSubtleBorder

@Composable
fun WeeklyInsightCard(
    daysCleanThisWeek: Long,
    urgesSurvivedThisWeek: Int,
    checkInsThisWeek: Int,
    language: com.aistudio.breakfree.recovr.model.AppLanguage,
    onDismiss: () -> Unit
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
                    text = Translations.t("weekly_insight_title", language),
                    color = GreenCalmLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onDismiss) {
                    Text(
                        text = Translations.t("weekly_insight_dismiss", language),
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = Translations.t("weekly_insight_summary", language)
                    .replace("{days}", daysCleanThisWeek.toString())
                    .replace("{urges}", urgesSurvivedThisWeek.toString())
                    .replace("{checkins}", checkInsThisWeek.toString()),
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (daysCleanThisWeek >= 7) {
                    Translations.t("weekly_insight_good", language)
                } else {
                    Translations.t("weekly_insight_keep", language)
                },
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}
