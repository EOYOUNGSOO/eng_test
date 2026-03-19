package com.example.engtest.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/** Pure Black 다크 모드: 배경 #000000, 카드·구분선 옅은 회색으로 입체감 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7B9EFF),           // 채도 낮은 소프트 블루 (포인트)
    onPrimary = Color(0xFF0D1B3A),
    primaryContainer = Color(0xFF2A3A5C),
    onPrimaryContainer = Color(0xFFD6E0FF),
    secondary = Color(0xFFB8A4E0),        // 바이올렛 계열
    onSecondary = Color(0xFF251E35),
    secondaryContainer = Color(0xFF3D3552),
    onSecondaryContainer = Color(0xFFE8DDFF),
    tertiary = Color(0xFF8BB4FF),
    onTertiary = Color(0xFF0D1F33),
    background = Color(0xFF000000),        // Pure Black (AMOLED)
    onBackground = Color(0xFFE1E1E1),     // 주 텍스트 Off-white
    surface = Color(0xFF1A1A1A),           // 카드/Surface — 옅은 회색, 배경과 구분
    onSurface = Color(0xFFE1E1E1),
    surfaceVariant = Color(0xFF1A1A1A),    // 보조 Surface (리스트 등)
    onSurfaceVariant = Color(0xFFB0B0B0),  // 보조 텍스트 Medium-gray
    outline = Color(0xFF252525),            // 구분선/테두리 — 옅은 회색 (카드 테두리 등)
    outlineVariant = Color(0xFF1F1F1F)     // 보조 구분선
)

/** 라이트: 연한 핑크 배경 + MZ 선호 인디고/라벤더 포인트 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF3730A3),
    secondary = Color(0xFF818CF8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7D2FE),   // 연한 인디고 (칩·버튼 통일)
    onSecondaryContainer = Color(0xFF4338CA),
    tertiary = Color(0xFF4F46E5),
    onTertiary = Color.White,
    background = Color(0xFFFDE2E4),         // 연한 핑크
    onBackground = Color(0xFF1F2937),
    surface = Color.White,
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFF1F2937)
)

/** 카드·버튼 20dp 이상 둥글게 + 입체감용 기본값 */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun EngTestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
