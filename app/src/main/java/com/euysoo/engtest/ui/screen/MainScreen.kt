package com.euysoo.engtest.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.ui.component.AppButton
import com.euysoo.engtest.ui.component.AppButtonStyle
import com.euysoo.engtest.ui.components.AppCopyrightFooter
import com.euysoo.engtest.ui.components.ScrollColumnWithBottomCopyright
import com.euysoo.engtest.ui.theme.AppTheme
import com.euysoo.engtest.ui.theme.EngTestTheme
import kotlin.math.max
import kotlin.math.roundToInt

private data class MainHomeLayout(
    val isLandscape: Boolean,
    val horizontalPadding: Dp,
    val topPadding: Dp,
    val bottomPadding: Dp,
    val gapHeaderStats: Dp,
    val gapStatsMenu: Dp,
    val menuCardsSpacing: Dp,
    val gapFooter: Dp,
    val brandSp: TextUnit,
    val titleSp: TextUnit,
    val badgeSp: TextUnit,
    val statValueSp: TextUnit,
    val statLabelSp: TextUnit,
    val statPadV: Dp,
    val statPadH: Dp,
    val statCorner: Dp,
    val menuIconBox: Dp,
    val menuIconInner: Dp,
    val menuTitleSp: TextUnit,
    val menuDescSp: TextUnit,
    val menuInnerPadH: Dp,
    val menuRowSpace: Dp,
    val menuCorner: Dp,
    val chevronSize: Dp,
    val footerSp: TextUnit,
    val descMaxLines: Int,
)

@Composable
private fun rememberMainHomeLayout(): MainHomeLayout {
    val cfg = LocalConfiguration.current
    return remember(cfg.orientation, cfg.screenWidthDp, cfg.screenHeightDp) {
        val w = cfg.screenWidthDp.coerceAtLeast(1)
        val land = cfg.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (land) {
            val padH = max(12, (w * 0.024f).roundToInt()).dp.coerceAtMost(40.dp)
            val narrow = w < 600
            val statScale = if (narrow) 0.88f else 0.92f
            val menuScale = if (narrow) 0.9f else 0.95f
            MainHomeLayout(
                isLandscape = true,
                horizontalPadding = padH,
                topPadding = 12.dp,
                bottomPadding = 10.dp,
                gapHeaderStats = 10.dp,
                gapStatsMenu = 8.dp,
                menuCardsSpacing = max(6, (w * 0.01f).roundToInt()).dp.coerceAtMost(12.dp),
                gapFooter = 10.dp,
                brandSp = 10.sp,
                titleSp = (if (narrow) 20 else 22).sp,
                badgeSp = 10.sp,
                statValueSp = (22f * statScale).sp,
                statLabelSp = (10f * statScale).sp,
                statPadV = (14f * statScale).dp.coerceAtLeast(8.dp),
                statPadH = (8f * statScale).dp.coerceAtLeast(4.dp),
                statCorner = (13f * statScale).dp,
                menuIconBox = (44f * menuScale).dp,
                menuIconInner = (20f * menuScale).dp,
                menuTitleSp = (15f * menuScale).sp,
                menuDescSp = (12f * menuScale).sp,
                menuInnerPadH = (12f * menuScale).dp.coerceAtLeast(6.dp),
                menuRowSpace = (10f * menuScale).dp.coerceAtLeast(6.dp),
                menuCorner = (16f * menuScale).dp,
                chevronSize = (16f * menuScale).dp,
                footerSp = 9.sp,
                descMaxLines = 2,
            )
        } else {
            val ratio = (w / 360f).coerceIn(0.9f, 1.12f)
            MainHomeLayout(
                isLandscape = false,
                horizontalPadding = (20f * ratio).dp.coerceIn(16.dp, 26.dp),
                topPadding = (20f * ratio).dp.coerceIn(16.dp, 24.dp),
                bottomPadding = (20f * ratio).dp.coerceIn(16.dp, 24.dp),
                gapHeaderStats = (20f * ratio).dp.coerceIn(16.dp, 24.dp),
                gapStatsMenu = (16f * ratio).dp.coerceIn(12.dp, 20.dp),
                menuCardsSpacing = (10f * ratio).dp.coerceIn(8.dp, 14.dp),
                gapFooter = (14f * ratio).dp.coerceIn(10.dp, 18.dp),
                brandSp = (11f * ratio).sp,
                titleSp = (26f * ratio).sp,
                badgeSp = (11f * ratio).sp,
                statValueSp = (22f * ratio).sp,
                statLabelSp = (10f * ratio).sp,
                statPadV = (16f * ratio).dp,
                statPadH = (8f * ratio).dp,
                statCorner = (14f * ratio).dp,
                menuIconBox = (44f * ratio).dp,
                menuIconInner = (20f * ratio).dp,
                menuTitleSp = (15f * ratio).sp,
                menuDescSp = (12f * ratio).sp,
                menuInnerPadH = (16f * ratio).dp,
                menuRowSpace = 12.dp,
                menuCorner = (18f * ratio).dp,
                chevronSize = (16f * ratio).dp,
                footerSp = (10f * ratio).sp,
                descMaxLines = 1,
            )
        }
    }
}

