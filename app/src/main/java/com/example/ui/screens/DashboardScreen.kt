package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Translations
import com.example.model.AppLanguage
import com.example.model.BadgesRepository
import com.example.ui.components.AuthCard
import com.example.ui.components.BadgeItem
import com.example.ui.components.DailyCheckInDialog
import com.example.ui.components.DailyCheckInProgressCard
import com.example.ui.components.GracePeriodOverlay
import com.example.ui.components.StreakProgressRing
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.OnEmerald
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseWarning
import com.example.ui.theme.RoseWarningDark
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.BreakFreeViewModel

@Composable
fun DashboardScreen(
    viewModel: BreakFreeViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val streakState by viewModel.streakState.collectAsState()
    val gracePeriodState by viewModel.gracePeriodState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val dailyCheckIns by viewModel.dailyCheckIns.collectAsState()
    val isCheckInDialogOpen by viewModel.isCheckInDialogOpen.collectAsState()

    val showVictoryDialog by viewModel.showVictoryDialog.collectAsState()
    val showRelapseDialog by viewModel.showRelapseDialog.collectAsState()
    val showResetConfirmationDialog by viewModel.showResetConfirmationDialog.collectAsState()
    val showSetDateDialog by viewModel.showSetDateDialog.collectAsState()

    val todayCheckIn = viewModel.getTodayCheckIn()

    val badges = remember(streakState.days, streakState.gracePeriodSavesCount, streakState.sosCompletedCount) {
        BadgesRepository.getAllBadges(
            streakDays = streakState.days,
            graceSaves = streakState.gracePeriodSavesCount,
            sosCompleted = streakState.sosCompletedCount
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live 120s Grace Period Warning Overlay (appears when triggered)
            if (gracePeriodState.isGraceActive) {
                item {
                    GracePeriodOverlay(
                        gracePeriodState = gracePeriodState,
                        language = language,
                        onStepBackClicked = { viewModel.stepBackInGracePeriod() }
                    )
                }
            }

            // Google Sign-In & Cloud Sync Card
            item {
                AuthCard(
                    currentUser = currentUser,
                    language = language,
                    onAuthSuccess = { viewModel.onAuthSuccess() },
                    onSignOut = { viewModel.onSignOut() }
                )
            }

            // Streak Progress Ring Card
            item {
                StreakProgressRing(
                    streakState = streakState,
                    language = language
                )
            }

            // High-Impact EMERGENCY SOS Button
            item {
                EmergencySosCard(
                    language = language,
                    onSosClicked = { viewModel.openSosModal() }
                )
            }

            // Daily Mood & Urge Tracking Feature Card
            item {
                DailyCheckInProgressCard(
                    language = language,
                    todayCheckIn = todayCheckIn,
                    recentCheckIns = dailyCheckIns,
                    onOpenCheckInDialog = { viewModel.openCheckInDialog(true) }
                )
            }

            // Quick Stats 2x2 Grid Cards
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModernStatCard(
                            title = Translations.t("longest_streak", language),
                            value = "${streakState.longestStreakSeconds / 86400} " + Translations.t("days", language),
                            icon = "🏆",
                            accentColor = AmberGold,
                            modifier = Modifier.weight(1f),
                            testTag = "stat_card_longest_streak"
                        )
                        ModernStatCard(
                            title = Translations.t("grace_saves", language),
                            value = "${streakState.gracePeriodSavesCount}",
                            icon = "⏳",
                            accentColor = EmeraldGreen,
                            modifier = Modifier.weight(1f),
                            testTag = "stat_card_grace_saves"
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModernStatCard(
                            title = Translations.t("sos_sessions", language),
                            value = "${streakState.sosCompletedCount}",
                            icon = "🧘",
                            accentColor = CyanAccent,
                            modifier = Modifier.weight(1f),
                            testTag = "stat_card_sos_sessions"
                        )
                        val phaseTitle = if (language == AppLanguage.ARABIC) streakState.recoveryPhase.titleAr else streakState.recoveryPhase.titleEn
                        ModernStatCard(
                            title = Translations.t("current_phase", language),
                            value = phaseTitle,
                            icon = streakState.recoveryPhase.icon,
                            accentColor = AmberGold,
                            modifier = Modifier.weight(1f),
                            testTag = "stat_card_current_phase",
                            isValueSmall = true
                        )
                    }
                }
            }

            // Test Simulation of 120s Grace Period Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateNavy),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Translations.t("simulate_grace_btn", language),
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (language == AppLanguage.ARABIC)
                                    "اختبر نافذة الـ 120 ثانية للتراجع الواعي وإنقاذ السلسلة"
                                else
                                    "Test the 120s mindfulness reversal window to protect streak",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.simulateGracePeriod() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoseAlert,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("simulate_grace_button")
                        ) {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "تجربة" else "Test",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Unlockable Badge Row Section
            item {
                var showAllBadges by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = Translations.t("badges_title", language),
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val unlockedCount = badges.count { it.unlocked }
                            Text(
                                text = "$unlockedCount / ${badges.size} " + Translations.t("badges_unlocked", language),
                                color = AmberGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        TextButton(onClick = { showAllBadges = !showAllBadges }) {
                            Text(
                                text = if (showAllBadges) Translations.t("badges_hide", language) else Translations.t("badges_view_all", language),
                                color = EmeraldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Horizontal Unlockable Badge Row
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(badges) { badge ->
                            UnlockableBadgeCard(
                                badge = badge,
                                language = language
                            )
                        }
                    }

                    // Expanded Badges Details (when user taps "View All")
                    if (showAllBadges) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            badges.forEach { badge ->
                                BadgeItem(
                                    badge = badge,
                                    language = language
                                )
                            }
                        }
                    }
                }
            }

            // Management Actions (Set Start Date & Honest Relapse Reset)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.showSetDateDialog(true) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("adjust_start_date_button")
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Translations.t("adjust_start_date", language), fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.showResetDialog(true) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseAlert),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_reset_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoseAlert)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Translations.t("reset_streak", language), fontSize = 11.sp, color = RoseAlert)
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
            onSaveCheckIn = { checkIn ->
                viewModel.saveDailyCheckIn(checkIn)
            }
        )
    }

    // Victory Dialog (Stepped back in 120s grace period!)
    if (showVictoryDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissVictoryDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "🛡️", fontSize = 24.sp)
                    Text(text = Translations.t("grace_victory_title", language), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Text(text = Translations.t("grace_victory_msg", language), color = TextSecondary, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissVictoryDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = OnEmerald)
                ) {
                    Text(text = Translations.t("continue_journey", language), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SlateNavy,
            titleContentColor = TextPrimary
        )
    }

    // Empathetic Relapse Dialog (120s timed out or manual reset)
    if (showRelapseDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRelapseDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "🌱", fontSize = 24.sp)
                    Text(text = Translations.t("grace_relapse_title", language), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Text(text = Translations.t("grace_relapse_msg", language), color = TextSecondary, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissRelapseDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = OnEmerald)
                ) {
                    Text(text = Translations.t("close", language), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SlateNavy,
            titleContentColor = TextPrimary
        )
    }

    // Manual Reset Confirmation Dialog
    if (showResetConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showResetDialog(false) },
            title = {
                Text(text = Translations.t("reset_streak", language), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = if (language == AppLanguage.ARABIC)
                        "هل أنت متأكد من رغبتك في تصفير العداد؟ التعافي مسار شجاع، والاعتراف بالانتكاسة هو الخطوة الأولى لبداية أقوى."
                    else
                        "Are you sure you want to reset your streak? Recovery takes courage, and acknowledging a slip is the first step toward greater strength.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.manualResetStreak() },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAlert)
                ) {
                    Text(text = if (language == AppLanguage.ARABIC) "تأكيد وإعادة البدء" else "Reset Now", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showResetDialog(false) }) {
                    Text(text = Translations.t("close", language), color = TextSecondary)
                }
            },
            containerColor = SlateNavy,
            titleContentColor = TextPrimary
        )
    }

    // Set Custom Start Clean Days Dialog
    if (showSetDateDialog) {
        var daysInput by remember { mutableStateOf("${streakState.days}") }
        AlertDialog(
            onDismissRequest = { viewModel.showSetDateDialog(false) },
            title = {
                Text(text = Translations.t("adjust_start_date", language), fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = if (language == AppLanguage.ARABIC)
                            "أدخل عدد الأيام النظيفة التي حققتها:"
                        else
                            "Enter how many clean days you have already achieved:",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = daysInput,
                        onValueChange = { daysInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text(Translations.t("days", language)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = daysInput.toIntOrNull() ?: 0
                        viewModel.setCustomCleanDays(num)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = OnEmerald)
                ) {
                    Text(text = if (language == AppLanguage.ARABIC) "حفظ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showSetDateDialog(false) }) {
                    Text(text = Translations.t("close", language), color = TextSecondary)
                }
            },
            containerColor = SlateNavy,
            titleContentColor = TextPrimary
        )
    }
}

@Composable
private fun EmergencySosCard(
    language: AppLanguage,
    onSosClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFE11D48),
                        RoseWarning,
                        Color(0xFFBE123C)
                    )
                )
            )
            .clickable(onClick = onSosClicked)
            .padding(18.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .testTag("emergency_sos_button")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🚨", fontSize = 20.sp)
                    Text(
                        text = Translations.t("emergency_sos_btn", language),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Translations.t("sos_subtext", language),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🧘", fontSize = 22.sp)
            }
        }
    }
}

