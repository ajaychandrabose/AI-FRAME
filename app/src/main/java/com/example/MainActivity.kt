package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.UserPreferences
import com.example.model.PhotoItem
import com.example.ui.camera.CameraScreen
import com.example.ui.camera.CameraViewModel
import com.example.ui.editor.PhotoEditorScreen
import com.example.ui.gallery.GalleryScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.CameraObsidian
import com.example.ui.theme.MyApplicationTheme

sealed class AppScreen {
    object Onboarding : AppScreen()
    object Camera : AppScreen()
    data class Editor(val photo: PhotoItem) : AppScreen()
    object Gallery : AppScreen()
    object Settings : AppScreen()
}

class MainActivity : ComponentActivity() {

    private val cameraViewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userPrefs = UserPreferences(this)
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val initialScreen = if (userPrefs.hasCompletedOnboarding() && hasCameraPermission) {
            AppScreen.Camera
        } else {
            AppScreen.Onboarding
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CameraObsidian
                ) {
                    var currentScreen by remember { mutableStateOf<AppScreen>(initialScreen) }

                    when (val screen = currentScreen) {
                        is AppScreen.Onboarding -> {
                            OnboardingScreen(
                                onFinishOnboarding = {
                                    userPrefs.setOnboardingComplete()
                                    currentScreen = AppScreen.Camera
                                }
                            )
                        }

                        is AppScreen.Camera -> {
                            CameraScreen(
                                viewModel = cameraViewModel,
                                onNavigateToEditor = { photoItem ->
                                    currentScreen = AppScreen.Editor(photoItem)
                                },
                                onNavigateToGallery = {
                                    currentScreen = AppScreen.Gallery
                                },
                                onNavigateToSettings = {
                                    currentScreen = AppScreen.Settings
                                }
                            )
                        }

                        is AppScreen.Editor -> {
                            PhotoEditorScreen(
                                photoItem = screen.photo,
                                onNavigateBack = {
                                    currentScreen = AppScreen.Camera
                                },
                                onNavigateToGallery = {
                                    currentScreen = AppScreen.Gallery
                                }
                            )
                        }

                        is AppScreen.Gallery -> {
                            GalleryScreen(
                                onNavigateBack = {
                                    currentScreen = AppScreen.Camera
                                },
                                onOpenPhotoEditor = { photoItem ->
                                    currentScreen = AppScreen.Editor(photoItem)
                                }
                            )
                        }

                        is AppScreen.Settings -> {
                            SettingsScreen(
                                viewModel = cameraViewModel,
                                onNavigateBack = {
                                    currentScreen = AppScreen.Camera
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
