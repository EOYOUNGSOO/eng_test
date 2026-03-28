@file:Suppress("SpellCheckingInspection")

package com.euysoo.engtest.ui.screen.wordmanage

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.R
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.data.entity.WordDifficulty
import com.euysoo.engtest.domain.model.WordWithStats
import com.euysoo.engtest.ui.component.AppButton
import com.euysoo.engtest.ui.component.AppButtonStyle
import com.euysoo.engtest.ui.component.AppChipButton
import com.euysoo.engtest.ui.component.AppFullWidthButton
import com.euysoo.engtest.ui.components.AppCopyrightFooter
import com.euysoo.engtest.ui.components.AppTopBar
import com.euysoo.engtest.ui.components.AppTopBarPill
import com.euysoo.engtest.ui.theme.AppTheme
import com.euysoo.engtest.ui.worddetail.WordDetailBottomSheet
import com.euysoo.engtest.ui.worddetail.WordDetailViewModel
import com.euysoo.engtest.ui.worddetail.WordDetailViewModelFactory
import com.euysoo.engtest.util.phoneticDisplayText
import com.euysoo.engtest.util.starCount
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordManageScreen(onBack: () -> Unit) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val app = context.applicationContext as EngTestApplication
    val viewModel: WordManageViewModel =
        viewModel(
            factory = WordManageViewModelFactory(app.appContainer),
        )
    val wordDetailViewModel: WordDetailViewModel =
        viewModel(
            factory = WordDetailViewModelFactory(app.appContainer),
        )

    val wordsWithStats by viewModel.wordsWithStats.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val recentlyAddedIds by viewModel.recentlyAddedIds.collectAsStateWithLifecycle()
    val showInitButton by viewModel.showInitButton.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.clearRecentlyAdded()
    }
    val wordToEdit by viewModel.wordToEdit.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showAddWordDialog by remember { mutableStateOf(false) }
    var wordToDelete by remember { mutableStateOf<Word?>(null) }
    var wordForBook by remember { mutableStateOf<Word?>(null) }
    val wordBooks by viewModel.wordBooks.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    var selectedWordForDetail by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.consumeSnackbarMessage()
    }

    var ttsReady by remember { mutableStateOf(false) }
    val tts =
        remember {
            TextToSpeech(context.applicationContext) { status: Int ->
                ttsReady = (status == TextToSpeech.SUCCESS)
            }
        }
    LaunchedEffect(ttsReady) {
        if (ttsReady) tts.language = Locale.US
    }
    DisposableEffect(tts) {
        onDispose {
            try {
                tts.stop()
                tts.shutdown()
            } catch (_: Exception) {
                // TTS 해제 시 예외 무시
            }
        }
    }

    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = {},
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    AppTopBar(
                        title = "단어 관리",
                        onBackClick = onBack,
                        trailingBadgeText = "%,d".format(totalCount),
                        trailingExtras = {
                            AppTopBarPill(
                                text = "단어추가",
                                enabled = syncState !is SyncUiState.Loading,
                                onClick = { showAddWordDialog = true },
                            )
                            if (showInitButton) {
                                AppTopBarPill(
                                    text = "초기화",
                                    enabled = syncState !is SyncUiState.Loading,
                                    contentColor = colors.pinkMain,
                                    onClick = { showConfirmDialog = true },
                                )
                            }
                        },
                    )
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
                item {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .background(colors.bgCard, RoundedCornerShape(14.dp))
                                .border(0.5.dp, colors.borderDefault, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(16.dp),
                            )
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                singleLine = true,
                                textStyle =
                                    androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        color = colors.textSecondary,
                                    ),
                                cursorBrush = SolidColor(colors.purpleMain),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "영어 단어 검색",
                                                fontSize = 13.sp,
                                                color = colors.textMuted,
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val levels = listOf("전체", "초등", "중등", "고등")
                        val selectedLevel =
                            when (filter) {
                                null -> "전체"
                                WordDifficulty.ELEMENTARY -> "초등"
                                WordDifficulty.MIDDLE -> "중등"
                                WordDifficulty.HIGH -> "고등"
                            }
                        levels.forEach { level ->
                            val isSelected = selectedLevel == level
                            AppChipButton(
                                text = level,
                                selected = isSelected,
                                onClick = {
                                    when (level) {
                                        "전체" -> viewModel.setFilter(null)
                                        "초등" -> viewModel.setFilter(WordDifficulty.ELEMENTARY)
                                        "중등" -> viewModel.setFilter(WordDifficulty.MIDDLE)
                                        "고등" -> viewModel.setFilter(WordDifficulty.HIGH)
                                    }
                                },
                            )
                        }
                    }
                }
                itemsIndexed(
                    wordsWithStats,
                    key = { _, it -> it.word.id },
                ) { _, item ->
                    WordListItem(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        item = item,
                        isRecentlyAdded = item.word.id in recentlyAddedIds,
                        onSpeak = { text -> if (ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null) },
                        onDetail = { selectedWordForDetail = item.word.word },
                        onEdit = { viewModel.setWordToEdit(item.word) },
                        onDelete = { wordToDelete = item.word },
                        onAddToBook = { wordForBook = item.word },
                    )
                }
                item {
                    AppCopyrightFooter(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            if (syncState is SyncUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("초기 단어 등록") },
            text = {
                Text("교육부 필수어휘 파일과 DB를 동기화합니다. 계속하시겠습니까?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.loadInitialWords()
                    },
                ) {
                    Text("확인", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("취소", color = MaterialTheme.colorScheme.onSurface)
                }
            },
        )
    }

    wordToDelete?.let { word ->
        AlertDialog(
            onDismissRequest = { wordToDelete = null },
            title = { Text("단어 삭제") },
            text = { Text("이 단어를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWord(word)
                        wordToDelete = null
                    },
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { wordToDelete = null }) {
                    Text("취소", color = MaterialTheme.colorScheme.onSurface)
                }
            },
        )
    }

    wordToEdit?.let { word ->
        WordEditDialog(
            word = word,
            onDismiss = { viewModel.setWordToEdit(null) },
            onSave = { updated -> viewModel.updateWord(updated) },
        )
    }

    if (showAddWordDialog) {
        AddWordDialog(
            snackbarHostState = snackbarHostState,
            onDismiss = {
                showAddWordDialog = false
            },
            onSave = { newWord ->
                scope.launch {
                    val outcome = viewModel.addWordIfNew(newWord)
                    showAddWordDialog = false
                    val message =
                        when (outcome) {
                            AddWordOutcome.ADDED -> "단어가 추가되었습니다"
                            AddWordOutcome.DUPLICATE -> "이미 존재하는 단어입니다"
                            AddWordOutcome.FAILED -> context.getString(R.string.snackbar_word_add_failed)
                        }
                    snackbarHostState.showSnackbar(message)
                }
            },
        )
    }

    if (syncState is SyncUiState.Success) {
        SyncResultDialog(
            result = (syncState as SyncUiState.Success).result,
            onDismiss = { viewModel.resetSyncState() },
        )
    }

    if (syncState is SyncUiState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.resetSyncState() },
            title = { Text("오류") },
            text = { Text((syncState as SyncUiState.Error).message) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetSyncState() }) {
                    Text("확인")
                }
            },
        )
    }

    selectedWordForDetail?.let { targetWord ->
        WordDetailBottomSheet(
            word = targetWord,
            viewModel = wordDetailViewModel,
            onDismiss = { selectedWordForDetail = null },
        )
    }

    wordForBook?.let { w ->
        var newBookName by remember(w.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { wordForBook = null },
            title = { Text("단어장에 담기") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "\"${w.word}\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.purpleMain,
                    )
                    if (wordBooks.isEmpty()) {
                        Text("단어장이 없습니다. 아래에서 이름을 입력해 새로 만드세요.", fontSize = 13.sp, color = colors.textMuted)
                    } else {
                        Text("기존 단어장", fontSize = 12.sp, color = colors.textMuted)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 160.dp)) {
                            wordBooks.forEach { book ->
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            val ok = viewModel.addWordToWordBook(w.id, book.id)
                                            wordForBook = null
                                            snackbarHostState.showSnackbar(
                                                if (ok) "\"${book.name}\"에 담았습니다" else "이미 이 단어장에 있습니다",
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(book.name, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("새 단어장 만들어 담기", fontSize = 12.sp, color = colors.textMuted)
                    OutlinedTextField(
                        value = newBookName,
                        onValueChange = { newBookName = it },
                        singleLine = true,
                        label = { Text("단어장 이름") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val name = newBookName.trim()
                            if (name.isEmpty()) {
                                snackbarHostState.showSnackbar("단어장 이름을 입력하세요")
                            } else {
                                val ok = viewModel.createWordBookAndAddWord(w.id, name)
                                wordForBook = null
                                snackbarHostState.showSnackbar(
                                    if (ok) "새 단어장에 담았습니다" else "추가에 실패했습니다",
                                )
                            }
                        }
                    },
                ) {
                    Text("새 단어장에 담기", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { wordForBook = null }) {
                    Text("취소")
                }
            },
        )
    }
}

@Composable
private fun SyncResultDialog(
    result: com.euysoo.engtest.domain.model.SyncResult,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "초기화 완료", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("버전: ${result.sourceVersion}")
                HorizontalDivider()
                SyncResultRow("전체 단어", "${result.totalInFile}개")
                SyncResultRow("신규 추가", "${result.addedCount}개", highlight = result.addedCount > 0)
                SyncResultRow("수정됨", "${result.updatedCount}개", highlight = result.updatedCount > 0)
                SyncResultRow("변경 없음 (스킵)", "${result.skippedCount}개")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("확인") }
        },
    )
}

