package com.euysoo.engtest.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.euysoo.engtest.ui.components.AppTopBar
import com.euysoo.engtest.ui.theme.AppTheme

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val colors = AppTheme.colors
    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = { AppTopBar(title = "개인정보처리방침", onBackClick = onBack) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            PolicySection(
                title = "1. 수집하는 개인정보 항목",
                content =
                    "본 앱(영어단어 암기장)은 별도의 회원가입 없이 사용할 수 있으며, " +
                        "서버에 어떠한 개인정보도 전송하지 않습니다.\n\n" +
                        "• 단어 학습 기록: 기기 내부 저장소(SQLite)에만 저장\n" +
                        "• 테스트 결과: 기기 내부 저장소에만 저장\n" +
                        "• 카메라/갤러리 이미지: OCR 처리 후 즉시 삭제, 외부 전송 없음",
            )
            PolicySection(
                title = "2. 개인정보의 이용 목적",
                content =
                    "수집한 정보는 앱 기능 제공(단어 학습·테스트·기록 확인)에만 사용하며, " +
                        "제3자에게 제공하지 않습니다.",
            )
            PolicySection(
                title = "3. 개인정보의 보유 및 이용 기간",
                content = "모든 데이터는 기기 내부에 저장되며, 앱 삭제 시 함께 삭제됩니다.",
            )
            PolicySection(
                title = "4. 인터넷 권한 사용",
                content =
                    "발음 기호 조회(Free Dictionary API) 목적으로 인터넷 접속을 사용합니다. " +
                        "이 과정에서 단어 텍스트만 전송되며, 개인식별 정보는 포함되지 않습니다.",
            )
            PolicySection(
                title = "5. 카메라 및 저장소 권한",
                content =
                    "OCR 단어 추가 기능 사용 시 카메라 및 갤러리 접근 권한이 필요합니다. " +
                        "촬영된 이미지는 텍스트 인식 후 즉시 메모리에서 해제되며, 외부로 전송되지 않습니다.",
            )
            PolicySection(
                title = "6. 문의",
                content = "개인정보처리방침에 관한 문의는 앱 스토어 개발자 연락처를 이용해 주세요.",
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("최종 수정일: 2026년 4월 17일", fontSize = 12.sp, color = colors.textMuted)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    content: String,
) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.purpleMain)
        Text(content, fontSize = 14.sp, color = colors.textPrimary, lineHeight = 22.sp)
        HorizontalDivider(color = colors.borderDefault)
    }
}
