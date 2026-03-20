package com.example.engtest.ui.screen.wordtest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engtest.data.entity.Word
import com.example.engtest.ui.component.AppButton
import com.example.engtest.ui.component.AppButtonStyle
import com.example.engtest.ui.components.AppTopBar
import com.example.engtest.ui.theme.AppTheme
import com.example.engtest.ui.theme.AppDimens
import com.example.engtest.util.phoneticDisplayText
import com.example.engtest.util.starCount
import android.speech.tts.TextToSpeech
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordTestScreen(
    viewModel: WordTestViewModel,
    onBack: () -> Unit,
    onTestFinished: () -> Unit
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context.applicationContext) { status -> ttsReady = (status == TextToSpeech.SUCCESS) }
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
    val words by viewModel.words.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val showResult by viewModel.showResult.collectAsStateWithLifecycle()
    val showingMeaning by viewModel.showingMeaning.collectAsStateWithLifecycle()
    val answers by viewModel.answers.collectAsStateWithLifecycle()
    val testStartTime by viewModel.testStartTime.collectAsStateWithLifecycle()

    val testBackground = colors.bgPrimary

    Scaffold(
        containerColor = testBackground,
        topBar = {
            AppTopBar(
                title = if (showResult) "테스트 결과" else "단어 테스트",
                onBackClick = { if (showResult) onTestFinished() else onBack() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(testBackground)
        ) {
            when {
                showResult -> {
                    TestResultContent(
                        words = words,
                        answers = answers,
                        score = viewModel.getScore(),
                        testStartTimeMillis = testStartTime,
                        onSpeak = { w -> if (ttsReady) tts.speak(w.word, TextToSpeech.QUEUE_FLUSH, null, null) },
                        onFinish = onTestFinished
                    )
                }
                words.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "단어를 불러오는 중...",
                            style = MaterialTheme.typography.bodyLarge,
                        color = colors.textMuted
                        )
                    }
                }
                currentIndex >= words.size -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "테스트가 완료되었습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                        color = colors.textMuted
                        )
                    }
                }
                else -> {
                    val word = words[currentIndex]
                    val total = words.size
                    val progress = (currentIndex + 1).toFloat() / total.coerceAtLeast(1)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                    ) {
                        // 색감 뚜렷한 프로그레스 바 (10문제 중 현재 진행)
                        Text(
                            text = "${currentIndex + 1} / $total",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textMuted,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = colors.bgCard
                        )
                        Spacer(modifier = Modifier.height(28.dp))

                        // 단어 카드 상단 시작 위치: 상하의 35% (위 35% / 아래 65%)
                        Spacer(modifier = Modifier.weight(0.35f))
                        Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        2.dp,
                                        colors.borderDefault,
                                        RoundedCornerShape(24.dp)
                                    )
                                    .clip(RoundedCornerShape(24.dp)),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.bgCard),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if (!showingMeaning) {
                                        Text(
                                            text = "남은 시간: ${remainingSeconds}초",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = colors.purpleMain,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                    }
                                    // 영어 단어 — 길이에 따라 한 줄로 표시되도록 글자 크기 조절
                                    Text(
                                        text = word.word,
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontSize = when (word.word.length) {
                                            in 0..6 -> 48.sp
                                            in 7..9 -> 42.sp
                                            in 10..12 -> 36.sp
                                            in 13..16 -> 30.sp
                                            in 17..20 -> 24.sp
                                            else -> 20.sp
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        maxLines = 1
                                    )
                                    if (showingMeaning) {
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Text(
                                            text = word.meaning,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = colors.purpleMain
                                        )
                                        Text(
                                            text = word.phoneticDisplayText(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.textDim,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        if (word.partOfSpeech.isNotBlank()) {
                                            Row(
                                                modifier = Modifier.padding(top = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = word.partOfSpeech,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = colors.textMuted
                                                )
                                                IconButton(
                                                    onClick = { if (ttsReady) tts.speak(word.word, TextToSpeech.QUEUE_FLUSH, null, null) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                        contentDescription = "발음 재생",
                                                        tint = colors.purpleMain
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        if (!showingMeaning) {
                            // 카드 아래 65% 영역: 체크/엑스 버튼 구간
                            Spacer(modifier = Modifier.weight(0.15f))
                            val checkBlue = colors.greenMain
                            val xRed = colors.pinkMain
                            val iconBtnColors = ButtonDefaults.buttonColors(
                                containerColor = colors.bgCard,
                                contentColor = colors.textSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.recordAnswer(true) },
                                    modifier = Modifier.size(48.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = iconBtnColors
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = "알고 있음",
                                        modifier = Modifier.size(28.dp),
                                        tint = checkBlue
                                    )
                                }
                                Spacer(modifier = Modifier.size(16.dp))
                                Button(
                                    onClick = { viewModel.recordAnswer(false) },
                                    modifier = Modifier.size(48.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = iconBtnColors
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "모름",
                                        modifier = Modifier.size(28.dp),
                                        tint = xRed
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(0.5f))
                        } else {
                            // 결과 확인: "X 로 변경"만 표시 (알았다고 생각했는데 정답 확인 후 모름으로 변경)
                            val lastAnswerKnown = answers.isNotEmpty() && answers.last()
                            val xRed = Color(0xFFE53935)
                            val iconBtnColors = ButtonDefaults.buttonColors(
                                containerColor = colors.bgCard,
                                contentColor = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (lastAnswerKnown) {
                                    Button(
                                        onClick = { viewModel.changeLastAnswerToUnknown() },
                                        modifier = Modifier.height(44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = iconBtnColors,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "엑스로 변경",
                                            modifier = Modifier.size(20.dp),
                                            tint = xRed
                                        )
                                        Spacer(modifier = Modifier.size(6.dp))
                                        Text("로 변경", style = MaterialTheme.typography.labelLarge)
                                    }
                                    Spacer(modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.weight(0.2f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = { viewModel.confirmAndProceed() },
                                    modifier = Modifier.height(48.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = iconBtnColors,
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Text("확인", fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.weight(0.45f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestResultContent(
    words: List<Word>,
    answers: List<Boolean>,
    score: Int,
    testStartTimeMillis: Long,
    onSpeak: (Word) -> Unit,
    onFinish: () -> Unit
) {
    val colors = AppTheme.colors
    if (words.size != answers.size) return
    val dateFormat = SimpleDateFormat("yyyy. M. d. HH:mm", Locale.getDefault())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimens.screenPadding)
    ) {
        Text(
            text = "점수: ${score}점",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.purpleMain
        )
        Text(
            text = dateFormat.format(Date(testStartTimeMillis)),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted
        )
        Spacer(modifier = Modifier.height(AppDimens.screenPadding))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = AppDimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimens.listItemSpacing)
        ) {
            itemsIndexed(words) { index, word ->
                ResultWordItem(
                    word = word,
                    known = answers.getOrElse(index) { false },
                    onSpeak = { onSpeak(word) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppDimens.screenPadding),
            horizontalArrangement = Arrangement.Center
        ) {
            AppButton(
                text = "완료",
                style = AppButtonStyle.SECONDARY,
                onClick = onFinish
            )
        }
    }
}

@Composable
private fun ResultWordItem(
    word: Word,
    known: Boolean,
    onSpeak: () -> Unit
) {
    val colors = AppTheme.colors
    // 이번 테스트 1회분만 표시 (정답 100% or 0%, 시도 1회)
    val statsText = remember(known) {
        val correctPct = if (known) 100 else 0
        val wrongPct = 100 - correctPct
        "정답 ${correctPct}% · 오답 ${wrongPct}% · 시도 1회"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, colors.borderDefault, RoundedCornerShape(AppDimens.cardCornerRadius)),
        shape = RoundedCornerShape(AppDimens.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.cardElevation),
        colors = CardDefaults.cardColors(
            containerColor = colors.bgCard
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
                        color = colors.purpleMain
                    )
                    Text(
                        text = word.phoneticDisplayText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textDim
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
                            color = colors.textMuted
                        )
                    }
                    Text(
                        text = word.meaning,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "(${"★".repeat(word.difficulty.starCount)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.purpleMain
                    )
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(AppDimens.iconButtonSize)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "발음 재생",
                            tint = colors.purpleMain
                        )
                    }
                }
                // 3줄: 정답율, 오답율, 시도회수
                Text(
                    text = statsText,
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
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
                        color = if (known) colors.greenMain else colors.pinkMain
                    )
                }
            }
        }
    }
}

