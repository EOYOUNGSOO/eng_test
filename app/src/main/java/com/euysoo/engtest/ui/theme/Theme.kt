package com.euysoo.engtest.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AppShapes =
    Shapes(
        extraSmall = RoundedCornerShape(12.dp),
        small = RoundedCornerShape(16.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

data class AppColors(
    val bgPrimary: Color,
    val bgCard: Color,
    val bgIcon: Color,
    val bgIconGreen: Color,
    val bgCardAccent: Color,
    val borderDefault: Color,
    val borderAccent: Color,
    val purpleMain: Color,
    val purpleLight: Color,
    val greenMain: Color,
    val pinkMain: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDim: Color,
    val badgePurpleBg: Color,
    val badgePurpleText: Color,
)

val LightAppColors =
    AppColors(
        bgPrimary = Light_BgPrimary,
        bgCard = Light_BgCard,
        bgIcon = Light_BgIcon,
        bgIconGreen = Light_BgIconGreen,
        bgCardAccent = Light_BgCardAccent,
        borderDefault = Light_BorderDefault,
        borderAccent = Light_BorderAccent,
        purpleMain = Light_PurpleMain,
        purpleLight = Light_PurpleLight,
        greenMain = Light_GreenMain,
        pinkMain = Light_PinkMain,
        textPrimary = Light_TextPrimary,
        textSecondary = Light_TextSecondary,
        textMuted = Light_TextMuted,
        textDim = Light_TextDim,
        badgePurpleBg = Light_BadgePurpleBg,
        badgePurpleText = Light_BadgePurpleText,
    )

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}

@Composable
fun EngTestTheme(content: @Composable () -> Unit) {
    val colorScheme =
        lightColorScheme(
            background = Light_BgPrimary,
            surface = Light_BgCard,
            primary = Light_PurpleMain,
            secondary = Light_GreenMain,
            tertiary = Light_PinkMain,
            onBackground = Light_TextPrimary,
            onSurface = Light_TextSecondary,
        )
    CompositionLocalProvider(LocalAppColors provides LightAppColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}
