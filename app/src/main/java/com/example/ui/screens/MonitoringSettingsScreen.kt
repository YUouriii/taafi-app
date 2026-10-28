package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Translations
import com.example.model.AppLanguage
import com.example.monitoring.ExplicitContentDetector
import com.example.monitoring.MonitoringManager
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElevatedNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GlassBorderRose
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.OnEmerald
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseWarningDark
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.BreakFreeViewModel

@Composable
fun MonitoringSettingsScreen(
    viewModel: BreakFreeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val isServiceConnected by MonitoringManager.isServiceConnected.collectAsState()

    var strictGuardMode by remember { mutableStateOf(true) }
    var warningBufferEnabled by remember { mutableStateOf(true) }
    var explicitFilterEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
    ) {
        // Screen Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateNavy)
                .border(width = 1.dp, color = GlassBorderSubtle)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmeraldLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = Translations.t("shield_title", language),
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Prominent 120-Second Warning Buffer Banner (Key View 2 Requirement)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateNavy),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorderRose, RoundedCornerShape(22.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        RoseAlert.copy(alpha = 0.18f),
                                        SlateNavy
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(RoseAlert.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "⏳", fontSize = 16.sp)
                                    }
                                    Text(
                                        text = Translations.t("grace_banner_title", language),
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(AmberGold.copy(alpha = 0.2f))
                                        .border(1.dp, AmberGold.copy(alpha = 0.5f), RoundedCornerShape(50))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "120s Buffer",
                                        color = AmberGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = Translations.t("grace_rule_card_desc", language),
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { viewModel.simulateGracePeriod() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoseAlert,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_simulate_grace_button")
                            ) {
                                Text(
                                    text = Translations.t("simulate_grace_btn", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Clean Toggles for Strict Guard Mode (Key View 2 Requirement)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateNavy),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(22.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "إعدادات الحماية الذكية" else "Protection Controls",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Toggle 1: Strict Guard Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = Translations.t("strict_guard_mode", language),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = Translations.t("strict_guard_desc", language),
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                            Switch(
                                checked = strictGuardMode,
                                onCheckedChange = { strictGuardMode = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldGreen,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = ElevatedNavy
                                ),
                                modifier = Modifier.testTag("toggle_strict_guard_mode")
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = GlassBorderSubtle
                        )

                        // Toggle 2: 120s Warning Buffer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = Translations.t("grace_buffer_toggle", language),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = Translations.t("grace_buffer_desc", language),
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                            Switch(
                                checked = warningBufferEnabled,
                                onCheckedChange = { warningBufferEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AmberGold,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = ElevatedNavy
                                ),
                                modifier = Modifier.testTag("toggle_warning_buffer")
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = GlassBorderSubtle
                        )

                        // Toggle 3: Explicit Domain Blocker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = Translations.t("keyword_filter_toggle", language),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = Translations.t("keyword_filter_desc", language),
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                            Switch(
                                checked = explicitFilterEnabled,
                                onCheckedChange = { explicitFilterEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldGreen,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = ElevatedNavy
                                ),
                                modifier = Modifier.testTag("toggle_explicit_filter")
                            )
                        }
                    }
                }
            }

            // Service Status Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateNavy),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isServiceConnected) EmeraldGreen.copy(alpha = 0.5f) else AmberGold.copy(alpha = 0.4f),
                            RoundedCornerShape(22.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Translations.t("service_status", language),
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isServiceConnected) EmeraldGreen.copy(alpha = 0.2f)
                                        else AmberGold.copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (isServiceConnected)
                                        Translations.t("service_active", language)
                                    else
                                        Translations.t("service_inactive", language),
                                    color = if (isServiceConnected) EmeraldLight else AmberGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (language == AppLanguage.ARABIC)
                                "يقوم درع المراقبة الذكي بفحص نوافذ المتصفحات في الخلفية لرصد المحفزات والمواقع الإباحية وتفعيل نافذة الـ 120 ثانية لحماية تعافيك."
                            else
                                "The Smart Shield inspects browser foreground URLs and adult search terms, giving you the 120s conscious grace period to step back safely.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val fallback = Intent(Settings.ACTION_SETTINGS)
                                    fallback.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(fallback)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isServiceConnected) ElevatedNavy else EmeraldGreen,
                                contentColor = if (isServiceConnected) TextPrimary else OnEmerald
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_accessibility_settings_btn")
                        ) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.ARABIC)
                                    "إعدادات إمكانية الوصول (Accessibility)"
                                else
                                    "Open Accessibility Settings",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Explicit Domain Rules & Database stats
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateNavy),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(22.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "🛡️", fontSize = 18.sp)
                                Text(
                                    text = Translations.t("test_scanner_title", language),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(EmeraldGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${ExplicitContentDetector.getTotalBlockedRulesCount()} " +
                                            if (language == AppLanguage.ARABIC) "قاعدة" else "Rules",
                                    color = EmeraldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = Translations.t("test_scanner_desc", language),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Language Switcher Tile
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateNavy),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(22.dp))
                        .clickable { viewModel.toggleLanguage() }
                        .testTag("language_switcher_tile")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = Translations.t("language_toggle", language),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "العربية (RTL) - اضغط للتبديل" else "English (LTR) - Tap to toggle",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(EmeraldGreen)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "English" else "العربية",
                                color = OnEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
