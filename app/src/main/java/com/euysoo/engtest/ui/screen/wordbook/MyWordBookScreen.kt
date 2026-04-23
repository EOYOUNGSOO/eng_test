package com.euysoo.engtest.ui.screen.wordbook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.data.entity.WordBook
import com.euysoo.engtest.domain.wrongnote.WrongNoteBookRepository
import com.euysoo.engtest.domain.wrongnote.WrongNoteDifficultyOption
import com.euysoo.engtest.domain.wrongnote.WrongNoteFillSelection
import com.euysoo.engtest.domain.wrongnote.WrongNoteOutcome
import com.euysoo.engtest.ui.component.AppButton
import com.euysoo.engtest.ui.component.AppButtonStyle
import com.euysoo.engtest.ui.component.AppFullWidthButton
import com.euysoo.engtest.ui.components.AppCopyrightFooter
import com.euysoo.engtest.ui.components.AppTopBar
import com.euysoo.engtest.ui.theme.AppColors
import com.euysoo.engtest.ui.theme.AppDimens
import com.euysoo.engtest.ui.theme.AppTheme
import com.euysoo.engtest.ui.theme.mzBookEmoji
import com.euysoo.engtest.ui.theme.mzIconAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWordBookScreen(
    onBack: () -> Unit,
    onOpenBook: (Long) -> Unit,
    onNavigateToOcrGuide: () -> Unit,
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val app = context.applicationContext as EngTestApplication
    val viewModel: MyWordBookViewModel = viewModel(factory = MyWordBookViewModelFactory(app.appContainer))
    val books by viewModel.books.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showWrongNoteDialog by remember { mutableStateOf(false) }
    var wrongNoteInfo by remember { mutableStateOf<String?>(null) }
    var mixPending by remember { mutableStateOf<WrongNoteMixPending?>(null) }
    var bookToRename by remember { mutableStateOf<WordBook?>(null) }
    var bookToDelete by remember { mutableStateOf<WordBook?>(null) }

    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = {
            AppTopBar(title = "나의 단어장", onBackClick = onBack)
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                item { Spacer(modifier = Modifier.height(12.dp)) }
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AppFullWidthButton(
                            text = "새 단어장 만들기",
                            onClick = { showCreateDialog = true },
                            style = AppButtonStyle.PRIMARY,
                        )
                        AppFullWidthButton(
                            text = "📷  이미지로 단어 추가",
                            onClick = onNavigateToOcrGuide,
                            style = AppButtonStyle.SECONDARY,
                        )
                        AppFullWidthButton(
                            text = "오답노트 만들기",
                            onClick = { showWrongNoteDialog = true },
                            style = AppButtonStyle.DANGER,
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
                if (books.isEmpty()) {
                    item {
                        Text(
                            text = "등록된 단어장이 없습니다.\n단어 관리에서 단어를 단어장에 담을 수 있습니다.",
                            fontSize = 14.sp,
                            color = colors.textMuted,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                } else {
                    itemsIndexed(books, key = { _, b -> b.id }) { index, book ->
                        WordBookRow(
                            listIndex = index,
                            book = book,
                            onOpen = { onOpenBook(book.id) },
                            onRename = { bookToRename = book },
                            onDelete = { bookToDelete = book },
                        )
                    }
                }
            }
            AppCopyrightFooter()
        }
    }

    if (showWrongNoteDialog) {
        WrongNoteCreateDialog(
            onDismiss = { showWrongNoteDialog = false },
            onSubmit = { title, difficulty, fill ->
                viewModel.createWrongNoteBook(title, difficulty, fill) { outcome ->
                    when (outcome) {
                        is WrongNoteOutcome.Created -> {
                            showWrongNoteDialog = false
                            wrongNoteInfo = "「${title.trim()}」 오답노트를 만들었습니다."
                        }
                        WrongNoteOutcome.EmptyTitle -> {
                            wrongNoteInfo = "오답노트 제목을 입력해 주세요."
                        }
                        WrongNoteOutcome.NoWrongHistory -> {
                            wrongNoteInfo = "아직 틀린 단어 기록이 없습니다. 단어 테스트를 먼저 진행해 주세요."
                        }
                        WrongNoteOutcome.NoWrongAfterDifficulty -> {
                            wrongNoteInfo = "선택한 난이도에 해당하는 오답 단어가 없습니다. 난이도를 바꿔 보세요."
                        }
                        is WrongNoteOutcome.NeedMixConfirm -> {
                            showWrongNoteDialog = false
                            mixPending =
                                WrongNoteMixPending(
                                    title = title.trim(),
                                    difficulty = difficulty,
                                    wrongCount = outcome.wrongCount,
                                )
                        }
                        WrongNoteOutcome.CreateFailed -> {
                            wrongNoteInfo = "만들기에 실패했습니다. 잠시 후 다시 시도해 주세요."
                        }
                    }
                }
            },
        )
    }

    mixPending?.let { pending ->
        AlertDialog(
            onDismissRequest = { mixPending = null },
            title = { Text("틀린 단어 ${pending.wrongCount}개") },
            text = {
                Text(
                    "100개를 채우려면 틀린 단어 외에 같은 난이도(또는 전체)에서 임의 단어를 섞어야 합니다.\n\n" +
                        "「예」는 섞어서 ${WrongNoteBookRepository.WRONG_NOTE_TARGET}개를 담고, " +
                        "「아니오」는 틀린 단어 ${pending.wrongCount}개만 담습니다.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val p = pending
                        mixPending = null
                        viewModel.createWrongNoteBook(
                            p.title,
                            p.difficulty,
                            WrongNoteFillSelection.MIX_100,
                        ) { outcome ->
                            when (outcome) {
                                is WrongNoteOutcome.Created -> {
                                    wrongNoteInfo = "「${p.title}」 오답노트를 만들었습니다."
                                }
                                WrongNoteOutcome.CreateFailed -> {
                                    wrongNoteInfo = "만들기에 실패했습니다. 잠시 후 다시 시도해 주세요."
                                }
                                else -> {
                                    wrongNoteInfo = "오답노트를 만들 수 없습니다. 조건을 확인해 주세요."
                                }
                            }
                        }
                    },
                ) {
                    Text("예", color = colors.purpleMain, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val p = pending
                        mixPending = null
                        viewModel.createWrongNoteBook(
                            p.title,
                            p.difficulty,
                            WrongNoteFillSelection.WRONG_ONLY_AVAILABLE,
                        ) { outcome ->
                            when (outcome) {
                                is WrongNoteOutcome.Created -> {
                                    wrongNoteInfo =
                                        "「${p.title}」 오답노트를 만들었습니다. (틀린 단어 ${p.wrongCount}개만 담음)"
                                }
                                WrongNoteOutcome.CreateFailed -> {
                                    wrongNoteInfo = "만들기에 실패했습니다. 잠시 후 다시 시도해 주세요."
                                }
                                else -> {
                                    wrongNoteInfo = "오답노트를 만들 수 없습니다."
                                }
                            }
                        }
                    },
                ) {
                    Text("아니오", fontWeight = FontWeight.Medium)
                }
            },
        )
    }

    wrongNoteInfo?.let { msg ->
        AlertDialog(
            onDismissRequest = { wrongNoteInfo = null },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { wrongNoteInfo = null }) {
                    Text("확인", color = colors.purpleMain)
                }
            },
        )
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("새 단어장") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("이름") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.createBook(name)
                        showCreateDialog = false
                    },
                ) {
                    Text("만들기", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("취소")
                }
            },
        )
    }

    bookToRename?.let { book ->
        var name by remember(book.id) { mutableStateOf(book.name) }
        AlertDialog(
            onDismissRequest = { bookToRename = null },
            title = { Text("이름 변경") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renameBook(book, name)
                        bookToRename = null
                    },
                ) {
                    Text("저장", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToRename = null }) {
                    Text("취소")
                }
            },
        )
    }

    bookToDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            title = { Text("단어장 삭제") },
            text = { Text("\"${book.name}\"을(를) 삭제할까요? 포함된 단어 연결만 제거됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBook(book.id)
                        bookToDelete = null
                    },
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToDelete = null }) {
                    Text("취소")
                }
            },
        )
    }
}

