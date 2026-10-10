package com.example.model

data class EditAdjustments(
    val filter: FilterType = FilterType.NONE,
    val filterIntensity: Float = 1.0f,
    val brightness: Float = 0f,    // -100 to 100
    val contrast: Float = 1.0f,    // 0.5 to 2.0
    val saturation: Float = 1.0f,  // 0.0 to 2.0
    val warmth: Float = 0f,        // -100 to 100
    val highlights: Float = 0f,    // -100 to 100
    val shadows: Float = 0f,       // -100 to 100
    val rotationDegrees: Int = 0,  // 0, 90, 180, 270
    val isFlippedHorizontal: Boolean = false,
    val cropRatio: AspectRatioMode? = null
)

data class PhotoItem(
    val uriString: String,
    val displayName: String,
    val dateAddedMillis: Long,
    val width: Int = 0,
    val height: Int = 0,
    val compositionScore: Int = 85,
    val sceneDetected: String = "General"
)
