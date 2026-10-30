package com.aistudio.breakfree.recovr.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.ui.theme.AuroraTeal
import com.aistudio.breakfree.recovr.ui.theme.BackgroundBottom
import com.aistudio.breakfree.recovr.ui.theme.BackgroundTop
import com.aistudio.breakfree.recovr.ui.theme.BlackBorderStrong
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.BlackSurface
import com.aistudio.breakfree.recovr.ui.theme.CardGradientBottom
import com.aistudio.breakfree.recovr.ui.theme.CardGradientTop
import com.aistudio.breakfree.recovr.ui.theme.DangerRed
import com.aistudio.breakfree.recovr.ui.theme.GlassBorderSubtle
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmContainer
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmGlow
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.OnGreenCalm
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.ui.theme.WhiteGlowing
import com.aistudio.breakfree.recovr.ui.theme.WhiteSubtleBorder

/**
 * Shared visual language for the whole app.
 *
 * The rule: screens describe *content*, these components own spacing, radius,
 * border and surface treatment. That is what keeps five screens looking like
 * one product.
 */

val ScreenGutter = 20.dp
val CardRadius = 26.dp
val InnerRadius = 18.dp

/**
 * Letter tracking for headings and labels.
 *
 * Tightening Latin headings looks good, but Arabic is a cursive script: any letter
 * spacing pulls the joined glyphs apart and makes the word read as loose, isolated
 * letters. So tracking is applied only in LTR layouts and collapses to zero in RTL.
 */
@Composable
fun headingTracking(ltr: TextUnit): TextUnit =
    if (LocalLayoutDirection.current == LayoutDirection.Rtl) 0.sp else ltr

/**
 * Ambient background: a deep vertical wash plus two very soft radial glows.
 * Cheaper than a bitmap and stays crisp at any size.
 */
@Composable
fun BreakFreeBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        BackgroundTop,
                        BlackSurface,
                        BackgroundBottom
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GreenCalmGlow, Color.Transparent),
                        center = Offset(size.width * 0.82f, size.height * 0.06f),
                        radius = size.width * 0.75f
                    ),
                    radius = size.width * 0.75f,
                    center = Offset(size.width * 0.82f, size.height * 0.06f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1A5EEAD4), Color.Transparent),
                        center = Offset(size.width * 0.08f, size.height * 0.62f),
                        radius = size.width * 0.7f
                    ),
                    radius = size.width * 0.7f,
                    center = Offset(size.width * 0.08f, size.height * 0.62f)
                )
            }
    ) {
        content()
    }
}

/**
 * The app's card. A subtle top-to-bottom gradient instead of a flat fill makes
 * stacked cards readable without adding hard shadows.
 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(CardRadius)
    val borderColor = accent?.copy(alpha = 0.42f) ?: WhiteSubtleBorder
    val base = Modifier
        .fillMaxWidth()
        .shadow(if (accent != null) 18.dp else 0.dp, shape, clip = false)
        .clip(shape)
        .background(
            Brush.verticalGradient(
                listOf(
                    if (accent != null) accent.copy(alpha = 0.10f) else CardGradientTop,
                    CardGradientBottom
                )
            )
        )
        .border(1.dp, borderColor, shape)
    val clickable = if (onClick != null) base.clickable(onClick = onClick) else base

    Column(
        modifier = modifier
            .then(clickable)
            .padding(contentPadding),
        content = content
    )
}

/** Section heading with an optional text action on the trailing edge. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = headingTracking((-0.2).sp)
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                color = GreenCalmLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

/** Small tinted status chip. */
@Composable
fun Pill(
    text: String,
    color: Color = GreenCalmLight,
    modifier: Modifier = Modifier,
    icon: CostumeIconType? = null,
    filled: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (filled) color.copy(alpha = 0.16f) else color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon.toVector(),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = headingTracking(0.2.sp),
            maxLines = 1,
            softWrap = false
        )
    }
}

/** Rounded-square icon tile, used in rows and stat cards. */
@Composable
fun IconTile(
    type: CostumeIconType,
    tint: Color = GreenCalmLight,
    size: Dp = 42.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.34f))
            .background(tint.copy(alpha = 0.12f))
            .border(1.dp, tint.copy(alpha = 0.22f), RoundedCornerShape(size * 0.34f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = type.toVector(),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.48f)
        )
    }
}

/**
 * One settings line: icon, title, optional subtitle, trailing control.
 * Kept at 56dp minimum height so it is comfortable to tap.
 */
