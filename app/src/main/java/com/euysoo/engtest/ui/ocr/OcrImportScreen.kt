package com.euysoo.engtest.ui.ocr

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.data.entity.PartOfSpeech
import com.euysoo.engtest.data.entity.WordDifficulty
import com.euysoo.engtest.di.AppContainer
import com.euysoo.engtest.ui.component.AppButtonStyle
import com.euysoo.engtest.ui.component.AppChipButton
import com.euysoo.engtest.ui.component.AppFullWidthButton
import com.euysoo.engtest.ui.components.AppTopBar
import com.euysoo.engtest.ui.components.PartOfSpeechSelector
import com.euysoo.engtest.ui.theme.AppColors
import com.euysoo.engtest.ui.theme.AppTheme
import com.euysoo.engtest.util.OcrHelper
import com.euysoo.engtest.util.ParsedWord

@Composable
fun OcrImportScreen(
    initialBitmap: Bitmap,
    onBack: () -> Unit,
    onShowGuide: () -> Unit,
    onSaveComplete: (bookId: Long) -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as EngTestApplication
    val viewModel: OcrViewModel = viewModel(factory = OcrViewModelFactory(app.appContainer))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editingIndex by viewModel.editingIndex.collectAsStateWithLifecycle()
    val colors = AppTheme.colors

    var showBookPicker by remember { mutableStateOf(false) }
    var pendingWords by remember { mutableStateOf<List<ParsedWord>>(emptyList()) }
    var saveNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(initialBitmap) {
        viewModel.processImage(initialBitmap)
    }

    LaunchedEffect(uiState) {
        val success = uiState as? OcrUiState.SaveSuccess ?: return@LaunchedEffect
        if (!saveNavigated) {
            saveNavigated = true
            onSaveComplete(success.summary.bookId)
        }
    }

    if (showBookPicker) {
        BookPickerDialog(
            container = app.appContainer,
            onDismiss = { showBookPicker = false },
            onBookSelected = { bId, bName ->
                showBookPicker = false
                viewModel.saveWords(pendingWords, bId, bName)
            },
        )
    }

    editingIndex?.let { idx ->
        val currentWords =
            when (val s = uiState) {
                is OcrUiState.Result -> s.words
                is OcrUiState.PartialResult -> s.words
                else -> null
            }
        currentWords?.getOrNull(idx)?.let { target ->
            OcrWordEditDialog(
                parsedWord = target,
                onDismiss = { viewModel.closeEditDialog() },
                onSave = { word, pos, meaning, difficulty ->
                    viewModel.saveEdit(idx, word, pos, meaning, difficulty)
                },
            )
        }
    }

    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = { AppTopBar(title = "단어 인식 결과", onBackClick = onBack) },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when (val state = uiState) {
                is OcrUiState.Idle -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = colors.purpleMain)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("단어를 인식하는 중...", color = colors.textMuted, fontSize = 14.sp)
                    }
                }
                is OcrUiState.Processing -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = colors.purpleMain)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.engineLabel,
                            color = colors.textMuted,
                            fontSize = 14.sp,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val isGemini = state.engineLabel.contains("Gemini", ignoreCase = true)
                        Text(
                            text = if (isGemini) "AI 고정밀 인식" else "오프라인 모드",
                            fontSize = 11.sp,
                            color = if (isGemini) colors.purpleMain else colors.textMuted,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                is OcrUiState.Saving -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = colors.purpleMain)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("단어를 저장하는 중...", color = colors.textMuted, fontSize = 14.sp)
                    }
                }
                is OcrUiState.Error -> {
                    OcrErrorContent(
                        state = state,
                        onRetry = {
                            viewModel.reset()
                            onBack()
                        },
                        onShowGuide = onShowGuide,
                        colors = colors,
                    )
                }
                is OcrUiState.Result -> {
                    OcrResultContent(
                        words = state.words,
                        warningMessage = null,
                        viewModel = viewModel,
                        onRetry = {
                            viewModel.reset()
                            onBack()
                        },
                        onSaveClick = { words ->
                            pendingWords = words
                            showBookPicker = true
                        },
                        colors = colors,
                    )
                }
                is OcrUiState.PartialResult -> {
                    OcrResultContent(
                        words = state.words,
                        warningMessage = state.warningMessage,
                        viewModel = viewModel,
                        onRetry = {
                            viewModel.reset()
                            onBack()
                        },
                        onSaveClick = { words ->
                            pendingWords = words
                            showBookPicker = true
                        },
                        colors = colors,
                    )
                }
                is OcrUiState.SaveSuccess -> { /* saveSummary LaunchedEffect에서 처리 */ }
            }
        }
    }
}

