package com.euysoo.engtest.ui.screen.wordbook

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.data.entity.WordBook
import com.euysoo.engtest.ui.component.AppButton
import com.euysoo.engtest.ui.component.AppButtonStyle
import com.euysoo.engtest.ui.components.AppCopyrightFooter
import com.euysoo.engtest.ui.components.AppTopBar
import com.euysoo.engtest.ui.theme.AppTheme
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWordBookScreen(
    onBack: () -> Unit,
    onOpenBook: (Long) -> Unit,
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val app = context.applicationContext as EngTestApplication
    val viewModel: MyWordBookViewModel = viewModel(factory = MyWordBookViewModelFactory(app))
    val books by viewModel.books.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var bookToRename by remember { mutableStateOf<WordBook?>(null) }
    var bookToDelete by remember { mutableStateOf<WordBook?>(null) }

    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = {
            AppTopBar(title = "나의 단어장", onBackClick = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(12.dp)) }
                item {
                    AppButton(
                        text = "새 단어장 만들기",
                        onClick = { showCreateDialog = true },
                        style = AppButtonStyle.PRIMARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
                if (books.isEmpty()) {
                    item {
                        Text(
                            text = "등록된 단어장이 없습니다.\n단어 관리에서 단어를 단어장에 담을 수 있습니다.",
                            fontSize = 14.sp,
                            color = colors.textMuted,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    items(books, key = { it.id }) { book ->
                        WordBookRow(
                            book = book,
                            onOpen = { onOpenBook(book.id) },
                            onRename = { bookToRename = book },
                            onDelete = { bookToDelete = book }
                        )
                    }
                }
            }
            AppCopyrightFooter()
        }
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
                    label = { Text("이름") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.createBook(name)
                        showCreateDialog = false
                    }
                ) {
                    Text("만들기", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("취소")
                }
            }
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
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renameBook(book, name)
                        bookToRename = null
                    }
                ) {
                    Text("저장", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToRename = null }) {
                    Text("취소")
                }
            }
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
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToDelete = null }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
private fun WordBookRow(
    book: WordBook,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, colors.borderDefault, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
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
