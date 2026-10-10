package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CameraColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = CameraObsidian,
    primaryContainer = CameraSurfaceCard,
    onPrimaryContainer = GoldAccent,
    secondary = CyanAccent,
    onSecondary = CameraObsidian,
    secondaryContainer = CameraSurfaceDark,
    onSecondaryContainer = CyanAccent,
    tertiary = CoralShutter,
    onTertiary = TextPrimary,
    background = CameraObsidian,
    onBackground = TextPrimary,
    surface = CameraSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CameraSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = CameraGlassBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Pro camera default is dark
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = CameraColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = CameraObsidian.toArgb()
                it.navigationBarColor = CameraObsidian.toArgb()
                val controller = WindowCompat.getInsetsController(it, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
