package com.example.engtest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
    val badgePurpleText: Color
)

val LightAppColors = AppColors(
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
    badgePurpleText = Light_BadgePurpleText
)

val DarkAppColors = AppColors(
    bgPrimary = Dark_BgPrimary,
    bgCard = Dark_BgCard,
    bgIcon = Dark_BgIcon,
    bgIconGreen = Dark_BgIconGreen,
    bgCardAccent = Dark_BgCardAccent,
    borderDefault = Dark_BorderDefault,
    borderAccent = Dark_BorderAccent,
    purpleMain = Dark_PurpleMain,
    purpleLight = Dark_PurpleLight,
    greenMain = Dark_GreenMain,
    pinkMain = Dark_PinkMain,
    textPrimary = Dark_TextPrimary,
    textSecondary = Dark_TextSecondary,
    textMuted = Dark_TextMuted,
    textDim = Dark_TextDim,
    badgePurpleBg = Dark_BadgePurpleBg,
    badgePurpleText = Dark_BadgePurpleText
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}

@Composable
fun EngTestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            background = Dark_BgPrimary,
            surface = Dark_BgCard,
            primary = Dark_PurpleMain,
            secondary = Dark_GreenMain,
            tertiary = Dark_PinkMain,
            onBackground = Dark_TextPrimary,
            onSurface = Dark_TextSecondary
        )
    } else {
        lightColorScheme(
            background = Light_BgPrimary,
            surface = Light_BgCard,
            primary = Light_PurpleMain,
            secondary = Light_GreenMain,
            tertiary = Light_PinkMain,
            onBackground = Light_TextPrimary,
            onSurface = Light_TextSecondary
        )
    }
    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
