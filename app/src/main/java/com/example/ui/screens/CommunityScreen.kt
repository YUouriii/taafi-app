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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Translations
import com.example.model.AppLanguage
import com.example.model.CommunityCategory
import com.example.model.CommunityPost
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElevatedNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.OnEmerald
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.BreakFreeViewModel

@Composable
fun CommunityScreen(
    viewModel: BreakFreeViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val posts by viewModel.communityPosts.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val showNewPostDialog by viewModel.showNewPostDialog.collectAsState()

    val filteredPosts = remember(posts, selectedCategory) {
        if (selectedCategory == CommunityCategory.ALL) posts
        else posts.filter { it.category == selectedCategory }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateNavy)
                    .border(width = 1.dp, color = GlassBorderSubtle)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.2f))
                            .border(1.dp, CyanAccent.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🤝", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = Translations.t("community_title", language),
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Translations.t("community_subtitle", language),
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Categories Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CommunityCategory.entries.toTypedArray()) { cat ->
                    val isSelected = cat == selectedCategory
                    val catTitle = if (language == AppLanguage.ARABIC) cat.titleAr else cat.titleEn
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) EmeraldGreen else SlateNavy)
                            .border(
                                1.dp,
                                if (isSelected) EmeraldLight else GlassBorderSubtle,
                                RoundedCornerShape(50)
                            )
                            .clickable { viewModel.setCategory(cat) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("category_chip_${cat.name}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = cat.emoji, fontSize = 12.sp)
                            Text(
                                text = catTitle,
                                color = if (isSelected) OnEmerald else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Feed of Posts
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredPosts, key = { it.id }) { post ->
                    CommunityPostCard(
                        post = post,
                        language = language,
                        onFistBump = { viewModel.toggleFistBump(post.id) }
                    )
                }
            }
        }

        // Floating Action Button to Post
        ExtendedFloatingActionButton(
            onClick = { viewModel.openNewPostDialog(true) },
            containerColor = EmeraldGreen,
            contentColor = OnEmerald,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("new_post_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = Translations.t("share_story_btn", language),
                fontWeight = FontWeight.Bold
            )
        }
    }

    // New Post Dialog
    if (showNewPostDialog) {
        var aliasInput by remember { mutableStateOf("") }
        var contentInput by remember { mutableStateOf("") }
        var postCategory by remember { mutableStateOf(CommunityCategory.DAILY_MOTIVATION) }

        AlertDialog(
            onDismissRequest = { viewModel.openNewPostDialog(false) },
            title = {
                Text(
                    text = Translations.t("post_dialog_title", language),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = aliasInput,
                        onValueChange = { aliasInput = it },
                        label = { Text(Translations.t("post_alias_label", language)) },
                        placeholder = { Text("e.g. SoberFalcon #312") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text(Translations.t("post_content_label", language)) },
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createPost(aliasInput, contentInput, postCategory)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = OnEmerald
                    ),
                    modifier = Modifier.testTag("publish_post_button")
                ) {
                    Text(
                        text = Translations.t("publish_btn", language),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.openNewPostDialog(false) }) {
                    Text(text = Translations.t("close", language), color = TextSecondary)
                }
            },
            containerColor = SlateNavy,
            titleContentColor = TextPrimary
        )
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPost,
    language: AppLanguage,
    onFistBump: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateNavy),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(20.dp))
            .testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author row
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
                            .background(Color(post.authorAvatarBg)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorAlias.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = post.authorAlias,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        // Clean Time Badge Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(EmeraldGreen.copy(alpha = 0.16f))
                                .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🌱", fontSize = 10.sp)
                            Text(
                                text = "${post.daysClean} ${Translations.t("days", language)} " +
                                        if (language == AppLanguage.ARABIC) "نقية" else "clean",
                                color = EmeraldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Category pill
                val catTitle = if (language == AppLanguage.ARABIC) post.category.titleAr else post.category.titleEn
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(ElevatedNavy)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${post.category.emoji} $catTitle",
                        color = AmberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body
            val postContent = if (language == AppLanguage.ARABIC) post.contentAr else post.contentEn
            Text(
                text = postContent,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Support Metrics Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (post.userCheered) EmeraldGreen.copy(alpha = 0.22f) else ElevatedNavy
                        )
                        .border(
                            1.dp,
                            if (post.userCheered) EmeraldGreen.copy(alpha = 0.6f) else GlassBorderSubtle,
                            RoundedCornerShape(50)
                        )
                        .clickable(onClick = onFistBump)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("fist_bump_button_${post.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "👊", fontSize = 14.sp)
                    Text(
                        text = "${Translations.t("fist_bump", language)} (${post.fistBumps})",
                        color = if (post.userCheered) EmeraldLight else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🛡️", fontSize = 11.sp)
                    Text(
                        text = if (language == AppLanguage.ARABIC) "دعم مجهول" else "Brotherhood",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
