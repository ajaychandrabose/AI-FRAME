package com.example.model

enum class SceneType(
    val title: String,
    val iconName: String,
    val recommendedGrid: CompositionGridType,
    val defaultAdvice: String
) {
    PORTRAIT(
        title = "Portrait",
        iconName = "Face",
        recommendedGrid = CompositionGridType.PORTRAIT_GUIDE,
        defaultAdvice = "Place eyes on the upper horizontal line for an engaging, natural gaze."
    ),
    GROUP(
        title = "Group Photo",
        iconName = "Groups",
        recommendedGrid = CompositionGridType.SYMMETRY,
        defaultAdvice = "Maintain even spacing between subjects and keep all faces inside the frame."
    ),
    LANDSCAPE(
        title = "Landscape",
        iconName = "Landscape",
        recommendedGrid = CompositionGridType.LANDSCAPE_GUIDE,
        defaultAdvice = "Align the horizon with the top or bottom third line to balance sky and earth."
    ),
    ARCHITECTURE(
        title = "Architecture",
        iconName = "LocationCity",
        recommendedGrid = CompositionGridType.ARCHITECTURE_HORIZON,
        defaultAdvice = "Align vertical building edges with the plumb lines to avoid perspective distortion."
    ),
    FOOD(
        title = "Food & Product",
        iconName = "Restaurant",
        recommendedGrid = CompositionGridType.CENTER,
        defaultAdvice = "Frame at a 45-degree angle or directly top-down for maximum culinary appeal."
    ),
    LOW_LIGHT(
        title = "Low Light",
        iconName = "NightsStay",
        recommendedGrid = CompositionGridType.MINIMAL,
        defaultAdvice = "Hold device steady with both hands; use Night Owl filter to boost shadow detail."
    ),
    BACKLIT(
        title = "Backlit Scene",
        iconName = "WbSunny",
        recommendedGrid = CompositionGridType.RULE_OF_THIRDS,
        defaultAdvice = "Tap to expose for the subject's face or angle away from direct solar glare."
    ),
    STREET(
        title = "Street Photography",
        iconName = "DirectionsWalk",
        recommendedGrid = CompositionGridType.LEADING_LINES,
        defaultAdvice = "Look for diagonals and sidewalks leading the viewer into the scene."
    ),
    GENERAL(
        title = "General Scene",
        iconName = "Camera",
        recommendedGrid = CompositionGridType.RULE_OF_THIRDS,
        defaultAdvice = "Use intersection points to anchor the main subject with pleasing negative space."
    );
}
