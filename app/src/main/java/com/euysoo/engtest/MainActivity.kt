package com.euysoo.engtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.euysoo.engtest.ui.navigation.EngTestNavHost
import com.euysoo.engtest.ui.theme.EngTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val isDark = isSystemInDarkTheme()
            SideEffect {
                window.statusBarColor = if (isDark) 0xFF0F1620.toInt() else 0xFFF5F3EF.toInt()
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDark
            }
            EngTestTheme {
                // 단일 Surface로 오버드로 방지: 하위 화면은 별도 전체 배경 없이 콘텐츠만 그림
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EngTestNavHost()
                }
            }
        }
    }
}