@Composable
private fun BookPickerDialog(
    container: AppContainer,
    onDismiss: () -> Unit,
    onBookSelected: (bookId: Long, bookName: String) -> Unit,
) {
    val colors = AppTheme.colors
    val books by container.database.wordBookDao().getAllBooks().collectAsStateWithLifecycle(initialValue = emptyList())

    var showNewBookInput by remember { mutableStateOf(false) }
    var newBookName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("단어장 선택", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (showNewBookInput) {
                    OutlinedTextField(
                        value = newBookName,
                        onValueChange = { newBookName = it },
                        label = { Text("새 단어장 이름") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = {
                                showNewBookInput = false
                                newBookName = ""
                            },
                        ) {
                            Text("취소", color = colors.textMuted)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = {
                                val trimmed = newBookName.trim()
                                if (trimmed.isNotBlank()) {
                                    onBookSelected(-1L, trimmed)
                                }
                            },
                            enabled = newBookName.trim().isNotEmpty(),
                        ) {
                            Text("만들기", color = colors.purpleMain, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    colors.purpleMain.copy(alpha = 0.08f),
                                    RoundedCornerShape(10.dp),
                                ).border(
                                    0.5.dp,
                                    colors.purpleMain.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp),
                                ).clickable { showNewBookInput = true }
                                .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Default.Add, null, tint = colors.purpleMain, modifier = Modifier.size(18.dp))
                        Text(
                            "새 단어장 만들기",
                            color = colors.purpleMain,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                        )
                    }

                    if (books.isNotEmpty()) {
                        HorizontalDivider(color = colors.borderDefault)
                        Text("기존 단어장", fontSize = 12.sp, color = colors.textMuted)
                    }

                    books.forEach { book ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(colors.bgCard, RoundedCornerShape(10.dp))
                                    .border(0.5.dp, colors.borderDefault, RoundedCornerShape(10.dp))
                                    .clickable { onBookSelected(book.id, book.name) }
                                    .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.LibraryBooks,
                                null,
                                tint = colors.purpleMain,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                book.name,
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                Icons.Default.ChevronRight,
                                null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소", color = colors.textMuted) }
        },
    )
}

@Composable
private fun OcrErrorContent(
    state: OcrUiState.Error,
    onRetry: () -> Unit,
    onShowGuide: () -> Unit,
    colors: AppColors,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val icon =
            when (state.type) {
                OcrErrorType.IMAGE_TOO_SMALL -> Icons.Default.ZoomIn
                OcrErrorType.NO_TEXT -> Icons.Default.ImageSearch
                OcrErrorType.NO_WORDS -> Icons.Default.FormatListBulleted
                OcrErrorType.LOW_QUALITY -> Icons.Default.Warning
                OcrErrorType.ENGINE_FAILURE -> Icons.Default.ErrorOutline
                OcrErrorType.RATE_LIMIT -> Icons.Default.Warning
                OcrErrorType.API_KEY_ERROR -> Icons.Default.Warning
            }
        Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFFE65100))
        Spacer(modifier = Modifier.height(16.dp))
        Text(state.message, textAlign = TextAlign.Center, color = colors.textPrimary, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = colors.purpleMain),
        ) {
            Text("다시 촬영하기")
        }
        if (state.showGuide) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onShowGuide) { Text("📋 작성 가이드 보기") }
        }
    }
}

