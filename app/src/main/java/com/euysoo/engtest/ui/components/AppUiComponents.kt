package com.euysoo.engtest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.euysoo.engtest.ui.theme.AppDimens
import com.euysoo.engtest.ui.theme.AppTheme

/**
 * 상단바 우측 액션용 캡슐(단어 수 배지와 동일 테두리·배경).
 */
@Composable
fun AppTopBarPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color? = null,
) {
    val colors = AppTheme.colors
    val textColor =
        when {
            !enabled -> colors.textMuted
            contentColor != null -> contentColor
            else -> colors.purpleMain
        }
    Box(
        modifier =
            modifier
                .background(colors.bgCard, RoundedCornerShape(AppDimens.topBarBadgeCorner))
                .border(AppDimens.appCardBorder, colors.borderDefault, RoundedCornerShape(AppDimens.topBarBadgeCorner))
                .clickable(enabled = enabled, onClick = onClick)
                .padding(
                    horizontal = AppDimens.topBarBadgePaddingH,
                    vertical = AppDimens.topBarBadgePaddingV,
                ),
    ) {
        Text(text = text, fontSize = AppDimens.topBarBadgeFont, color = textColor)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppTopBar(
    title: String,
    onBackClick: () -> Unit,
    trailingBadgeText: String? = null,
    trailingExtras: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = AppTheme.colors
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = AppDimens.topBarTitleFont,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
            )
        },
        navigationIcon = {
            Box(
                modifier =
                    Modifier
                        .padding(start = 8.dp)
                        .size(AppDimens.topBarBackBoxSize)
                        .background(colors.bgCard, RoundedCornerShape(AppDimens.topBarBackCorner))
                        .border(AppDimens.appCardBorder, colors.borderDefault, RoundedCornerShape(AppDimens.topBarBackCorner))
                        .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "뒤로가기",
                    tint = colors.purpleMain,
                    modifier = Modifier.size(AppDimens.topBarBackIconSize),
                )
            }
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                trailingBadgeText?.let {
                    Box(
                        modifier =
                            Modifier
                                .background(colors.bgCard, RoundedCornerShape(AppDimens.topBarBadgeCorner))
                                .border(AppDimens.appCardBorder, colors.borderDefault, RoundedCornerShape(AppDimens.topBarBadgeCorner))
                                .padding(
                                    horizontal = AppDimens.topBarBadgePaddingH,
                                    vertical = AppDimens.topBarBadgePaddingV,
                                ),
                    ) {
                        Text(text = it, fontSize = AppDimens.topBarBadgeFont, color = colors.purpleMain)
                    }
                }
                trailingExtras?.invoke(this)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.bgPrimary),
    )
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    val resolvedContainerColor = containerColor ?: colors.bgCard
    val resolvedBorderColor = borderColor ?: colors.borderDefault
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(
        modifier =
            modifier
                .background(resolvedContainerColor, RoundedCornerShape(AppDimens.appCardCorner))
                .border(AppDimens.appCardBorder, resolvedBorderColor, RoundedCornerShape(AppDimens.appCardCorner))
                .then(clickableModifier)
                .padding(AppDimens.appCardPadding),
    ) {
        content()
    }
}
