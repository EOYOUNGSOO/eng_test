@file:Suppress("SpellCheckingInspection")
package com.example.engtest.ui.screen.wordmanage

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engtest.EngTestApplication
import com.example.engtest.data.entity.Word
import com.example.engtest.data.entity.WordDifficulty
import com.example.engtest.util.phoneticDisplayText
import com.example.engtest.util.starCount
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordManageScreen(
    onBack: () -> Unit,
    onHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as EngTestApplication
    val viewModel: WordManageViewModel = viewModel(
        factory = WordManageViewModelFactory(app)
    )

    val wordsWithStats by viewModel.wordsWithStats.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val recentlyAddedIds by viewModel.recentlyAddedIds.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.clearRecentlyAdded()
    }
    val wordToEdit by viewModel.wordToEdit.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadResult by viewModel.loadResult.collectAsStateWithLifecycle()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showAddWordDialog by remember { mutableStateOf(false) }
    var wordToDelete by remember { mutableStateOf<Word?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(loadResult) {
        loadResult?.let { count ->
            snackbarHostState.showSnackbar("${count}개의 단어가 등록되었습니다")
            viewModel.clearLoadResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("단어 관리") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("영어 단어 검색") },
                    singleLine = true
                )
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filter == null,
                        onClick = { viewModel.setFilter(null) },
                        label = { Text("전체") },
                        colors = chipColors
                    )
                    FilterChip(
                        selected = filter == WordDifficulty.ELEMENTARY,
                        onClick = { viewModel.setFilter(WordDifficulty.ELEMENTARY) },
                        label = { Text("초등") },
                        colors = chipColors
                    )
                    FilterChip(
                        selected = filter == WordDifficulty.MIDDLE,
                        onClick = { viewModel.setFilter(WordDifficulty.MIDDLE) },
                        label = { Text("중등") },
                        colors = chipColors
                    )
                    FilterChip(
                        selected = filter == WordDifficulty.HIGH,
                        onClick = { viewModel.setFilter(WordDifficulty.HIGH) },
                        label = { Text("고등") },
                        colors = chipColors
                    )
                }

                var ttsReady by remember { mutableStateOf(false) }
                val tts = remember {
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
                        } catch (_: Exception) { /* TTS 해제 시 예외 무시 */ }
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = wordsWithStats,
                        key = { _, it -> it.word.id }
                    ) { _, item ->
                        WordListItem(
                            item = item,
                            isRecentlyAdded = item.word.id in recentlyAddedIds,
                            onSpeak = { text -> if (ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null) },
                            onEdit = { viewModel.setWordToEdit(item.word) },
                            onDelete = { wordToDelete = item.word }
                        )
                    }
                }

                val buttonColors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showAddWordDialog = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = buttonColors
                    ) {
                        Text("단어추가")
                    }
                    Button(
                        onClick = onHome,
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = buttonColors
                    ) {
                        Text("홈")
                    }
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = buttonColors
                    ) {
                        Text("초기화")
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
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
                Text("기존 데이터를 모두 지우고 초기 상태로 되돌리겠습니까?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.loadInitialWords()
                    }
                ) {
                    Text("확인", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("취소", color = MaterialTheme.colorScheme.onSurface)
                }
            }
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
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { wordToDelete = null }) {
                    Text("취소", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    wordToEdit?.let { word ->
        WordEditDialog(
            word = word,
            onDismiss = { viewModel.setWordToEdit(null) },
            onSave = { updated -> viewModel.updateWord(updated) }
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
                    val added = viewModel.addWordIfNew(newWord)
                    showAddWordDialog = false
                    val message = if (added) "단어가 추가되었습니다" else "이미 존재하는 단어입니다"
                    snackbarHostState.showSnackbar(message)
                }
            }
        )
    }
}

