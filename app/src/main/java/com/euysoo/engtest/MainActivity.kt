package com.euysoo.engtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.euysoo.engtest.ui.navigation.EngTestNavHost
import com.euysoo.engtest.ui.theme.EngTestTheme
import com.euysoo.engtest.ui.theme.Light_BgPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val barArgb = Light_BgPrimary.toArgb()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(barArgb),
            navigationBarStyle = SystemBarStyle.dark(barArgb),
        )
        setContent {
            EngTestTheme {
                // 단일 Surface로 오버드로 방지: 하위 화면은 별도 전체 배경 없이 콘텐츠만 그림
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    EngTestNavHost()
                }
            }
        }
    }
}
