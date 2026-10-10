package com.example.ui.editor

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.SavedPhotoRepository
import com.example.editor.ImageProcessing
import com.example.model.AspectRatioMode
import com.example.model.EditAdjustments
import com.example.model.FilterType
import com.example.model.PhotoItem
import com.example.ui.theme.CameraObsidian
import com.example.ui.theme.CameraSurfaceCard
import com.example.ui.theme.CameraSurfaceDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

enum class EditorTab(val label: String) {
    FILTERS("Filters"),
    ADJUST("Adjust"),
    CROP_ROTATE("Transform")
}

@Composable
fun PhotoEditorScreen(
    photoItem: PhotoItem,
    onNavigateBack: () -> Unit,
    onNavigateToGallery: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { SavedPhotoRepository(context) }

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isComparingOriginal by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf(EditorTab.FILTERS) }

    // Edit adjustments state
    var adjustments by remember { mutableStateOf(EditAdjustments()) }
    val historyUndoStack = remember { mutableStateListOf<EditAdjustments>() }
    val historyRedoStack = remember { mutableStateListOf<EditAdjustments>() }

    BackHandler {
        onNavigateBack()
    }

    // Load bitmap from URI
    LaunchedEffect(photoItem.uriString) {
        isLoading = true
        val loaded = repo.loadBitmapFromUri(Uri.parse(photoItem.uriString))
        originalBitmap = loaded
        previewBitmap = loaded
        isLoading = false
    }

    // Re-render preview bitmap when adjustments change
    fun updateAdjustments(newAdjustments: EditAdjustments) {
        historyUndoStack.add(adjustments)
        historyRedoStack.clear()
        adjustments = newAdjustments

        originalBitmap?.let { orig ->
            scope.launch {
                previewBitmap = ImageProcessing.applyAdjustments(orig, newAdjustments)
            }
        }
    }

    fun undo() {
        if (historyUndoStack.isNotEmpty()) {
            val previous = historyUndoStack.removeLast()
            historyRedoStack.add(adjustments)
            adjustments = previous
            originalBitmap?.let { orig ->
                scope.launch {
                    previewBitmap = ImageProcessing.applyAdjustments(orig, previous)
                }
            }
        }
    }

    fun redo() {
        if (historyRedoStack.isNotEmpty()) {
            val next = historyRedoStack.removeLast()
            historyUndoStack.add(adjustments)
            adjustments = next
            originalBitmap?.let { orig ->
                scope.launch {
                    previewBitmap = ImageProcessing.applyAdjustments(orig, next)
                }
            }
        }
    }

    fun saveEditedPhoto() {
        if (isSaving || previewBitmap == null) return
        isSaving = true
        scope.launch {
            val savedUri = repo.saveBitmapToMediaStore(
                previewBitmap!!,
                sceneName = "AI Edited",
                compositionScore = photoItem.compositionScore
            )
            isSaving = false
            if (savedUri != null) {
                Toast.makeText(context, "Saved edited photo to Gallery!", Toast.LENGTH_SHORT).show()
                onNavigateToGallery()
            } else {
                Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sharePhoto() {
        val uri = Uri.parse(photoItem.uriString)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Photo via"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CameraObsidian)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            // Undo / Redo controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { undo() },
                    enabled = historyUndoStack.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (historyUndoStack.isNotEmpty()) TextPrimary else TextMuted
                    )
                }

                IconButton(
                    onClick = { redo() },
                    enabled = historyRedoStack.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (historyRedoStack.isNotEmpty()) TextPrimary else TextMuted
                    )
                }

                // AI Auto Enhance Button
                IconButton(
                    onClick = {
                        updateAdjustments(
                            adjustments.copy(
                                filter = FilterType.NATURAL,
                                brightness = 8f,
                                contrast = 1.08f,
                                saturation = 1.12f,
                                warmth = 6f
                            )
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Enhance",
                        tint = GoldAccent
                    )
                }
            }

            // Save and Share
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { sharePhoto() }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TextSecondary
                    )
                }

                IconButton(
                    onClick = { saveEditedPhoto() },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = GoldAccent,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = GoldAccent
                        )
                    }
                }
            }
        }

        // Photo Canvas Viewport with Press-to-Compare
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isComparingOriginal = true
                            tryAwaitRelease()
                            isComparingOriginal = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = GoldAccent)
            } else {
                val bitmapToDisplay = if (isComparingOriginal) originalBitmap else previewBitmap
                if (bitmapToDisplay != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmapToDisplay.asImageBitmap(),
                        contentDescription = "Photo Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Compare badge indicator
            if (isComparingOriginal) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldAccent)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Original", color = CameraObsidian, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = "Press & hold to compare original",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Editor Tabs
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = CameraSurfaceDark,
            contentColor = GoldAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = GoldAccent
                )
            }
        ) {
            EditorTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.label,
                            color = if (selectedTab == tab) GoldAccent else TextSecondary,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Bottom Controls Panel depending on selected tab
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CameraSurfaceDark)
                .padding(16.dp)
        ) {
            when (selectedTab) {
                EditorTab.FILTERS -> {
                    FiltersPanel(
                        adjustments = adjustments,
                        onAdjustmentsChange = { updateAdjustments(it) }
                    )
                }
                EditorTab.ADJUST -> {
                    SlidersAdjustmentPanel(
                        adjustments = adjustments,
                        onAdjustmentsChange = { updateAdjustments(it) }
                    )
                }
                EditorTab.CROP_ROTATE -> {
                    TransformPanel(
                        adjustments = adjustments,
                        onAdjustmentsChange = { updateAdjustments(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltersPanel(
    adjustments: EditAdjustments,
    onAdjustmentsChange: (EditAdjustments) -> Unit
) {
    Column {
        if (adjustments.filter != FilterType.NONE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${adjustments.filter.title} Intensity",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "${(adjustments.filterIntensity * 100).toInt()}%",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = adjustments.filterIntensity,
                onValueChange = { onAdjustmentsChange(adjustments.copy(filterIntensity = it)) },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = GoldAccent,
                    activeTrackColor = GoldAccent,
                    inactiveTrackColor = CameraSurfaceCard
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(FilterType.values()) { filter ->
                val isSelected = adjustments.filter == filter
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAdjustmentsChange(adjustments.copy(filter = filter)) }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) GoldAccent else CameraSurfaceCard)
                            .border(1.dp, if (isSelected) GoldAccent else Color(0x33FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter.title.take(2).uppercase(),
                            color = if (isSelected) CameraObsidian else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = filter.title,
                        color = if (isSelected) GoldAccent else TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SlidersAdjustmentPanel(
    adjustments: EditAdjustments,
    onAdjustmentsChange: (EditAdjustments) -> Unit
) {
    var activeSlider by remember { mutableStateOf("Brightness") }

    Column {
        // Slider selection chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            val sliders = listOf("Brightness", "Contrast", "Saturation", "Warmth")
            items(sliders) { label ->
                val isSelected = activeSlider == label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) GoldAccent else CameraSurfaceCard)
                        .clickable { activeSlider = label }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) CameraObsidian else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Active slider control
        when (activeSlider) {
            "Brightness" -> {
                AdjustmentSliderRow(
                    label = "Brightness",
                    value = adjustments.brightness,
                    valueRange = -50f..50f,
                    valueFormatter = { String.format("%+.0f", it) },
                    onValueChange = { onAdjustmentsChange(adjustments.copy(brightness = it)) }
                )
            }
            "Contrast" -> {
                AdjustmentSliderRow(
                    label = "Contrast",
                    value = adjustments.contrast,
                    valueRange = 0.5f..1.8f,
                    valueFormatter = { String.format("%.2f", it) },
                    onValueChange = { onAdjustmentsChange(adjustments.copy(contrast = it)) }
                )
            }
            "Saturation" -> {
                AdjustmentSliderRow(
                    label = "Saturation",
                    value = adjustments.saturation,
                    valueRange = 0.0f..2.0f,
                    valueFormatter = { String.format("%.2f", it) },
                    onValueChange = { onAdjustmentsChange(adjustments.copy(saturation = it)) }
                )
            }
            "Warmth" -> {
                AdjustmentSliderRow(
                    label = "Warmth",
                    value = adjustments.warmth,
                    valueRange = -50f..50f,
                    valueFormatter = { String.format("%+.0f", it) },
                    onValueChange = { onAdjustmentsChange(adjustments.copy(warmth = it)) }
                )
            }
        }
    }
}

@Composable
private fun AdjustmentSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueFormatter: (Float) -> String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextSecondary, fontSize = 12.sp)
            Text(text = valueFormatter(value), color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = GoldAccent,
                activeTrackColor = GoldAccent,
                inactiveTrackColor = CameraSurfaceCard
            )
        )
    }
}

@Composable
private fun TransformPanel(
    adjustments: EditAdjustments,
    onAdjustmentsChange: (EditAdjustments) -> Unit
) {
    Column {
        // Rotate and Flip Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = {
                    val nextRotation = (adjustments.rotationDegrees + 90) % 360
                    onAdjustmentsChange(adjustments.copy(rotationDegrees = nextRotation))
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CameraSurfaceCard)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate", tint = GoldAccent)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rotate 90°", color = TextPrimary, fontSize = 12.sp)
                }
            }

            IconButton(
                onClick = {
                    onAdjustmentsChange(adjustments.copy(isFlippedHorizontal = !adjustments.isFlippedHorizontal))
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CameraSurfaceCard)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Icon(Icons.Default.Flip, contentDescription = "Flip", tint = GoldAccent)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Flip H", color = TextPrimary, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Crop Ratio Selection
        Text(
            text = "Aspect Ratio Crop",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val ratios = listOf(
                null to "Original",
                AspectRatioMode.RATIO_1_1 to "1:1",
                AspectRatioMode.RATIO_4_3 to "4:3",
                AspectRatioMode.RATIO_16_9 to "16:9"
            )
            for ((ratio, label) in ratios) {
                val isSelected = adjustments.cropRatio == ratio
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) GoldAccent else CameraSurfaceCard)
                        .clickable { onAdjustmentsChange(adjustments.copy(cropRatio = ratio)) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) CameraObsidian else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
