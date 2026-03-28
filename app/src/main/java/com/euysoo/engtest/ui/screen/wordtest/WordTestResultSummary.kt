package com.euysoo.engtest.ui.screen.wordtest

import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.ui.component.AppButton
import com.euysoo.engtest.ui.component.AppButtonStyle
import com.euysoo.engtest.ui.components.AppCopyrightFooter
import com.euysoo.engtest.ui.theme.AppDimens
import com.euysoo.engtest.ui.theme.AppTheme
import com.euysoo.engtest.util.phoneticDisplayText
import com.euysoo.engtest.util.starCount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 점수·목록·완료 버튼만 (저작권 제외). 부모에서 화면 하단에 [AppCopyrightFooter]를 붙일 때 사용. */
fun LazyListScope.testResultSummaryItemsNoFooter(
    words: List<Word>,
    answers: List<Boolean>,
    score: Int,
    testStartTimeMillis: Long,
    onSpeak: (Word) -> Unit,
    onFinish: () -> Unit
) {
    if (words.size != answers.size) return
    val dateFormat = SimpleDateFormat("yyyy. M. d. HH:mm", Locale.getDefault())
    item {
        val colors = AppTheme.colors
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.screenPadding)
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
        }
    }
    itemsIndexed(
        words,
        key = { _, w -> w.id }
    ) { index, word ->
        ResultSummaryWordItem(
            word = word,
            known = answers.getOrElse(index) { false },
            onSpeak = { onSpeak(word) },
            modifier = Modifier.padding(horizontal = AppDimens.screenPadding)
        )
    }
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimens.screenPadding,
                    vertical = AppDimens.screenPadding
                ),
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

/** 부모 [LazyColumn] 안에서 저작권까지 한 스크롤로 넣을 때. */
fun LazyListScope.testResultSummaryItems(
    words: List<Word>,
    answers: List<Boolean>,
    score: Int,
    testStartTimeMillis: Long,
    onSpeak: (Word) -> Unit,
    onFinish: () -> Unit
) {
    testResultSummaryItemsNoFooter(
        words, answers, score, testStartTimeMillis, onSpeak, onFinish
    )
    item {
        AppCopyrightFooter(
            modifier = Modifier.padding(horizontal = AppDimens.screenPadding)
        )
    }
}

@Composable
fun TestResultSummaryContent(
    words: List<Word>,
    answers: List<Boolean>,
    score: Int,
    testStartTimeMillis: Long,
    onSpeak: (Word) -> Unit,
    onFinish: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = AppDimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimens.listItemSpacing)
        ) {
            testResultSummaryItemsNoFooter(
                words = words,
                answers = answers,
                score = score,
                testStartTimeMillis = testStartTimeMillis,
                onSpeak = onSpeak,
                onFinish = onFinish
            )
        }
        AppCopyrightFooter(
            modifier = Modifier.padding(horizontal = AppDimens.screenPadding)
        )
    }
}

@Composable
private fun ResultSummaryWordItem(
    word: Word,
    known: Boolean,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val statsText = remember(known) {
        val correctPct = if (known) 100 else 0
        val wrongPct = 100 - correctPct
        "정답 ${correctPct}% · 오답 ${wrongPct}% · 시도 1회"
    }
    Card(
        modifier = modifier
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
                Text(
                    text = statsText,
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
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
