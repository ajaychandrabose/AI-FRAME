package com.example.data

import android.graphics.ColorMatrix
import com.example.model.FilterType
import kotlin.math.cos
import kotlin.math.sin

object FilterPresets {

    fun getColorMatrix(filter: FilterType, intensity: Float = 1.0f): ColorMatrix {
        val base = getBaseMatrix(filter)
        if (intensity >= 0.999f) {
            return ColorMatrix(base)
        }
        val identity = FloatArray(20) { if (it % 6 == 0) 1f else 0f }
        val blended = FloatArray(20)
        for (i in 0 until 20) {
            blended[i] = identity[i] + (base[i] - identity[i]) * intensity.coerceIn(0f, 1f)
        }
        return ColorMatrix(blended)
    }

    private fun getBaseMatrix(filter: FilterType): FloatArray {
        return when (filter) {
            FilterType.NONE -> floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )

            FilterType.NATURAL -> floatArrayOf(
                1.05f, 0.00f, 0.00f, 0f, 5f,
                0.00f, 1.04f, 0.00f, 0f, 5f,
                0.00f, 0.00f, 0.98f, 0f, 0f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterType.CINEMATIC -> floatArrayOf(
                1.15f, 0.02f, -0.05f, 0f, 10f,
                -0.02f, 1.05f, 0.05f, 0f, -5f,
                -0.10f, 0.08f, 1.25f, 0f, 15f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterType.PORTRAIT -> floatArrayOf(
                1.12f, 0.05f, 0.00f, 0f, 12f,
                0.02f, 1.06f, 0.02f, 0f, 8f,
                -0.02f, 0.00f, 0.95f, 0f, -2f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterType.WARM_SUNSET -> floatArrayOf(
                1.25f, 0.10f, 0.00f, 0f, 25f,
                0.05f, 1.10f, 0.00f, 0f, 12f,
                -0.08f, -0.05f, 0.85f, 0f, -15f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterType.COOL_TONES -> floatArrayOf(
                0.90f, 0.00f, 0.00f, 0f, -10f,
                0.00f, 1.02f, 0.02f, 0f, 0f,
                0.05f, 0.05f, 1.25f, 0f, 20f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterType.FILM_VINTAGE -> floatArrayOf(
                1.08f, 0.05f, 0.00f, 0f, 18f,
                0.00f, 1.00f, 0.04f, 0f, 12f,
                0.02f, 0.00f, 0.88f, 0f, 15f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterType.BLACK_WHITE -> floatArrayOf(
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0.000f, 0.000f, 0.000f, 1f, 0f
            )

            FilterType.LANDSCAPE -> floatArrayOf(
                1.10f, -0.05f, 0.00f, 0f, 5f,
                -0.02f, 1.18f, 0.00f, 0f, 10f,
                0.00f, -0.05f, 1.25f, 0f, 18f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterType.FOOD -> floatArrayOf(
                1.18f, 0.05f, 0.00f, 0f, 18f,
                0.02f, 1.12f, 0.00f, 0f, 10f,
                -0.05f, 0.00f, 0.92f, 0f, -5f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterType.NIGHT -> floatArrayOf(
                1.15f, 0.00f, 0.00f, 0f, 25f,
                0.00f, 1.15f, 0.00f, 0f, 25f,
                0.05f, 0.05f, 1.20f, 0f, 35f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterType.SOFT_SKIN -> floatArrayOf(
                1.08f, 0.06f, 0.00f, 0f, 15f,
                0.04f, 1.05f, 0.02f, 0f, 10f,
                -0.02f, 0.00f, 0.96f, 0f, 2f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterType.VIBRANT -> floatArrayOf(
                1.22f, -0.08f, -0.05f, 0f, 8f,
                -0.06f, 1.24f, -0.06f, 0f, 8f,
                -0.05f, -0.08f, 1.25f, 0f, 12f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterType.MATTE -> floatArrayOf(
                0.95f, 0.00f, 0.00f, 0f, 22f,
                0.00f, 0.95f, 0.00f, 0f, 22f,
                0.00f, 0.00f, 0.95f, 0f, 22f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterType.PRO_STUDIO -> floatArrayOf(
                1.04f, 0.00f, 0.00f, 0f, 2f,
                0.00f, 1.04f, 0.00f, 0f, 2f,
                0.00f, 0.00f, 1.04f, 0f, 2f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )
        }
    }

    /**
     * Builds a comprehensive ColorMatrix combining filter preset and manual slider adjustments
     * (brightness: -100..100, contrast: 0.5..2.0, saturation: 0..2, warmth: -100..100)
     */
    fun buildCombinedColorMatrix(
        filter: FilterType,
        filterIntensity: Float,
        brightness: Float, // -100 to 100
        contrast: Float,   // 0.5 to 2.0 (1.0 default)
        saturation: Float, // 0.0 to 2.0 (1.0 default)
        warmth: Float      // -100 to 100
    ): ColorMatrix {
        val result = ColorMatrix(getColorMatrix(filter, filterIntensity))

        // Saturation adjustment
        if (saturation != 1.0f) {
            val satMatrix = ColorMatrix()
            satMatrix.setSaturation(saturation.coerceIn(0f, 3f))
            result.postConcat(satMatrix)
        }

        // Contrast adjustment
        if (contrast != 1.0f) {
            val scale = contrast.coerceIn(0.2f, 3.0f)
            val translate = (-0.5f * scale + 0.5f) * 255f
            val contrastMatrix = ColorMatrix(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            result.postConcat(contrastMatrix)
        }

        // Brightness & Warmth translation
        val b = brightness.coerceIn(-100f, 100f)
        val w = warmth.coerceIn(-100f, 100f)
        if (b != 0f || w != 0f) {
            val adjMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, b + (w * 0.5f),
                    0f, 1f, 0f, 0f, b + (w * 0.2f),
                    0f, 0f, 1f, 0f, b - (w * 0.5f),
                    0f, 0f, 0f, 1f, 0f
                )
            )
            result.postConcat(adjMatrix)
        }

        return result
    }
}
