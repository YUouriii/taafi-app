package com.aistudio.breakfree.recovr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.breakfree.recovr.BuildConfig
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.stealth.StealthManager
import com.aistudio.breakfree.recovr.model.Badge
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.BadgesRepository
import com.aistudio.breakfree.recovr.ui.components.BadgeItem
import com.aistudio.breakfree.recovr.ui.components.BrandMark
import com.aistudio.breakfree.recovr.ui.components.CostumeBadge
import com.aistudio.breakfree.recovr.ui.components.CostumeBadge
import com.aistudio.breakfree.recovr.ui.components.CostumeEmoji
import com.aistudio.breakfree.recovr.ui.components.CostumeIconType
import com.aistudio.breakfree.recovr.ui.components.DailyCheckInDialog
import com.aistudio.breakfree.recovr.ui.components.DailyCheckInProgressCard
import com.aistudio.breakfree.recovr.ui.components.GracePeriodOverlay
import com.aistudio.breakfree.recovr.ui.components.IconTile
import com.aistudio.breakfree.recovr.ui.components.Pill
import com.aistudio.breakfree.recovr.ui.components.ScreenGutter
import com.aistudio.breakfree.recovr.ui.components.SectionHeader
import com.aistudio.breakfree.recovr.ui.components.SoftCard
import com.aistudio.breakfree.recovr.ui.components.SosChip
import com.aistudio.breakfree.recovr.ui.components.StreakProgressRing
import com.aistudio.breakfree.recovr.ui.components.headingTracking
import com.aistudio.breakfree.recovr.ui.components.rememberOverlayPermissionGranted
import com.aistudio.breakfree.recovr.ui.theme.AuroraTeal
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.DangerRed
import com.aistudio.breakfree.recovr.ui.theme.DangerRedDark
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmContainer
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.OnDanger
import com.aistudio.breakfree.recovr.ui.theme.SoftGold
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.viewmodel.BreakFreeViewModel

