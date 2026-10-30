package com.aistudio.breakfree.recovr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tsunami
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmBorder
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmContainer
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.WhiteGlow
import com.aistudio.breakfree.recovr.ui.theme.WhiteGlowBorder
import com.aistudio.breakfree.recovr.ui.theme.WhiteGlowing
import com.aistudio.breakfree.recovr.ui.theme.WhiteSubtleBorder

/**
 * Every glyph the app can show. These are vector icons, never platform emoji:
 * emoji render differently on every OEM, can't be tinted, and look out of place
 * against a dark, quiet interface.
 */
enum class CostumeIconType {
    // Identity / navigation
    SPROUT,         // Growth, clean streak, renewal
    HOME,           // Dashboard
    SHIELD,         // Protection, Guard
    SHIELD_CHECK,   // Protection verified
    BRAIN,          // Mind, AI companion
    COMMUNITY,      // Anonymous brotherhood
    CHAT,           // Conversation
    SETTINGS,       // App settings

    // Progress
    FLAME,          // Energy, first spark
    LIGHTNING,      // Acute detox
    MEDITATION,     // Calm breathing
    CROWN,          // Mastery
    TROPHY,         // Personal best
    SUNRISE,        // Clarity
    FORTRESS,       // One week fortress
    WAVE,           // Urge surfing
    TIMER,          // Grace period
    CHART,          // Insights
    SPARKLE,        // Achievement
    HEART,          // Care / support

    // Utility
    ALERT,          // Emergency / SOS
    FIST,           // Brotherhood / fist bump
    LOCKED,         // Locked achievement
    CHECK,          // Confirmed
    INFO,           // Information
    PRIVACY,        // Privacy / consent
    NOTIFICATIONS,  // Notifications
    CALENDAR,       // Dates
    LANGUAGE,       // Language
    CLOUD,          // Cloud sync
    USER,           // Account
    ARROW,          // Navigation affordance
    DELETE,         // Destructive action
    COPY,           // Copy a value to the clipboard
    REFRESH,        // Re-check a system state

    // Senses & mood
    WATER,
    MOOD_1,
    MOOD_2,
    MOOD_3,
    MOOD_4,
    MOOD_5,
    SENSE_VISION,
    SENSE_TOUCH,
    SENSE_HEARING,
    SENSE_SCENT,
    SENSE_SIP
}

