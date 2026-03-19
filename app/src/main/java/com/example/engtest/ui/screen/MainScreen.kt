package com.example.engtest.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engtest.EngTestApplication
import com.example.engtest.ui.theme.EngTestTheme

private val CaptionGray = Color(0xFF6B7280)
private val PastelBlue = Color(0xFFDBEAFE)
private val PastelGreen = Color(0xFFD1FAE5)
private val PastelViolet = Color(0xFFEDE9FE)

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

    val scrollState = rememberScrollState()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "영어단어 테스트",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "단어를 관리하고 테스트해 보세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // 2단계: 하나의 대시보드 카드 + 숫자 강조 + 라벨 캡션 + 파스텔 구분
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeStatChip(
                        label = "전체 단어 수",
                        value = "${stats.wordCount}",
                        pastelBg = PastelBlue,
                        modifier = Modifier.weight(1f)
                    )
                    HomeStatChip(
                        label = "테스트 건수",
                        value = "${stats.testCount}",
                        pastelBg = PastelGreen,
                        modifier = Modifier.weight(1f)
                    )
                    HomeStatChip(
                        label = "평균 점수",
                        value = if (stats.testCount > 0) "${stats.averageScore.toInt()}점" else "-",
                        pastelBg = PastelViolet,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            MenuCard(
                title = "단어 관리",
                description = "단어 추가·수정·삭제 및 초기 데이터로 초기화",
                icon = Icons.Outlined.MenuBook,
                onClick = onNavigateToWordManage
            )
            Spacer(modifier = Modifier.height(12.dp))
            MenuCard(
                title = "단어 테스트",
                description = "10개 단어로 테스트 진행",
                icon = Icons.Outlined.Assignment,
                onClick = onNavigateToWordTest
            )
            Spacer(modifier = Modifier.height(12.dp))
            MenuCard(
                title = "기록 및 통계",
                description = "테스트 이력과 점수 확인",
                icon = Icons.Outlined.BarChart,
                onClick = onNavigateToRecords
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val captionColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                val captionStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                Text(
                    text = "© 2026. ",
                    style = captionStyle,
                    color = captionColor
                )
                Text(
                    text = "경주아빠",
                    style = captionStyle.copy(fontWeight = FontWeight.Bold),
                    color = captionColor
                )
                Text(
                    text = ". All rights reserved.",
                    style = captionStyle,
                    color = captionColor
                )
            }
            Spacer(modifier = Modifier.height(80.dp))
        }

        // 4단계: 퀵 테스트 FAB
        FloatingActionButton(
            onClick = onNavigateToWordTest,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Assignment,
                contentDescription = "퀵 테스트 시작",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun HomeStatChip(
    label: String,
    value: String,
    pastelBg: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(pastelBg.copy(alpha = 0.8f))
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = CaptionGray
        )
    }
}

@Composable
private fun MenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = CaptionGray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
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