@Composable
fun MainScreen(
    onNavigateToWordManage: () -> Unit,
    onNavigateToMyWordBook: () -> Unit,
    onNavigateToWordTest: () -> Unit,
    onNavigateToRecords: () -> Unit,
) {
    @Suppress("DEPRECATION")
    val menuBookIcon = Icons.Outlined.MenuBook
    val colors = AppTheme.colors
    val context = LocalContext.current
    val app = context.applicationContext as EngTestApplication
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(app.appContainer))
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val layout = rememberMainHomeLayout()

    /** 가로 모드는 세로 공간이 좁아 스크롤로 전체(저작권 포함) 탐색 */
    val needsScroll = layout.isLandscape

    val contentModifier =
        Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .then(if (layout.isLandscape) Modifier.widthIn(max = 920.dp) else Modifier)
            .systemBarsPadding()
            .padding(horizontal = layout.horizontalPadding)
            .padding(top = layout.topPadding, bottom = layout.bottomPadding)

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colors.bgPrimary),
    ) {
        if (needsScroll) {
            ScrollColumnWithBottomCopyright(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .then(contentModifier),
                copyrightFontSize = layout.footerSp,
                mainContent = {
                    MainHomeScrollableBody(
                        layout = layout,
                        needsScroll = true,
                        stats = stats,
                        menuBookIcon = menuBookIcon,
                        onNavigateToWordManage = onNavigateToWordManage,
                        onNavigateToMyWordBook = onNavigateToMyWordBook,
                        onNavigateToWordTest = onNavigateToWordTest,
                        onNavigateToRecords = onNavigateToRecords,
                    )
                },
            )
        } else {
            Column(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .then(contentModifier),
            ) {
                // 본문만 세로로 확장 — 루트에서 fillMaxHeight를 쓰면 저작권이 화면 밖으로 밀림
                Column(
                    modifier =
                        Modifier
                            .weight(1f, fill = true)
                            .fillMaxWidth()
                            .fillMaxHeight(),
                ) {
                    MainHomeScrollableBody(
                        layout = layout,
                        needsScroll = false,
                        stats = stats,
                        menuBookIcon = menuBookIcon,
                        onNavigateToWordManage = onNavigateToWordManage,
                        onNavigateToMyWordBook = onNavigateToMyWordBook,
                        onNavigateToWordTest = onNavigateToWordTest,
                        onNavigateToRecords = onNavigateToRecords,
                    )
                }
                Spacer(modifier = Modifier.height(layout.gapFooter))
                AppCopyrightFooter(fontSize = layout.footerSp)
            }
        }
    }
}