@Composable
private fun WordBookRow(
    listIndex: Int,
    book: WordBook,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = AppTheme.colors
    val accent = mzIconAccent(listIndex)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.bgPrimary, RoundedCornerShape(16.dp))
                .border(AppDimens.appCardBorder, colors.borderDefault, RoundedCornerShape(16.dp))
                .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
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
                    Text(text = mzBookEmoji(listIndex), fontSize = 22.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "${listIndex + 1}.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.purpleMain,
                        )
                        Text(
                            text = book.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                        )
                    }
                }
            }
            AppButton(text = "열기", onClick = onOpen, style = AppButtonStyle.PRIMARY)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppButton(text = "이름 변경", onClick = onRename, style = AppButtonStyle.SECONDARY)
            AppButton(text = "삭제", onClick = onDelete, style = AppButtonStyle.DANGER)
        }
    }
}

private data class WrongNoteMixPending(
    val title: String,
    val difficulty: WrongNoteDifficultyOption,
    val wrongCount: Int,
)

@Composable
private fun WrongNoteDifficultyChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: AppColors,
) {
    val bg = if (selected) colors.purpleLight.copy(alpha = 0.15f) else colors.bgPrimary
    val borderC = if (selected) colors.purpleMain else colors.borderDefault
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(AppDimens.appCardBorder, borderC, RoundedCornerShape(20.dp))
                .background(bg, RoundedCornerShape(20.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.purpleMain else colors.textSecondary,
        )
    }
}

