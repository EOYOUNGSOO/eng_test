package com.example.engtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.engtest.ui.navigation.EngTestNavHost
import com.example.engtest.ui.theme.EngTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.parseColor("#0D0D0D")
        setContent {
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
