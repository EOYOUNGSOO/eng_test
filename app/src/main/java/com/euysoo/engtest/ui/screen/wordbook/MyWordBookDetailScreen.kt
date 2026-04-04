package com.euysoo.engtest.ui.screen.wordbook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.data.entity.Word
import com.euysoo.engtest.ui.components.AppCopyrightFooter
import com.euysoo.engtest.ui.components.AppTopBar
import com.euysoo.engtest.ui.theme.AppColors
import com.euysoo.engtest.ui.theme.AppDimens
import com.euysoo.engtest.ui.theme.AppTheme
import com.euysoo.engtest.ui.theme.mzBookEmoji
import com.euysoo.engtest.ui.theme.mzIconAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWordBookDetailScreen(
    bookId: Long,
    onBack: () -> Unit,
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val app = context.applicationContext as EngTestApplication
    val viewModel: MyWordBookDetailViewModel =
        viewModel(
            factory = MyWordBookDetailViewModelFactory(app.appContainer, bookId),
        )
    val wordItems by viewModel.wordItems.collectAsStateWithLifecycle()
    val book by viewModel.book.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResultsNotInBook.collectAsStateWithLifecycle()
    val highlightWordIds by viewModel.highlightWordIds.collectAsStateWithLifecycle()

    var wordToRemove by remember { mutableStateOf<Word?>(null) }
    var selectedSearchWordId by remember { mutableLongStateOf(-1L) }
    var selectedBookWordId by remember { mutableLongStateOf(-1L) }

    LaunchedEffect(searchResults) {
        if (selectedSearchWordId >= 0L && searchResults.none { it.id == selectedSearchWordId }) {
            selectedSearchWordId = -1L
        }
    }
    LaunchedEffect(wordItems) {
        if (selectedBookWordId >= 0L && wordItems.none { it.word.id == selectedBookWordId }) {
            selectedBookWordId = -1L
        }
    }

    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = {
            AppTopBar(
                title = book?.name ?: "단어장",
                onBackClick = onBack,
            )
        },
    ) { padding ->
        val scrollState = rememberScrollState()
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
        ) {
            val minSearchH = maxHeight * (5f / 11f)
            val minBookH = maxHeight * (6f / 11f)
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
            ) {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = minSearchH),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.bgCard,
                    border = BorderStroke(AppDimens.appCardBorder, colors.borderDefault),
                    shadowElevation = 2.dp,
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "단어 검색",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.purpleMain,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::setSearchQuery,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("영단어 또는 뜻으로 검색", color = colors.textMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = colors.textMuted,
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    when {
                        searchQuery.isBlank() -> {
                            Text(
                                text = "검색어를 입력하면 추가할 단어를 고를 수 있습니다.",
                                fontSize = 13.sp,
                                color = colors.textMuted,
                            )
                        }
                        searchResults.isEmpty() -> {
                            Text(
                                text = "일치하는 단어가 없거나 이미 이 단어장에 모두 담겼습니다.",
                                fontSize = 13.sp,
                                color = colors.textMuted,
                            )
                        }
                        else -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                searchResults.forEachIndexed { idx, word ->
                                    key(word.id) {
                                        SearchResultRow(
                                            listIndex = idx,
                                            word = word,
                                            selected = word.id == selectedSearchWordId,
                                            colors = colors,
                                            onClick = {
                                                selectedSearchWordId = word.id
                                                selectedBookWordId = -1L
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.bgCard,
                    border = BorderStroke(AppDimens.appCardBorder, colors.borderAccent.copy(alpha = 0.45f)),
                    shadowElevation = 1.dp,
                ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val upEnabled = selectedBookWordId >= 0L
                    Surface(
                        shape = CircleShape,
                        color = if (upEnabled) colors.pinkMain.copy(alpha = 0.12f) else colors.bgCard,
                        border =
                            BorderStroke(
                                width = if (upEnabled) 1.dp else 0.5.dp,
                                color =
                                    if (upEnabled) {
                                        colors.pinkMain.copy(alpha = 0.55f)
                                    } else {
                                        colors.borderDefault.copy(alpha = 0.6f)
                                    },
                            ),
                        modifier = Modifier.size(46.dp),
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedBookWordId >= 0L) {
                                    viewModel.removeWord(selectedBookWordId)
                                    selectedBookWordId = -1L
                                }
                            },
                            enabled = upEnabled,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowUp,
                                contentDescription = "단어장에서 빼기",
                                modifier = Modifier.size(22.dp),
                                tint = if (upEnabled) colors.pinkMain else colors.textMuted,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 20.dp))
                    val downEnabled = selectedSearchWordId >= 0L
                    Surface(
                        shape = CircleShape,
                        color = if (downEnabled) colors.greenMain.copy(alpha = 0.16f) else colors.bgCard,
                        border =
                            BorderStroke(
                                width = if (downEnabled) 2.5.dp else 1.dp,
                                color = if (downEnabled) colors.greenMain else colors.borderDefault.copy(alpha = 0.55f),
                            ),
                        modifier = Modifier.size(56.dp),
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedSearchWordId >= 0L) {
                                    viewModel.addWordToBook(selectedSearchWordId)
                                    selectedSearchWordId = -1L
                                }
                            },
                            enabled = downEnabled,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "단어장에 담기",
                                modifier = Modifier.size(32.dp),
                                tint = if (downEnabled) colors.greenMain else colors.textMuted,
                            )
                        }
                    }
                }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = minBookH),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.bgCard,
                    border = BorderStroke(AppDimens.appCardBorder, colors.borderDefault),
                    shadowElevation = 2.dp,
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "나의 단어장",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.purpleMain,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    Text(
                        text = "포함 단어 ${wordItems.size}개",
                        fontSize = 13.sp,
                        color = colors.textMuted,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    if (wordItems.isEmpty()) {
                        Text(
                            text = "이 단어장에 담긴 단어가 없습니다.\n위에서 검색한 뒤 ↓로 담거나, 단어 관리에서 추가해 보세요.",
                            fontSize = 14.sp,
                            color = colors.textMuted,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            wordItems.forEachIndexed { index, item ->
                                key(item.word.id) {
                                    WordBookEntryRow(
                                        index = index + 1,
                                        word = item.word,
                                        highlighted = item.word.id in highlightWordIds,
                                        selected = item.word.id == selectedBookWordId,
                                        colors = colors,
                                        onRowClick = {
                                            selectedBookWordId = item.word.id
                                            selectedSearchWordId = -1L
                                        },
                                        onRemoveClick = { wordToRemove = item.word },
                                    )
                                }
                            }
                        }
                    }
                    }
                }

                AppCopyrightFooter(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 24.dp),
                )
            }
        }
    }

    wordToRemove?.let { w ->
        AlertDialog(
            onDismissRequest = { wordToRemove = null },
            title = { Text("단어장에서 제거") },
            text = { Text("\"${w.word}\"을(를) 이 단어장에서 뺄까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeWord(w.id)
                        wordToRemove = null
                        if (selectedBookWordId == w.id) selectedBookWordId = -1L
                    },
                ) {
                    Text("제거")
                }
            },
            dismissButton = {
                TextButton(onClick = { wordToRemove = null }) {
                    Text("취소")
                }
            },
        )
    }
}

