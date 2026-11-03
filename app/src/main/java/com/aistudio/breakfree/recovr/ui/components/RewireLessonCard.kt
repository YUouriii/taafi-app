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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.RewireLesson
import com.aistudio.breakfree.recovr.model.RewireLessons
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.OnGreenCalm
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.ui.theme.WhiteSubtleBorder

@Composable
fun RewireLessonCard(
    currentIndex: Int,
    completed: Set<Int>,
    language: AppLanguage,
    onMarkComplete: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val lesson: RewireLesson? = RewireLessons.byId(currentIndex)
    val completedCount = completed.size

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
                    text = Translations.t("rewire_title", language),
                    color = GreenCalmLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$completedCount/30",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { completedCount / 30f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = GreenCalm,
                trackColor = WhiteSubtleBorder
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (lesson != null) {
                Text(
                    text = "${lesson.id}. ${lesson.titleFor(language)}",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = lesson.bodyFor(language),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onPrevious, enabled = currentIndex > 1) {
                        Text(
                            text = Translations.t("rewire_prev", language),
                            color = if (currentIndex > 1) TextSecondary else TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    if (!completed.contains(lesson.id)) {
                        Button(
                            onClick = { onMarkComplete(lesson.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenCalm,
                                contentColor = OnGreenCalm
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = Translations.t("rewire_mark_done", language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = Translations.t("rewire_done", language),
                            color = GreenCalmLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    TextButton(onClick = onNext, enabled = currentIndex < 30) {
                        Text(
                            text = Translations.t("rewire_next", language),
                            color = if (currentIndex < 30) TextSecondary else TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Text(
                    text = Translations.t("rewire_complete", language),
                    color = GreenCalmLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
