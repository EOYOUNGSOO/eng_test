package com.euysoo.engtest.ui.screen

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
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.ui.component.AppButton
import com.euysoo.engtest.ui.component.AppButtonStyle
import com.euysoo.engtest.ui.theme.AppTheme
import com.euysoo.engtest.ui.theme.EngTestTheme

@Composable
fun MainScreen(
    onNavigateToWordManage: () -> Unit,
    onNavigateToWordTest: () -> Unit,
    onNavigateToRecords: () -> Unit,
) {
    @Suppress("DEPRECATION")
    val menuBookIcon = Icons.Outlined.MenuBook
    val colors = AppTheme.colors
    val context = LocalContext.current
    val app = context.applicationContext as EngTestApplication
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(app))
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPrimary)
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
                    icon = menuBookIcon,
                    iconTint = colors.purpleMain,
                    iconBg = colors.bgIcon,
                    title = "단어 관리",
                    description = "단어탐색, 단어 추가, 단어 수정",
                    trailingContent = { ChevronIcon() },
                    containerColor = colors.bgCard,
                    borderColor = colors.borderDefault,
                    onClick = onNavigateToWordManage
                )
                MenuCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.CheckCircle,
                    iconTint = colors.purpleLight,
                    iconBg = Color(0xFF231535),
                    title = "단어 테스트",
                    description = "10문제 · 랜덤 출제 · 레벨 선택",
                    trailingContent = {
                        AppButton(
                            text = "START",
                            onClick = onNavigateToWordTest,
                            style = AppButtonStyle.PRIMARY
                        )
                    },
                    containerColor = colors.bgCardAccent,
                    borderColor = colors.borderAccent,
                    onClick = onNavigateToWordTest
                )
                MenuCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.BarChart,
                    iconTint = colors.greenMain,
                    iconBg = colors.bgIconGreen,
                    title = "기록 및 통계",
                    description = "테스트결과 목록, 결과 상세보기",
                    trailingContent = { ChevronIcon() },
                    containerColor = colors.bgCard,
                    borderColor = colors.borderDefault,
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
    val colors = AppTheme.colors
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colors.purpleMain)
            )
            Text(
                text = "VOCA MASTER",
                fontSize = 11.sp,
                color = colors.textDim,
                letterSpacing = 0.10.em
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "영어단어 암기장",
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentWidth()
                .background(colors.bgIcon, RoundedCornerShape(20.dp))
                .border(0.5.dp, colors.borderDefault, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(colors.purpleMain)
            )
            Text(
                text = "교육부 필수어휘 3,000개",
                fontSize = 11.sp,
                color = colors.purpleMain,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatCardRow(
    totalWordCount: Int,
    testCount: Int,
    avgScore: Int
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            value = "%,d".format(totalWordCount),
            label = "전체 단어",
            valueColor = colors.purpleMain,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "$testCount",
            label = "테스트 횟수",
            valueColor = colors.greenMain,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "${avgScore}점",
            label = "평균 점수",
            valueColor = colors.pinkMain,
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
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .background(colors.bgCard, RoundedCornerShape(14.dp))
            .border(0.5.dp, colors.borderDefault, RoundedCornerShape(14.dp))
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
                color = colors.textMuted
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
    val colors = AppTheme.colors
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
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = colors.textMuted,
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

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    EngTestTheme {
        MainScreen(
            onNavigateToWordManage = {},
            onNavigateToWordTest = {},
            onNavigateToRecords = {},
        )
    }
}
