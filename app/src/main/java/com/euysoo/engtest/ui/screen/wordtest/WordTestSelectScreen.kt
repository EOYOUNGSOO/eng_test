package com.euysoo.engtest.ui.screen.wordtest

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.euysoo.engtest.ui.component.AppButton
import com.euysoo.engtest.ui.component.AppButtonStyle
import com.euysoo.engtest.ui.components.AppCard
import com.euysoo.engtest.ui.components.AppTopBar
import com.euysoo.engtest.ui.theme.AppTheme

/** 난이도 선택 값: Nav 인자로 전달 */
const val DIFFICULTY_ALL = "all"
const val DIFFICULTY_ELEMENTARY = "elementary"
const val DIFFICULTY_MIDDLE = "middle"
const val DIFFICULTY_HIGH = "high"

data class DifficultyOption(
    val key: String,
    val label: String,
    val subtitle: String,
    val tint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordTestSelectScreen(
    onSelectDifficulty: (String) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    val colors = AppTheme.colors
    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = {
            AppTopBar(title = "단어 테스트", onBackClick = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .background(colors.bgPrimary)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "난이도를 선택하세요",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(24.dp))

            listOf(
                DifficultyOption(DIFFICULTY_ALL, "전체", "모든 단어에서 10문항", Color(0xFF6366F1)),
                DifficultyOption(DIFFICULTY_ELEMENTARY, "초등", "초등 수준 단어만", Color(0xFF22C55E)),
                DifficultyOption(DIFFICULTY_MIDDLE, "중등", "중등 수준 단어만", Color(0xFFF59E0B)),
                DifficultyOption(DIFFICULTY_HIGH, "고등", "고등 수준 단어만", Color(0xFFEF4444))
            ).forEach { option ->
                DifficultyCard(
                    option = option,
                    colors = colors,
                    onClick = { onSelectDifficulty(option.key) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppButton(
                    text = "홈",
                    style = AppButtonStyle.SECONDARY,
                    onClick = onHome
                )
            }
        }
    }
}

@Composable
private fun DifficultyCard(
    option: DifficultyOption,
    colors: com.euysoo.engtest.ui.theme.AppColors,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colors.bgCard,
        borderColor = colors.borderDefault,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(option.tint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = option.tint,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = option.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
        }
    }
}
