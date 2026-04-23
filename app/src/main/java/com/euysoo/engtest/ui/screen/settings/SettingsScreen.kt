package com.euysoo.engtest.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.euysoo.engtest.BuildConfig
import com.euysoo.engtest.ui.components.APP_COPYRIGHT_LINE
import com.euysoo.engtest.ui.components.AppTopBar
import com.euysoo.engtest.ui.theme.AppTheme

private enum class SettingsMenu {
    PRIVACY,
    OPENSOURCE,
    APP_GUIDE,
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val colors = AppTheme.colors
    var selectedMenu by remember { mutableStateOf<SettingsMenu?>(null) }

    selectedMenu?.let { menu ->
        when (menu) {
            SettingsMenu.PRIVACY -> PrivacyPolicyScreen(onBack = { selectedMenu = null })
            SettingsMenu.OPENSOURCE -> OpenSourceScreen(onBack = { selectedMenu = null })
            SettingsMenu.APP_GUIDE -> AppGuideScreen(onBack = { selectedMenu = null })
        }
        return
    }

    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = { AppTopBar(title = "설정", onBackClick = onBack) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            SettingsItem(
                icon = Icons.Filled.Description,
                title = "앱 기능 설명",
                subtitle = "단어 학습·테스트·단어장 기능 안내",
                onClick = { selectedMenu = SettingsMenu.APP_GUIDE },
            )
            SettingsItem(
                icon = Icons.Filled.PrivacyTip,
                title = "개인정보처리방침",
                subtitle = "개인정보 수집·이용·보관 정책",
                onClick = { selectedMenu = SettingsMenu.PRIVACY },
            )
            SettingsItem(
                icon = Icons.Filled.Info,
                title = "오픈소스 라이선스",
                subtitle = "사용된 오픈소스 라이브러리 목록",
                onClick = { selectedMenu = SettingsMenu.OPENSOURCE },
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text =
                    "버전 ${BuildConfig.VERSION_NAME}${
                        if (BuildConfig.DEBUG) "-${BuildConfig.BUILD_TYPE.uppercase()}" else ""
                    }",
                fontSize = 13.sp,
                color = colors.textMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Text(
                text = APP_COPYRIGHT_LINE,
                fontSize = 11.sp,
                color = colors.textMuted,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.bgCard, RoundedCornerShape(14.dp))
                .border(0.5.dp, colors.borderDefault, RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(42.dp)
                    .background(colors.bgIcon, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.purpleMain, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
            Text(subtitle, fontSize = 12.sp, color = colors.textMuted)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
    }
}
