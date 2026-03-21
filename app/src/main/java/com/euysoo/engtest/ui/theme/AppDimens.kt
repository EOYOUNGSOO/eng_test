package com.euysoo.engtest.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 앱 전역에서 반복 사용하는 크기·간격.
 * res/values/dimens.xml과 용도 동일; Compose에서는 이 객체를 사용해 관리 부담을 줄임.
 */
object AppDimens {
    // Legacy
    val cardCornerRadius = 12.dp
    val cardElevation = 2.dp
    val cardPadding = 12.dp
    val listItemSpacing = 8.dp
    val screenPadding = 16.dp
    val screenPaddingLarge = 24.dp
    val iconButtonSize = 32.dp

    // Top bar tokens
    val topBarBackBoxSize = 32.dp
    val topBarBackIconSize = 16.dp
    val topBarBackCorner = 10.dp
    val topBarBadgeCorner = 20.dp
    val topBarBadgePaddingH = 10.dp
    val topBarBadgePaddingV = 4.dp
    val topBarTitleFont = 17.sp
    val topBarBadgeFont = 11.sp

    // Card tokens
    val appCardCorner = 16.dp
    val appCardBorder = 0.5.dp
    val appCardPadding = 14.dp

    // Action button tokens
    val actionButtonCorner = 14.dp
    val actionButtonBorder = 0.5.dp
    val actionButtonPaddingV = 13.dp
    val actionButtonFont = 13.sp

    // Filter chip tokens
    val filterChipCorner = 20.dp
    val filterChipBorder = 0.5.dp
    val filterChipPaddingH = 14.dp
    val filterChipPaddingV = 6.dp
    val filterChipFont = 12.sp
}