@Composable
private fun ModernStatCard(
    title: String,
    value: String,
    icon: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    isValueSmall: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateNavy),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(18.dp))
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 15.sp)
                }
                Text(
                    text = value,
                    color = accentColor,
                    fontSize = if (isValueSmall) 13.sp else 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun UnlockableBadgeCard(
    badge: com.example.model.Badge,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val title = if (language == AppLanguage.ARABIC) badge.titleAr else badge.titleEn

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (badge.unlocked) SlateNavy else Color(0xFF0F172E)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .width(136.dp)
            .border(
                1.dp,
                if (badge.unlocked) AmberGold.copy(alpha = 0.6f) else GlassBorderSubtle,
                RoundedCornerShape(20.dp)
            )
            .testTag("badge_card_${badge.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.unlocked) AmberGold.copy(alpha = 0.2f) else Color(0xFF131D3B)
                    )
                    .border(
                        1.dp,
                        if (badge.unlocked) AmberGold.copy(alpha = 0.45f) else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badge.unlocked) badge.iconEmoji else "🔒",
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = if (badge.unlocked) TextPrimary else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Unlock status pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (badge.unlocked) EmeraldGreen.copy(alpha = 0.2f) else Color(0x1A94A3B8)
                    )
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (badge.unlocked)
                        (if (language == AppLanguage.ARABIC) "✓ مُحقق" else "✓ Unlocked")
                    else
                        (if (language == AppLanguage.ARABIC) "قيد التقدم" else "Locked"),
                    color = if (badge.unlocked) EmeraldLight else TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
