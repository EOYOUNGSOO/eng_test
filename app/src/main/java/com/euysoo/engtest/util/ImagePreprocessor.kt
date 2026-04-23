package com.euysoo.engtest.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlin.math.max

/**
 * ML Kit 오프라인 OCR 인식률 향상을 위한 이미지 전처리 (Grayscale → 대비 → 이진화 → 스케일).
 */
object ImagePreprocessor {
    /**
     * ML Kit 호출 직전 권장 전처리: 그레이스케일 → 대비 강화(1.4) → 긴 변 1500px 미만이면 1.5배 업스케일.
     * 표 OCR에서 얇은 격자·경계 근처 글자 인식률 개선에 사용한다.
     */
    fun preprocessBitmap(src: Bitmap): Bitmap {
        val gray = toGrayscale(src)
        val enhanced = enhanceContrast(gray, contrast = 1.4f, brightness = -12f)
        if (gray !== enhanced) gray.recycle()
        val longEdge = max(enhanced.width, enhanced.height)
        val scaled =
            if (longEdge < 1500) {
                val w = (enhanced.width * 1.5f).toInt().coerceAtMost(2400)
                val h = (enhanced.height * 1.5f).toInt().coerceAtMost(3200)
                val up = Bitmap.createScaledBitmap(enhanced, w, h, true)
                if (up !== enhanced) enhanced.recycle()
                val out = scaleToWidth(up, 1600)
                if (out !== up) up.recycle()
                out
            } else {
                scaleToWidth(enhanced, 1600)
            }
        return scaled
    }

    fun prepareForMlKitAdaptive(
        original: Bitmap,
        targetWidth: Int = 1280,
    ): Bitmap {
        val gray = toGrayscale(original)
        val enhanced = enhanceContrast(gray, contrast = 1.35f, brightness = -10f)
        if (gray !== enhanced) gray.recycle()
        val scaled = scaleToWidth(enhanced, targetWidth)
        if (scaled !== enhanced) enhanced.recycle()
        return scaled
    }

    fun prepareForMlKit(
        original: Bitmap,
        targetWidth: Int = 1280,
    ): Bitmap {
        val gray = toGrayscale(original)
        val enhanced = enhanceContrast(gray, contrast = 1.5f, brightness = -20f)
        if (gray !== enhanced) gray.recycle()
        val binary = binarize(enhanced)
        if (binary !== enhanced) enhanced.recycle()
        val scaled = scaleToWidth(binary, targetWidth)
        if (scaled !== binary) binary.recycle()
        return scaled
    }

    fun toGrayscale(src: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }

    fun enhanceContrast(
        src: Bitmap,
        contrast: Float = 1.5f,
        brightness: Float = -20f,
    ): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val translate = (1f - contrast) * 128f + brightness
        val cm =
            ColorMatrix(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, translate,
                    0f, contrast, 0f, 0f, translate,
                    0f, 0f, contrast, 0f, translate,
                    0f, 0f, 0f, 1f, 0f,
                ),
            )
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }

    fun binarize(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        var totalBrightness = 0L
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            totalBrightness += (r + g + b) / 3
        }
        val avgBrightness = (totalBrightness / pixels.size).toInt().coerceIn(1, 254)
        val isDarkBackground = avgBrightness < 128

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val gray = (r + g + b) / 3

            pixels[i] =
                if (isDarkBackground) {
                    if (gray > avgBrightness) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
                } else {
                    if (gray < avgBrightness) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                }
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    fun scaleToWidth(
        src: Bitmap,
        targetWidth: Int,
    ): Bitmap {
        if (src.width >= targetWidth) return src
        val scale = targetWidth.toFloat() / src.width
        val targetHeight = (src.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, targetWidth, targetHeight, true)
    }

    fun isValidForOcr(bitmap: Bitmap): Boolean =
        bitmap.width >= 400 &&
            bitmap.height >= 400 &&
            bitmap.width <= 8000 &&
            bitmap.height <= 8000
}
