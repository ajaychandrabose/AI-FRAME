package com.example.model

import androidx.compose.ui.graphics.Color

enum class FlashMode(val label: String) {
    OFF("Off"),
    AUTO("Auto"),
    ON("On"),
    TORCH("Torch")
}

enum class TimerMode(val seconds: Int, val label: String) {
    OFF(0, "Off"),
    SEC_3(3, "3s"),
    SEC_5(5, "5s"),
    SEC_10(10, "10s")
}

enum class GridLineColor(val label: String, val color: Color) {
    WHITE("Crisp White", Color(0xFFFFFFFF)),
    GOLD("Electric Gold", Color(0xFFFFB800)),
    CYAN("Pro Cyan", Color(0xFF00E5FF)),
    EMERALD("Neon Green", Color(0xFF10B981)),
    CORAL("Warm Coral", Color(0xFFFF5252))
}

data class GridSettings(
    val isVisible: Boolean = true,
    val opacity: Float = 0.75f,
    val thicknessDp: Float = 1.5f,
    val selectedColor: GridLineColor = GridLineColor.GOLD
)

data class CameraSettings(
    val flashMode: FlashMode = FlashMode.AUTO,
    val timerMode: TimerMode = TimerMode.OFF,
    val isFrontCamera: Boolean = false,
    val aspectRatio: AspectRatioMode = AspectRatioMode.RATIO_4_3,
    val gridType: CompositionGridType = CompositionGridType.RULE_OF_THIRDS,
    val gridSettings: GridSettings = GridSettings(),
    val isAiAdvisorEnabled: Boolean = true,
    val isPoseCoachActive: Boolean = false,
    val selectedPoseGuide: PoseGuideType = PoseGuideType.NONE,
    val isHapticsEnabled: Boolean = true,
    val isBurstModeActive: Boolean = false,
    val isCloudGeminiAnalysisEnabled: Boolean = true
)
