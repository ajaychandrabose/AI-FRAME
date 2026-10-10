package com.example.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import com.example.data.FilterPresets
import com.example.model.AspectRatioMode
import com.example.model.EditAdjustments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageProcessing {

    /**
     * Applies full edit adjustments (ColorMatrix filter, brightness, contrast,
     * saturation, warmth, rotation, flipping, and aspect ratio crop) to a Bitmap.
     */
    suspend fun applyAdjustments(
        source: Bitmap,
        adjustments: EditAdjustments
    ): Bitmap = withContext(Dispatchers.Default) {
        var currentBitmap = source

        // 1. Rotation and Flip
        if (adjustments.rotationDegrees != 0 || adjustments.isFlippedHorizontal) {
            val matrix = Matrix().apply {
                if (adjustments.isFlippedHorizontal) {
                    postScale(-1f, 1f, currentBitmap.width / 2f, currentBitmap.height / 2f)
                }
                if (adjustments.rotationDegrees != 0) {
                    postRotate(adjustments.rotationDegrees.toFloat())
                }
            }
            val transformed = Bitmap.createBitmap(
                currentBitmap,
                0,
                0,
                currentBitmap.width,
                currentBitmap.height,
                matrix,
                true
            )
            if (currentBitmap != source) {
                currentBitmap.recycle()
            }
            currentBitmap = transformed
        }

        // 2. Aspect ratio crop
        adjustments.cropRatio?.let { ratioMode ->
            val targetRatio = ratioMode.ratio
            val currentRatio = currentBitmap.width.toFloat() / currentBitmap.height.toFloat()

            val cropWidth: Int
            val cropHeight: Int
            val cropX: Int
            val cropY: Int

            if (currentRatio > targetRatio) {
                // Too wide, crop sides
                cropHeight = currentBitmap.height
                cropWidth = (cropHeight * targetRatio).toInt().coerceAtMost(currentBitmap.width)
                cropX = (currentBitmap.width - cropWidth) / 2
                cropY = 0
            } else {
                // Too tall, crop top and bottom
                cropWidth = currentBitmap.width
                cropHeight = (cropWidth / targetRatio).toInt().coerceAtMost(currentBitmap.height)
                cropX = 0
                cropY = (currentBitmap.height - cropHeight) / 2
            }

            if (cropWidth > 0 && cropHeight > 0 && (cropWidth != currentBitmap.width || cropHeight != currentBitmap.height)) {
                val cropped = Bitmap.createBitmap(currentBitmap, cropX, cropY, cropWidth, cropHeight)
                if (currentBitmap != source) {
                    currentBitmap.recycle()
                }
                currentBitmap = cropped
            }
        }

        // 3. Color Grading & Adjustments via Canvas and ColorMatrixColorFilter
        val cm = FilterPresets.buildCombinedColorMatrix(
            filter = adjustments.filter,
            filterIntensity = adjustments.filterIntensity,
            brightness = adjustments.brightness,
            contrast = adjustments.contrast,
            saturation = adjustments.saturation,
            warmth = adjustments.warmth
        )

        val output = Bitmap.createBitmap(
            currentBitmap.width,
            currentBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }
        canvas.drawBitmap(currentBitmap, 0f, 0f, paint)

        if (currentBitmap != source) {
            currentBitmap.recycle()
        }

        return@withContext output
    }
}