@Composable
fun SettingRow(
    iconType: CostumeIconType,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    tint: Color = GreenCalmLight,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val base = Modifier
        .fillMaxWidth()
        .heightIn(min = 58.dp)
        .clip(RoundedCornerShape(InnerRadius))
    val clickable = if (onClick != null) base.clickable(onClick = onClick) else base

    Row(
        modifier = modifier
            .then(clickable)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(type = iconType, tint = tint, size = 40.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

/** Two-option segmented control (language, units, anything binary). */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BlackElevated)
            .border(1.dp, WhiteSubtleBorder, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(
                targetValue = if (selected) GreenCalmContainer else Color.Transparent,
                animationSpec = tween(220),
                label = "segmentBg"
            )
            val border by animateColorAsState(
                targetValue = if (selected) GreenCalm.copy(alpha = 0.55f) else Color.Transparent,
                animationSpec = tween(220),
                label = "segmentBorder"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) GreenCalmLight else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/** Full-width primary action. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: CostumeIconType? = null,
    enabled: Boolean = true,
    containerColor: Color = GreenCalm,
    contentColor: Color = OnGreenCalm
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (enabled) containerColor else BlackElevated)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon.toVector(),
                contentDescription = null,
                tint = if (enabled) contentColor else TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = if (enabled) contentColor else TextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Full-width low-emphasis action. */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: CostumeIconType? = null,
    tint: Color = TextSecondary,
    borderColor: Color = WhiteSubtleBorder
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Transparent)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon.toVector(),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = tint,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Screen header used by every tab: identity, title, SOS shortcut. */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconType: CostumeIconType? = null,
    onSosClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenGutter, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconType != null) {
            CostumeBadge(type = iconType, size = 40.dp, isCalmGreen = true, isFilled = true, rounded = true)
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = headingTracking((-0.4).sp)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
        }
        if (onSosClick != null) {
            SosChip(onClick = onSosClick)
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
fun SosChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(50))
            .background(DangerRed.copy(alpha = 0.14f))
            .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = CostumeIconType.ALERT.toVector(),
            contentDescription = null,
            tint = Color(0xFFFF8FA3),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "SOS",
            color = Color(0xFFFF8FA3),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )
    }
}

/**
 * The BreakFree mark: a small sprout drawn with paths, so it is genuinely the
 * app's own icon rather than a font glyph.
 */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    glow: Boolean = true
) {
    val shape = RoundedCornerShape(size * 0.34f)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1B3B2E), Color(0xFF0C1C16))
                )
            )
            .border(1.dp, GreenCalm.copy(alpha = 0.45f), shape)
            .then(
                if (glow) Modifier.shadow(14.dp, shape, spotColor = GreenCalm.copy(alpha = 0.55f))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.62f)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val stemBottom = h * 0.94f
            val stemTop = h * 0.34f

            // stem
            drawLine(
                color = Color(0xFF9FF3CE),
                start = Offset(cx, stemBottom),
                end = Offset(cx, stemTop),
                strokeWidth = w * 0.075f,
                cap = StrokeCap.Round
            )

            // left leaf
            val leftLeaf = Path().apply {
                moveTo(cx, h * 0.56f)
                cubicTo(
                    w * 0.10f, h * 0.52f,
                    w * 0.06f, h * 0.20f,
                    w * 0.30f, h * 0.14f
                )
                cubicTo(
                    w * 0.42f, h * 0.34f,
                    w * 0.34f, h * 0.52f,
                    cx, h * 0.56f
                )
                close()
            }
            drawPath(leftLeaf, Brush.linearGradient(listOf(GreenCalm, AuroraTeal)))

            // right leaf (smaller, rotated feel)
            val rightLeaf = Path().apply {
                moveTo(cx, h * 0.44f)
                cubicTo(
                    w * 0.56f, h * 0.38f,
                    w * 0.66f, h * 0.10f,
                    w * 0.92f, h * 0.16f
                )
                cubicTo(
                    w * 0.90f, h * 0.44f,
                    w * 0.72f, h * 0.50f,
                    cx, h * 0.44f
                )
                close()
            }
            drawPath(rightLeaf, Brush.linearGradient(listOf(Color(0xFF9FF3CE), GreenCalm)))
        }
    }
}

/** Hairline divider with generous breathing room. */
@Composable
fun SoftDivider(modifier: Modifier = Modifier, color: Color = BlackBorderStrong) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color.copy(alpha = 0.6f))
    )
}

/** Empty-state block shared by list screens. */
@Composable
fun EmptyState(
    iconType: CostumeIconType,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(GreenCalmContainer)
                .border(1.dp, GreenCalm.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconType.toVector(),
                contentDescription = null,
                tint = GreenCalmLight,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            color = TextMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}
