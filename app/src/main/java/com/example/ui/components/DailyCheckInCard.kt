package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Translations
import com.example.model.AppLanguage
import com.example.model.DailyCheckIn
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElevatedNavy
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.OnEmerald
import com.example.ui.theme.RoseWarning
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DailyCheckInProgressCard(
    language: AppLanguage,
    todayCheckIn: DailyCheckIn?,
    recentCheckIns: List<DailyCheckIn>,
    onOpenCheckInDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateNavy),
        shape = RoundedCornerShape(22.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(22.dp))
            .testTag("daily_checkin_progress_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📊", fontSize = 18.sp)
                    }

                    Column {
                        Text(
                            text = Translations.t("daily_checkin_title", language),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = Translations.t("daily_checkin_subtitle", language),
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Today's Status Banner
            if (todayCheckIn != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(EmeraldContainer.copy(alpha = 0.6f))
                        .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = todayCheckIn.moodEmoji, fontSize = 24.sp)
                        Column {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "تم تسجيل مراجعة اليوم بنجاح" else "Today's check-in logged!",
                                color = EmeraldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${if (language == AppLanguage.ARABIC) "شدة الرغبة" else "Peak Urge"}: ${todayCheckIn.urgeIntensity}/10 • ${todayCheckIn.primaryEmotions.take(2).joinToString()}",
                                color = TextPrimary.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = onOpenCheckInDialog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = OnEmerald
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("update_checkin_button")
                    ) {
                        Text(text = Translations.t("edit_today_log", language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onOpenCheckInDialog,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Color(0xFF00363D)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("log_today_button")
                ) {
                    Icon(imageVector = Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Translations.t("log_today_btn", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Recent check-in mini cards
            if (recentCheckIns.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = Translations.t("recent_logs_title", language),
                    color = TextPrimary,
                    fontSize = 12.sp,
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
}

@Composable
private fun RecentCheckInPill(
    log: DailyCheckIn,
    language: AppLanguage
) {
    val isAr = language == AppLanguage.ARABIC
    val urgeColor = Color(log.urgeColorHex)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ElevatedNavy)
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = log.moodEmoji, fontSize = 16.sp)
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
}
