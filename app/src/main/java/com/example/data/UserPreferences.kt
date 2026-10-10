package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AspectRatioMode
import com.example.model.CameraSettings
import com.example.model.CompositionGridType
import com.example.model.FlashMode
import com.example.model.GridLineColor
import com.example.model.GridSettings
import com.example.model.PoseGuideType
import com.example.model.TimerMode

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_frame_camera_prefs", Context.MODE_PRIVATE)

    fun loadSettings(): CameraSettings {
        val gridTypeName = prefs.getString("grid_type", CompositionGridType.RULE_OF_THIRDS.name)
            ?: CompositionGridType.RULE_OF_THIRDS.name
        val gridType = try {
            CompositionGridType.valueOf(gridTypeName)
        } catch (e: Exception) {
            CompositionGridType.RULE_OF_THIRDS
        }

        val lineColorName = prefs.getString("grid_line_color", GridLineColor.GOLD.name)
            ?: GridLineColor.GOLD.name
        val lineColor = try {
            GridLineColor.valueOf(lineColorName)
        } catch (e: Exception) {
            GridLineColor.GOLD
        }

        val gridOpacity = prefs.getFloat("grid_opacity", 0.75f)
        val gridThickness = prefs.getFloat("grid_thickness", 1.5f)
        val gridVisible = prefs.getBoolean("grid_visible", true)

        val flashName = prefs.getString("flash_mode", FlashMode.AUTO.name) ?: FlashMode.AUTO.name
        val flashMode = try {
            FlashMode.valueOf(flashName)
        } catch (e: Exception) {
            FlashMode.AUTO
        }

        val timerSeconds = prefs.getInt("timer_mode", 0)
        val timerMode = when (timerSeconds) {
            3 -> TimerMode.SEC_3
            5 -> TimerMode.SEC_5
            10 -> TimerMode.SEC_10
            else -> TimerMode.OFF
        }

        val ratioName = prefs.getString("aspect_ratio", AspectRatioMode.RATIO_4_3.name)
            ?: AspectRatioMode.RATIO_4_3.name
        val aspectRatio = try {
            AspectRatioMode.valueOf(ratioName)
        } catch (e: Exception) {
            AspectRatioMode.RATIO_4_3
        }

        val aiEnabled = prefs.getBoolean("ai_advisor_enabled", true)
        val hapticsEnabled = prefs.getBoolean("haptics_enabled", true)

        return CameraSettings(
            flashMode = flashMode,
            timerMode = timerMode,
            isFrontCamera = false,
            aspectRatio = aspectRatio,
            gridType = gridType,
            gridSettings = GridSettings(
                isVisible = gridVisible,
                opacity = gridOpacity,
                thicknessDp = gridThickness,
                selectedColor = lineColor
            ),
            isAiAdvisorEnabled = aiEnabled,
            isPoseCoachActive = false,
            selectedPoseGuide = PoseGuideType.NONE,
            isHapticsEnabled = hapticsEnabled,
            isCloudGeminiAnalysisEnabled = true
        )
    }

    fun saveGridType(gridType: CompositionGridType) {
        prefs.edit().putString("grid_type", gridType.name).apply()
    }

    fun saveGridSettings(settings: GridSettings) {
        prefs.edit()
            .putBoolean("grid_visible", settings.isVisible)
            .putFloat("grid_opacity", settings.opacity)
            .putFloat("grid_thickness", settings.thicknessDp)
            .putString("grid_line_color", settings.selectedColor.name)
            .apply()
    }

    fun saveAspectRatio(ratio: AspectRatioMode) {
        prefs.edit().putString("aspect_ratio", ratio.name).apply()
    }

    fun saveFlashMode(flash: FlashMode) {
        prefs.edit().putString("flash_mode", flash.name).apply()
    }

    fun saveTimerMode(timer: TimerMode) {
        prefs.edit().putInt("timer_mode", timer.seconds).apply()
    }

    fun saveAiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("ai_advisor_enabled", enabled).apply()
    }

    fun saveHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
    }

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean("onboarding_complete", false)
    }

    fun setOnboardingComplete() {
        prefs.edit().putBoolean("onboarding_complete", true).apply()
    }
}
