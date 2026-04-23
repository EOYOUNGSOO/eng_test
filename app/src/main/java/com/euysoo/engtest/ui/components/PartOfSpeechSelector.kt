package com.euysoo.engtest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.euysoo.engtest.data.entity.PartOfSpeech
import com.euysoo.engtest.ui.theme.AppTheme

/**
 * 품사 멀티선택(칩) UI. 3열 그리드.
 * 선택: 보라 배경 + 흰 글자, 미선택: 카드 배경 + 테두리.
 */
@Composable
fun PartOfSpeechSelector(
    selectedLabels: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val all = PartOfSpeech.allLabels
    val selectedSet = selectedLabels.map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        all.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { label ->
                    val on = label in selectedSet
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .background(
                                    color = if (on) colors.purpleMain else colors.bgCard,
                                    shape = RoundedCornerShape(10.dp),
                                ).then(
                                    if (!on) {
                                        Modifier.border(0.5.dp, colors.borderDefault, RoundedCornerShape(10.dp))
                                    } else {
                                        Modifier
                                    },
                                ).clickable {
                                    val next =
                                        if (on) {
                                            selectedSet - label
                                        } else {
                                            selectedSet + label
                                        }
                                    onSelectionChange(next.sortedBy { lbl -> all.indexOf(lbl) })
                                }.padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            if (on) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier =
                                        Modifier
                                            .padding(end = 4.dp)
                                            .size(16.dp),
                                )
                            }
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (on) FontWeight.Medium else FontWeight.Normal,
                                color = if (on) Color.White else colors.textMuted,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )
                        }
                    }
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