@Composable
private fun WrongNoteFillOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = if (selected) 1.dp else AppDimens.appCardBorder,
                    color = if (selected) colors.purpleMain else colors.borderDefault,
                    shape = RoundedCornerShape(14.dp),
                ).background(
                    if (selected) colors.purpleLight.copy(alpha = 0.12f) else colors.bgPrimary,
                    RoundedCornerShape(14.dp),
                ).clickable(onClick = onClick)
                .padding(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = colors.textMuted,
        )
    }
}

@Composable
private fun WrongNoteCreateDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, WrongNoteDifficultyOption, WrongNoteFillSelection) -> Unit,
) {
    val colors = AppTheme.colors
    var title by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf(WrongNoteDifficultyOption.ALL) }
    var fill by remember { mutableStateOf(WrongNoteFillSelection.WRONG_RANDOM_100) }
    val scroll = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = colors.bgCard),
            border = BorderStroke(AppDimens.appCardBorder, colors.borderDefault),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(20.dp)
                        .verticalScroll(scroll),
            ) {
                Text(
                    text = "오답노트 만들기",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "저장된 테스트 결과에서 한 번이라도 틀린 단어를 모읍니다.",
                    fontSize = 13.sp,
                    color = colors.textMuted,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("오답노트 제목") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "난이도",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.purpleMain,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WrongNoteDifficultyChip(
                        label = "전체",
                        selected = difficulty == WrongNoteDifficultyOption.ALL,
                        onClick = { difficulty = WrongNoteDifficultyOption.ALL },
                        colors = colors,
                    )
                    WrongNoteDifficultyChip(
                        label = "초등",
                        selected = difficulty == WrongNoteDifficultyOption.ELEMENTARY,
                        onClick = { difficulty = WrongNoteDifficultyOption.ELEMENTARY },
                        colors = colors,
                    )
                    WrongNoteDifficultyChip(
                        label = "중등",
                        selected = difficulty == WrongNoteDifficultyOption.MIDDLE,
                        onClick = { difficulty = WrongNoteDifficultyOption.MIDDLE },
                        colors = colors,
                    )
                    WrongNoteDifficultyChip(
                        label = "고등",
                        selected = difficulty == WrongNoteDifficultyOption.HIGH,
                        onClick = { difficulty = WrongNoteDifficultyOption.HIGH },
                        colors = colors,
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "단어 구성",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.purpleMain,
                )
                Spacer(modifier = Modifier.height(8.dp))
                WrongNoteFillOptionRow(
                    title = "틀린 단어 임의 100개",
                    subtitle = "오답으로 남은 단어 중에서만 무작위로 최대 100개를 담습니다. (100개 미만이면 안내 후 선택)",
                    selected = fill == WrongNoteFillSelection.WRONG_RANDOM_100,
                    onClick = { fill = WrongNoteFillSelection.WRONG_RANDOM_100 },
                )
                Spacer(modifier = Modifier.height(8.dp))
                WrongNoteFillOptionRow(
                    title = "틀린 단어와 임의 단어 섞어 100개",
                    subtitle = "오답 단어를 우선 담고, 부족하면 같은 난이도(또는 전체)에서 임의로 채웁니다.",
                    selected = fill == WrongNoteFillSelection.MIX_100,
                    onClick = { fill = WrongNoteFillSelection.MIX_100 },
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소", color = colors.textMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onSubmit(title, difficulty, fill) },
                        enabled = title.isNotBlank(),
                    ) {
                        Text("만들기", color = colors.purpleMain, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
