package com.example.ui.camera

import android.graphics.ColorMatrixColorFilter
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.FilterPresets
import com.example.model.AspectRatioMode
import com.example.model.CameraSettings
import com.example.model.FilterType
import com.example.model.FlashMode
import java.util.concurrent.Executors

@Composable
fun CameraPreviewView(
    settings: CameraSettings,
    selectedFilter: FilterType,
    filterIntensity: Float,
    isComparingOriginal: Boolean,
    frameAnalyzer: FrameAnalyzer,
    onImageCaptureCreated: (ImageCapture) -> Unit,
    onTapFocus: (Offset) -> Unit,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var currentZoom by remember { mutableFloatStateOf(1f) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    val newZoom = (currentZoom * zoom).coerceIn(1f, 8f)
                    currentZoom = newZoom
                    cameraControl?.setZoomRatio(newZoom)
                    onZoomChange(newZoom)
                }
            }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val cameraSelector = if (settings.isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val flashMode = when (settings.flashMode) {
                        FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
                        FlashMode.ON -> ImageCapture.FLASH_MODE_ON
                        FlashMode.OFF, FlashMode.TORCH -> ImageCapture.FLASH_MODE_OFF
                    }

                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setFlashMode(flashMode)
                        .build()

                    onImageCaptureCreated(imageCapture)

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, frameAnalyzer)
                        }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture,
                            imageAnalysis
                        )
                        cameraControl = camera.cameraControl

                        // Torch control
                        if (settings.flashMode == FlashMode.TORCH) {
                            camera.cameraControl.enableTorch(true)
                        } else {
                            camera.cameraControl.enableTorch(false)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                // Tap to focus on previewView
                previewView.setOnTouchListener { view, event ->
                    if (event.action == android.view.MotionEvent.ACTION_UP) {
                        val factory = previewView.meteringPointFactory
                        val point = factory.createPoint(event.x, event.y)
                        val action = FocusMeteringAction.Builder(point).build()
                        cameraControl?.startFocusAndMetering(action)
                        onTapFocus(Offset(event.x, event.y))
                        view.performClick()
                    }
                    true
                }

                previewView
            },
            update = { previewView ->
                // Handle Torch dynamic toggle
                if (settings.flashMode == FlashMode.TORCH) {
                    cameraControl?.enableTorch(true)
                } else {
                    cameraControl?.enableTorch(false)
                }
            }
        )
    }
}
