package com.example.model

enum class PoseGuideType(
    val title: String,
    val subtitle: String,
    val idealAngleDescription: String,
    val coachingAdvice: String
) {
    NONE(
        title = "None",
        subtitle = "Free shooting",
        idealAngleDescription = "No overlay active",
        coachingAdvice = "Hold camera steady and align with composition grid."
    ),
    PORTRAIT_HEADSHOT(
        title = "Headshot",
        subtitle = "Bust & Eye Level",
        idealAngleDescription = "Eye line at 1/3 height, head tilted 3° - 8°",
        coachingAdvice = "Drop shoulders, lengthen neck slightly, and point chin slightly down toward camera."
    ),
    STANDING_THREE_QUARTER(
        title = "Standing 3/4",
        subtitle = "Slimming Depth",
        idealAngleDescription = "Body rotated 45°, face turned toward lens",
        coachingAdvice = "Shift weight onto rear leg, angle body 45°, and turn head toward the camera."
    ),
    CASUAL_SITTING(
        title = "Casual Sitting",
        subtitle = "Relaxed Posture",
        idealAngleDescription = "Back straight, elbows relaxed, knees angled",
        coachingAdvice = "Sit slightly forward on the chair, lean slightly toward lens, and relax hands."
    ),
    CLOSEUP_SELFIE(
        title = "Golden Selfie",
        subtitle = "High Angle",
        idealAngleDescription = "Camera slightly above eye level, 15° down-angle",
        coachingAdvice = "Raise camera slightly above forehead level, tilt head 5°, and catch eye catchlights."
    ),
    FULL_BODY(
        title = "Full Body",
        subtitle = "Headroom & Proportions",
        idealAngleDescription = "Headroom 15% at top, feet anchored at bottom 10%",
        coachingAdvice = "Hold camera at waist height to elongate legs, and leave breathing space above head."
    ),
    FASHION_EDITORIAL(
        title = "Editorial Pose",
        subtitle = "Dynamic Tension",
        idealAngleDescription = "Asymmetric shoulder height, strong jawline",
        coachingAdvice = "Create asymmetry by dropping one shoulder, lifting chin, and relaxing jaw."
    )
}
