package com.euysoo.engtest.ui.ocr

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.euysoo.engtest.ui.components.AppTopBar
import com.euysoo.engtest.ui.theme.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

private fun loadBitmapFromUri(
    contentResolver: android.content.ContentResolver,
    uri: Uri,
): Bitmap? =
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }.getOrNull()

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OcrGuideScreen(
    onBack: () -> Unit,
    onImageReady: (Bitmap) -> Unit,
) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicturePreview(),
        ) { bitmap -> bitmap?.let { onImageReady(it) } }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia(),
        ) { uri: Uri? ->
            uri?.let {
                loadBitmapFromUri(context.contentResolver, it)?.let(onImageReady)
            }
        }

    Scaffold(
        containerColor = colors.bgPrimary,
        topBar = { AppTopBar(title = "이미지로 단어 추가", onBackClick = onBack) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "📋 단어 목록 작성 방법",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(colors.bgCard, RoundedCornerShape(12.dp))
                        .border(0.5.dp, colors.borderDefault, RoundedCornerShape(12.dp))
                        .padding(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "단어 인식률을 높이기 위한 방법",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                    )
                    Text(
                        text =
                            "가. 흰색 바탕에 검정 글씨로 인쇄하거나, 엑셀 등에서 「단어 / 품사 / 뜻」 세 칸으로 정리한 목록을 " +
                                "화면에 꽉 차게 보이도록 한 뒤 스크린샷(화면 캡처)으로 저장해 올리면 인식률이 가장 안정적입니다.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = colors.textMuted,
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(colors.bgCard, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.purpleMain.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "권장 형식",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.purpleMain,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val examples =
                        listOf(
                            "단어        품사    뜻",
                            "──────────────────────",
                            "appear      v.      나타나다",
                            "achieve     v.      달성하다",
                            "believe     v.      믿다",
                            "beautiful   adj.    아름다운",
                            "knowledge   n.      지식",
                        )
                    examples.forEach { line ->
                        Text(
                            text = line,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color =
                                when {
                                    line.startsWith("─") -> colors.textMuted
                                    line.startsWith("단어") -> colors.purpleMain
                                    else -> colors.textPrimary
                                },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.purpleMain),
                ) {
                    Icon(Icons.Filled.Photo, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("갤러리 선택")
                }
                Button(
                    onClick = {
                        if (cameraPermission.status.isGranted) {
                            cameraLauncher.launch(null)
                        } else {
                            cameraPermission.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.purpleMain),
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("카메라 촬영")
                }
            }

            Text(
                text = "단어·품사·뜻 사이에 공백 2칸 이상, 또는 -  :  – 기호를 사용하세요.",
                fontSize = 13.sp,
                color = colors.textMuted,
            )

            GuideSection(
                icon = Icons.Filled.CheckCircle,
                iconTint = Color(0xFF2E7D32),
                title = "✅ 잘 인식되는 경우",
                items =
                    listOf(
                        "단어 / 품사 / 뜻 순서로 작성",
                        "항목 간 구분자 사용 (공백 2칸, -, :)",
                        "밝고 그림자 없는 환경에서 촬영",
                        "줄이 반듯하게 보이도록 촬영",
                        "인쇄체 또는 또렷한 필기체",
                    ),
                bgColor = colors.bgCard,
                borderColor = colors.borderDefault,
                titleColor = colors.textPrimary,
                itemColor = colors.textMuted,
            )

            GuideSection(
                icon = Icons.Filled.Warning,
                iconTint = Color(0xFFE65100),
                title = "⚠️ 인식이 어려운 경우",
                items =
                    listOf(
                        "흘림체 필기 또는 너무 작은 글씨",
                        "어두운 환경, 흔들린 사진",
                        "단어와 뜻 사이 구분자 없음",
                        "줄 간격이 너무 좁은 경우",
                        "배경이 복잡하거나 색상이 비슷한 경우",
                    ),
                bgColor = colors.bgCard,
                borderColor = colors.borderDefault,
                titleColor = colors.textPrimary,
                itemColor = colors.textMuted,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GuideSection(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    items: List<String>,
    bgColor: Color,
    borderColor: Color,
    titleColor: Color,
    itemColor: Color,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(bgColor, RoundedCornerShape(12.dp))
                .border(0.5.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
            }
            items.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("•", fontSize = 13.sp, color = itemColor)
                    Text(item, fontSize = 13.sp, color = itemColor)
                }
            }
        }
    }
}
