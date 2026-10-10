package com.example.model

enum class CompositionGridType(
    val title: String,
    val subtitle: String,
    val category: String,
    val description: String
) {
    RULE_OF_THIRDS(
        title = "Rule of Thirds",
        subtitle = "Classic 3×3 Grid",
        category = "Essential",
        description = "Place subjects along gridlines or at the 4 intersection power points for natural visual balance."
    ),
    GOLDEN_RATIO(
        title = "Golden Ratio",
        subtitle = "Phi 1:0.618:1",
        category = "Artistic",
        description = "Divine proportion grid bringing organic harmony and classical aesthetic balance."
    ),
    GOLDEN_SPIRAL(
        title = "Golden Spiral",
        subtitle = "Fibonacci Flow",
        category = "Artistic",
        description = "Guides the viewer's eye along a sweeping curve into the primary focal subject."
    ),
    GOLDEN_TRIANGLE(
        title = "Golden Triangle",
        subtitle = "Dynamic Diagonals",
        category = "Dynamic",
        description = "Diagonal line intersecting perpendiculars; great for angled subjects and active shots."
    ),
    DIAGONAL(
        title = "Diagonal",
        subtitle = "Tension & Flow",
        category = "Dynamic",
        description = "Emphasizes diagonal lines for dramatic depth, speed, and perspective."
    ),
    CENTER(
        title = "Center Focus",
        subtitle = "Direct Symmetry",
        category = "Symmetry",
        description = "Bullseye composition with concentric guides for bold, isolated focal subjects."
    ),
    SYMMETRY(
        title = "Symmetry Grid",
        subtitle = "Dual Axis Mirror",
        category = "Symmetry",
        description = "Perfect for reflections, balanced portraits, patterns, and geometric architecture."
    ),
    LEADING_LINES(
        title = "Leading Lines",
        subtitle = "Vanishing Perspective",
        category = "Depth",
        description = "Converging guides that lead the observer's gaze toward infinity or distant subjects."
    ),
    PORTRAIT_GUIDE(
        title = "Portrait Guide",
        subtitle = "Face & Shoulders",
        category = "Portrait",
        description = "Soft head oval, eye-line marker, and shoulder guides for flattering human framing."
    ),
    LANDSCAPE_GUIDE(
        title = "Landscape Guide",
        subtitle = "Horizon & Foreground",
        category = "Landscape",
        description = "Dual horizon levels (1/3 sky or 2/3 sky) with foreground subject anchors."
    ),
    HEADROOM_EYELINE(
        title = "Headroom & Eyeline",
        subtitle = "Pro Video & Photo",
        category = "Portrait",
        description = "Ensures sufficient space above the head and positions the subject's eyes naturally."
    ),
    ARCHITECTURE_HORIZON(
        title = "Architecture & Level",
        subtitle = "Plumb Lines & Horizon",
        category = "Architecture",
        description = "Vertical perspective correction lines combined with a high-precision level guide."
    ),
    MINIMAL(
        title = "Minimal Guide",
        subtitle = "Clean Corners",
        category = "Essential",
        description = "Unobtrusive corner marks and center crosshair for distraction-free shooting."
    );

    companion object {
        fun default(): CompositionGridType = RULE_OF_THIRDS
    }
}