fun CostumeIconType.toVector(): ImageVector {
    return when (this) {
        CostumeIconType.SPROUT -> Icons.Filled.Spa
        CostumeIconType.HOME -> Icons.Filled.Home
        CostumeIconType.SHIELD -> Icons.Filled.Shield
        CostumeIconType.SHIELD_CHECK -> Icons.Filled.VerifiedUser
        CostumeIconType.BRAIN -> Icons.Filled.Psychology
        CostumeIconType.COMMUNITY -> Icons.Filled.Groups
        CostumeIconType.CHAT -> Icons.Filled.ChatBubble
        CostumeIconType.SETTINGS -> Icons.Filled.Settings

        CostumeIconType.FLAME -> Icons.Filled.LocalFireDepartment
        CostumeIconType.LIGHTNING -> Icons.Filled.Bolt
        CostumeIconType.MEDITATION -> Icons.Filled.SelfImprovement
        CostumeIconType.CROWN -> Icons.Filled.WorkspacePremium
        CostumeIconType.TROPHY -> Icons.Filled.EmojiEvents
        CostumeIconType.SUNRISE -> Icons.Filled.WbSunny
        CostumeIconType.FORTRESS -> Icons.Filled.Castle
        CostumeIconType.WAVE -> Icons.Filled.Tsunami
        CostumeIconType.TIMER -> Icons.Filled.HourglassTop
        CostumeIconType.CHART -> Icons.Filled.Insights
        CostumeIconType.SPARKLE -> Icons.Filled.AutoAwesome
        CostumeIconType.HEART -> Icons.Filled.Favorite

        CostumeIconType.ALERT -> Icons.Filled.CrisisAlert
        CostumeIconType.FIST -> Icons.Filled.SportsMma
        CostumeIconType.LOCKED -> Icons.Filled.Lock
        CostumeIconType.CHECK -> Icons.Filled.Check
        CostumeIconType.INFO -> Icons.Filled.Info
        CostumeIconType.PRIVACY -> Icons.Filled.PrivacyTip
        CostumeIconType.NOTIFICATIONS -> Icons.Filled.Notifications
        CostumeIconType.CALENDAR -> Icons.Filled.CalendarMonth
        CostumeIconType.LANGUAGE -> Icons.Filled.Language
        CostumeIconType.CLOUD -> Icons.Filled.CloudDone
        CostumeIconType.USER -> Icons.Filled.Person
        CostumeIconType.ARROW -> Icons.AutoMirrored.Filled.ArrowForward
        CostumeIconType.DELETE -> Icons.Filled.DeleteOutline
        CostumeIconType.COPY -> Icons.Filled.ContentCopy
        CostumeIconType.REFRESH -> Icons.Filled.Refresh

        CostumeIconType.WATER -> Icons.Filled.LocalDrink
        CostumeIconType.MOOD_1 -> Icons.Filled.SentimentVeryDissatisfied
        CostumeIconType.MOOD_2 -> Icons.Filled.SentimentDissatisfied
        CostumeIconType.MOOD_3 -> Icons.Filled.SentimentNeutral
        CostumeIconType.MOOD_4 -> Icons.Filled.SentimentSatisfied
        CostumeIconType.MOOD_5 -> Icons.Filled.SentimentVerySatisfied
        CostumeIconType.SENSE_VISION -> Icons.Filled.Visibility
        CostumeIconType.SENSE_TOUCH -> Icons.Filled.TouchApp
        CostumeIconType.SENSE_HEARING -> Icons.Filled.Hearing
        CostumeIconType.SENSE_SCENT -> Icons.Filled.Air
        CostumeIconType.SENSE_SIP -> Icons.Filled.LocalDrink
    }
}

/**
 * A tinted, optionally glowing vector icon. This is the app's "emoji": it
 * inherits the theme, scales cleanly and never falls back to a platform glyph.
 */
@Composable
fun CostumeEmoji(
    type: CostumeIconType,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = GreenCalmLight,
    isGlowing: Boolean = false,
    contentDescription: String? = null
) {
    Icon(
        imageVector = type.toVector(),
        contentDescription = contentDescription ?: type.name,
        tint = tint,
        modifier = modifier
            .size(size)
            .then(
                if (isGlowing) Modifier.shadow(8.dp, CircleShape, ambientColor = tint, spotColor = tint)
                else Modifier
            )
    )
}

/**
 * Circular or squircle emblem with a tinted wash, hairline rim and vector glyph.
 */
@Composable
fun CostumeBadge(
    type: CostumeIconType,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = (size.value * 0.5f).dp,
    isGlowing: Boolean = false,
    isCalmGreen: Boolean = true,
    isFilled: Boolean = false,
    rounded: Boolean = false
) {
    val shape = if (rounded) RoundedCornerShape(size * 0.32f) else CircleShape
    val borderColor = when {
        isGlowing -> WhiteGlowBorder
        isCalmGreen -> GreenCalmBorder
        else -> WhiteSubtleBorder
    }
    val iconTint = when {
        isGlowing -> WhiteGlowing
        isCalmGreen -> GreenCalmLight
        else -> WhiteGlowing
    }
    val containerBg = when {
        isFilled && isCalmGreen -> GreenCalmContainer
        isFilled -> WhiteGlow
        else -> BlackCard
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(containerBg)
            .border(1.dp, borderColor, shape)
            .then(
                if (isGlowing) Modifier.shadow(12.dp, shape, spotColor = WhiteGlowing)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = type.toVector(),
            contentDescription = type.name,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}
