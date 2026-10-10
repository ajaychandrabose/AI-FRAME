package com.example.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.CompositionGridType
import com.example.model.GridSettings

@Composable
fun CompositionOverlay(
    gridType: CompositionGridType,
    gridSettings: GridSettings,
    modifier: Modifier = Modifier
) {
    if (!gridSettings.isVisible) return

    val baseColor = gridSettings.selectedColor.color
    val strokeColor = baseColor.copy(alpha = gridSettings.opacity.coerceIn(0.1f, 1f))
    val strokeWidth = gridSettings.thicknessDp.dp

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val pxWidth = strokeWidth.toPx()

        when (gridType) {
            CompositionGridType.RULE_OF_THIRDS -> {
                drawRuleOfThirds(w, h, strokeColor, pxWidth)
            }
            CompositionGridType.GOLDEN_RATIO -> {
                drawGoldenRatio(w, h, strokeColor, pxWidth)
            }
            CompositionGridType.GOLDEN_SPIRAL -> {
                drawGoldenSpiral(w, h, strokeColor, pxWidth)
            }
            CompositionGridType.GOLDEN_TRIANGLE -> {
                drawGoldenTriangle(w, h, strokeColor, pxWidth)
            }
            CompositionGridType.DIAGONAL -> {
                drawDiagonal(w, h, strokeColor, pxWidth)
            }
            CompositionGridType.CENTER -> {
                drawCenterComposition(w, h, strokeColor, pxWidth)
            }
            CompositionGridType.SYMMETRY -> {
                drawSymmetryGrid(w, h, strokeColor, pxWidth)
            }
            CompositionGridType.LEADING_LINES -> {
                drawLeadingLines(w, h, strokeColor, pxWidth)
            }
            CompositionGridType.PORTRAIT_GUIDE -> {
                drawPortraitGuide(w, h, strokeColor, pxWidth)
            }
            CompositionGridType.LANDSCAPE_GUIDE -> {
                drawLandscapeGuide(w, h, strokeColor, pxWidth)
            }
            CompositionGridType.HEADROOM_EYELINE -> {
                drawHeadroomEyeline(w, h, strokeColor, pxWidth)
            }
            CompositionGridType.ARCHITECTURE_HORIZON -> {
                drawArchitectureHorizon(w, h, strokeColor, pxWidth)
            }
            CompositionGridType.MINIMAL -> {
                drawMinimalGuide(w, h, strokeColor, pxWidth)
            }
        }
    }
}

private fun DrawScope.drawRuleOfThirds(w: Float, h: Float, color: Color, strokeWidth: Float) {
    val x1 = w / 3f
    val x2 = 2f * w / 3f
    val y1 = h / 3f
    val y2 = 2f * h / 3f

    // Grid lines
    drawLine(color, Offset(x1, 0f), Offset(x1, h), strokeWidth)
    drawLine(color, Offset(x2, 0f), Offset(x2, h), strokeWidth)
    drawLine(color, Offset(0f, y1), Offset(w, y1), strokeWidth)
    drawLine(color, Offset(0f, y2), Offset(w, y2), strokeWidth)

    // Power intersections rings
    val intersections = listOf(
        Offset(x1, y1), Offset(x2, y1),
        Offset(x1, y2), Offset(x2, y2)
    )
    for (pt in intersections) {
        drawCircle(color.copy(alpha = 0.35f), radius = 18f, center = pt)
        drawCircle(color, radius = 5f, center = pt)
    }
}

private fun DrawScope.drawGoldenRatio(w: Float, h: Float, color: Color, strokeWidth: Float) {
    val phi = 0.6180339887f
    val x1 = w * (1f - phi)
    val x2 = w * phi
    val y1 = h * (1f - phi)
    val y2 = h * phi

    drawLine(color, Offset(x1, 0f), Offset(x1, h), strokeWidth)
    drawLine(color, Offset(x2, 0f), Offset(x2, h), strokeWidth)
    drawLine(color, Offset(0f, y1), Offset(w, y1), strokeWidth)
    drawLine(color, Offset(0f, y2), Offset(w, y2), strokeWidth)

    val pts = listOf(Offset(x1, y1), Offset(x2, y1), Offset(x1, y2), Offset(x2, y2))
    for (pt in pts) {
        drawCircle(color, radius = 6f, center = pt)
    }
}

