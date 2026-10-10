package com.example.model

import android.graphics.RectF

data class DetectedFaceInfo(
    val boundingBox: RectF, // Normalized 0..1 in preview coordinate space
    val headEulerAngleX: Float, // Pitch: head tilted up (+) or down (-)
    val headEulerAngleY: Float, // Yaw: head turned right (+) or left (-)
    val headEulerAngleZ: Float, // Roll: head tilted sideways
    val smilingProbability: Float?,
    val leftEyeOpenProbability: Float?,
    val rightEyeOpenProbability: Float?
)

data class FilterRecommendation(
    val filter: FilterType,
    val reason: String
)

data class FrameAnalysisResult(
    val sceneType: SceneType = SceneType.GENERAL,
    val sceneConfidence: Float = 0.85f,
    val detectedFaces: List<DetectedFaceInfo> = emptyList(),
    val averageLuminance: Float = 128f, // 0..255
    val contrastScore: Float = 60f,     // 0..100
    val horizonRollDegrees: Float = 0f, // tilt angle in degrees
    val isHorizonLevel: Boolean = true,
    val compositionScore: Int = 85,     // 0..100
    val scoreRuleOfThirds: Int = 85,
    val scoreLighting: Int = 80,
    val scoreLevel: Int = 90,
    val scoreFraming: Int = 85,
    val actionableAdvice: String = "Place your primary subject near the golden grid intersection.",
    val poseGuidanceAdvice: String? = null,
    val recommendedGrid: CompositionGridType = CompositionGridType.RULE_OF_THIRDS,
    val recommendedFilters: List<FilterRecommendation> = listOf(
        FilterRecommendation(FilterType.NATURAL, "Balanced natural luminance suitable for current ambient lighting."),
        FilterRecommendation(FilterType.CINEMATIC, "Rich shadow depth and teal/amber separation."),
        FilterRecommendation(FilterType.PRO_STUDIO, "Clean tonal reproduction preserving highlight detail.")
    )
) {
    companion object {
        fun default(): FrameAnalysisResult = FrameAnalysisResult()
    }
}
