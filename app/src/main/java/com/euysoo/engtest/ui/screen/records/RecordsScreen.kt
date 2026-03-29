package com.euysoo.engtest.ui.screen.records

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.data.entity.TestResult
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.ui.components.AppCopyrightFooter
import com.euysoo.engtest.ui.components.AppTopBar
import com.euysoo.engtest.ui.screen.wordtest.formatDifficultyLabel
import com.euysoo.engtest.ui.screen.wordtest.resolveDifficultyLabelForResult
import com.euysoo.engtest.ui.navigation.NavRoutes
import com.euysoo.engtest.ui.theme.AppDimens
import com.euysoo.engtest.ui.theme.AppTheme
import com.euysoo.engtest.ui.worddetail.WordDetailBottomSheet
import com.euysoo.engtest.ui.worddetail.WordDetailViewModel
import com.euysoo.engtest.ui.worddetail.WordDetailViewModelFactory
import com.euysoo.engtest.util.phoneticDisplayText
import com.euysoo.engtest.util.starCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ShadowPurple = Color(0xFFE2D1F9)
private val ShadowMint = Color(0xFFC1F0C1)

/**
 * 기록 목록(날짜 필터 + 결과 카드). 상세는 [NavRoutes.RECORDS_DETAIL_ROUTE]로 이동해
 * NavController 백 스택이 2단계가 되므로 시스템/상단 뒤로가기가 목록만 pop한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsListScreen(
    onBack: () -> Unit,
    onOpenResultDetail: (Long) -> Unit,
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val app = context.applicationContext as EngTestApplication
    val viewModel: RecordsViewModel = viewModel(factory = RecordsViewModelFactory(app.appContainer))

    val resultsList by viewModel.resultsList.collectAsStateWithLifecycle()
    val fromMillis by viewModel.fromMillis.collectAsStateWithLifecycle()
    val toMillis by viewModel.toMillis.collectAsStateWithLifecycle()
    val recordsSnackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val recordsSnackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(recordsSnackbarMessage) {
        val msg = recordsSnackbarMessage ?: return@LaunchedEffect
        recordsSnackbarHostState.showSnackbar(msg)
        viewModel.consumeSnackbarMessage()
    }

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    BackHandler {
        when {
            showFromPicker || showToPicker -> {
                showFromPicker = false
                showToPicker = false
            }
            else -> onBack()
        }
    }

    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = {},
        snackbarHost = { SnackbarHost(recordsSnackbarHostState) },
    ) { padding ->
        val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                AppTopBar(
                    title = "기록 및 통계",
                    onBackClick = onBack,
                )
                LazyColumn(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        DateRangeRow(
                            fromLabel = "언제부터?",
                            toLabel = "언제까지?",
                            fromText = dateFormat.format(Date(fromMillis)),
                            toText = dateFormat.format(Date(toMillis)),
                            onFromClick = { showFromPicker = true },
                            onToClick = { showToPicker = true },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "결과 목록",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = "시험횟수: ${resultsList.size}",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textMuted,
                            )
                        }
                    }
                    itemsIndexed(
                        resultsList,
                        key = { _, r -> r.id },
                    ) { index, result ->
                        val listNumber = resultsList.size - index
                        ResultListItem(
                            result = result,
                            listNumber = listNumber,
                            onClick = { onOpenResultDetail(result.id) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
                AppCopyrightFooter(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            if (showFromPicker) {
                RoundedDatePickerDialog(
                    initialMillis = fromMillis,
                    onConfirm = {
                        viewModel.setFromMillis(it)
                        showFromPicker = false
                    },
                    onDismiss = { showFromPicker = false },
                )
            }
            if (showToPicker) {
                RoundedDatePickerDialog(
                    initialMillis = toMillis,
                    onConfirm = {
                        viewModel.setToMillis(it)
                        showToPicker = false
                    },
                    onDismiss = { showToPicker = false },
                )
            }
        }
    }
}

/**
 * 단일 시험 결과 상세. [viewModel]은 목록 화면과 동일한 [NavBackStackEntry]에 묶어 날짜 필터 등 상태를 공유한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsResultDetailScreen(
    resultId: Long,
    viewModel: RecordsViewModel,
    onBack: () -> Unit,
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val app = context.applicationContext as EngTestApplication
    val wordDetailViewModel: WordDetailViewModel = viewModel(factory = WordDetailViewModelFactory(app.appContainer))

    LaunchedEffect(resultId) {
        val ok = viewModel.loadResultByIdForDetailAwait(resultId)
        if (!ok) onBack()
    }

    val selectedResult by viewModel.selectedResult.collectAsStateWithLifecycle()
    val resultWords by viewModel.resultWords.collectAsStateWithLifecycle()
    val resultWordStats by viewModel.resultWordStats.collectAsStateWithLifecycle()
    val recordsSnackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val recordsSnackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(recordsSnackbarMessage) {
        val msg = recordsSnackbarMessage ?: return@LaunchedEffect
        recordsSnackbarHostState.showSnackbar(msg)
        viewModel.consumeSnackbarMessage()
    }

    var selectedWordForDetail by remember { mutableStateOf<String?>(null) }

    BackHandler {
        when {
            selectedWordForDetail != null -> selectedWordForDetail = null
            else -> onBack()
        }
    }

    var ttsReady by remember { mutableStateOf(false) }
    val tts =
        remember {
            TextToSpeech(context.applicationContext) { status ->
                ttsReady = (status == TextToSpeech.SUCCESS)
            }
        }
    DisposableEffect(tts) {
        onDispose {
            try {
                tts.stop()
                tts.shutdown()
            } catch (_: Exception) {
            }
        }
    }
    LaunchedEffect(ttsReady) {
        if (ttsReady) tts.setLanguage(Locale.US)
    }

    var difficultyDetailLabel by remember { mutableStateOf("") }
    LaunchedEffect(selectedResult) {
        val r = selectedResult
        difficultyDetailLabel =
            if (r != null) {
                withContext(Dispatchers.IO) {
                    resolveDifficultyLabelForResult(r.difficulty, app.database.wordBookDao())
                }
            } else {
                ""
            }
    }

    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = {},
        snackbarHost = { SnackbarHost(recordsSnackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val result = selectedResult
            if (result != null && result.id == resultId) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AppTopBar(
                        title = "테스트 결과",
                        onBackClick = onBack,
                    )
                    LazyColumn(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                    ) {
                        resultDetailScrollItems(
                            result = result,
                            difficultyLabel = difficultyDetailLabel,
                            resultWords = resultWords,
                            resultWordStats = resultWordStats,
                            onSpeakWord = { w ->
                                if (ttsReady) {
                                    tts.speak(w.word, TextToSpeech.QUEUE_FLUSH, null, null)
                                }
                            },
                            onWordDetail = { selectedWordForDetail = it },
                        )
                    }
                    AppCopyrightFooter(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "불러오는 중...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                    )
                }
            }
        }
    }

    selectedWordForDetail?.let { targetWord ->
        WordDetailBottomSheet(
            word = targetWord,
            viewModel = wordDetailViewModel,
            onDismiss = { selectedWordForDetail = null },
        )
    }
}

@Composable
private fun DateRangeRow(
    modifier: Modifier = Modifier,
    fromLabel: String,
    toLabel: String,
    fromText: String,
    toText: String,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateBox(
            modifier = Modifier.weight(1f),
            label = fromLabel,
            dateText = fromText,
            shadowColor = ShadowPurple,
            onClick = onFromClick,
        )
        Text(
            text = "~",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DateBox(
            modifier = Modifier.weight(1f),
            label = toLabel,
            dateText = toText,
            shadowColor = ShadowMint,
            onClick = onToClick,
        )
    }
}

@Composable
private fun DateBox(
    modifier: Modifier = Modifier,
    label: String,
    dateText: String,
    shadowColor: Color,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 80),
    )

    Box(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 4.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(shadowColor),
        )
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .border(0.5.dp, colors.borderDefault, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.bgCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted,
                    )
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                    )
                }
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "날짜 선택",
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
    onDismiss: () -> Unit,
) {
    val state =
        rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            initialDisplayedMonthMillis = initialMillis,
        )
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier =
                Modifier
                    .padding(24.dp)
                    .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                DatePicker(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소")
                    }
                    TextButton(
                        onClick = {
                            state.selectedDateMillis?.let { onConfirm(it) }
                        },
                    ) {
                        Text("확인", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun LazyListScope.resultDetailScrollItems(
    result: TestResult,
    difficultyLabel: String,
    resultWords: List<Pair<Word, Boolean>>,
    resultWordStats: Map<Long, Pair<Int, Int>>,
    onSpeakWord: (Word) -> Unit,
    onWordDetail: (String) -> Unit,
) {
    item {
        val colors = AppTheme.colors
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = "점수: ${result.score * 10}점",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.purpleMain,
            )
            val dateFormat = SimpleDateFormat("yyyy. M. d. HH:mm", Locale.getDefault())
            Text(
                text = dateFormat.format(Date(result.testDateMillis)),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
            )
            if (difficultyLabel.isNotBlank()) {
                Text(
                    text = "범위: $difficultyLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
    if (resultWords.isEmpty()) {
        item {
            val colors = AppTheme.colors
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "단어 정보를 불러오는 중이거나 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                )
            }
        }
    } else {
        itemsIndexed(
            resultWords,
            key = { _, pair -> pair.first.id },
        ) { _, (word, known) ->
            val stats = resultWordStats[word.id]
            ResultWordItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                word = word,
                known = known,
                stats = stats,
                onSpeak = { onSpeakWord(word) },
                onDetail = { onWordDetail(word.word) },
            )
        }
    }
}

private fun testTypeLabelForList(testType: String): String =
    when (testType) {
        TestResult.TEST_TYPE_SELF -> "자기테스트"
        TestResult.TEST_TYPE_MULTIPLE_CHOICE -> "객관식"
        else -> if (testType.isBlank()) "—" else testType
    }

@Composable
private fun ResultListItem(
    result: TestResult,
    listNumber: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dateFormat = SimpleDateFormat("yyyy. M. d. HH:mm", Locale.getDefault())
    val leftScroll = rememberScrollState()
    Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .border(0.5.dp, colors.borderDefault, RoundedCornerShape(AppDimens.cardCornerRadius)),
        shape = RoundedCornerShape(AppDimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        colors =
            CardDefaults.cardColors(
                containerColor = colors.bgCard,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.screenPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .horizontalScroll(leftScroll),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = listNumber.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.badgePurpleText,
                )
                Text(
                    text = dateFormat.format(Date(result.testDateMillis)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
                Text(
                    text = testTypeLabelForList(result.testType),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.pinkMain,
                )
                Text(
                    text = formatDifficultyLabel(result.difficulty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                )
            }
            Text(
                text = "${result.score * 10}점",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.purpleMain,
            )
        }
    }
}

@Composable
private fun ResultWordItem(
    modifier: Modifier = Modifier,
    word: Word,
    known: Boolean,
    stats: Pair<Int, Int>?,
    onSpeak: () -> Unit,
    onDetail: () -> Unit,
) {
    val colors = AppTheme.colors
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .border(0.5.dp, colors.borderDefault, RoundedCornerShape(AppDimens.cardCornerRadius)),
        shape = RoundedCornerShape(AppDimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        colors =
            CardDefaults.cardColors(
                containerColor = colors.bgCard,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 1줄: 영어단어 · 발음기호
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.purpleMain,
                    )
                    Text(
                        text = word.phoneticDisplayText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textDim,
                    )
                }
                // 2줄: 품사 · 단어뜻 · 난이도(별) · 스피커
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (word.partOfSpeech.isNotBlank()) {
                        Text(
                            text = word.partOfSpeech,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted,
                        )
                    }
                    Text(
                        text = word.meaning,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                    )
                    Text(
                        text = "(${"★".repeat(word.difficulty.starCount)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.pinkMain,
                    )
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "발음 재생",
                            tint = colors.purpleMain,
                        )
                    }
                }
                // 3줄: 정답율, 오답율, 시도회수
                Text(
                    text =
                        if (stats != null && stats.second > 0) {
                            val (c, t) = stats
                            val correctPct = (c * 100 / t).toInt()
                            val wrongPct = 100 - correctPct
                            "정답 $correctPct% · 오답 $wrongPct% · 시도 ${t}회"
                        } else {
                            "시도 0회"
                        },
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted,
                )
            }
            Row(
                modifier =
                    Modifier
                        .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onDetail,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "상세보기",
                        tint = colors.purpleMain,
                    )
                }
                // O / X — 목록 항목 높이의 45% 크기
                BoxWithConstraints(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .width(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier.height(maxHeight * 0.45f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (known) "O" else "X",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (known) colors.greenMain else colors.pinkMain,
                        )
                    }
                }
            }
        }
    }
}
