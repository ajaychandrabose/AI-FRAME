package com.example.ui.camera

import android.graphics.RectF
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.model.CompositionGridType
import com.example.model.DetectedFaceInfo
import com.example.model.FilterRecommendation
import com.example.model.FilterType
import com.example.model.FrameAnalysisResult
import com.example.model.SceneType
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.math.abs

class FrameAnalyzer(
    private val onAnalysisComplete: (FrameAnalysisResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val detectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setMinFaceSize(0.12f)
        .build()

    private val faceDetector = FaceDetection.getClient(detectorOptions)

    private var lastAnalyzedTimestamp = 0L
    private val throttleIntervalMs = 280L

    var currentSensorRollDegrees: Float = 0f

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp < throttleIntervalMs) {
            imageProxy.close()
            return
        }
        lastAnalyzedTimestamp = currentTimestamp

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val width = imageProxy.width.toFloat()
        val height = imageProxy.height.toFloat()

        // 1. Analyze frame luminance & dynamic contrast from Y plane
        var sumLuma = 0L
        var minLuma = 255
        var maxLuma = 0
        var horizGrad = 0L
        var vertGrad = 0L

        val yBuffer = mediaImage.planes[0].buffer
        val yRowStride = mediaImage.planes[0].rowStride
        val yPixelStride = mediaImage.planes[0].pixelStride
        val step = 16 // Downsample for real-time speed

        val wInt = imageProxy.width
        val hInt = imageProxy.height

        var sampleCount = 0
        for (y in 0 until hInt - step step step) {
            val rowOffset = y * yRowStride
            for (x in 0 until wInt - step step step) {
                val index = rowOffset + (x * yPixelStride)
                if (index < yBuffer.limit()) {
                    val pixel = yBuffer.get(index).toInt() and 0xFF
                    sumLuma += pixel
                    if (pixel < minLuma) minLuma = pixel
                    if (pixel > maxLuma) maxLuma = pixel

                    // Compute gradient for edge orientation analysis
                    val rightIndex = rowOffset + ((x + step) * yPixelStride)
                    val downIndex = (y + step) * yRowStride + (x * yPixelStride)
                    if (rightIndex < yBuffer.limit()) {
                        val pRight = yBuffer.get(rightIndex).toInt() and 0xFF
                        horizGrad += abs(pixel - pRight)
                    }
                    if (downIndex < yBuffer.limit()) {
                        val pDown = yBuffer.get(downIndex).toInt() and 0xFF
                        vertGrad += abs(pixel - pDown)
                    }
                    sampleCount++
                }
            }
        }

        val avgLuminance = if (sampleCount > 0) (sumLuma / sampleCount.toFloat()) else 128f
        val dynamicRange = (maxLuma - minLuma).toFloat()
        val contrastScore = (dynamicRange / 2.55f).coerceIn(0f, 100f)

        // 2. ML Kit Face Detection
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                val detectedFacesList = mutableListOf<DetectedFaceInfo>()

                // Effective dimensions depending on rotation
                val isRotated = rotationDegrees == 90 || rotationDegrees == 270
                val frameW = if (isRotated) height else width
                val frameH = if (isRotated) width else height

                for (face in faces) {
                    val box = face.boundingBox
                    val normBox = RectF(
                        (box.left / frameW).coerceIn(0f, 1f),
                        (box.top / frameH).coerceIn(0f, 1f),
                        (box.right / frameW).coerceIn(0f, 1f),
                        (box.bottom / frameH).coerceIn(0f, 1f)
                    )
                    detectedFacesList.add(
                        DetectedFaceInfo(
                            boundingBox = normBox,
                            headEulerAngleX = face.headEulerAngleX,
                            headEulerAngleY = face.headEulerAngleY,
                            headEulerAngleZ = face.headEulerAngleZ,
                            smilingProbability = face.smilingProbability,
                            leftEyeOpenProbability = face.leftEyeOpenProbability,
                            rightEyeOpenProbability = face.rightEyeOpenProbability
                        )
                    )
                }

                // 3. Scene Detection Heuristics
                val sceneType: SceneType = when {
                    detectedFacesList.size >= 2 -> SceneType.GROUP
                    detectedFacesList.size == 1 -> SceneType.PORTRAIT
                    avgLuminance < 58f -> SceneType.LOW_LIGHT
                    contrastScore > 82f && avgLuminance > 160f -> SceneType.BACKLIT
                    horizGrad > (vertGrad * 1.35f) -> SceneType.LANDSCAPE
                    vertGrad > (horizGrad * 1.35f) -> SceneType.ARCHITECTURE
                    else -> SceneType.GENERAL
                }

                // 4. Composition score computation
                val isHorizonLevel = abs(currentSensorRollDegrees) <= 1.2f
                val levelScore = (100 - (abs(currentSensorRollDegrees) * 4f)).toInt().coerceIn(40, 100)

                var thirdsScore = 80
                var framingScore = 82
                var actionableTip = sceneType.defaultAdvice
                var poseAdvice: String? = null

                if (detectedFacesList.isNotEmpty()) {
                    val primaryFace = detectedFacesList.first()
                    val faceCenter = (primaryFace.boundingBox.left + primaryFace.boundingBox.right) / 2f
                    val faceTop = primaryFace.boundingBox.top

                    // Check alignment with 1/3 or 2/3 vertical lines
                    val distToThird = minOf(abs(faceCenter - 0.333f), abs(faceCenter - 0.667f))
                    thirdsScore = (100 - (distToThird * 120f)).toInt().coerceIn(55, 98)

                    // Headroom check
                    framingScore = when {
                        faceTop < 0.08f -> {
                            actionableTip = "Tilt camera up slightly to give the subject more headroom."
                            65
                        }
                        faceTop > 0.30f -> {
                            actionableTip = "Move closer or raise lens so eyes sit along the upper third."
                            70
                        }
                        else -> {
                            actionableTip = "Excellent headroom! Subject is anchored naturally."
                            92
                        }
                    }

                    // Pose coaching advice
                    poseAdvice = when {
                        abs(primaryFace.headEulerAngleZ) > 8f ->
                            "Relax shoulders and keep chin level with the horizon."
                        abs(primaryFace.headEulerAngleY) > 20f ->
                            "Slight 3/4 turn adds appealing depth to the facial contour."
                        primaryFace.smilingProbability?.let { it > 0.6f } == true ->
                            "Great natural expression! Keep holding that pose."
                        else ->
                            "Direct eye contact with the camera creates strong emotional presence."
                    }
                } else {
                    if (!isHorizonLevel) {
                        actionableTip = "Rotate phone by ${String.format("%.1f", abs(currentSensorRollDegrees))}° to level the horizon."
                    }
                }

                // Lighting score
                val lightingScore = when {
                    avgLuminance in 80f..180f -> 92
                    avgLuminance < 50f -> 60
                    avgLuminance > 220f -> 65
                    else -> 80
                }

                val overallScore = ((thirdsScore * 0.35f) + (levelScore * 0.25f) + (lightingScore * 0.20f) + (framingScore * 0.20f)).toInt().coerceIn(50, 99)

                // Recommended Grid
                val recGrid = when (sceneType) {
                    SceneType.PORTRAIT -> CompositionGridType.PORTRAIT_GUIDE
                    SceneType.GROUP -> CompositionGridType.SYMMETRY
                    SceneType.LANDSCAPE -> CompositionGridType.LANDSCAPE_GUIDE
                    SceneType.ARCHITECTURE -> CompositionGridType.ARCHITECTURE_HORIZON
                    SceneType.FOOD -> CompositionGridType.CENTER
                    SceneType.LOW_LIGHT -> CompositionGridType.MINIMAL
                    SceneType.BACKLIT -> CompositionGridType.RULE_OF_THIRDS
                    SceneType.STREET -> CompositionGridType.LEADING_LINES
                    SceneType.GENERAL -> CompositionGridType.RULE_OF_THIRDS
                }

                // Recommended Filters with reasons
                val recFilters = when (sceneType) {
                    SceneType.PORTRAIT -> listOf(
                        FilterRecommendation(FilterType.PORTRAIT, "Smooths harsh contrast while preserving radiant skin tones."),
                        FilterRecommendation(FilterType.SOFT_SKIN, "Softens highlights and gives a flattering peachy glow."),
                        FilterRecommendation(FilterType.NATURAL, "Balanced neutral baseline preserving authentic colors.")
                    )
                    SceneType.GROUP -> listOf(
                        FilterRecommendation(FilterType.NATURAL, "Keeps varied skin tones natural and consistent."),
                        FilterRecommendation(FilterType.PORTRAIT, "Adds gentle warmth across the group."),
                        FilterRecommendation(FilterType.VIBRANT, "Energizes lively group gathering colors.")
                    )
                    SceneType.LANDSCAPE -> listOf(
                        FilterRecommendation(FilterType.LANDSCAPE, "Punches up sky blues and lush greenery with crisp clarity."),
                        FilterRecommendation(FilterType.WARM_SUNSET, "Enriches golden hour amber bounce light."),
                        FilterRecommendation(FilterType.COOL_TONES, "Crisp modern contrast ideal for water and architecture.")
                    )
                    SceneType.ARCHITECTURE, SceneType.STREET -> listOf(
                        FilterRecommendation(FilterType.CINEMATIC, "Teal and amber color separation gives an urban cinematic vibe."),
                        FilterRecommendation(FilterType.BLACK_WHITE, "Strips distracting colors to emphasize geometry and lines."),
                        FilterRecommendation(FilterType.MATTE, "Editorial faded blacks with a sophisticated aesthetic.")
                    )
                    SceneType.LOW_LIGHT -> listOf(
                        FilterRecommendation(FilterType.NIGHT, "Extracts shadow texture without noise exaggeration."),
                        FilterRecommendation(FilterType.CINEMATIC, "Gives night city lights a neon cinematic contrast."),
                        FilterRecommendation(FilterType.BLACK_WHITE, "Timeless high-contrast night monochrome.")
                    )
                    SceneType.FOOD -> listOf(
                        FilterRecommendation(FilterType.FOOD, "Warm saturation boost that makes culinary colors pop."),
                        FilterRecommendation(FilterType.WARM_SUNSET, "Golden illumination highlighting delicious highlights."),
                        FilterRecommendation(FilterType.NATURAL, "Crisp appetizing clarity.")
                    )
                    else -> listOf(
                        FilterRecommendation(FilterType.NATURAL, "Neutral calibrated dynamic balance."),
                        FilterRecommendation(FilterType.CINEMATIC, "Blockbuster depth and moody shadows."),
                        FilterRecommendation(FilterType.PRO_STUDIO, "Studio baseline preserving all highlight details.")
                    )
                }

                val result = FrameAnalysisResult(
                    sceneType = sceneType,
                    sceneConfidence = 0.90f,
                    detectedFaces = detectedFacesList,
                    averageLuminance = avgLuminance,
                    contrastScore = contrastScore,
                    horizonRollDegrees = currentSensorRollDegrees,
                    isHorizonLevel = isHorizonLevel,
                    compositionScore = overallScore,
                    scoreRuleOfThirds = thirdsScore,
                    scoreLighting = lightingScore,
                    scoreLevel = levelScore,
                    scoreFraming = framingScore,
                    actionableAdvice = actionableTip,
                    poseGuidanceAdvice = poseAdvice,
                    recommendedGrid = recGrid,
                    recommendedFilters = recFilters
                )

                onAnalysisComplete(result)
                imageProxy.close()
            }
            .addOnFailureListener {
                imageProxy.close()
            }
    }
}
