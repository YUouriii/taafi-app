package com.aistudio.breakfree.recovr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.CommunityCategory
import com.aistudio.breakfree.recovr.model.CommunityPost
import com.aistudio.breakfree.recovr.ui.components.CostumeEmoji
import com.aistudio.breakfree.recovr.ui.components.CostumeIconType
import com.aistudio.breakfree.recovr.ui.components.EmptyState
import com.aistudio.breakfree.recovr.ui.components.Pill
import com.aistudio.breakfree.recovr.ui.components.PrimaryButton
import com.aistudio.breakfree.recovr.ui.components.ScreenGutter
import com.aistudio.breakfree.recovr.ui.components.ScreenHeader
import com.aistudio.breakfree.recovr.ui.components.SoftCard
import com.aistudio.breakfree.recovr.ui.components.SoftDivider
import com.aistudio.breakfree.recovr.ui.theme.BlackCard
import com.aistudio.breakfree.recovr.ui.theme.BlackElevated
import com.aistudio.breakfree.recovr.ui.theme.GreenCalm
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmContainer
import com.aistudio.breakfree.recovr.ui.theme.GreenCalmLight
import com.aistudio.breakfree.recovr.ui.theme.OnGreenCalm
import com.aistudio.breakfree.recovr.ui.theme.SoftGold
import com.aistudio.breakfree.recovr.ui.theme.TextMuted
import com.aistudio.breakfree.recovr.ui.theme.TextPrimary
import com.aistudio.breakfree.recovr.ui.theme.TextSecondary
import com.aistudio.breakfree.recovr.ui.theme.WhiteSubtleBorder
import com.aistudio.breakfree.recovr.viewmodel.BreakFreeViewModel

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

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = Translations.t("community_title", language),
                subtitle = Translations.t("community_subtitle", language),
                iconType = CostumeIconType.COMMUNITY
            )

            // Category filter
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                contentPadding = PaddingValues(horizontal = ScreenGutter),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CommunityCategory.entries.toTypedArray()) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = category == selectedCategory,
                        language = language,
                        onClick = { viewModel.setCategory(category) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = ScreenGutter),
                contentPadding = PaddingValues(bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredPosts.isEmpty()) {
                    item {
                        EmptyState(
                            iconType = CostumeIconType.COMMUNITY,
                            title = Translations.t("community_empty_title", language),
                            description = Translations.t("community_empty_desc", language),
                            modifier = Modifier.testTag("community_empty_state")
                        )
                    }
                }

                items(filteredPosts, key = { it.id }) { post ->
                    CommunityPostCard(
                        post = post,
                        language = language,
                        onFistBump = { viewModel.toggleFistBump(post.id) }
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { viewModel.openNewPostDialog(true) },
            containerColor = GreenCalm,
            contentColor = OnGreenCalm,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 18.dp, end = ScreenGutter)
                .testTag("new_post_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = Translations.t("community_post_btn", language),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }

    if (showNewPostDialog) {
        NewPostDialog(
            language = language,
            onDismiss = { viewModel.openNewPostDialog(false) },
            onPublish = { alias, content, category ->
                viewModel.createPost(alias, content, category)
            }
        )
    }
}

@Composable
private fun CategoryChip(
    category: CommunityCategory,
    isSelected: Boolean,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val title = if (language == AppLanguage.ARABIC) category.titleAr else category.titleEn
    val tint = if (isSelected) GreenCalmLight else TextSecondary

    Row(
        modifier = Modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) GreenCalmContainer else BlackCard)
            .border(
                1.dp,
                if (isSelected) GreenCalm.copy(alpha = 0.4f) else WhiteSubtleBorder,
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp)
            .testTag("category_chip_${category.name}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CostumeEmoji(type = category.costumeType, size = 14.dp, tint = tint)
        Text(
            text = title,
            color = tint,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPost,
    language: AppLanguage,
    onFistBump: () -> Unit
) {
    SoftCard(contentPadding = 16.dp, modifier = Modifier.testTag("post_card_${post.id}")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(post.authorAvatarBg).copy(alpha = 0.22f))
                    .border(1.dp, Color(post.authorAvatarBg).copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = post.authorAlias.take(1).uppercase(),
                    color = Color(post.authorAvatarBg),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.authorAlias,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Pill(
                    text = "${post.daysClean} " + Translations.t("days", language) + " " +
                        Translations.t("community_clean", language),
                    color = GreenCalmLight,
                    icon = CostumeIconType.SPROUT
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Pill(
                text = if (language == AppLanguage.ARABIC) post.category.titleAr else post.category.titleEn,
                color = SoftGold,
                icon = post.category.costumeType
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (language == AppLanguage.ARABIC) post.contentAr else post.contentEn,
            color = TextPrimary,
            fontSize = 13.5.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(14.dp))
        SoftDivider()
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (post.userCheered) GreenCalmContainer else BlackElevated
                    )
                    .border(
                        1.dp,
                        if (post.userCheered) GreenCalm.copy(alpha = 0.45f) else WhiteSubtleBorder,
                        RoundedCornerShape(50)
                    )
                    .clickable(onClick = onFistBump)
                    .padding(horizontal = 13.dp, vertical = 6.dp)
                    .testTag("fist_bump_button_${post.id}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CostumeEmoji(
                    type = CostumeIconType.FIST,
                    size = 15.dp,
                    tint = if (post.userCheered) GreenCalmLight else TextSecondary
                )
                Text(
                    text = "${Translations.t("fist_bump", language)} (${post.fistBumps})",
                    color = if (post.userCheered) GreenCalmLight else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                CostumeEmoji(
                    type = CostumeIconType.SHIELD,
                    size = 12.dp,
                    tint = TextMuted
                )
                Text(
                    text = Translations.t("community_brotherhood", language),
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewPostDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onPublish: (alias: String, content: String, category: CommunityCategory) -> Unit
) {
    var aliasInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var postCategory by remember { mutableStateOf(CommunityCategory.DAILY_MOTIVATION) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                    placeholder = { Text("SoberFalcon #312") },
                    singleLine = true,
                    colors = communityFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    label = { Text(Translations.t("post_content_label", language)) },
                    maxLines = 5,
                    colors = communityFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Text(
                    text = Translations.t("post_category_label", language),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CommunityCategory.entries.forEach { category ->
                        val selected = category == postCategory
                        val label = if (language == AppLanguage.ARABIC) category.titleAr else category.titleEn
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) GreenCalmContainer else BlackElevated)
                                .border(
                                    1.dp,
                                    if (selected) GreenCalm.copy(alpha = 0.45f) else WhiteSubtleBorder,
                                    RoundedCornerShape(50)
                                )
                                .clickable { postCategory = category }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            CostumeEmoji(
                                type = category.costumeType,
                                size = 12.dp,
                                tint = if (selected) GreenCalmLight else TextMuted
                            )
                            Text(
                                text = label,
                                color = if (selected) GreenCalmLight else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Box(modifier = Modifier.padding(start = 8.dp)) {
                PrimaryButton(
                    text = Translations.t("publish_btn", language),
                    onClick = { onPublish(aliasInput, contentInput, postCategory) },
                    enabled = contentInput.isNotBlank(),
                    modifier = Modifier
                        .width(132.dp)
                        .testTag("publish_post_button")
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = Translations.t("close", language),
                    color = TextSecondary
                )
            }
        },
        containerColor = BlackElevated,
        titleContentColor = TextPrimary
    )
}

@Composable
private fun communityFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GreenCalm,
    unfocusedBorderColor = WhiteSubtleBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = GreenCalmLight
)
