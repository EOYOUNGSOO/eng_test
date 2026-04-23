package com.euysoo.engtest.ui.screen.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

private data class GuideSection(
    val title: String,
    val description: String,
)

@Composable
fun AppGuideScreen(onBack: () -> Unit) {
    val colors = AppTheme.colors
    val guideItems =
        listOf(
            GuideSection(
                title = "📚 단어 학습",
                description =
                    "교육부 지정 필수 어휘 3,000개가 초등·중등·고등 난이도로 분류되어 제공됩니다. " +
                        "단어 카드를 넘기며 영어 단어와 뜻을 익힐 수 있습니다.",
            ),
            GuideSection(
                title = "✏️ 테스트",
                description =
                    "셀프 테스트와 4지선다 테스트 두 가지 방식으로 학습 성취도를 확인합니다. " +
                        "난이도별 필터링으로 원하는 수준의 단어만 골라 테스트할 수 있습니다.",
            ),
            GuideSection(
                title = "📖 나의 단어장",
                description =
                    "자주 틀리거나 외우고 싶은 단어를 모아 나만의 단어장을 만들 수 있습니다. " +
                        "단어표 이미지를 촬영하면 OCR로 단어를 자동 인식해 단어장에 추가할 수 있습니다.",
            ),
            GuideSection(
                title = "📷 이미지로 단어 추가 (OCR)",
                description =
                    "교재나 프린트물의 단어 목록을 카메라로 찍으면 단어·품사·뜻을 자동 인식합니다. " +
                        "인식 결과를 직접 수정한 후 단어장에 저장할 수 있습니다.",
            ),
            GuideSection(
                title = "❌ 오답노트",
                description =
                    "테스트 이력을 분석해 자주 틀린 단어를 자동으로 모은 오답노트 단어장을 만듭니다. " +
                        "난이도별 필터와 임의 단어 채우기 옵션을 제공합니다.",
            ),
            GuideSection(
                title = "🔊 발음 기호",
                description =
                    "각 단어의 IPA 발음 기호를 Free Dictionary API로 자동 조회합니다. " +
                        "Wi-Fi 환경에서 15분마다 미조회 단어를 백그라운드로 업데이트합니다.",
            ),
            GuideSection(
                title = "🔄 초기화",
                description =
                    "단어 관리 화면의 '초기화' 버튼으로 교육부 표준 어휘 3,000개로 데이터를 복원합니다. " +
                        "직접 추가하거나 수정한 단어는 초기화 시 삭제되니 주의하세요.",
            ),
        )

    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = { AppTopBar(title = "앱 기능 설명", onBackClick = onBack) },
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
            Spacer(modifier = Modifier.height(4.dp))
            guideItems.forEach { item ->
                Card(
                    modifier =
                        Modifier.border(0.5.dp, colors.borderDefault, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.bgCard),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                        )
                        Text(
                            text = item.description,
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }
            HorizontalDivider(color = colors.borderDefault)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
