package com.euysoo.engtest.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.euysoo.engtest.ui.components.AppCopyrightFooter
import com.euysoo.engtest.util.AppLogger
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    var logs by remember { mutableStateOf(AppLogger.logs) }
    var crashLog by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            logs = AppLogger.logs
            delay(1000)
        }
    }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("crash_log", android.content.Context.MODE_PRIVATE)
        crashLog = prefs.getString("last_crash", null) ?: "크래시 기록 없음"
    }

    Scaffold(
        containerColor = Color(0xFF0D0D0D),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "로그 뷰어",
                        fontSize = 16.sp,
                        color = Color(0xFFF0EEFF)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                            tint = Color(0xFFA78BFA)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            val allLogs = logs.joinToString("\n") { entry ->
                                val time = fmt.format(Date(entry.timestamp))
                                "[$time][${entry.level}][${entry.tag}] ${entry.message}"
                            }
                            clipboardManager.setText(AnnotatedString(allLogs))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "복사",
                            tint = Color(0xFF34D399)
                        )
                    }
                    IconButton(
                        onClick = {
                            AppLogger.clear()
                            logs = emptyList()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "삭제",
                            tint = Color(0xFFF472B6)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF16151F)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF16151F),
                contentColor = Color(0xFFA78BFA)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "실시간 로그 (${logs.size})",
                            fontSize = 12.sp,
                            color = if (selectedTab == 0) Color(0xFFA78BFA)
                            else Color(0xFF4E4D62)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "마지막 크래시",
                            fontSize = 12.sp,
                            color = if (selectedTab == 1) Color(0xFFF472B6)
                            else Color(0xFF4E4D62)
                        )
                    }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (logs.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "로그 없음",
                                        color = Color(0xFF4E4D62),
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentPadding = PaddingValues(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    itemsIndexed(
                                        logs.reversed(),
                                        key = { index, e -> "${e.timestamp}_${index}_${e.tag}" }
                                    ) { _, entry ->
                                        LogEntryRow(entry = entry)
                                    }
                                }
                            }
                            AppCopyrightFooter(
                                fontSize = 10.sp,
                                textColor = Color(0xFF4E4D62)
                            )
                        }
                    }
                    1 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(crashLog))
                                    }
                                ) {
                                    Text("복사", color = Color(0xFF34D399), fontSize = 12.sp)
                                }
                                TextButton(
                                    onClick = {
                                        context.getSharedPreferences("crash_log", android.content.Context.MODE_PRIVATE)
                                            .edit().remove("last_crash").apply()
                                        crashLog = "크래시 기록 없음"
                                    }
                                ) {
                                    Text("삭제", color = Color(0xFFF472B6), fontSize = 12.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .background(Color(0xFF16151F), shape = MaterialTheme.shapes.medium)
                                    .padding(12.dp)
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = crashLog,
                                    color = if (crashLog == "크래시 기록 없음") Color(0xFF4E4D62)
                                    else Color(0xFFF472B6),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
                            AppCopyrightFooter(
                                fontSize = 10.sp,
                                textColor = Color(0xFF4E4D62)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntryRow(entry: AppLogger.LogEntry) {
    val levelColor = when (entry.level) {
        "E" -> Color(0xFFF472B6)
        "W" -> Color(0xFFFBBF24)
        "I" -> Color(0xFF34D399)
        else -> Color(0xFF4E4D62)
    }
    val time = remember(entry.timestamp) {
        SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(entry.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF16151F), shape = MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = time,
                fontSize = 10.sp,
                color = Color(0xFF4E4D62),
                fontFamily = FontFamily.Monospace
            )
            Box(
                modifier = Modifier
                    .background(levelColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = entry.level,
                    fontSize = 10.sp,
                    color = levelColor,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = entry.tag,
                fontSize = 10.sp,
                color = Color(0xFFA78BFA),
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Text(
                text = entry.message,
                fontSize = 11.sp,
                color = if (entry.level == "E") Color(0xFFF472B6)
                else Color(0xFFE8E6FF),
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }
    }
}
