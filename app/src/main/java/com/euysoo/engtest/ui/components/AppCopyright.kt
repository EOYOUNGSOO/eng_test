package com.euysoo.engtest.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 앱 전역 저작권 문구 (메인·상세 화면 공통) */
const val APP_COPYRIGHT_LINE = "© 2026. 경주아빠. All rights reserved."

@Composable
fun AppCopyrightFooter(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 10.sp,
    textColor: Color = Color(0xFF2E2D3D),
) {
    Text(
        text = APP_COPYRIGHT_LINE,
        fontSize = fontSize,
        color = textColor,
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
    )
}

/**
 * [mainContent]와 저작권을 한 덩어리로 세로 스크롤한다.
 * 본문 높이가 뷰포트보다 짧으면 저작권은 화면 하단에 맞춘다.
 */
@Composable
fun ScrollColumnWithBottomCopyright(
    modifier: Modifier = Modifier,
    copyrightFontSize: TextUnit = 10.sp,
    mainContent: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewportPx = with(density) { maxHeight.roundToPx() }
        SubcomposeLayout(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
        ) { constraints ->
            val maxW = constraints.maxWidth
            val loose =
                Constraints(
                    minWidth = constraints.minWidth,
                    maxWidth = maxW,
                    minHeight = 0,
                    maxHeight = Constraints.Infinity,
                )
            val footerMeasurable =
                subcompose("footer") {
                    AppCopyrightFooter(fontSize = copyrightFontSize)
                }.first()
            val footerPlaceable = footerMeasurable.measure(loose)
            val mainPlaceable = subcompose("main", mainContent).first().measure(loose)
            val mainH = mainPlaceable.height
            val footerH = footerPlaceable.height
            val spacerH = (viewportPx - mainH - footerH).coerceAtLeast(0)
            val totalH = mainH + spacerH + footerH
            layout(maxW, totalH) {
                mainPlaceable.place(0, 0)
                footerPlaceable.place(0, mainH + spacerH)
            }
        }
    }
}