@Composable
private fun WordListItem(
    item: WordWithStats,
    isRecentlyAdded: Boolean,
    onSpeak: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val word = item.word
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1A1A1A), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            // 홈 메뉴 카드(surface #1A1A1A)보다 연한 배경으로 ROW 경계 구분
            containerColor = if (isRecentlyAdded) {
                Color(0xFF2A2A35)  // 최근 추가: 살짝 블루 톤
            } else {
                Color(0xFF252525)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.6f)) {
                // 1줄: 영어단어(볼드)
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                // 2줄: 발음기호 | 품사
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = word.phoneticDisplayText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = word.partOfSpeech,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // 3줄: 단어 뜻 + 난이도(괄호 안 별표)
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = word.meaning,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "(${"★".repeat(word.difficulty.starCount)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                // 4줄: 정답율, 오답율, 시도회수
                Text(
                    text = if (item.totalCount > 0) {
                        "정답 ${(item.correctRate!! * 100).toInt()}% · 오답 ${(item.wrongRate!! * 100).toInt()}% · 시도 ${item.totalCount}회"
                    } else {
                        "시도 0회"
                    },
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.weight(0.4f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onSpeak(word.word) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "발음 재생",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
                TextButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("편집", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(
                    onClick = onDelete,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun WordEditDialog(
    word: Word,
    onDismiss: () -> Unit,
    onSave: (Word) -> Unit
) {
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

    val keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Default
    )

    val modalBg = Color(0xFF000000)
    val inputBg = MaterialTheme.colorScheme.primaryContainer
    val dividerColor = Color(0xFF354560)
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = inputBg,
        focusedContainerColor = inputBg,
        unfocusedBorderColor = dividerColor,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        unfocusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
        focusedLabelColor = MaterialTheme.colorScheme.primary
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("단어 편집", color = MaterialTheme.colorScheme.onBackground) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(modalBg)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(inputBg)
                        .padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = wordText,
                        onValueChange = { wordText = it },
                        label = { Text("단어") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = keyboardOptions,
                        colors = textFieldColors
                    )
                }
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(inputBg)
                        .padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = posText,
                        onValueChange = { posText = it },
                        label = { Text("품사") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = keyboardOptions,
                        colors = textFieldColors
                    )
                }
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(inputBg)
                        .padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = meaningText,
                        onValueChange = { meaningText = it },
                        label = { Text("뜻") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = keyboardOptions,
                        colors = textFieldColors
                    )
                }
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(inputBg)
                        .padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            WordDifficulty.ELEMENTARY to "초등",
                            WordDifficulty.MIDDLE to "중등",
                            WordDifficulty.HIGH to "고등"
                        ).forEach { (d, label) ->
                            FilterChip(
                                selected = difficulty == d,
                                onClick = { difficulty = d },
                                label = { Text(label) }
                            )
                        }
                    }
                }
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedWord = wordText.trim()
                    val phonetic = if (trimmedWord == word.word) word.phonetic else null
                    onSave(
                        word.copy(
                            word = trimmedWord,
                            partOfSpeech = posText.trim(),
                            meaning = meaningText.trim(),
                            difficulty = difficulty,
                            phonetic = phonetic
                        )
                    )
                }
            ) {
                Text("저장", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

@Composable
private fun AddWordDialog(
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onSave: (Word) -> Unit
) {
    var wordText by remember { mutableStateOf("") }
    var posText by remember { mutableStateOf("") }
    var meaningText by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf(WordDifficulty.ELEMENTARY) }
    val scope = rememberCoroutineScope()

    val keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Default
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("신규 단어 추가") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = wordText,
                    onValueChange = { wordText = it },
                    label = { Text("단어") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = keyboardOptions
                )
                OutlinedTextField(
                    value = posText,
                    onValueChange = { posText = it },
                    label = { Text("품사") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = keyboardOptions
                )
                OutlinedTextField(
                    value = meaningText,
                    onValueChange = { meaningText = it },
                    label = { Text("뜻") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = keyboardOptions
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        WordDifficulty.ELEMENTARY to "초등",
                        WordDifficulty.MIDDLE to "중등",
                        WordDifficulty.HIGH to "고등"
                    ).forEach { (d, label) ->
                        FilterChip(
                            selected = difficulty == d,
                            onClick = { difficulty = d },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val w = wordText.trim()
                    val p = posText.trim()
                    val m = meaningText.trim()
                    if (w.isBlank() || m.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("단어와 뜻을 입력하세요")
                        }
                        return@TextButton
                    }
                    onSave(
                        Word(
                            id = 0,
                            word = w,
                            partOfSpeech = p.ifBlank { "-" },
                            meaning = m,
                            difficulty = difficulty
                        )
                    )
                }
            ) {
                Text("저장", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