@Composable
private fun MainHomeScrollableBody(
    layout: MainHomeLayout,
    needsScroll: Boolean,
    stats: HomeStats,
    menuBookIcon: ImageVector,
    onNavigateToWordManage: () -> Unit,
    onNavigateToMyWordBook: () -> Unit,
    onNavigateToWordTest: () -> Unit,
    onNavigateToRecords: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (!needsScroll) Modifier.fillMaxHeight() else Modifier),
    ) {
        AppHeader(layout = layout)
        Spacer(modifier = Modifier.height(layout.gapHeaderStats))
        StatCardRow(
            layout = layout,
            totalWordCount = stats.wordCount,
            testCount = stats.testCount,
            avgScore = if (stats.testCount > 0) stats.averageScore.toInt() else 0,
        )
        Spacer(modifier = Modifier.height(layout.gapStatsMenu))

        if (layout.isLandscape) {
            val menuColumnMod =
                if (needsScroll) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .fillMaxHeight()
                }
            val rowMod =
                if (needsScroll) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                }
            Column(
                modifier = menuColumnMod,
                verticalArrangement = Arrangement.spacedBy(layout.menuCardsSpacing),
            ) {
                Row(
                    modifier = rowMod,
                    horizontalArrangement = Arrangement.spacedBy(layout.menuCardsSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val landscapeCardMod =
                        if (needsScroll) {
                            Modifier
                                .weight(1f)
                                .heightIn(min = 104.dp)
                        } else {
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        }
                    MenuCard(
                        modifier = landscapeCardMod,
                        layout = layout,
                        icon = menuBookIcon,
                        iconTint = colors.purpleMain,
                        iconBg = colors.bgIcon,
                        title = "단어 관리",
                        description = "단어탐색, 단어 추가, 단어 수정",
                        trailingContent = { ChevronIcon(size = layout.chevronSize) },
                        containerColor = colors.bgCard,
                        borderColor = colors.borderDefault,
                        onClick = onNavigateToWordManage,
                    )
                    MenuCard(
                        modifier = landscapeCardMod,
                        layout = layout,
                        icon = Icons.AutoMirrored.Outlined.LibraryBooks,
                        iconTint = Color(0xFF7C3AED),
                        iconBg = colors.bgIcon,
                        title = "나의 단어장",
                        description = "단어장 만들기 · 단어 담기",
                        trailingContent = { ChevronIcon(size = layout.chevronSize) },
                        containerColor = colors.bgCard,
                        borderColor = colors.borderDefault,
                        onClick = onNavigateToMyWordBook,
                    )
                }
                Row(
                    modifier = rowMod,
                    horizontalArrangement = Arrangement.spacedBy(layout.menuCardsSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val landscapeCardMod =
                        if (needsScroll) {
                            Modifier
                                .weight(1f)
                                .heightIn(min = 104.dp)
                        } else {
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        }
                    MenuCard(
                        modifier = landscapeCardMod,
                        layout = layout,
                        icon = Icons.Outlined.CheckCircle,
                        iconTint = colors.purpleLight,
                        iconBg = Color(0xFF231535),
                        title = "단어 테스트",
                        description = "10문제 · 유형·난이도 선택",
                        trailingContent = {
                            AppButton(
                                text = "START",
                                onClick = onNavigateToWordTest,
                                style = AppButtonStyle.PRIMARY,
                            )
                        },
                        containerColor = colors.bgCardAccent,
                        borderColor = colors.borderAccent,
                        onClick = onNavigateToWordTest,
                    )
                    MenuCard(
                        modifier = landscapeCardMod,
                        layout = layout,
                        icon = Icons.Outlined.BarChart,
                        iconTint = colors.greenMain,
                        iconBg = colors.bgIconGreen,
                        title = "기록 및 통계",
                        description = "테스트결과 목록, 결과 상세보기",
                        trailingContent = { ChevronIcon(size = layout.chevronSize) },
                        containerColor = colors.bgCard,
                        borderColor = colors.borderDefault,
                        onClick = onNavigateToRecords,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(layout.menuCardsSpacing),
            ) {
                MenuCard(
                    modifier = Modifier.weight(1f),
                    layout = layout,
                    icon = menuBookIcon,
                    iconTint = colors.purpleMain,
                    iconBg = colors.bgIcon,
                    title = "단어 관리",
                    description = "단어탐색, 단어 추가, 단어 수정",
                    trailingContent = { ChevronIcon(size = layout.chevronSize) },
                    containerColor = colors.bgCard,
                    borderColor = colors.borderDefault,
                    onClick = onNavigateToWordManage,
                )
                MenuCard(
                    modifier = Modifier.weight(1f),
                    layout = layout,
                    icon = Icons.AutoMirrored.Outlined.LibraryBooks,
                    iconTint = Color(0xFF7C3AED),
                    iconBg = colors.bgIcon,
                    title = "나의 단어장",
                    description = "단어장 만들기 · 단어 담기",
                    trailingContent = { ChevronIcon(size = layout.chevronSize) },
                    containerColor = colors.bgCard,
                    borderColor = colors.borderDefault,
                    onClick = onNavigateToMyWordBook,
                )
                MenuCard(
                    modifier = Modifier.weight(1f),
                    layout = layout,
                    icon = Icons.Outlined.CheckCircle,
                    iconTint = colors.purpleLight,
                    iconBg = Color(0xFF231535),
                    title = "단어 테스트",
                    description = "10문제 · 유형·난이도 선택",
                    trailingContent = {
                        AppButton(
                            text = "START",
                            onClick = onNavigateToWordTest,
                            style = AppButtonStyle.PRIMARY,
                        )
                    },
                    containerColor = colors.bgCardAccent,
                    borderColor = colors.borderAccent,
                    onClick = onNavigateToWordTest,
                )
                MenuCard(
                    modifier = Modifier.weight(1f),
                    layout = layout,
                    icon = Icons.Outlined.BarChart,
                    iconTint = colors.greenMain,
                    iconBg = colors.bgIconGreen,
                    title = "기록 및 통계",
                    description = "테스트결과 목록, 결과 상세보기",
                    trailingContent = { ChevronIcon(size = layout.chevronSize) },
                    containerColor = colors.bgCard,
                    borderColor = colors.borderDefault,
                    onClick = onNavigateToRecords,
                )
            }
        }
    }
}

@Composable
private fun AppHeader(layout: MainHomeLayout) {
    val colors = AppTheme.colors
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.purpleMain),
            )
            Text(
                text = "VOCA MASTER",
                fontSize = layout.brandSp,
                color = colors.textDim,
                letterSpacing = 0.10.em,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "영어단어 암기장",
            fontSize = layout.titleSp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .wrapContentWidth()
                    .background(colors.bgIcon, RoundedCornerShape(20.dp))
                    .border(0.5.dp, colors.borderDefault, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(colors.purpleMain),
            )
            Text(
                text = "교육부 필수어휘 3,000개",
                fontSize = layout.badgeSp,
                color = colors.purpleMain,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun StatCardRow(
    layout: MainHomeLayout,
    totalWordCount: Int,
    testCount: Int,
    avgScore: Int,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard(
            layout = layout,
            value = "%,d".format(totalWordCount),
            label = "전체 단어",
            valueColor = colors.purpleMain,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            layout = layout,
            value = "$testCount",
            label = "테스트 횟수",
            valueColor = colors.greenMain,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            layout = layout,
            value = "${avgScore}점",
            label = "평균 점수",
            valueColor = colors.pinkMain,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    layout: MainHomeLayout,
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Box(
        modifier =
            modifier
                .background(colors.bgCard, RoundedCornerShape(layout.statCorner))
                .border(0.5.dp, colors.borderDefault, RoundedCornerShape(layout.statCorner))
                .padding(vertical = layout.statPadV, horizontal = layout.statPadH),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = layout.statValueSp,
                fontWeight = FontWeight.Medium,
                color = valueColor,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = layout.statLabelSp,
                color = colors.textMuted,
            )
        }
    }
}

@Composable
private fun MenuCard(
    modifier: Modifier = Modifier,
    layout: MainHomeLayout,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    description: String,
    trailingContent: @Composable () -> Unit,
    containerColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(containerColor, RoundedCornerShape(layout.menuCorner))
                .border(0.5.dp, borderColor, RoundedCornerShape(layout.menuCorner))
                .clickable(onClick = onClick)
                .padding(horizontal = layout.menuInnerPadH),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(layout.menuRowSpace),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(layout.menuIconBox)
                        .background(iconBg, RoundedCornerShape((layout.menuCorner.value * 0.72f).dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(layout.menuIconInner),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = layout.menuTitleSp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = layout.menuDescSp,
                    color = colors.textMuted,
                    maxLines = layout.descMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            trailingContent()
        }
    }
}

@Composable
private fun ChevronIcon(size: Dp) {
    Icon(
        imageVector = Icons.Filled.ChevronRight,
        contentDescription = null,
        tint = Color(0xFF3D3C52),
        modifier = Modifier.size(size),
    )
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    EngTestTheme {
        MainScreen(
            onNavigateToWordManage = {},
            onNavigateToMyWordBook = {},
            onNavigateToWordTest = {},
            onNavigateToRecords = {},
        )
    }
}
