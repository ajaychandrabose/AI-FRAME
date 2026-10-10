package com.example.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.DetectedFaceInfo
import com.example.model.PoseGuideType
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent

@Composable
fun PoseOverlay(
    poseGuideType: PoseGuideType,
    detectedFaces: List<DetectedFaceInfo>,
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Draw static Pose Silhouette Guide if selected
        if (poseGuideType != PoseGuideType.NONE) {
            drawPoseSilhouette(poseGuideType, w, h)
        }

        // 2. Draw live detected faces bounding boxes and orientation guides
        for (face in detectedFaces) {
            val rect = face.boundingBox
            // Adjust for front camera mirror if needed
            val left = if (isFrontCamera) (1f - rect.right) * w else rect.left * w
            val right = if (isFrontCamera) (1f - rect.left) * w else rect.right * w
            val top = rect.top * h
            val bottom = rect.bottom * h

            val boxW = right - left
            val boxH = bottom - top
            val centerX = left + boxW / 2f
            val centerY = top + boxH / 2f

            // Alignment feedback color: green if head tilt is level (within ±8°), else gold/cyan
            val isLevel = kotlin.math.abs(face.headEulerAngleZ) <= 8f
            val boxColor = if (isLevel) EmeraldSuccess else CyanAccent

            // Bounding box with corner brackets
            val bracketLen = minOf(boxW, boxH) * 0.25f
            val stroke = 2.dp.toPx()

            // Top-Left
            drawLine(boxColor, Offset(left, top), Offset(left + bracketLen, top), stroke)
            drawLine(boxColor, Offset(left, top), Offset(left, top + bracketLen), stroke)
            // Top-Right
            drawLine(boxColor, Offset(right, top), Offset(right - bracketLen, top), stroke)
            drawLine(boxColor, Offset(right, top), Offset(right, top + bracketLen), stroke)
            // Bottom-Left
            drawLine(boxColor, Offset(left, bottom), Offset(left + bracketLen, bottom), stroke)
            drawLine(boxColor, Offset(left, bottom), Offset(left, bottom - bracketLen), stroke)
            // Bottom-Right
            drawLine(boxColor, Offset(right, bottom), Offset(right - bracketLen, bottom), stroke)
            drawLine(boxColor, Offset(right, bottom), Offset(right, bottom - bracketLen), stroke)

            // Eye line and head orientation vector
            val eyeLevelY = top + (boxH * 0.38f)
            drawLine(
                boxColor.copy(alpha = 0.6f),
                Offset(left + 8f, eyeLevelY),
                Offset(right - 8f, eyeLevelY),
                strokeWidth = 1.5.dp.toPx()
            )

            // Smile indicator dot
            face.smilingProbability?.let { smile ->
                if (smile > 0.6f) {
                    drawCircle(GoldAccent, radius = 6.dp.toPx(), center = Offset(centerX, bottom - 12f))
                }
            }
        }
    }
}

