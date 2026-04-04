package com.euysoo.engtest.ui.screen.wordtest

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.euysoo.engtest.ui.components.AppCopyrightFooter
import com.euysoo.engtest.ui.components.AppTopBar
import com.euysoo.engtest.ui.theme.AppTheme
import com.euysoo.engtest.ui.theme.mzChoiceKeycap
import com.euysoo.engtest.ui.theme.mzIconAccent
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleChoiceTestScreen(
    viewModel: MultipleChoiceTestViewModel,
    onBack: () -> Unit,
    onTestFinished: () -> Unit,
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }
    val tts =
        remember {
            TextToSpeech(context.applicationContext) { status -> ttsReady = (status == TextToSpeech.SUCCESS) }
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
        if (ttsReady) tts.language = Locale.US
    }

    val loadFinished by viewModel.loadFinished.collectAsStateWithLifecycle()
    val questions by viewModel.questions.collectAsStateWithLifecycle()
    val words by viewModel.words.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()
    val showResult by viewModel.showResult.collectAsStateWithLifecycle()
    val answers by viewModel.answers.collectAsStateWithLifecycle()
    val testStartTime by viewModel.testStartTime.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = {},
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(colors.bgPrimary),
        ) {
            when {
                !loadFinished -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                        ) {
                            item {
                                AppTopBar(
                                    title = "객관식 테스트",
                                    onBackClick = onBack,
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "단어를 불러오는 중...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colors.textMuted,
                                    )
                                }
                            }
                        }
                        AppCopyrightFooter()
                    }
                }
                showResult -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                        ) {
                            item {
                                AppTopBar(
                                    title = "객관식 결과",
                                    onBackClick = onTestFinished,
                                )
                            }
                            testResultSummaryItemsNoFooter(
                                words = words,
                                answers = answers,
                                score = viewModel.getScore(),
                                testStartTimeMillis = testStartTime,
                                onSpeak = { w ->
                                    if (ttsReady) tts.speak(w.word, TextToSpeech.QUEUE_FLUSH, null, null)
                                },
                                onFinish = onTestFinished,
                            )
                        }
                        AppCopyrightFooter()
                    }
                }
                questions.isEmpty() -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                        ) {
                            item {
                                AppTopBar(
                                    title = "객관식 테스트",
                                    onBackClick = onBack,
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "출제할 단어가 없습니다. (단어장이 비었을 수 있습니다)",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colors.textMuted,
                                        modifier = Modifier.padding(24.dp),
                                    )
                                }
                            }
                        }
                        AppCopyrightFooter()
                    }
                }
                currentIndex >= questions.size -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                        ) {
                            item {
                                AppTopBar(
                                    title = "객관식 테스트",
                                    onBackClick = onBack,
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("처리 중...", color = colors.textMuted)
                                }
                            }
                        }
                        AppCopyrightFooter()
                    }
                }
                else -> {
                    val q = questions[currentIndex]
                    val total = questions.size
                    val progress = (currentIndex + 1).toFloat() / total.coerceAtLeast(1)
                    Column(modifier = Modifier.fillMaxSize()) {
                        AppTopBar(
                            title = "객관식 테스트",
                            onBackClick = onBack,
                        )
                        Column(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 8.dp),
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .fillMaxWidth(),
                            ) {
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(colors.bgCard, RoundedCornerShape(16.dp))
                                            .border(0.5.dp, colors.borderDefault, RoundedCornerShape(16.dp))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                ) {
                                    Text(
                                        text = "${currentIndex + 1} / $total",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = colors.textMuted,
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(5.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = colors.bgPrimary,
                                    )
                                }
                                Spacer(modifier = Modifier.height(18.dp))
                                Card(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .border(2.dp, colors.borderDefault, RoundedCornerShape(24.dp))
                                            .clip(RoundedCornerShape(24.dp)),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.bgCard),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                ) {
                                    Column(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            text = "뜻을 고르세요",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = colors.textMuted,
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = q.word.word,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.purpleMain,
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    q.options.forEachIndexed { idx, meaning ->
                                        MultipleChoiceOptionCell(
                                            optionIndex = idx,
                                            meaning = meaning,
                                            onClick = { viewModel.submitChoice(idx) },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                        AppCopyrightFooter()
                    }
                }
            }
        }
    }
}

@Composable
private fun MultipleChoiceOptionCell(
    optionIndex: Int,
    meaning: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val accent = mzIconAccent(optionIndex)
    Row(
        modifier =
            modifier
                .heightIn(min = 72.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(0.5.dp, colors.borderDefault, RoundedCornerShape(14.dp))
                .background(colors.bgPrimary)
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(accent.background),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = mzChoiceKeycap(optionIndex),
                fontSize = 17.sp,
                maxLines = 1,
            )
        }
        Text(
            text = meaning,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