private fun DrawScope.drawGoldenSpiral(w: Float, h: Float, color: Color, strokeWidth: Float) {
    // Golden bounding boxes and spiral path
    val path = Path()
    val phi = 0.618f
    val xCut = w * phi
    val yCut = h * phi

    // Major golden rect outlines
    drawLine(color.copy(alpha = 0.4f), Offset(xCut, 0f), Offset(xCut, h), strokeWidth = strokeWidth * 0.75f)
    drawLine(color.copy(alpha = 0.4f), Offset(0f, yCut), Offset(xCut, yCut), strokeWidth = strokeWidth * 0.75f)

    // Smooth sweeping Fibonacci spiral path winding into the focal quadrant
    path.moveTo(0f, h)
    path.cubicTo(0f, 0f, xCut, 0f, xCut, yCut)
    val xSub = xCut + (w - xCut) * (1f - phi)
    path.cubicTo(xCut, h * 0.9f, w, h * 0.8f, w, yCut)
    path.cubicTo(w, yCut * 0.4f, xSub, yCut * 0.6f, xSub, yCut * 0.85f)

    drawPath(path, color, style = Stroke(width = strokeWidth))
    drawCircle(color, radius = 8f, center = Offset(xSub, yCut * 0.85f))
    drawCircle(color.copy(alpha = 0.3f), radius = 22f, center = Offset(xSub, yCut * 0.85f))
}

private fun DrawScope.drawGoldenTriangle(w: Float, h: Float, color: Color, strokeWidth: Float) {
    // Diagonal from bottom-left to top-right
    drawLine(color, Offset(0f, h), Offset(w, 0f), strokeWidth)

    // Perpendiculars from top-left and bottom-right to the main diagonal
    // Intersection points along the line
    val lenSq = w * w + h * h
    val perp1X = (w * h * h) / lenSq
    val perp1Y = h - (h * h * h) / lenSq
    drawLine(color, Offset(0f, 0f), Offset(perp1X, perp1Y), strokeWidth)

    val perp2X = w - perp1X
    val perp2Y = h - perp1Y
    drawLine(color, Offset(w, h), Offset(perp2X, perp2Y), strokeWidth)

    drawCircle(color, radius = 6f, center = Offset(perp1X, perp1Y))
    drawCircle(color, radius = 6f, center = Offset(perp2X, perp2Y))
}

private fun DrawScope.drawDiagonal(w: Float, h: Float, color: Color, strokeWidth: Float) {
    drawLine(color, Offset(0f, 0f), Offset(w, h), strokeWidth)
    drawLine(color, Offset(0f, h), Offset(w, 0f), strokeWidth)
    // Corner diamonds
    drawLine(color.copy(alpha = 0.5f), Offset(w / 2f, 0f), Offset(w, h / 2f), strokeWidth * 0.7f)
    drawLine(color.copy(alpha = 0.5f), Offset(w, h / 2f), Offset(w / 2f, h), strokeWidth * 0.7f)
    drawLine(color.copy(alpha = 0.5f), Offset(w / 2f, h), Offset(0f, h / 2f), strokeWidth * 0.7f)
    drawLine(color.copy(alpha = 0.5f), Offset(0f, h / 2f), Offset(w / 2f, 0f), strokeWidth * 0.7f)
}

