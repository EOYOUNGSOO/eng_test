package com.euysoo.engtest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Pretendard 폰트 파일(res/font)이 있으면 Font(R.font.pretendard_medium) 등으로 교체 가능 */
private val AppFontFamily = FontFamily.Default

val Typography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 28.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 26.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            ),
    )
