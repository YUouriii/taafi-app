package com.aistudio.breakfree.recovr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.Badge
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmBorder
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.WhiteDim
import com.aistudio.breakfree.recovr.ui.theme.WhiteGlowing
import com.aistudio.breakfree.recovr.ui.theme.WhiteMuted
import com.aistudio.breakfree.recovr.ui.theme.WhiteSubtleBorder

@Composable
fun BadgeItem(
    badge: Badge,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val title = if (language == AppLanguage.ARABIC) badge.titleAr else badge.titleEn
    val desc = if (language == AppLanguage.ARABIC) badge.descriptionAr else badge.descriptionEn

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (badge.unlocked) BlackCard else BlackElevated)
            .border(
                1.dp,
                if (badge.unlocked) GreenCalmBorder else WhiteSubtleBorder.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
            .alpha(if (badge.unlocked) 1f else 0.45f)
            .testTag("badge_item_${badge.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CostumeBadge(
            type = if (badge.unlocked) badge.costumeType else CostumeIconType.LOCKED,
            size = 46.dp,
            isCalmGreen = badge.unlocked,
            isGlowing = badge.unlocked
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = if (badge.unlocked) WhiteGlowing else WhiteDim,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (badge.unlocked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    CostumeEmoji(
                        type = CostumeIconType.CHECK,
                        size = 13.dp,
                        tint = GreenCalmLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = desc,
                color = WhiteMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
