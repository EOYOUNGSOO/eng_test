package com.euysoo.engtest.ui.screen.wordtest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.ui.components.AppCard
import com.euysoo.engtest.ui.components.AppCopyrightFooter
import com.euysoo.engtest.ui.components.AppTopBar
import com.euysoo.engtest.ui.theme.AppDimens
import com.euysoo.engtest.ui.theme.AppTheme
import com.euysoo.engtest.ui.theme.mzBookEmoji
import com.euysoo.engtest.ui.theme.mzIconAccent

private const val INTERNAL_MY_BOOK = "__my_book__"

data class DifficultyOption(
    val key: String,
    val label: String,
    val subtitle: String,
    val tint: Color,
)

private enum class TestKind { Self, MultipleChoice }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordTestSelectScreen(
    onNavigateSelfTest: (String) -> Unit,
    onNavigateMultipleChoice: (String) -> Unit,
    onBack: () -> Unit,
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val app = context.applicationContext as EngTestApplication
    val selectVm: WordTestSelectViewModel = viewModel(factory = WordTestSelectViewModelFactory(app.appContainer))
    val books by selectVm.books.collectAsStateWithLifecycle()

    var testKind by remember { mutableStateOf<TestKind?>(null) }
    var showBookPicker by remember { mutableStateOf(false) }
    var showEmptyBookHint by remember { mutableStateOf(false) }

    val difficultyOptions =
        remember {
            listOf(
                DifficultyOption(DIFFICULTY_ALL, "전체", "모든 단어에서 10문항", Color(0xFF6366F1)),
                DifficultyOption(DIFFICULTY_ELEMENTARY, "초등", "초등 수준 단어만", Color(0xFF22C55E)),
                DifficultyOption(DIFFICULTY_MIDDLE, "중등", "중등 수준 단어만", Color(0xFFF59E0B)),
                DifficultyOption(DIFFICULTY_HIGH, "고등", "고등 수준 단어만", Color(0xFFEF4444)),
                DifficultyOption(INTERNAL_MY_BOOK, "나의 단어장", "담아 둔 단어 중 10문항", Color(0xFF9333EA)),
            )
        }

    fun navigateWithDifficulty(key: String) {
        when (testKind) {
            TestKind.Self -> onNavigateSelfTest(key)
            TestKind.MultipleChoice -> onNavigateMultipleChoice(key)
            null -> Unit
        }
    }

    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = {},
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .background(colors.bgPrimary),
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                item {
                    AppTopBar(title = "단어 테스트", onBackClick = onBack)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                if (testKind == null) {
                    item {
                        Text(
                            text = "테스트 유형을 선택하세요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary,
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                    item {
                        TypeCard(
                            title = "자기 테스트",
                            subtitle = "알겠음 / 모름 · 뜻 확인",
                            icon = Icons.Outlined.CheckCircle,
                            accentOrdinal = 0,
                            colors = colors,
                            onClick = { testKind = TestKind.Self },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                    item {
                        TypeCard(
                            title = "객관식",
                            subtitle = "4지선다 · 뜻 고르기",
                            icon = Icons.AutoMirrored.Outlined.List,
                            accentOrdinal = 1,
                            colors = colors,
                            onClick = { testKind = TestKind.MultipleChoice },
                        )
                    }
                } else {
                    item {
                        Text(
                            text =
                                when (testKind) {
                                    TestKind.Self -> "자기 테스트 — 난이도 선택"
                                    TestKind.MultipleChoice -> "객관식 — 난이도 선택"
                                    null -> ""
                                },
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary,
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item {
                        TextButton(onClick = { testKind = null }) {
                            Text("← 유형 다시 선택", color = colors.purpleMain)
                        }
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                    itemsIndexed(difficultyOptions, key = { _, o -> o.key }) { index, option ->
                        DifficultyCard(
                            option = option,
                            accentOrdinal = index,
                            colors = colors,
                            onClick = {
                                if (option.key == INTERNAL_MY_BOOK) {
                                    if (books.isEmpty()) {
                                        showEmptyBookHint = true
                                    } else {
                                        showBookPicker = true
                                    }
                                } else {
                                    navigateWithDifficulty(option.key)
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            AppCopyrightFooter()
        }
    }

    if (showEmptyBookHint) {
        AppStyledDialog(onDismiss = { showEmptyBookHint = false }) {
            Text(
                text = "나의 단어장",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "등록된 단어장이 없습니다. 메인 화면의 \"나의 단어장\"에서 단어장을 만든 뒤, 단어 관리에서 단어를 담아 주세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { showEmptyBookHint = false }) {
                    Text("확인", color = colors.purpleMain, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showBookPicker) {
        AppStyledDialog(onDismiss = { showBookPicker = false }) {
            Text(
                text = "단어장 선택",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(books, key = { _, b -> b.id }) { index, book ->
                    val accent = mzIconAccent(index)
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.bgPrimary, RoundedCornerShape(14.dp))
                                .border(AppDimens.appCardBorder, colors.borderDefault, RoundedCornerShape(14.dp))
                                .clickable {
                                    showBookPicker = false
                                    navigateWithDifficulty(myBookDifficultyKey(book.id))
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accent.background),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = mzBookEmoji(index), fontSize = 22.sp)
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.purpleMain,
                            )
                            Text(
                                text = book.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { showBookPicker = false }) {
                    Text("닫기", color = colors.purpleMain, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AppStyledDialog(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = colors.bgCard),
            border = BorderStroke(AppDimens.appCardBorder, colors.borderDefault),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp), content = content)
        }
    }
}

@Composable
private fun TypeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentOrdinal: Int,
    colors: com.euysoo.engtest.ui.theme.AppColors,
    onClick: () -> Unit,
) {
    val accent = mzIconAccent(accentOrdinal)
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colors.bgCard,
        borderColor = colors.borderDefault,
        onClick = onClick,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent.background),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent.foreground,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun DifficultyCard(
    option: DifficultyOption,
    accentOrdinal: Int,
    colors: com.euysoo.engtest.ui.theme.AppColors,
    onClick: () -> Unit,
) {
    val accent = mzIconAccent(accentOrdinal)
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colors.bgCard,
        borderColor = colors.borderDefault,
        onClick = onClick,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent.background),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = accent.foreground,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                )
                Text(
                    text = option.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                )
            }
        }
    }
}
