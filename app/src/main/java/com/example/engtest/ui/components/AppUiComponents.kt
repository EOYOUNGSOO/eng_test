package com.example.engtest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engtest.ui.theme.BgCard
import com.example.engtest.ui.theme.BgPrimary
import com.example.engtest.ui.theme.BorderDefault
import com.example.engtest.ui.theme.AppDimens
import com.example.engtest.ui.theme.PurpleMain
import com.example.engtest.ui.theme.TextDim
import com.example.engtest.ui.theme.TextPrimary
import com.example.engtest.ui.theme.TextSecondary

enum class AppActionButtonStyle { Primary, Secondary, Muted }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppTopBar(
    title: String,
    onBackClick: () -> Unit,
    trailingBadgeText: String? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = AppDimens.topBarTitleFont,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(AppDimens.topBarBackBoxSize)
                    .background(BgCard, RoundedCornerShape(AppDimens.topBarBackCorner))
                    .border(AppDimens.appCardBorder, BorderDefault, RoundedCornerShape(AppDimens.topBarBackCorner))
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "뒤로가기",
                    tint = PurpleMain,
                    modifier = Modifier.size(AppDimens.topBarBackIconSize)
                )
            }
        },
        actions = {
            trailingBadgeText?.let {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .background(BgCard, RoundedCornerShape(AppDimens.topBarBadgeCorner))
                        .border(AppDimens.appCardBorder, BorderDefault, RoundedCornerShape(AppDimens.topBarBadgeCorner))
                        .padding(
                            horizontal = AppDimens.topBarBadgePaddingH,
                            vertical = AppDimens.topBarBadgePaddingV
                        )
                ) {
                    Text(text = it, fontSize = AppDimens.topBarBadgeFont, color = PurpleMain)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BgPrimary)
    )
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color = BgCard,
    borderColor: Color = BorderDefault,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(AppDimens.appCardCorner))
            .border(AppDimens.appCardBorder, borderColor, RoundedCornerShape(AppDimens.appCardCorner))
            .then(clickableModifier)
            .padding(AppDimens.appCardPadding)
    ) {
        content()
    }
}

@Composable
fun AppActionButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: AppActionButtonStyle = AppActionButtonStyle.Secondary,
    onClick: () -> Unit
) {
    val base = when (style) {
        AppActionButtonStyle.Primary -> Modifier.background(
            brush = Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), PurpleMain)),
            shape = RoundedCornerShape(AppDimens.actionButtonCorner)
        )
        AppActionButtonStyle.Secondary,
        AppActionButtonStyle.Muted -> Modifier.background(BgCard, RoundedCornerShape(AppDimens.actionButtonCorner))
            .border(AppDimens.actionButtonBorder, BorderDefault, RoundedCornerShape(AppDimens.actionButtonCorner))
    }
    val textColor = when (style) {
        AppActionButtonStyle.Primary -> Color.White
        AppActionButtonStyle.Secondary -> TextSecondary
        AppActionButtonStyle.Muted -> TextDim
    }
    Box(
        modifier = modifier
            .then(base)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = AppDimens.actionButtonPaddingV),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = AppDimens.actionButtonFont,
            fontWeight = FontWeight.Medium,
            color = if (enabled) textColor else TextDim
        )
    }
}

@Composable
fun AppFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) PurpleMain else BgCard,
                shape = RoundedCornerShape(AppDimens.filterChipCorner)
            )
            .then(
                if (!selected) Modifier.border(AppDimens.filterChipBorder, BorderDefault, RoundedCornerShape(AppDimens.filterChipCorner))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = AppDimens.filterChipPaddingH, vertical = AppDimens.filterChipPaddingV)
    ) {
        Text(
            text = text,
            fontSize = AppDimens.filterChipFont,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) BgPrimary else TextDim
        )
    }
}