@Composable
fun DashboardScreen(
    viewModel: BreakFreeViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    // The summary changes once a day, not once a second: collecting it here keeps the
    // per-second countdown from recomposing the whole dashboard.
    val streakSummary by viewModel.streakSummary.collectAsState()
    val gracePeriodState by viewModel.gracePeriodState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val dailyCheckIns by viewModel.dailyCheckIns.collectAsState()
    val isCheckInDialogOpen by viewModel.isCheckInDialogOpen.collectAsState()

    val duressMode by StealthManager.duressMode.collectAsState()

    if (duressMode) {
        // Duress PIN was entered: show a plausible empty dashboard instead of real data.
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = Translations.t("stealth_duress_welcome", language),
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Translations.t("stealth_duress_empty", language),
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    val showVictoryDialog by viewModel.showVictoryDialog.collectAsState()
    val showRelapseDialog by viewModel.showRelapseDialog.collectAsState()

    // Keyed on the list so the dialog always edits the current entry rather than a
    // stale snapshot captured before the last save.
    val todayCheckIn = remember(dailyCheckIns) { viewModel.getTodayCheckIn() }

    // Whether the floating reminder can be drawn at all; decides which of the two
    // reminder cards is used (never both).
    val overlayGranted = rememberOverlayPermissionGranted()

    val badges = remember(streakSummary.days, streakSummary.gracePeriodSavesCount, streakSummary.sosCompletedCount) {
        BadgesRepository.getAllBadges(
            streakDays = streakSummary.days,
            graceSaves = streakSummary.gracePeriodSavesCount,
            sosCompleted = streakSummary.sosCompletedCount
        )
    }

    val displayName = remember(currentUser) {
        currentUser?.displayName?.trim()?.takeIf { it.isNotEmpty() }
            ?: currentUser?.email?.substringBefore("@")
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeHeader(
                language = language,
                displayName = displayName,
                onSosClick = { viewModel.openSosModal() }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = ScreenGutter,
                    end = ScreenGutter,
                    top = 2.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live 120s step-back warning. Only inline when the floating reminder
                // cannot be drawn: with the overlay permission granted the same card is
                // already on screen above every app, and two of them would just be noise.
                if (gracePeriodState.isGraceActive && !overlayGranted) {
                    item {
                        GracePeriodOverlay(
                            gracePeriodState = gracePeriodState,
                            language = language
                        )
                    }
                }

                // The hero: one card that answers "how long have I been free?"
                item {
                    LiveStreakProgressRing(viewModel = viewModel, language = language)
                }

                // Emergency SOS
                item {
                    EmergencySosCard(
                        language = language,
                        onSosClicked = { viewModel.openSosModal() }
                    )
                }

                // Daily mood & urge tracking
                item {
                    DailyCheckInProgressCard(
                        language = language,
                        todayCheckIn = todayCheckIn,
                        recentCheckIns = dailyCheckIns,
                        onOpenCheckInDialog = { viewModel.openCheckInDialog(true) }
                    )
                }

                // Quick stats
                item {
                    SectionHeader(title = Translations.t("quick_stats", language))
                }
                item {
                    QuickStatsGrid(
                        language = language,
                        streakSummary = streakSummary
                    )
                }

                // Developer-only preview of the step-back window. It fabricates a
                // detection, so it must never appear in a release build.
                if (BuildConfig.DEBUG) {
                    item {
                        DebugGraceCard(
                            language = language,
                            onSimulate = { viewModel.simulateGracePeriod() }
                        )
                    }
                }

                // Badges
                item {
                    BadgesSectionHeader(language = language, badges = badges)
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(badges, key = { it.id }) { badge ->
                            BadgeShowcaseCard(badge = badge, language = language)
                        }
                    }
                }

                item {
                    var showAllBadges by remember { mutableStateOf(false) }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(
                            title = Translations.t("badges_title", language),
                            actionLabel = if (showAllBadges) {
                                Translations.t("badges_hide", language)
                            } else {
                                Translations.t("badges_view_all", language)
                            },
                            onAction = { showAllBadges = !showAllBadges }
                        )
                        if (showAllBadges) {
                            Spacer(modifier = Modifier.height(2.dp))
                            badges.forEach { badge ->
                                BadgeItem(badge = badge, language = language)
                            }
                        }
                    }
                }
            }
        }
    }

    // Daily Check-In Dialog
    if (isCheckInDialogOpen) {
        DailyCheckInDialog(
            language = language,
            existingCheckIn = todayCheckIn,
            onDismiss = { viewModel.openCheckInDialog(false) },
            onSaveCheckIn = { checkIn -> viewModel.saveDailyCheckIn(checkIn) }
        )
    }

    // Victory: the user stepped back inside the grace window
    if (showVictoryDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissVictoryDialog() },
            icon = {
                CostumeBadge(
                    type = CostumeIconType.SHIELD_CHECK,
                    size = 46.dp,
                    isGlowing = true,
                    isCalmGreen = true,
                    isFilled = true
                )
            },
            title = {
                Text(
                    text = Translations.t("grace_victory_title", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = Translations.t("grace_victory_msg", language),
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissVictoryDialog() }) {
                    Text(
                        text = Translations.t("continue_journey", language),
                        color = GreenCalmLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = BlackElevated,
            titleContentColor = TextPrimary
        )
    }

    // Honest relapse acknowledgement
    if (showRelapseDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRelapseDialog() },
            icon = {
                CostumeBadge(
                    type = CostumeIconType.SPROUT,
                    size = 46.dp,
                    isCalmGreen = true,
                    isFilled = true
                )
            },
            title = {
                Text(
                    text = Translations.t("grace_relapse_title", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = Translations.t("grace_relapse_msg", language),
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissRelapseDialog() }) {
                    Text(
                        text = Translations.t("close", language),
                        color = GreenCalmLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = BlackElevated,
            titleContentColor = TextPrimary
        )
    }
}

/**
 * Home header: the app mark, a human greeting and a permanent SOS shortcut.
 * It sits outside the list so the emergency affordance is always one tap away.
 */
@Composable
private fun HomeHeader(
    language: AppLanguage,
    displayName: String?,
    onSosClick: () -> Unit
) {
    val greeting = Translations.t("home_greeting", language)
    val title = if (displayName != null) "$greeting, $displayName" else greeting

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenGutter, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrandMark(size = 42.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = headingTracking((-0.3).sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = Translations.t("home_greeting_anon", language),
                color = TextMuted,
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        SosChip(onClick = onSosClick)
    }
}

@Composable
private fun EmergencySosCard(
    language: AppLanguage,
    onSosClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(DangerRed, DangerRedDark)))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(24.dp))
            .clickable(onClick = onSosClicked)
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .testTag("emergency_sos_button"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            CostumeEmoji(
                type = CostumeIconType.ALERT,
                size = 21.dp,
                tint = OnDanger
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = Translations.t("emergency_sos_btn", language),
                color = OnDanger,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = headingTracking(0.6.sp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = Translations.t("sos_subtext", language),
                color = OnDanger.copy(alpha = 0.86f),
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        CostumeEmoji(
            type = CostumeIconType.ARROW,
            size = 18.dp,
            tint = OnDanger.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun QuickStatsGrid(
    language: AppLanguage,
    streakSummary: com.aistudio.breakfree.recovr.model.StreakSummary
) {
    val phaseTitle = if (language == AppLanguage.ARABIC) {
        streakSummary.recoveryPhase.titleAr
    } else {
        streakSummary.recoveryPhase.titleEn
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                title = Translations.t("longest_streak", language),
                value = "${streakSummary.longestStreakSeconds / 86400} " + Translations.t("days", language),
                iconType = CostumeIconType.TROPHY,
                tint = SoftGold,
                modifier = Modifier.weight(1f),
                testTag = "stat_card_longest_streak"
            )
            StatTile(
                title = Translations.t("grace_saves", language),
                value = "${streakSummary.gracePeriodSavesCount}",
                iconType = CostumeIconType.TIMER,
                tint = GreenCalmLight,
                modifier = Modifier.weight(1f),
                testTag = "stat_card_grace_saves"
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                title = Translations.t("sos_sessions", language),
                value = "${streakSummary.sosCompletedCount}",
                iconType = CostumeIconType.MEDITATION,
                tint = AuroraTeal,
                modifier = Modifier.weight(1f),
                testTag = "stat_card_sos_sessions"
            )
            StatTile(
                title = Translations.t("current_phase", language),
                value = phaseTitle,
                iconType = streakSummary.recoveryPhase.costumeType,
                tint = SoftGold,
                modifier = Modifier.weight(1f),
                testTag = "stat_card_current_phase",
                valueSize = 13.sp
            )
        }
    }
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    iconType: CostumeIconType,
    tint: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    valueSize: androidx.compose.ui.unit.TextUnit = 17.sp
) {
    SoftCard(
        modifier = modifier.testTag(testTag),
        contentPadding = 14.dp
    ) {
        IconTile(type = iconType, tint = tint, size = 34.dp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = valueSize,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            color = TextMuted,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            maxLines = 2
        )
    }
}

@Composable
private fun BadgesSectionHeader(language: AppLanguage, badges: List<Badge>) {
    val unlocked = badges.count { it.unlocked }
    SectionHeader(
        title = Translations.t("badges_title", language),
        subtitle = "$unlocked / ${badges.size} " + Translations.t("badges_progress", language)
    )
}

@Composable
private fun BadgeShowcaseCard(
    badge: Badge,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val title = if (language == AppLanguage.ARABIC) badge.titleAr else badge.titleEn
    val unlocked = badge.unlocked

    Column(
        modifier = modifier
            .width(128.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (unlocked) GreenCalmContainer.copy(alpha = 0.55f) else BlackElevated
            )
            .border(
                1.dp,
                if (unlocked) GreenCalm.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.07f),
                RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 12.dp, vertical = 14.dp)
            .testTag("badge_card_${badge.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CostumeBadge(
            type = if (unlocked) badge.costumeType else CostumeIconType.LOCKED,
            size = 46.dp,
            isCalmGreen = unlocked,
            isGlowing = unlocked,
            isFilled = unlocked
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = title,
            color = if (unlocked) TextPrimary else TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Pill(
            text = if (unlocked) {
                Translations.t("badges_unlocked_label", language)
            } else {
                Translations.t("badges_locked", language)
            },
            color = if (unlocked) GreenCalmLight else TextMuted,
            icon = if (unlocked) CostumeIconType.CHECK else CostumeIconType.LOCKED
        )
    }
}

@Composable
private fun DebugGraceCard(
    language: AppLanguage,
    onSimulate: () -> Unit
) {
    SoftCard(contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(type = CostumeIconType.TIMER, tint = SoftGold, size = 38.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Translations.t("simulate_grace_btn", language),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = Translations.t("simulate_grace_desc", language),
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SoftGold.copy(alpha = 0.14f))
                .border(1.dp, SoftGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .clickable(onClick = onSimulate)
                .padding(vertical = 11.dp)
                .testTag("simulate_grace_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (language == AppLanguage.ARABIC) "تجربة" else "Test",
                color = SoftGold,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp
            )
        }
    }
}

/**
 * Collects the per-second [com.aistudio.breakfree.recovr.model.StreakState] locally, so only this card
 * recomposes on every tick instead of the entire dashboard.
 */
@Composable
private fun LiveStreakProgressRing(
    viewModel: BreakFreeViewModel,
    language: AppLanguage
) {
    val streakState by viewModel.streakState.collectAsState()
    StreakProgressRing(
        streakState = streakState,
        language = language
    )
}