private fun DrawScope.drawCenterComposition(w: Float, h: Float, color: Color, strokeWidth: Float) {
    val center = Offset(w / 2f, h / 2f)
    val minDim = minOf(w, h)
    // Concentric target circles
    drawCircle(color.copy(alpha = 0.25f), radius = minDim * 0.38f, center = center, style = Stroke(strokeWidth))
    drawCircle(color, radius = minDim * 0.20f, center = center, style = Stroke(strokeWidth))
    drawCircle(color, radius = 6f, center = center)

    // Center crosshair ticks
    val tick = minDim * 0.08f
    drawLine(color, Offset(center.x - tick, center.y), Offset(center.x + tick, center.y), strokeWidth)
    drawLine(color, Offset(center.x, center.y - tick), Offset(center.x, center.y + tick), strokeWidth)
}

private fun DrawScope.drawSymmetryGrid(w: Float, h: Float, color: Color, strokeWidth: Float) {
    val cx = w / 2f
    val cy = h / 2f
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)

    // Strong vertical mirror line
    drawLine(color, Offset(cx, 0f), Offset(cx, h), strokeWidth * 1.3f)
    // Horizontal reflection line
    drawLine(color, Offset(0f, cy), Offset(w, cy), strokeWidth)

    // Quadrant dividers (dashed)
    drawLine(color.copy(alpha = 0.4f), Offset(w / 4f, 0f), Offset(w / 4f, h), strokeWidth * 0.7f, pathEffect = dashEffect)
    drawLine(color.copy(alpha = 0.4f), Offset(3f * w / 4f, 0f), Offset(3f * w / 4f, h), strokeWidth * 0.7f, pathEffect = dashEffect)
    drawCircle(color, radius = 6f, center = Offset(cx, cy))
}

private fun DrawScope.drawLeadingLines(w: Float, h: Float, color: Color, strokeWidth: Float) {
    val vanishPt = Offset(w * 0.5f, h * 0.38f)

    // Lines converging to vanishing point from bottom corners & edges
    drawLine(color, Offset(0f, h), vanishPt, strokeWidth)
    drawLine(color, Offset(w, h), vanishPt, strokeWidth)
    drawLine(color.copy(alpha = 0.5f), Offset(w * 0.25f, h), vanishPt, strokeWidth * 0.8f)
    drawLine(color.copy(alpha = 0.5f), Offset(w * 0.75f, h), vanishPt, strokeWidth * 0.8f)

    // Horizon line across vanishing point
    drawLine(color.copy(alpha = 0.6f), Offset(0f, vanishPt.y), Offset(w, vanishPt.y), strokeWidth * 0.8f)
    drawCircle(color, radius = 7f, center = vanishPt)
}

private fun DrawScope.drawPortraitGuide(w: Float, h: Float, color: Color, strokeWidth: Float) {
    val cx = w / 2f
    val headCenterY = h * 0.38f
    val headRadiusX = w * 0.22f
    val headRadiusY = h * 0.16f

    // Head oval guide
    drawOval(
        color = color,
        topLeft = Offset(cx - headRadiusX, headCenterY - headRadiusY),
        size = Size(headRadiusX * 2, headRadiusY * 2),
        style = Stroke(strokeWidth)
    )

    // Eye line indicator (approx 45% down the head)
    val eyeY = headCenterY - (headRadiusY * 0.1f)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    drawLine(color, Offset(cx - headRadiusX * 1.25f, eyeY), Offset(cx + headRadiusX * 1.25f, eyeY), strokeWidth, pathEffect = dashEffect)

    // Left and right eye cross-markers
    drawCircle(color, radius = 4f, center = Offset(cx - headRadiusX * 0.45f, eyeY))
    drawCircle(color, radius = 4f, center = Offset(cx + headRadiusX * 0.45f, eyeY))

    // Shoulder slopes
    val path = Path().apply {
        moveTo(cx - headRadiusX * 0.7f, headCenterY + headRadiusY)
        quadraticTo(cx - headRadiusX * 1.5f, h * 0.65f, 0f, h * 0.85f)
        moveTo(cx + headRadiusX * 0.7f, headCenterY + headRadiusY)
        quadraticTo(cx + headRadiusX * 1.5f, h * 0.65f, w, h * 0.85f)
    }
    drawPath(path, color.copy(alpha = 0.6f), style = Stroke(strokeWidth))
}

