package com.euysoo.engtest.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.euysoo.engtest.ui.theme.AppTheme

enum class AppButtonStyle {
    PRIMARY,
    SECONDARY,
    DANGER,
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    style: AppButtonStyle = AppButtonStyle.SECONDARY,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val bgColor =
        when (style) {
            AppButtonStyle.PRIMARY -> if (enabled) colors.purpleMain else Color(0xFF2A2740)
            AppButtonStyle.SECONDARY -> colors.bgCard
            AppButtonStyle.DANGER -> colors.bgCard
        }
    val textColor =
        when (style) {
            AppButtonStyle.PRIMARY -> if (enabled) Color.White else colors.textMuted
            AppButtonStyle.SECONDARY -> colors.textSecondary
            AppButtonStyle.DANGER -> colors.pinkMain
        }
    val borderColor =
        when (style) {
            AppButtonStyle.PRIMARY -> Color.Transparent
            else -> colors.borderDefault
        }

    Box(
        modifier =
            modifier
                .wrapContentWidth()
                .background(bgColor, RoundedCornerShape(10.dp))
                .then(
                    if (borderColor != Color.Transparent) {
                        Modifier.border(0.5.dp, borderColor, RoundedCornerShape(10.dp))
                    } else {
                        Modifier
                    },
                ).clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (style == AppButtonStyle.PRIMARY) FontWeight.Medium else FontWeight.Normal,
            color = textColor,
            maxLines = 1,
        )
    }
}

@Composable
fun AppFullWidthButton(
    text: String,
    onClick: () -> Unit,
    style: AppButtonStyle = AppButtonStyle.SECONDARY,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color =
                        when (style) {
                            AppButtonStyle.PRIMARY -> if (enabled) colors.purpleMain else Color(0xFF2A2740)
                            else -> colors.bgPrimary
                        },
                    shape = RoundedCornerShape(12.dp),
                ).then(
                    if (style != AppButtonStyle.PRIMARY) {
                        Modifier.border(0.5.dp, colors.borderDefault, RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                    },
                ).clickable(enabled = enabled, onClick = onClick)
                .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (style == AppButtonStyle.PRIMARY) FontWeight.Medium else FontWeight.Normal,
            color =
                when (style) {
                    AppButtonStyle.PRIMARY -> if (enabled) Color.White else colors.textMuted
                    AppButtonStyle.SECONDARY -> colors.textSecondary
                    AppButtonStyle.DANGER -> colors.pinkMain
                },
            maxLines = 1,
        )
    }
}

@Composable
fun AppChipButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Box(
        modifier =
            modifier
                .wrapContentWidth()
                .background(
                    color = if (selected) colors.purpleMain else colors.bgCard,
                    shape = RoundedCornerShape(20.dp),
                ).then(
                    if (!selected) {
                        Modifier.border(0.5.dp, colors.borderDefault, RoundedCornerShape(20.dp))
                    } else {
                        Modifier
                    },
                ).clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 7.dp),
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) colors.bgIcon else colors.textMuted,
            maxLines = 1,
        )
    }
}