@Composable
private fun SearchResultRow(
    listIndex: Int,
    word: Word,
    selected: Boolean,
    colors: AppColors,
    onClick: () -> Unit,
) {
    val borderWidth = if (selected) 1.5.dp else 0.5.dp
    val borderColor = if (selected) colors.purpleMain else colors.borderDefault
    val accent = mzIconAccent(listIndex)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.bgPrimary, RoundedCornerShape(14.dp))
                .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(12.dp),
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
            Text(text = mzBookEmoji(listIndex), fontSize = 20.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = word.word,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.purpleMain,
            )
            Text(
                text = word.meaning,
                fontSize = 13.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun WordBookEntryRow(
    index: Int,
    word: Word,
    highlighted: Boolean,
    selected: Boolean,
    colors: AppColors,
    onRowClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    val borderWidth =
        when {
            selected -> 2.dp
            highlighted && !selected -> 1.5.dp
            else -> 0.5.dp
        }
    val borderColor =
        when {
            selected -> colors.purpleMain
            highlighted && !selected -> colors.greenMain.copy(alpha = 0.75f)
            else -> colors.borderDefault
        }
    val fillColor =
        when {
            selected -> colors.purpleLight.copy(alpha = 0.18f)
            highlighted -> colors.greenMain.copy(alpha = 0.14f)
            else -> colors.bgPrimary
        }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(fillColor, RoundedCornerShape(14.dp))
                .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
                .clickable(onClick = onRowClick)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val accent = mzIconAccent(index - 1)
            Box(
                modifier =
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.background),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = mzBookEmoji(index - 1), fontSize = 18.sp)
            }
            Text(
                text = "$index.",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color =
                    when {
                        selected -> colors.purpleMain
                        highlighted -> colors.greenMain
                        else -> colors.textMuted
                    },
                modifier =
                    Modifier
                        .widthIn(min = 24.dp)
                        .padding(end = 4.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.word,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.purpleMain,
                )
                Text(
                    text = word.meaning,
                    fontSize = 13.sp,
                    color = if (highlighted) colors.textSecondary.copy(alpha = 0.92f) else colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        IconButton(
            onClick = onRemoveClick,
            modifier = Modifier.padding(start = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = "단어장에서 제거",
                tint = if (highlighted) colors.greenMain.copy(alpha = 0.9f) else colors.purpleMain,
            )
        }
    }
}
