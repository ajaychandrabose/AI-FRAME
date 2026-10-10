package com.example.ui.camera

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import java.util.Locale

@Composable
fun LevelHorizonIndicator(
    rollDegrees: Float,
    isLevel: Boolean,
    modifier: Modifier = Modifier
) {
    val levelColor by animateColorAsState(
        targetValue = if (isLevel) EmeraldSuccess else Color(0xAAFFFFFF),
        label = "horizonColor"
    )

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val barLen = 70.dp.toPx()
            val stroke = (if (isLevel) 2.5f else 1.5f).dp.toPx()

            // Horizon indicator with rotation
            val clampedAngle = rollDegrees.coerceIn(-30f, 30f)
            val rad = Math.toRadians(clampedAngle.toDouble())
            val cosA = Math.cos(rad).toFloat()
            val sinA = Math.sin(rad).toFloat()

            // Left wing
            val leftStart = Offset(cx - (barLen * 1.5f * cosA), cy - (barLen * 1.5f * sinA))
            val leftEnd = Offset(cx - (barLen * 0.4f * cosA), cy - (barLen * 0.4f * sinA))
            drawLine(levelColor, leftStart, leftEnd, stroke)

            // Right wing
            val rightStart = Offset(cx + (barLen * 0.4f * cosA), cy + (barLen * 0.4f * sinA))
            val rightEnd = Offset(cx + (barLen * 1.5f * cosA), cy + (barLen * 1.5f * sinA))
            drawLine(levelColor, rightStart, rightEnd, stroke)

            // Center target reticle
            drawCircle(levelColor, radius = if (isLevel) 5.dp.toPx() else 3.dp.toPx(), center = Offset(cx, cy))
        }

        // Degree readout
        if (kotlin.math.abs(rollDegrees) > 0.5f) {
            Text(
                text = String.format(Locale.US, "%+.1f°", rollDegrees),
                color = levelColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 40.dp)
            )
        }
    }
}
