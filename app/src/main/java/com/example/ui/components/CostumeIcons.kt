package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tsunami
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
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackElevated
import com.example.ui.theme.BlackSurface
import com.example.ui.theme.GreenCalm
import com.example.ui.theme.GreenCalmBorder
import com.example.ui.theme.GreenCalmGlow
import com.example.ui.theme.GreenCalmLight
import com.example.ui.theme.PureBlack
import com.example.ui.theme.WhiteGlow
import com.example.ui.theme.WhiteGlowBorder
import com.example.ui.theme.WhiteGlowing
import com.example.ui.theme.WhiteSubtleBorder

enum class CostumeIconType {
    SHIELD,         // Protection, Guard, Defense
    SPROUT,         // Growth, Clean streak, Renewal
    FLAME,          // Energy, Spark, Motivation
    LIGHTNING,      // Power, Acute detox, Unshakeable
    BRAIN,          // Mind, Neural rewiring, Executive control
    MEDITATION,     // Zen, Calm breathing, Self-improvement
    CROWN,          // Mastery, Freedom, Milestone
    TIMER,          // 120s Grace period, Hourglass
    WAVE,           // Urge surfing, Flow
    ALERT,          // Emergency, SOS, Alert
    FIST,           // Brotherhood, Fist bump, Strength
    TROPHY,         // Longest streak, Achievements
    COMMUNITY,      // Anonymous brotherhood, Groups
    SUNRISE,        // Clarity, Dawning
    FORTRESS,       // One week fortress, Castle
    LOCKED,         // Locked achievement
    CHART,          // Mood log, Insights
    WATER,          // Cold water splash, Drink
    MOOD_1,         // Struggling (Level 1)
    MOOD_2,         // Vulnerable (Level 2)
    MOOD_3,         // Steady / Neutral (Level 3)
    MOOD_4,         // Calm / Clear (Level 4)
    MOOD_5,         // Victorious / Empowered (Level 5)
    SENSE_VISION,   // Look around
    SENSE_TOUCH,    // Touch physical surface
    SENSE_HEARING,  // Listen
    SENSE_SCENT,    // Inhale / Smell
    SENSE_SIP       // Sip cold water
}

fun costumeTypeFromEmoji(emoji: String): CostumeIconType {
    return when {
        emoji.contains("🔥") -> CostumeIconType.FLAME
        emoji.contains("🌱") -> CostumeIconType.SPROUT
        emoji.contains("🛡️") || emoji.contains("🛡") -> CostumeIconType.SHIELD
        emoji.contains("⚡") -> CostumeIconType.LIGHTNING
        emoji.contains("🧠") -> CostumeIconType.BRAIN
        emoji.contains("🧘") -> CostumeIconType.MEDITATION
        emoji.contains("👑") -> CostumeIconType.CROWN
        emoji.contains("⏳") || emoji.contains("⌛") -> CostumeIconType.TIMER
        emoji.contains("🌊") -> CostumeIconType.WAVE
        emoji.contains("🚨") || emoji.contains("⚠️") -> CostumeIconType.ALERT
        emoji.contains("👊") -> CostumeIconType.FIST
        emoji.contains("🏆") -> CostumeIconType.TROPHY
        emoji.contains("🌅") -> CostumeIconType.SUNRISE
        emoji.contains("🏰") -> CostumeIconType.FORTRESS
        emoji.contains("🔒") -> CostumeIconType.LOCKED
        emoji.contains("📊") -> CostumeIconType.CHART
        emoji.contains("❄️") || emoji.contains("💧") -> CostumeIconType.WATER
        emoji.contains("🤝") || emoji.contains("🌐") -> CostumeIconType.COMMUNITY
        emoji.contains("😞") -> CostumeIconType.MOOD_1
        emoji.contains("😟") -> CostumeIconType.MOOD_2
        emoji.contains("😐") -> CostumeIconType.MOOD_3
        emoji.contains("🙂") -> CostumeIconType.MOOD_4
        emoji.contains("👀") -> CostumeIconType.SENSE_VISION
        emoji.contains("✋") -> CostumeIconType.SENSE_TOUCH
        emoji.contains("👂") -> CostumeIconType.SENSE_HEARING
        emoji.contains("👃") -> CostumeIconType.SENSE_SCENT
        emoji.contains("👅") -> CostumeIconType.SENSE_SIP
        else -> CostumeIconType.SPROUT
    }
}

fun CostumeIconType.toVector(): ImageVector {
    return when (this) {
        CostumeIconType.SHIELD -> Icons.Filled.Shield
        CostumeIconType.SPROUT -> Icons.Filled.Spa
        CostumeIconType.FLAME -> Icons.Filled.LocalFireDepartment
        CostumeIconType.LIGHTNING -> Icons.Filled.Bolt
        CostumeIconType.BRAIN -> Icons.Filled.Psychology
        CostumeIconType.MEDITATION -> Icons.Filled.SelfImprovement
        CostumeIconType.CROWN -> Icons.Filled.WorkspacePremium
        CostumeIconType.TIMER -> Icons.Filled.HourglassTop
        CostumeIconType.WAVE -> Icons.Filled.Tsunami
        CostumeIconType.ALERT -> Icons.Filled.CrisisAlert
        CostumeIconType.FIST -> Icons.Filled.SportsMma
        CostumeIconType.TROPHY -> Icons.Filled.EmojiEvents
        CostumeIconType.COMMUNITY -> Icons.Filled.Groups
        CostumeIconType.SUNRISE -> Icons.Filled.WbSunny
        CostumeIconType.FORTRESS -> Icons.Filled.Castle
        CostumeIconType.LOCKED -> Icons.Filled.Lock
        CostumeIconType.CHART -> Icons.Filled.Insights
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
 * CostumeEmoji:
 * Replaces standard native emojis with custom app icons designed strictly
 * in Black, White Glowing, and Green Calm.
 */
@Composable
fun CostumeEmoji(
    type: CostumeIconType,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = GreenCalm,
    isGlowing: Boolean = false
) {
    Icon(
        imageVector = type.toVector(),
        contentDescription = type.name,
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
 * CostumeBadge:
 * A premium circular emblem container with dark black background, glowing white or calm green rim,
 * and custom vector icon inside.
 */
@Composable
fun CostumeBadge(
    type: CostumeIconType,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = (size.value * 0.55f).dp,
    isGlowing: Boolean = false,
    isCalmGreen: Boolean = true,
    isFilled: Boolean = false
) {
    val borderColor = if (isGlowing) WhiteGlowBorder else if (isCalmGreen) GreenCalmBorder else WhiteSubtleBorder
    val iconTint = if (isGlowing) WhiteGlowing else if (isCalmGreen) GreenCalmLight else WhiteGlowing
    val containerBg = if (isFilled) {
        if (isCalmGreen) Color(0xFF032B1F) else Color(0x33FFFFFF)
    } else {
        BlackCard
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerBg)
            .border(1.dp, borderColor, CircleShape)
            .then(
                if (isGlowing) Modifier.shadow(12.dp, CircleShape, spotColor = WhiteGlowing)
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
