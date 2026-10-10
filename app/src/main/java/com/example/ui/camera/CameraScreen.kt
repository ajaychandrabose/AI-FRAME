package com.example.ui.camera

import androidx.camera.core.ImageCapture
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AspectRatioMode
import com.example.model.FilterType
import com.example.model.PhotoItem
import com.example.ui.components.AiAdvisorBanner
import com.example.ui.components.AiScoreBottomSheet
import com.example.ui.components.FilterCarousel
import com.example.ui.components.GridSelectorSheet
import com.example.ui.components.PoseCoachSheet
import com.example.ui.components.QuickControlsBar
import com.example.ui.theme.CameraGlass
import com.example.ui.theme.CameraObsidian
import com.example.ui.theme.CameraSurfaceCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigateToEditor: (PhotoItem) -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val filterIntensity by viewModel.filterIntensity.collectAsState()
    val isComparingOriginal by viewModel.isComparingOriginal.collectAsState()
    val isCapturing by viewModel.isCapturing.collectAsState()
    val shutterFlash by viewModel.shutterFlash.collectAsState()
    val timerCountdown by viewModel.timerCountdown.collectAsState()
    val cloudCritique by viewModel.cloudCritique.collectAsState()
    val isCloudLoading by viewModel.isCloudLoading.collectAsState()
    val recentPhotos by viewModel.recentPhotos.collectAsState()

    var imageCaptureInstance by remember { mutableStateOf<ImageCapture?>(null) }
    var tapFocusPoint by remember { mutableStateOf<Offset?>(null) }
    var isFilterDrawerOpen by remember { mutableStateOf(false) }

    var showScoreSheet by remember { mutableStateOf(false) }
    var showGridSheet by remember { mutableStateOf(false) }
    var showPoseSheet by remember { mutableStateOf(false) }

    val scoreSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val gridSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val poseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Clear focus reticle after 3 seconds
    LaunchedEffect(tapFocusPoint) {
        if (tapFocusPoint != null) {
            delay(3000)
            tapFocusPoint = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CameraObsidian)
    ) {
        // Main Camera Viewport with Aspect Ratio Mask
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(settings.aspectRatio.ratio, matchHeightConstraintsFirst = true)
            ) {
                // CameraX Preview
                CameraPreviewView(
                    settings = settings,
                    selectedFilter = if (isComparingOriginal) FilterType.NONE else selectedFilter,
                    filterIntensity = filterIntensity,
                    isComparingOriginal = isComparingOriginal,
                    frameAnalyzer = viewModel.frameAnalyzer,
                    onImageCaptureCreated = { imageCaptureInstance = it },
                    onTapFocus = { tapFocusPoint = it },
                    onZoomChange = { viewModel.setZoom(it) },
                    modifier = Modifier.fillMaxSize()
                )

                // Composition Grid Overlay
                CompositionOverlay(
                    gridType = settings.gridType,
                    gridSettings = settings.gridSettings,
                    modifier = Modifier.fillMaxSize()
                )

                // Pose Silhouette & Live Face Overlay
                if (settings.isPoseCoachActive || analysisResult.detectedFaces.isNotEmpty()) {
                    PoseOverlay(
                        poseGuideType = settings.selectedPoseGuide,
                        detectedFaces = analysisResult.detectedFaces,
                        isFrontCamera = settings.isFrontCamera,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Virtual Horizon Level Indicator
                LevelHorizonIndicator(
                    rollDegrees = analysisResult.horizonRollDegrees,
                    isLevel = analysisResult.isHorizonLevel,
                    modifier = Modifier.fillMaxSize()
                )

                // Tap-to-Focus Reticle & Exposure slider
                ReticleOverlay(
                    focusPoint = tapFocusPoint,
                    exposureCompensation = viewModel.exposureCompensation.collectAsState().value,
                    onExposureChange = { viewModel.setExposure(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Shutter Flash Animation (White screen flash when photo captured)
        AnimatedVisibility(
            visible = shutterFlash,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }

        // Timer Countdown Overlay
        if (timerCountdown != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${timerCountdown}",
                    color = GoldAccent,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Top Controls: Status Bar + Quick Controls Bar + AI Advisor Banner
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            QuickControlsBar(
                settings = settings,
                onFlashToggle = { viewModel.cycleFlashMode() },
                onTimerToggle = { viewModel.cycleTimerMode() },
                onAspectRatioToggle = { viewModel.cycleAspectRatio() },
                onOpenGridSheet = { showGridSheet = true },
                onOpenPoseCoach = { showPoseSheet = true },
                onOpenSettings = onNavigateToSettings
            )

            // AI Advisor Suggestion Chip / Banner
            if (settings.isAiAdvisorEnabled) {
                AiAdvisorBanner(
                    analysisResult = analysisResult,
                    isVisible = true,
                    onOpenDetails = { showScoreSheet = true }
                )
            }
        }

        // Bottom Controls: Filters Drawer + Shutter Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Filter Carousel (Collapsible or always available above shutter)
            AnimatedVisibility(
                visible = isFilterDrawerOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FilterCarousel(
                    selectedFilter = selectedFilter,
                    filterIntensity = filterIntensity,
                    recommendedFilters = analysisResult.recommendedFilters,
                    isComparingOriginal = isComparingOriginal,
                    onFilterSelected = { viewModel.setFilter(it) },
                    onIntensityChanged = { viewModel.setFilterIntensity(it) },
                    onToggleCompare = { viewModel.toggleCompareOriginal(it) }
                )
            }

            // Shutter Button & Primary Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CameraObsidian.copy(alpha = 0.85f))
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Thumbnail Shortcut
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CameraSurfaceCard)
                        .border(1.5.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                        .clickable { onNavigateToGallery() },
                    contentAlignment = Alignment.Center
                ) {
                    val latest = recentPhotos.firstOrNull()
                    if (latest != null) {
                        AsyncImage(
                            model = latest.uriString,
                            contentDescription = "Gallery Thumbnail",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Filter Drawer Toggle Button
                IconButton(
                    onClick = { isFilterDrawerOpen = !isFilterDrawerOpen },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isFilterDrawerOpen || selectedFilter != FilterType.NONE) GoldAccent.copy(alpha = 0.2f) else CameraSurfaceCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Filters",
                        tint = if (selectedFilter != FilterType.NONE) GoldAccent else TextPrimary
                    )
                }

                // Shutter Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(3.5.dp, Color.White, CircleShape)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(if (isCapturing) GoldAccent else Color.White)
                        .clickable(enabled = !isCapturing && imageCaptureInstance != null) {
                            imageCaptureInstance?.let { capture ->
                                viewModel.takePhoto(capture, context) { photo ->
                                    onNavigateToEditor(photo)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = CameraObsidian,
                            strokeWidth = 3.dp
                        )
                    }
                }

                // Flip Camera (Front/Rear)
                IconButton(
                    onClick = { viewModel.toggleCameraFacing() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CameraSurfaceCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Switch Camera",
                        tint = TextPrimary
                    )
                }

                // AI Photography Score Button
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(CameraSurfaceCard)
                        .border(1.5.dp, GoldAccent.copy(alpha = 0.6f), CircleShape)
                        .clickable { showScoreSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Coach",
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${analysisResult.compositionScore}",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bottom Sheets
        if (showScoreSheet) {
            AiScoreBottomSheet(
                analysisResult = analysisResult,
                cloudCritique = cloudCritique,
                isCloudLoading = isCloudLoading,
                onApplyGrid = { viewModel.setGridType(it) },
                onApplyFilter = { viewModel.setFilter(it) },
                onRequestCloudCritique = {
                    // Trigger cloud critique
                    viewModel.recentPhotos.value.firstOrNull()?.let {
                        // Load bitmap
                    }
                },
                onDismiss = { showScoreSheet = false },
                sheetState = scoreSheetState
            )
        }

        if (showGridSheet) {
            GridSelectorSheet(
                selectedGrid = settings.gridType,
                gridSettings = settings.gridSettings,
                onGridSelected = { viewModel.setGridType(it) },
                onGridSettingsChanged = { viewModel.setGridSettings(it) },
                onDismiss = { showGridSheet = false },
                sheetState = gridSheetState
            )
        }

        if (showPoseSheet) {
            PoseCoachSheet(
                selectedPose = settings.selectedPoseGuide,
                isPoseCoachActive = settings.isPoseCoachActive,
                onToggleCoach = { viewModel.togglePoseCoach(it) },
                onPoseSelected = { viewModel.setPoseGuide(it) },
                onDismiss = { showPoseSheet = false },
                sheetState = poseSheetState
            )
        }
    }
}