private fun DrawScope.drawPoseSilhouette(pose: PoseGuideType, w: Float, h: Float) {
    val guideColor = CyanAccent.copy(alpha = 0.5f)
    val dash = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
    val strokeWidth = 2.dp.toPx()

    when (pose) {
        PoseGuideType.PORTRAIT_HEADSHOT -> {
            val cx = w / 2f
            val headCy = h * 0.35f
            val rx = w * 0.22f
            val ry = h * 0.16f

            // Head oval
            drawOval(
                guideColor,
                topLeft = Offset(cx - rx, headCy - ry),
                size = Size(rx * 2, ry * 2),
                style = Stroke(strokeWidth, pathEffect = dash)
            )

            // Neck & Shoulders
            val path = Path().apply {
                moveTo(cx - rx * 0.5f, headCy + ry)
                lineTo(cx - rx * 0.5f, headCy + ry + h * 0.08f)
                quadraticTo(cx - rx * 1.5f, headCy + ry + h * 0.12f, 0f, h * 0.82f)

                moveTo(cx + rx * 0.5f, headCy + ry)
                lineTo(cx + rx * 0.5f, headCy + ry + h * 0.08f)
                quadraticTo(cx + rx * 1.5f, headCy + ry + h * 0.12f, w, h * 0.82f)
            }
            drawPath(path, guideColor, style = Stroke(strokeWidth, pathEffect = dash))
        }

        PoseGuideType.STANDING_THREE_QUARTER -> {
            val cx = w * 0.52f
            val headCy = h * 0.25f
            val rx = w * 0.16f
            val ry = h * 0.12f

            drawOval(
                guideColor,
                topLeft = Offset(cx - rx, headCy - ry),
                size = Size(rx * 2, ry * 2),
                style = Stroke(strokeWidth, pathEffect = dash)
            )

            val bodyPath = Path().apply {
                // Angled shoulders
                moveTo(cx - w * 0.22f, h * 0.40f)
                lineTo(cx + w * 0.15f, h * 0.38f)
                // Torso angled
                lineTo(cx + w * 0.10f, h * 0.65f)
                lineTo(cx - w * 0.18f, h * 0.67f)
                close()
            }
            drawPath(bodyPath, guideColor, style = Stroke(strokeWidth, pathEffect = dash))
        }

        PoseGuideType.CASUAL_SITTING -> {
            val cx = w / 2f
            val headCy = h * 0.30f
            val rx = w * 0.18f
            val ry = h * 0.13f

            drawOval(
                guideColor,
                topLeft = Offset(cx - rx, headCy - ry),
                size = Size(rx * 2, ry * 2),
                style = Stroke(strokeWidth, pathEffect = dash)
            )

            val seatPath = Path().apply {
                moveTo(cx - w * 0.28f, h * 0.46f)
                lineTo(cx + w * 0.28f, h * 0.46f)
                lineTo(cx + w * 0.32f, h * 0.80f)
                lineTo(cx - w * 0.32f, h * 0.80f)
            }
            drawPath(seatPath, guideColor, style = Stroke(strokeWidth, pathEffect = dash))
        }

        PoseGuideType.CLOSEUP_SELFIE -> {
            val cx = w / 2f
            val headCy = h * 0.42f
            val rx = w * 0.32f
            val ry = h * 0.24f

            drawOval(
                guideColor,
                topLeft = Offset(cx - rx, headCy - ry),
                size = Size(rx * 2, ry * 2),
                style = Stroke(strokeWidth, pathEffect = dash)
            )

            // High angle diagonal indicator
            drawLine(guideColor.copy(alpha = 0.7f), Offset(w * 0.2f, h * 0.15f), Offset(w * 0.8f, h * 0.15f), strokeWidth)
        }

        PoseGuideType.FULL_BODY -> {
            val cx = w / 2f
            val headCy = h * 0.18f
            val rx = w * 0.12f
            val ry = h * 0.08f

            // Head
            drawOval(
                guideColor,
                topLeft = Offset(cx - rx, headCy - ry),
                size = Size(rx * 2, ry * 2),
                style = Stroke(strokeWidth, pathEffect = dash)
            )

            // Safe headroom line
            drawLine(guideColor, Offset(w * 0.15f, h * 0.08f), Offset(w * 0.85f, h * 0.08f), strokeWidth * 0.8f)
            // Ground foot placement line
            drawLine(guideColor, Offset(w * 0.15f, h * 0.92f), Offset(w * 0.85f, h * 0.92f), strokeWidth * 0.8f)

            // Body posture line
            drawLine(guideColor, Offset(cx, headCy + ry), Offset(cx, h * 0.58f), strokeWidth, pathEffect = dash)
            // Legs
            drawLine(guideColor, Offset(cx, h * 0.58f), Offset(cx - w * 0.12f, h * 0.90f), strokeWidth, pathEffect = dash)
            drawLine(guideColor, Offset(cx, h * 0.58f), Offset(cx + w * 0.12f, h * 0.90f), strokeWidth, pathEffect = dash)
        }

        PoseGuideType.FASHION_EDITORIAL -> {
            val cx = w / 2f
            val headCy = h * 0.32f
            val rx = w * 0.20f
            val ry = h * 0.14f

            // Tilted head
            drawOval(
                guideColor,
                topLeft = Offset(cx - rx, headCy - ry),
                size = Size(rx * 2, ry * 2),
                style = Stroke(strokeWidth, pathEffect = dash)
            )

            // Dramatic diagonal shoulder line (asymmetry)
            drawLine(guideColor, Offset(cx - w * 0.35f, h * 0.44f), Offset(cx + w * 0.30f, h * 0.54f), strokeWidth * 1.5f)
        }

        PoseGuideType.NONE -> {}
    }
}
