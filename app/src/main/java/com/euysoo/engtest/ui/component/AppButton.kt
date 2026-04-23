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
    /** 오답노트 등 — 빨간 배경 */
    WRONG_NOTE,
}

private val WrongNoteRed = Color(0xFFDC2626)
private val WrongNoteRedDisabled = Color(0xFF7F1D1D)

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    style: AppButtonStyle = AppButtonStyle.SECONDARY,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    /** true이면 가로로 꽉 채움(일렬 버튼 등) */
    fillMaxWidth: Boolean = false,
) {
    val colors = AppTheme.colors
    val bgColor =
        when (style) {
            AppButtonStyle.PRIMARY -> if (enabled) colors.purpleMain else Color(0xFF2A2740)
            AppButtonStyle.SECONDARY -> colors.bgCard
            AppButtonStyle.DANGER -> colors.bgCard
            AppButtonStyle.WRONG_NOTE -> if (enabled) WrongNoteRed else WrongNoteRedDisabled
        }
    val textColor =
        when (style) {
            AppButtonStyle.PRIMARY -> if (enabled) Color.White else colors.textMuted
            AppButtonStyle.SECONDARY -> colors.textSecondary
            AppButtonStyle.DANGER -> colors.pinkMain
            AppButtonStyle.WRONG_NOTE -> if (enabled) Color.White else colors.textMuted
        }
    val borderColor =
        when (style) {
            AppButtonStyle.PRIMARY, AppButtonStyle.WRONG_NOTE -> Color.Transparent
            else -> colors.borderDefault
        }

    Box(
        modifier =
            modifier
                .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.wrapContentWidth())
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
            fontWeight =
                if (style == AppButtonStyle.PRIMARY || style == AppButtonStyle.WRONG_NOTE) {
                    FontWeight.Medium
                } else {
                    FontWeight.Normal
                },
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
                            AppButtonStyle.WRONG_NOTE -> if (enabled) WrongNoteRed else WrongNoteRedDisabled
                            else -> colors.bgCard
                        },
                    shape = RoundedCornerShape(12.dp),
                ).then(
                    if (style != AppButtonStyle.PRIMARY && style != AppButtonStyle.WRONG_NOTE) {
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
            fontWeight =
                if (style == AppButtonStyle.PRIMARY || style == AppButtonStyle.WRONG_NOTE) {
                    FontWeight.Medium
                } else {
                    FontWeight.Normal
                },
            color =
                when (style) {
                    AppButtonStyle.PRIMARY -> if (enabled) Color.White else colors.textMuted
                    AppButtonStyle.SECONDARY -> colors.textSecondary
                    AppButtonStyle.DANGER -> colors.pinkMain
                    AppButtonStyle.WRONG_NOTE -> if (enabled) Color.White else colors.textMuted
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