@Composable
private fun OcrResultContent(
    words: List<ParsedWord>,
    warningMessage: String?,
    viewModel: OcrViewModel,
    onRetry: () -> Unit,
    onSaveClick: (List<ParsedWord>) -> Unit,
    colors: AppColors,
) {
    val selectedCount = words.count { it.isSelected }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "이미지 인식된 단어 목록을 꼼꼼히 확인하고 단어장을 추가해주세요.",
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = colors.textMuted,
        )
        warningMessage?.let {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFE65100))
                    Spacer(Modifier.width(8.dp))
                    Text(it, fontSize = 13.sp, color = Color(0xFF5D4037))
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${words.size}개 중 ${selectedCount}개 선택됨",
                fontSize = 13.sp,
                color = colors.textMuted,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier =
                        Modifier
                            .background(
                                colors.purpleMain.copy(alpha = 0.10f),
                                RoundedCornerShape(8.dp),
                            ).border(
                                0.5.dp,
                                colors.purpleMain.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp),
                            ).clickable { viewModel.selectAll() }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text(
                        "전체선택",
                        fontSize = 12.sp,
                        color = colors.purpleMain,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .background(colors.bgCard, RoundedCornerShape(8.dp))
                            .border(0.5.dp, colors.borderDefault, RoundedCornerShape(8.dp))
                            .clickable { viewModel.deselectAll() }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text("전체해제", fontSize = 12.sp, color = colors.textMuted)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            itemsIndexed(words, key = { index, w -> "${index}_${w.word}" }) { index, parsedWord ->
                OcrWordItem(
                    index = index,
                    parsedWord = parsedWord,
                    onToggle = { viewModel.toggleSelection(index) },
                    onUpdate = { w, p, m -> viewModel.updateWord(index, w, p, m) },
                    onEdit = { viewModel.openEditDialog(index) },
                    colors = colors,
                )
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
            ) {
                Text("다시 촬영")
            }

            Button(
                onClick = { onSaveClick(words) },
                modifier = Modifier.weight(1f),
                enabled = selectedCount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = colors.purpleMain),
            ) {
                Text("단어장에 추가 ($selectedCount)")
            }
        }
    }
}

