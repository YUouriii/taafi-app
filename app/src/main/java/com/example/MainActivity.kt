package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Translations
import com.example.model.AppLanguage
import com.example.ui.components.SosBreathingModal
import com.example.ui.screens.AiCompanionScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MonitoringSettingsScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkNavyBackground
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GlassBottomBarBg
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.RoseWarning
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.configureAppCheck
import com.example.viewmodel.BreakFreeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BreakFreeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val language by viewModel.language.collectAsState()
                val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    BreakFreeApp(viewModel = viewModel, language = language)
                }
            }
        }
    }
}

data class NavItem(
    val titleKey: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreakFreeApp(
    viewModel: BreakFreeViewModel,
    language: AppLanguage
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val isSosVisible by viewModel.isSosModalVisible.collectAsState()

    val navItems = listOf(
        NavItem("tab_home", Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
        NavItem("tab_guard", Icons.Filled.Shield, Icons.Outlined.Shield, "nav_tab_guard"),
        NavItem("tab_ai_chat", Icons.Filled.Psychology, Icons.Outlined.Psychology, "nav_tab_ai"),
        NavItem("tab_community", Icons.Filled.People, Icons.Outlined.People, "nav_tab_community")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen.copy(alpha = 0.2f))
                                .border(1.dp, EmeraldGreen.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🌱", fontSize = 16.sp)
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "BreakFree",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = " / تعافي",
                                    color = EmeraldLight,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Quick SOS button in top bar
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(RoseWarning.copy(alpha = 0.2f))
                            .border(1.dp, RoseWarning.copy(alpha = 0.6f), RoundedCornerShape(50))
                            .clickable { viewModel.openSosModal() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("top_bar_sos_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🚨", fontSize = 12.sp)
                            Text(
                                text = "SOS",
                                color = RoseWarning,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Language toggle button (EN / عربي)
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.testTag("language_toggle_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SlateNavy)
                                .border(1.dp, GlassBorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "EN" else "عربي",
                                color = CyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightNavy
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            // Floating Dark Glass Bar with active indicator glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(GlassBottomBarBg)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(26.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) EmeraldGreen.copy(alpha = 0.22f) else Color.Transparent
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) EmeraldGreen.copy(alpha = 0.55f) else Color.Transparent,
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp)
                                .testTag(item.testTag),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = Translations.t(item.titleKey, language),
                                    tint = if (isSelected) EmeraldLight else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = Translations.t(item.titleKey, language),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) EmeraldLight else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = MidnightNavy
    ) { innerPadding ->
        Crossfade(
            targetState = selectedTab,
            label = "tab_crossfade",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tab ->
            when (tab) {
                0 -> DashboardScreen(viewModel = viewModel)
                1 -> MonitoringSettingsScreen(viewModel = viewModel)
                2 -> AiCompanionScreen(viewModel = viewModel)
                3 -> CommunityScreen(viewModel = viewModel)
            }
        }

        // Full Screen Emergency SOS Modal
        SosBreathingModal(
            isVisible = isSosVisible,
            language = language,
            onDismiss = { viewModel.closeSosModal() },
            onComplete = { viewModel.completeSosSession() }
        )
    }
}
