package com.euysoo.engtest.ui.screen.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.euysoo.engtest.ui.components.AppTopBar
import com.euysoo.engtest.ui.theme.AppTheme

private fun StringBuilder.appendSection(
    name: String,
    license: String,
    details: String,
) {
    append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    append("📦 $name\n")
    append("라이선스: $license\n")
    append(details)
    append("\n\n")
}

@Composable
fun OpenSourceScreen(onBack: () -> Unit) {
    val colors = AppTheme.colors
    val licenseText =
        buildString {
            appendSection(
                "Jetpack Compose",
                "Apache License 2.0",
                "Copyright 2021 The Android Open Source Project\nhttps://github.com/androidx/androidx",
            )
            appendSection(
                "Kotlin",
                "Apache License 2.0",
                "Copyright 2010–2024 JetBrains s.r.o.\nhttps://github.com/JetBrains/kotlin",
            )
            appendSection(
                "Room",
                "Apache License 2.0",
                "Copyright 2017 The Android Open Source Project\nhttps://developer.android.com/jetpack/androidx/releases/room",
            )
            appendSection(
                "ML Kit Text Recognition",
                "ML Kit Terms of Service",
                "Copyright Google LLC\nhttps://developers.google.com/ml-kit",
            )
            appendSection(
                "CameraX",
                "Apache License 2.0",
                "Copyright 2019 The Android Open Source Project\nhttps://developer.android.com/jetpack/androidx/releases/camera",
            )
            appendSection(
                "Retrofit2",
                "Apache License 2.0",
                "Copyright 2013 Square, Inc.\nhttps://github.com/square/retrofit",
            )
            appendSection(
                "OkHttp",
                "Apache License 2.0",
                "Copyright 2019 Square, Inc.\nhttps://github.com/square/okhttp",
            )
            appendSection(
                "kotlinx.serialization",
                "Apache License 2.0",
                "Copyright 2017 JetBrains s.r.o.\nhttps://github.com/Kotlin/kotlinx.serialization",
            )
            appendSection(
                "WorkManager",
                "Apache License 2.0",
                "Copyright 2018 The Android Open Source Project\nhttps://developer.android.com/jetpack/androidx/releases/work",
            )
            appendSection(
                "Accompanist Permissions",
                "Apache License 2.0",
                "Copyright Google LLC\nhttps://github.com/google/accompanist",
            )
            appendSection(
                "Google Generative AI (Gemini) SDK",
                "Apache License 2.0",
                "Copyright Google LLC\nhttps://github.com/google-gemini/generative-ai-android",
            )
        }

    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = { AppTopBar(title = "오픈소스 라이선스", onBackClick = onBack) },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            SelectionContainer {
                Text(
                    text = licenseText,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = colors.textSecondary,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}