@Composable
private fun OcrWordItem(
    index: Int,
    parsedWord: ParsedWord,
    onToggle: () -> Unit,
    onUpdate: (String, String, String) -> Unit,
    onEdit: () -> Unit,
    colors: AppColors,
) {
    val meaningMissing = parsedWord.meaning.isBlank()
    val posMissing = parsedWord.partOfSpeech.isBlank()
    val cardBg =
        when {
            posMissing -> Color(0xFFFFEBEE)
            meaningMissing -> Color(0xFFFFF8E1)
            else -> colors.bgCard
        }
    val cardBorder =
        when {
            posMissing -> Color(0xFFE53935)
            meaningMissing -> Color(0xFFFFB300)
            else -> colors.borderDefault
        }

    var wordText by remember(parsedWord.word, index) { mutableStateOf(parsedWord.word) }
    var posText by remember(parsedWord.partOfSpeech, index) { mutableStateOf(parsedWord.partOfSpeech) }
    var meaningText by remember(parsedWord.meaning, index) { mutableStateOf(parsedWord.meaning) }

    LaunchedEffect(parsedWord.word, parsedWord.partOfSpeech, parsedWord.meaning) {
        wordText = parsedWord.word
        posText = parsedWord.partOfSpeech
        meaningText = parsedWord.meaning
    }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(cardBg, RoundedCornerShape(10.dp))
                .border(0.5.dp, cardBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = parsedWord.isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = colors.purpleMain),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BasicTextField(
                    value = wordText,
                    onValueChange = {
                        wordText = it
                        onUpdate(it, posText, meaningText)
                    },
                    textStyle =
                        TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                        ),
                    modifier = Modifier.weight(1f),
                )
                if (parsedWord.isAutoCorrected) {
                    Text(
                        text = "(자동보정)",
                        fontSize = 11.sp,
                        color = colors.purpleMain,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicTextField(
                    value = posText,
                    onValueChange = {
                        posText = it
                        onUpdate(wordText, it, meaningText)
                    },
                    textStyle = TextStyle(fontSize = 12.sp, color = colors.purpleMain),
                    decorationBox = { inner ->
                        Box {
                            if (posText.isBlank()) {
                                Text("?", fontSize = 14.sp, color = Color(0xFFE53935), fontWeight = FontWeight.SemiBold)
                            }
                            inner()
                        }
                    },
                    modifier = Modifier.width(56.dp),
                )
                BasicTextField(
                    value = meaningText,
                    onValueChange = {
                        meaningText = it
                        onUpdate(wordText, posText, it)
                    },
                    textStyle = TextStyle(fontSize = 13.sp, color = colors.textSecondary),
                    decorationBox = { inner ->
                        Box {
                            if (meaningText.isBlank()) {
                                Text("⚠️ 뜻 입력", fontSize = 13.sp, color = Color(0xFFE65100))
                            }
                            inner()
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(30.dp)
                        .background(colors.bgIcon, RoundedCornerShape(8.dp))
                        .border(0.5.dp, colors.borderDefault, RoundedCornerShape(8.dp))
                        .clickable { onEdit() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "편집",
                    tint = colors.purpleMain,
                    modifier = Modifier.size(15.dp),
                )
            }
            if (meaningMissing || posMissing) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = if (posMissing) "품사 없음" else "뜻 없음",
                    tint = if (posMissing) Color(0xFFE53935) else Color(0xFFE65100),
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

/**
 * OCR 단어 항목 편집 모달 (WordManageScreen WordEditDialog 스타일 정렬)
 */
@Composable
fun OcrWordEditDialog(
    parsedWord: ParsedWord,
    onDismiss: () -> Unit,
    onSave: (word: String, partOfSpeech: String, meaning: String, difficulty: WordDifficulty) -> Unit,
) {
    val colors = AppTheme.colors

    var wordText by remember(parsedWord.word) { mutableStateOf(parsedWord.word) }
    var posText by remember(parsedWord.partOfSpeech) { mutableStateOf(parsedWord.partOfSpeech) }
    var meaningText by remember(parsedWord.meaning) { mutableStateOf(parsedWord.meaning) }
    var difficulty by remember(parsedWord.difficulty) { mutableStateOf(parsedWord.difficulty) }

    LaunchedEffect(parsedWord.word, parsedWord.partOfSpeech, parsedWord.meaning) {
        wordText = parsedWord.word
        posText =
            OcrHelper.normalizePartOfSpeech(parsedWord.partOfSpeech).ifBlank { parsedWord.partOfSpeech.trim() }
        meaningText = parsedWord.meaning
        difficulty = parsedWord.difficulty
    }

    val keyboardOptions =
        KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Default,
        )

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

    val canSave = wordText.trim().isNotBlank() && meaningText.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("단어 편집", color = colors.textPrimary) },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(colors.bgPrimary)
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
                        singleLine = true,
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
                    Text(
                        text = "품사",
                        fontSize = 12.sp,
                        color = colors.textMuted,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    PartOfSpeechSelector(
                        selectedLabels = PartOfSpeech.parseLabels(posText),
                        onSelectionChange = { selected ->
                            posText = PartOfSpeech.toStorageString(selected)
                        },
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
                        minLines = 2,
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        confirmButton = {},
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
                    enabled = canSave,
                    onClick = {
                        onSave(
                            wordText.trim(),
                            posText.trim(),
                            meaningText.trim(),
                            difficulty,
                        )
                    },
                    style = AppButtonStyle.PRIMARY,
                    modifier = Modifier.weight(1f),
                )
            }
        },
    )
}
