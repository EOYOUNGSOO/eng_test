package com.example.engtest.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 앱 전역에서 반복 사용하는 크기·간격.
 * res/values/dimens.xml과 용도 동일; Compose에서는 이 객체를 사용해 관리 부담을 줄임.
 */
object AppDimens {
    val cardCornerRadius = 12.dp
    val cardElevation = 2.dp
    val cardPadding = 12.dp
    val listItemSpacing = 8.dp
    val screenPadding = 16.dp
    val screenPaddingLarge = 24.dp
    val iconButtonSize = 32.dp
}
