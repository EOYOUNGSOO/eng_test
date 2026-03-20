package com.example.engtest.ui.screen

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engtest.EngTestApplication
import com.example.engtest.ui.theme.BadgePurpleBg
import com.example.engtest.ui.theme.BadgePurpleText
import com.example.engtest.ui.theme.BgCard
import com.example.engtest.ui.theme.BgCardAccent
import com.example.engtest.ui.theme.BgIcon
import com.example.engtest.ui.theme.BgIconGreen
import com.example.engtest.ui.theme.BgPrimary
import com.example.engtest.ui.theme.BorderAccent
import com.example.engtest.ui.theme.BorderDefault
import com.example.engtest.ui.theme.EngTestTheme
import com.example.engtest.ui.theme.GreenMain
import com.example.engtest.ui.theme.PinkMain
import com.example.engtest.ui.theme.PurpleLight
import com.example.engtest.ui.theme.PurpleMain
import com.example.engtest.ui.theme.TextDim
import com.example.engtest.ui.theme.TextMuted
import com.example.engtest.ui.theme.TextPrimary
import com.example.engtest.ui.theme.TextSecondary

@Composable
fun MainScreen(
    onNavigateToWordManage: () -> Unit,
    onNavigateToWordTest: () -> Unit,
    onNavigateToRecords: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as EngTestApplication
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(app))
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 20.dp)
        ) {
            AppHeader()
            Spacer(modifier = Modifier.height(20.dp))
            StatCardRow(
                totalWordCount = stats.wordCount,
                testCount = stats.testCount,
                avgScore = if (stats.testCount > 0) stats.averageScore.toInt() else 0
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MenuCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.MenuBook,
                    iconTint = PurpleMain,
                    iconBg = BgIcon,
                    title = "단어 관리",
                    description = "단어 탐색 · 즐겨찾기 · 초기화",
                    trailingContent = { ChevronIcon() },
                    containerColor = BgCard,
                    borderColor = BorderDefault,
                    onClick = onNavigateToWordManage
                )
                MenuCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.CheckCircle,
                    iconTint = PurpleLight,
                    iconBg = Color(0xFF231535),
                    title = "단어 테스트",
                    description = "10문제 · 랜덤 출제 · 레벨 선택",
                    trailingContent = { StartBadge() },
                    containerColor = BgCardAccent,
                    borderColor = BorderAccent,
                    onClick = onNavigateToWordTest
                )
                MenuCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.BarChart,
                    iconTint = GreenMain,
                    iconBg = BgIconGreen,
                    title = "기록 및 통계",
                    description = "점수 분석 · 취약 단어 · 성장 그래프",
                    trailingContent = { ChevronIcon() },
                    containerColor = BgCard,
                    borderColor = BorderDefault,
                    onClick = onNavigateToRecords
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "© 2026. 경주아빠. All rights reserved.",
                fontSize = 10.sp,
                color = Color(0xFF2E2D3D),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AppHeader() {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(PurpleMain)
            )
            Text(
                text = "VOCA MASTER",
                fontSize = 11.sp,
                color = TextDim,
                letterSpacing = 0.12.em
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "영어단어 암기장",
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "내 손안의 영단어 파트너 📖",
            fontSize = 13.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun StatCardRow(
    totalWordCount: Int,
    testCount: Int,
    avgScore: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            value = "%,d".format(totalWordCount),
            label = "전체 단어",
            valueColor = PurpleMain,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "$testCount",
            label = "테스트 횟수",
            valueColor = GreenMain,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "${avgScore}점",
            label = "평균 점수",
            valueColor = PinkMain,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(BgCard, RoundedCornerShape(14.dp))
            .border(0.5.dp, BorderDefault, RoundedCornerShape(14.dp))
            .padding(vertical = 16.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun MenuCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    description: String,
    trailingContent: @Composable () -> Unit,
    containerColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(18.dp))
            .border(0.5.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBg, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            trailingContent()
        }
    }
}

@Composable
private fun ChevronIcon() {
    Icon(
        imageVector = Icons.Filled.ChevronRight,
        contentDescription = null,
        tint = Color(0xFF3D3C52),
        modifier = Modifier.size(16.dp)
    )
}

@Composable
private fun StartBadge() {
    Box(
        modifier = Modifier
            .background(BadgePurpleBg, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "START",
            fontSize = 10.sp,
            color = BadgePurpleText,
            letterSpacing = 0.08.em,
            fontStyle = FontStyle.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    EngTestTheme {
        MainScreen(
            onNavigateToWordManage = {},
            onNavigateToWordTest = {},
            onNavigateToRecords = {}
        )
    }
}