/**
 * 한 줄로 표시하며 가로 공간이 부족하면 [maxFontSp]에서 [minFontSp]까지 줄여 맞춤.
 */
@Composable
private fun AutoShrinkAnnotatedLine(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    maxFontSp: Float,
    minFontSp: Float,
    baseStyle: TextStyle = TextStyle(),
) {
    BoxWithConstraints(modifier = modifier) {
        val measurer = rememberTextMeasurer()
        val maxW = constraints.maxWidth
        val fontSp =
            remember(text, maxW, maxFontSp, minFontSp, baseStyle) {
                if (!constraints.hasBoundedWidth || maxW <= 0) {
                    return@remember maxFontSp
                }
                var sp = maxFontSp
                val minS = minFontSp
                while (sp >= minS) {
                    val merged = baseStyle.merge(TextStyle(fontSize = sp.sp))
                    val layout =
                        measurer.measure(
                            text = text,
                            style = merged,
                            constraints = Constraints(maxWidth = maxW),
                            maxLines = 1,
                        )
                    if (layout.size.width <= maxW) break
                    sp -= 0.5f
                }
                sp.coerceAtLeast(minS)
            }
        Text(
            text = text,
            style = baseStyle.merge(TextStyle(fontSize = fontSp.sp)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SyncResultRow(
    label: String,
    value: String,
    highlight: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, fontSize = 14.sp)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun WordListItem(
    modifier: Modifier = Modifier,
    item: WordWithStats,
    isRecentlyAdded: Boolean,
    onSpeak: (String) -> Unit,
    onDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToBook: () -> Unit,
) {
    @Suppress("DEPRECATION")
    val volumeUpIcon = Icons.Filled.VolumeUp
    val colors = AppTheme.colors
    val word = item.word
    val (correct, wrong) =
        remember(item.totalCount, item.correctRate, item.wrongRate) {
            if (item.totalCount > 0) {
                (item.correctRate!! * 100).toInt() to (item.wrongRate!! * 100).toInt()
            } else {
                0 to 0
            }
        }
    val phoneticLine = word.phoneticDisplayText()
    val row1Text =
        buildAnnotatedString {
            withStyle(SpanStyle(color = colors.purpleMain, fontWeight = FontWeight.Medium)) {
                append(word.word)
            }
            if (isRecentlyAdded) {
                append(" ")
                withStyle(SpanStyle(color = colors.pinkMain)) { append("★") }
            }
            append(" · ")
            withStyle(SpanStyle(color = colors.textMuted)) {
                append(word.partOfSpeech)
            }
            append(" · ")
            withStyle(SpanStyle(color = colors.textDim)) {
                append(phoneticLine)
            }
        }
    val stars = "★".repeat(word.difficulty.starCount)
    val row2Text =
        buildAnnotatedString {
            withStyle(SpanStyle(color = colors.textSecondary, fontWeight = FontWeight.Medium)) {
                append(word.meaning)
            }
            append(" · ")
            withStyle(SpanStyle(color = colors.purpleMain)) {
                append(stars)
            }
        }
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(if (isRecentlyAdded) colors.bgCardAccent else colors.bgCard, RoundedCornerShape(16.dp))
                .border(0.5.dp, colors.borderDefault, RoundedCornerShape(16.dp))
                .padding(14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AutoShrinkAnnotatedLine(
                    text = row1Text,
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                    maxFontSp = 16f,
                    minFontSp = 9f,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconActionBox(onClick = { onSpeak(word.word) }) {
                        Icon(
                            imageVector = volumeUpIcon,
                            contentDescription = "발음 듣기",
                            tint = colors.purpleMain,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                    IconActionBox(onClick = onDetail) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "상세 정보",
                            tint = colors.purpleMain,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
            }
            AutoShrinkAnnotatedLine(
                text = row2Text,
                modifier = Modifier.fillMaxWidth(),
                maxFontSp = 14f,
                minFontSp = 9f,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "정답 $correct%", fontSize = 10.sp, color = colors.greenMain)
                    Text(text = "·", fontSize = 10.sp, color = colors.textMuted)
                    Text(text = "오답 $wrong%", fontSize = 10.sp, color = colors.pinkMain)
                    Text(text = "·", fontSize = 10.sp, color = colors.textMuted)
                    Text(text = "시도 ${item.totalCount}회", fontSize = 10.sp, color = colors.textMuted)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppButton(
                        text = "단어장",
                        onClick = onAddToBook,
                        style = AppButtonStyle.SECONDARY,
                        modifier = Modifier.wrapContentWidth(),
                    )
                    AppButton(
                        text = "편집",
                        onClick = onEdit,
                        style = AppButtonStyle.SECONDARY,
                        modifier = Modifier.wrapContentWidth(),
                    )
                    AppButton(
                        text = "삭제",
                        onClick = onDelete,
                        style = AppButtonStyle.DANGER,
                        modifier = Modifier.wrapContentWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun IconActionBox(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    Box(
        modifier =
            Modifier
                .size(28.dp)
                .background(colors.bgIcon, RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun WordEditDialog(
    word: Word,
    onDismiss: () -> Unit,
    onSave: (Word) -> Unit,
) {
    val colors = AppTheme.colors
    var wordText by remember { mutableStateOf("") }
    var posText by remember { mutableStateOf("") }
    var meaningText by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf(word.difficulty) }

    LaunchedEffect(word.id) {
        wordText = word.word
        posText = word.partOfSpeech
        meaningText = word.meaning
        difficulty = word.difficulty
    }

    val keyboardOptions =
        KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Default,
        )

    val modalBg = colors.bgPrimary
    val inputBg = colors.bgCard
    val dividerColor = colors.borderDefault
    val textFieldColors =
        OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = inputBg,
            focusedContainerColor = inputBg,
            unfocusedBorderColor = dividerColor,
            focusedBorderColor = colors.purpleMain,
            unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unfocusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            focusedLabelColor = colors.purpleMain,
        )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("단어 편집", color = colors.textPrimary) },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(modalBg)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(inputBg)
                            .padding(12.dp),
                ) {
                    OutlinedTextField(
                        value = wordText,
                        onValueChange = { wordText = it },
                        label = { Text("단어") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = keyboardOptions,
                        colors = textFieldColors,
                    )
                }
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(inputBg)
                            .padding(12.dp),
                ) {
                    OutlinedTextField(
                        value = posText,
                        onValueChange = { posText = it },
                        label = { Text("품사") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = keyboardOptions,
                        colors = textFieldColors,
                    )
                }
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(inputBg)
                            .padding(12.dp),
                ) {
                    OutlinedTextField(
                        value = meaningText,
                        onValueChange = { meaningText = it },
                        label = { Text("뜻") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = keyboardOptions,
                        colors = textFieldColors,
                    )
                }
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(inputBg)
                            .padding(12.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            WordDifficulty.ELEMENTARY to "초등",
                            WordDifficulty.MIDDLE to "중등",
                            WordDifficulty.HIGH to "고등",
                        ).forEach { (d, label) ->
                            AppChipButton(
                                text = label,
                                selected = difficulty == d,
                                onClick = { difficulty = d },
                            )
                        }
                    }
                }
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppFullWidthButton(
                    text = "취소",
                    onClick = onDismiss,
                    style = AppButtonStyle.SECONDARY,
                    modifier = Modifier.weight(1f),
                )
                AppFullWidthButton(
                    text = "저장",
                    onClick = {
                        val trimmedWord = wordText.trim()
                        val phonetic = if (trimmedWord == word.word) word.phonetic else null
                        onSave(
                            word.copy(
                                word = trimmedWord,
                                partOfSpeech = posText.trim(),
                                meaning = meaningText.trim(),
                                difficulty = difficulty,
                                phonetic = phonetic,
                            ),
                        )
                    },
                    style = AppButtonStyle.PRIMARY,
                    modifier = Modifier.weight(1f),
                )
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun AddWordDialog(
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onSave: (Word) -> Unit,
) {
    val colors = AppTheme.colors
    var wordText by remember { mutableStateOf("") }
    var posText by remember { mutableStateOf("") }
    var meaningText by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf(WordDifficulty.ELEMENTARY) }
    val scope = rememberCoroutineScope()

    val keyboardOptions =
        KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Default,
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("신규 단어 추가", color = colors.textPrimary) },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = wordText,
                    onValueChange = { wordText = it },
                    label = { Text("단어") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = keyboardOptions,
                )
                OutlinedTextField(
                    value = posText,
                    onValueChange = { posText = it },
                    label = { Text("품사") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = keyboardOptions,
                )
                OutlinedTextField(
                    value = meaningText,
                    onValueChange = { meaningText = it },
                    label = { Text("뜻") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = keyboardOptions,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        WordDifficulty.ELEMENTARY to "초등",
                        WordDifficulty.MIDDLE to "중등",
                        WordDifficulty.HIGH to "고등",
                    ).forEach { (d, label) ->
                        AppChipButton(
                            text = label,
                            selected = difficulty == d,
                            onClick = { difficulty = d },
                        )
                    }
                }
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppFullWidthButton(
                    text = "취소",
                    onClick = onDismiss,
                    style = AppButtonStyle.SECONDARY,
                    modifier = Modifier.weight(1f),
                )
                AppFullWidthButton(
                    text = "저장",
                    onClick = {
                        val w = wordText.trim()
                        val p = posText.trim()
                        val m = meaningText.trim()
                        if (w.isBlank() || m.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("단어와 뜻을 입력하세요")
                            }
                            return@AppFullWidthButton
                        }
                        onSave(
                            Word(
                                id = 0,
                                word = w,
                                partOfSpeech = p.ifBlank { "-" },
                                meaning = m,
                                difficulty = difficulty,
                            ),
                        )
                    },
                    style = AppButtonStyle.PRIMARY,
                    modifier = Modifier.weight(1f),
                )
            }
        },
        confirmButton = {},
    )
}
