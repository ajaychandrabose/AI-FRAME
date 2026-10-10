package com.example.ui.camera

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiCritiqueResult
import com.example.data.GeminiCritiqueService
import com.example.data.SavedPhotoRepository
import com.example.data.UserPreferences
import com.example.editor.ImageProcessing
import com.example.model.AspectRatioMode
import com.example.model.CameraSettings
import com.example.model.CompositionGridType
import com.example.model.EditAdjustments
import com.example.model.FilterType
import com.example.model.FlashMode
import com.example.model.FrameAnalysisResult
import com.example.model.GridSettings
import com.example.model.PhotoItem
import com.example.model.PoseGuideType
import com.example.model.TimerMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import kotlin.math.atan2

class CameraViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val userPrefs = UserPreferences(application)
    private val photoRepo = SavedPhotoRepository(application)
    private val geminiService = GeminiCritiqueService()

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _settings = MutableStateFlow(userPrefs.loadSettings())
    val settings: StateFlow<CameraSettings> = _settings.asStateFlow()

    private val _analysisResult = MutableStateFlow(FrameAnalysisResult.default())
    val analysisResult: StateFlow<FrameAnalysisResult> = _analysisResult.asStateFlow()

    private val _selectedFilter = MutableStateFlow(FilterType.NONE)
    val selectedFilter: StateFlow<FilterType> = _selectedFilter.asStateFlow()

    private val _filterIntensity = MutableStateFlow(1.0f)
    val filterIntensity: StateFlow<Float> = _filterIntensity.asStateFlow()

    private val _isComparingOriginal = MutableStateFlow(false)
    val isComparingOriginal: StateFlow<Boolean> = _isComparingOriginal.asStateFlow()

    private val _zoomRatio = MutableStateFlow(1.0f)
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

    private val _exposureCompensation = MutableStateFlow(0f)
    val exposureCompensation: StateFlow<Float> = _exposureCompensation.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _timerCountdown = MutableStateFlow<Int?>(null)
    val timerCountdown: StateFlow<Int?> = _timerCountdown.asStateFlow()

    private val _shutterFlash = MutableStateFlow(false)
    val shutterFlash: StateFlow<Boolean> = _shutterFlash.asStateFlow()

    private val _latestCapturedPhoto = MutableStateFlow<PhotoItem?>(null)
    val latestCapturedPhoto: StateFlow<PhotoItem?> = _latestCapturedPhoto.asStateFlow()

    private val _cloudCritique = MutableStateFlow<AiCritiqueResult?>(null)
    val cloudCritique: StateFlow<AiCritiqueResult?> = _cloudCritique.asStateFlow()

    private val _isCloudLoading = MutableStateFlow(false)
    val isCloudLoading: StateFlow<Boolean> = _isCloudLoading.asStateFlow()

    private val _recentPhotos = MutableStateFlow<List<PhotoItem>>(emptyList())
    val recentPhotos: StateFlow<List<PhotoItem>> = _recentPhotos.asStateFlow()

    val frameAnalyzer = FrameAnalyzer { result ->
        _analysisResult.value = result
    }

    init {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        loadRecentPhotos()
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
    }

    // Accelerometer listener for physical horizon tilt
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            // Roll in degrees
            val roll = Math.toDegrees(atan2(x.toDouble(), y.toDouble())).toFloat()
            val normalizedRoll = if (roll > 90f) 180f - roll else if (roll < -90f) -180f - roll else roll
            frameAnalyzer.currentSensorRollDegrees = normalizedRoll
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setGridType(grid: CompositionGridType) {
        _settings.update { it.copy(gridType = grid) }
        userPrefs.saveGridType(grid)
        triggerHaptic()
    }

    fun setGridSettings(gridSettings: GridSettings) {
        _settings.update { it.copy(gridSettings = gridSettings) }
        userPrefs.saveGridSettings(gridSettings)
    }

    fun cycleAspectRatio() {
        val next = when (_settings.value.aspectRatio) {
            AspectRatioMode.RATIO_4_3 -> AspectRatioMode.RATIO_16_9
            AspectRatioMode.RATIO_16_9 -> AspectRatioMode.RATIO_1_1
            AspectRatioMode.RATIO_1_1 -> AspectRatioMode.RATIO_4_3
        }
        _settings.update { it.copy(aspectRatio = next) }
        userPrefs.saveAspectRatio(next)
        triggerHaptic()
    }

    fun cycleFlashMode() {
        val next = when (_settings.value.flashMode) {
            FlashMode.AUTO -> FlashMode.ON
            FlashMode.ON -> FlashMode.OFF
            FlashMode.OFF -> FlashMode.TORCH
            FlashMode.TORCH -> FlashMode.AUTO
        }
        _settings.update { it.copy(flashMode = next) }
        userPrefs.saveFlashMode(next)
        triggerHaptic()
    }

    fun cycleTimerMode() {
        val next = when (_settings.value.timerMode) {
            TimerMode.OFF -> TimerMode.SEC_3
            TimerMode.SEC_3 -> TimerMode.SEC_5
            TimerMode.SEC_5 -> TimerMode.SEC_10
            TimerMode.SEC_10 -> TimerMode.OFF
        }
        _settings.update { it.copy(timerMode = next) }
        userPrefs.saveTimerMode(next)
        triggerHaptic()
    }

    fun toggleCameraFacing() {
        _settings.update { it.copy(isFrontCamera = !it.isFrontCamera) }
        triggerHaptic()
    }

    fun togglePoseCoach(active: Boolean) {
        _settings.update {
            it.copy(
                isPoseCoachActive = active,
                selectedPoseGuide = if (active && it.selectedPoseGuide == PoseGuideType.NONE) PoseGuideType.PORTRAIT_HEADSHOT else it.selectedPoseGuide
            )
        }
        triggerHaptic()
    }

    fun setPoseGuide(pose: PoseGuideType) {
        _settings.update { it.copy(selectedPoseGuide = pose, isPoseCoachActive = true) }
        triggerHaptic()
    }

    fun setFilter(filter: FilterType) {
        _selectedFilter.value = filter
        triggerHaptic()
    }

    fun setFilterIntensity(intensity: Float) {
        _filterIntensity.value = intensity.coerceIn(0f, 1f)
    }

    fun toggleCompareOriginal(comparing: Boolean) {
        _isComparingOriginal.value = comparing
    }

    fun setExposure(value: Float) {
        _exposureCompensation.value = value.coerceIn(-2f, 2f)
    }

    fun setZoom(zoom: Float) {
        _zoomRatio.value = zoom.coerceIn(1f, 8f)
    }

    fun takePhoto(imageCapture: ImageCapture, context: Context, onPhotoReady: (PhotoItem) -> Unit) {
        if (_isCapturing.value) return

        viewModelScope.launch {
            val timerSeconds = _settings.value.timerMode.seconds
            if (timerSeconds > 0) {
                for (s in timerSeconds downTo 1) {
                    _timerCountdown.value = s
                    triggerHaptic()
                    delay(1000)
                }
                _timerCountdown.value = null
            }

            _isCapturing.value = true
            triggerShutterFlash()

            val executor = androidx.core.content.ContextCompat.getMainExecutor(context)
            imageCapture.takePicture(
                executor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                val bitmap = imageProxyToBitmap(image, _settings.value.isFrontCamera)
                                image.close()

                                if (bitmap != null) {
                                    val activeFilter = _selectedFilter.value
                                    val intensity = _filterIntensity.value

                                    val finalBitmap = if (activeFilter != FilterType.NONE && intensity > 0.05f) {
                                        ImageProcessing.applyAdjustments(
                                            bitmap,
                                            EditAdjustments(
                                                filter = activeFilter,
                                                filterIntensity = intensity,
                                                cropRatio = if (_settings.value.aspectRatio != AspectRatioMode.RATIO_4_3) _settings.value.aspectRatio else null
                                            )
                                        )
                                    } else bitmap

                                    val savedUri = photoRepo.saveBitmapToMediaStore(
                                        finalBitmap,
                                        _analysisResult.value.sceneType.title,
                                        _analysisResult.value.compositionScore
                                    )

                                    if (savedUri != null) {
                                        val item = PhotoItem(
                                            uriString = savedUri.toString(),
                                            displayName = "AI Frame Photo",
                                            dateAddedMillis = System.currentTimeMillis(),
                                            width = finalBitmap.width,
                                            height = finalBitmap.height,
                                            compositionScore = _analysisResult.value.compositionScore,
                                            sceneDetected = _analysisResult.value.sceneType.title
                                        )
                                        _latestCapturedPhoto.value = item
                                        loadRecentPhotos()
                                        withContext(Dispatchers.Main) {
                                            onPhotoReady(item)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                _isCapturing.value = false
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        exception.printStackTrace()
                        _isCapturing.value = false
                    }
                }
            )
        }
    }

    private fun triggerShutterFlash() {
        viewModelScope.launch {
            _shutterFlash.value = true
            delay(120)
            _shutterFlash.value = false
        }
    }

    fun requestCloudCritique(bitmap: Bitmap?) {
        if (bitmap == null || _isCloudLoading.value) return
        viewModelScope.launch {
            _isCloudLoading.value = true
            val critique = geminiService.analyzePhoto(bitmap, _analysisResult.value)
            _cloudCritique.value = critique
            _isCloudLoading.value = false
        }
    }

    fun loadRecentPhotos() {
        viewModelScope.launch {
            val list = photoRepo.loadSavedPhotos()
            _recentPhotos.value = list
        }
    }

    private fun imageProxyToBitmap(image: ImageProxy, isFront: Boolean): Bitmap? {
        val plane = image.planes[0]
        val buffer: ByteBuffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val rotationDegrees = image.imageInfo.rotationDegrees
        val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

        val matrix = Matrix().apply {
            if (isFront) {
                postScale(-1f, 1f)
            }
            if (rotationDegrees != 0) {
                postRotate(rotationDegrees.toFloat())
            }
        }
        return Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
    }

    private fun triggerHaptic() {
        if (!_settings.value.isHapticsEnabled) return
        val app = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(25)
        }
    }
}