private fun DrawScope.drawLandscapeGuide(w: Float, h: Float, color: Color, strokeWidth: Float) {
    val ySky = h * 0.33f
    val yGround = h * 0.67f

    // 1/3 Horizon (for prominent landscape)
    drawLine(color, Offset(0f, yGround), Offset(w, yGround), strokeWidth * 1.2f)
    // 2/3 Horizon (for prominent sky)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
    drawLine(color.copy(alpha = 0.5f), Offset(0f, ySky), Offset(w, ySky), strokeWidth, pathEffect = dashEffect)

    // Vertical focal point guide
    val xFocal = w * 0.67f
    drawLine(color.copy(alpha = 0.4f), Offset(xFocal, 0f), Offset(xFocal, h), strokeWidth * 0.8f)
    drawCircle(color, radius = 8f, center = Offset(xFocal, yGround))
}

private fun DrawScope.drawHeadroomEyeline(w: Float, h: Float, color: Color, strokeWidth: Float) {
    val headroomY = h * 0.15f
    val eyelineY = h * 0.33f

    // Safe headroom limit line
    drawLine(color.copy(alpha = 0.45f), Offset(w * 0.2f, headroomY), Offset(w * 0.8f, headroomY), strokeWidth)

    // Ideal eyeline height
    drawLine(color, Offset(0f, eyelineY), Offset(w, eyelineY), strokeWidth * 1.2f)

    // Vertical plumb line
    drawLine(color.copy(alpha = 0.35f), Offset(w / 2f, 0f), Offset(w / 2f, h), strokeWidth * 0.8f)

    drawCircle(color, radius = 6f, center = Offset(w / 2f, eyelineY))
}

private fun DrawScope.drawArchitectureHorizon(w: Float, h: Float, color: Color, strokeWidth: Float) {
    // 5 vertical plumb lines to ensure vertical perspective alignment
    for (i in 1..4) {
        val x = w * (i / 5f)
        drawLine(color, Offset(x, 0f), Offset(x, h), strokeWidth * 0.8f)
    }
    // Main horizon level
    val midY = h / 2f
    drawLine(color, Offset(0f, midY), Offset(w, midY), strokeWidth * 1.2f)
    drawCircle(color, radius = 6f, center = Offset(w / 2f, midY))
}

private fun DrawScope.drawMinimalGuide(w: Float, h: Float, color: Color, strokeWidth: Float) {
    val cornerLen = 32f
    val pad = 24f

    // Top-Left corner
    drawLine(color, Offset(pad, pad), Offset(pad + cornerLen, pad), strokeWidth)
    drawLine(color, Offset(pad, pad), Offset(pad, pad + cornerLen), strokeWidth)

    // Top-Right corner
    drawLine(color, Offset(w - pad - cornerLen, pad), Offset(w - pad, pad), strokeWidth)
    drawLine(color, Offset(w - pad, pad), Offset(w - pad, pad + cornerLen), strokeWidth)

    // Bottom-Left corner
    drawLine(color, Offset(pad, h - pad - cornerLen), Offset(pad, h - pad), strokeWidth)
    drawLine(color, Offset(pad, h - pad), Offset(pad + cornerLen, h - pad), strokeWidth)

    // Bottom-Right corner
    drawLine(color, Offset(w - pad, h - pad - cornerLen), Offset(w - pad, h - pad), strokeWidth)
    drawLine(color, Offset(w - pad - cornerLen, h - pad), Offset(w - pad, h - pad), strokeWidth)

    // Center crosshair
    val cx = w / 2f
    val cy = h / 2f
    val crossLen = 14f
    drawLine(color, Offset(cx - crossLen, cy), Offset(cx + crossLen, cy), strokeWidth)
    drawLine(color, Offset(cx, cy - crossLen), Offset(cx, cy + crossLen), strokeWidth)
}
