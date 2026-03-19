package com.example.engtest.ui.screen.records

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engtest.EngTestApplication
import com.example.engtest.data.entity.TestResult
import com.example.engtest.data.entity.Word
import com.example.engtest.data.entity.WordDifficulty
import com.example.engtest.ui.theme.AppDimens
import com.example.engtest.util.phoneticDisplayText
import com.example.engtest.util.starCount
import android.speech.tts.TextToSpeech
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ShadowPurple = Color(0xFFE2D1F9)
private val ShadowMint = Color(0xFFC1F0C1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    onBack: () -> Unit,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as EngTestApplication
    val viewModel: RecordsViewModel = viewModel(factory = RecordsViewModelFactory(app))

    val resultsList by viewModel.resultsList.collectAsStateWithLifecycle()
    val fromMillis by viewModel.fromMillis.collectAsStateWithLifecycle()
    val toMillis by viewModel.toMillis.collectAsStateWithLifecycle()
    val selectedResult by viewModel.selectedResult.collectAsStateWithLifecycle()
    val resultWords by viewModel.resultWords.collectAsStateWithLifecycle()
    val resultWordStats by viewModel.resultWordStats.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedResult != null) "테스트 결과" else "기록 및 통계"
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedResult != null) viewModel.clearSelection()
                            else onBack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (selectedResult != null) {
                ResultDetailContent(
                    result = selectedResult!!,
                    resultWords = resultWords,
                    resultWordStats = resultWordStats,
                    onHome = onBackToHome,
                    onList = { viewModel.clearSelection() }
                )
            } else {
                var showFromPicker by remember { mutableStateOf(false) }
                var showToPicker by remember { mutableStateOf(false) }
                Column(modifier = Modifier.fillMaxSize()) {
                    RecordsListContent(
                        modifier = Modifier.weight(1f),
                        fromMillis = fromMillis,
                        toMillis = toMillis,
                        onFromClick = { showFromPicker = true },
                        onToClick = { showToPicker = true },
                        resultsList = resultsList,
                        onResultClick = { viewModel.setSelectedResult(it) }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = onBackToHome,
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("홈")
                        }
                    }
                }
                if (showFromPicker) {
                    RoundedDatePickerDialog(
                        initialMillis = fromMillis,
                        onConfirm = { viewModel.setFromMillis(it); showFromPicker = false },
                        onDismiss = { showFromPicker = false }
                    )
                }
                if (showToPicker) {
                    RoundedDatePickerDialog(
                        initialMillis = toMillis,
                        onConfirm = { viewModel.setToMillis(it); showToPicker = false },
                        onDismiss = { showToPicker = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateRangeRow(
    fromLabel: String,
    toLabel: String,
    fromText: String,
    toText: String,
    onFromClick: () -> Unit,
    onToClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateBox(
            modifier = Modifier.weight(1f),
            label = fromLabel,
            dateText = fromText,
            shadowColor = ShadowPurple,
            onClick = onFromClick
        )
        Text(
            text = "~",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DateBox(
            modifier = Modifier.weight(1f),
            label = toLabel,
            dateText = toText,
            shadowColor = ShadowMint,
            onClick = onToClick
        )
    }
}

@Composable
private fun DateBox(
    modifier: Modifier = Modifier,
    label: String,
    dateText: String,
    shadowColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 80)
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(shadowColor)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "날짜 선택"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoundedDatePickerDialog(
    initialMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        initialDisplayedMonthMillis = initialMillis
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                DatePicker(
                    state = state,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소")
                    }
                    TextButton(
                        onClick = {
                            state.selectedDateMillis?.let { onConfirm(it) }
                        }
                    ) {
                        Text("확인", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordsListContent(
    modifier: Modifier = Modifier,
    fromMillis: Long,
    toMillis: Long,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    resultsList: List<TestResult>,
    onResultClick: (TestResult) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        DateRangeRow(
            fromLabel = "언제부터?",
            toLabel = "언제까지?",
            fromText = dateFormat.format(Date(fromMillis)),
            toText = dateFormat.format(Date(toMillis)),
            onFromClick = onFromClick,
            onToClick = onToClick
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "결과 목록",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(resultsList, key = { _, it -> it.id }) { _, result ->
                ResultListItem(
                    result = result,
                    onClick = { onResultClick(result) }
                )
            }
        }
    }
}

@Composable
private fun ResultListItem(
    result: TestResult,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy. M. d. HH:mm", Locale.getDefault())
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(AppDimens.cardCornerRadius)),
        shape = RoundedCornerShape(AppDimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.screenPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateFormat.format(Date(result.testDateMillis)),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${result.score * 10}점",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ResultDetailContent(
    result: TestResult,
    resultWords: List<Pair<Word, Boolean>>,
    resultWordStats: Map<Long, Pair<Int, Int>>,
    onHome: () -> Unit,
    onList: () -> Unit
) {
    val context = LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context.applicationContext) { status ->
            ttsReady = (status == TextToSpeech.SUCCESS)
        }
    }
    DisposableEffect(tts) {
        onDispose {
            try {
                tts.stop()
                tts.shutdown()
            } catch (_: Exception) { /* TTS 해제 시 예외 무시 */ }
        }
    }
    LaunchedEffect(ttsReady) {
        if (ttsReady) tts.setLanguage(Locale.US)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "점수: ${result.score * 10}점",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        val dateFormat = SimpleDateFormat("yyyy. M. d. HH:mm", Locale.getDefault())
        Text(
            text = dateFormat.format(Date(result.testDateMillis)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (resultWords.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "단어 정보를 불러오는 중이거나 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppDimens.listItemSpacing),
                contentPadding = PaddingValues(vertical = AppDimens.cardPadding)
            ) {
                itemsIndexed(
                    resultWords,
                    key = { _, pair -> pair.first.id }
                ) { _, (word, known) ->
                    val stats = resultWordStats[word.id]
                    ResultWordItem(
                        word = word,
                        known = known,
                        stats = stats,
                        onSpeak = { if (ttsReady) tts.speak(word.word, TextToSpeech.QUEUE_FLUSH, null, null) }
                    )
                }
            }
        }
        val buttonColors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onHome,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = buttonColors
            ) {
                Text("홈")
            }
            Spacer(modifier = Modifier.size(12.dp))
            Button(
                onClick = onList,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = buttonColors
            ) {
                Text("목록")
            }
        }
    }
}

@Composable
private fun ResultWordItem(
    word: Word,
    known: Boolean,
    stats: Pair<Int, Int>?,
    onSpeak: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(AppDimens.cardCornerRadius)),
        shape = RoundedCornerShape(AppDimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 1줄: 영어단어 · 발음기호
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = word.phoneticDisplayText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // 2줄: 품사 · 단어뜻 · 난이도(별) · 스피커
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (word.partOfSpeech.isNotBlank()) {
                        Text(
                            text = word.partOfSpeech,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "발음 재생",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // 3줄: 정답율, 오답율, 시도회수
                Text(
                    text = if (stats != null && stats.second > 0) {
                        val (c, t) = stats
                        val correctPct = (c * 100 / t).toInt()
                        val wrongPct = 100 - correctPct
                        "정답 ${correctPct}% · 오답 ${wrongPct}% · 시도 ${t}회"
                    } else {
                        "시도 0회"
                    },
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // O / X — 목록 항목 높이의 45% 크기
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 8.dp)
                    .width(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.height(maxHeight * 0.45f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (known) "O" else "X",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (known) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
