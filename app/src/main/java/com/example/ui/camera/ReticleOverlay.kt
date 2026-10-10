package com.example.ui.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoldAccent

@Composable
fun ReticleOverlay(
    focusPoint: Offset?,
    exposureCompensation: Float, // -2f to +2f
    onExposureChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (focusPoint == null) return

    Box(modifier = modifier.fillMaxSize()) {
        val sizePx = 80.dp
        val halfSizePx = 40.dp

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (focusPoint.x - halfSizePx.toPx()).toInt(),
                        y = (focusPoint.y - halfSizePx.toPx()).toInt()
                    )
                }
                .size(sizePx)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        // Invert drag: up increases exposure, down decreases
                        val delta = -dragAmount / 120f
                        onExposureChange((exposureCompensation + delta).coerceIn(-2f, 2f))
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.width * 0.42f
                val stroke = 2.dp.toPx()

                // Focus ring
                drawCircle(GoldAccent, radius = r, center = Offset(cx, cy), style = Stroke(stroke))

                // Corner notches
                val notch = 8.dp.toPx()
                drawLine(GoldAccent, Offset(cx - r, cy), Offset(cx - r + notch, cy), stroke)
                drawLine(GoldAccent, Offset(cx + r - notch, cy), Offset(cx + r, cy), stroke)
                drawLine(GoldAccent, Offset(cx, cy - r), Offset(cx, cy - r + notch), stroke)
                drawLine(GoldAccent, Offset(cx, cy + r - notch), Offset(cx, cy + r), stroke)
            }

            // Sun icon next to focus ring with exposure height offset
            val sunOffsetY = (-exposureCompensation * 16).dp
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = "Exposure",
                tint = GoldAccent,
                modifier = Modifier
                    .offset(x = 44.dp, y = sunOffsetY)
                    .size(20.dp)
            )
        }
    }
}
