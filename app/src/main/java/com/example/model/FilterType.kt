package com.example.model

enum class FilterType(
    val title: String,
    val category: String,
    val description: String,
    val aiWhyReason: String
) {
    NONE(
        title = "Original",
        category = "Natural",
        description = "Standard neutral sensor processing without alterations.",
        aiWhyReason = "Maintains authentic natural colors and dynamic range."
    ),
    NATURAL(
        title = "Natural Pro",
        category = "Natural",
        description = "Clean, balanced tones with subtle contrast lift and skin preservation.",
        aiWhyReason = "Balanced luminance and neutral white balance suitable for everyday scenes."
    ),
    CINEMATIC(
        title = "Cinematic",
        category = "Creative",
        description = "Teal and orange color harmony with rich, moody shadow depth.",
        aiWhyReason = "Gives dramatic blockbuster mood and cinematic color separation."
    ),
    PORTRAIT(
        title = "Portrait Glow",
        category = "Portrait",
        description = "Flattering warm skin tones with soft highlight roll-off and gentle clarity.",
        aiWhyReason = "Enhances facial tones while smoothing harsh facial micro-shadows."
    ),
    WARM_SUNSET(
        title = "Golden Hour",
        category = "Warm",
        description = "Luminous amber, copper highlights, and golden warmth.",
        aiWhyReason = "Maximizes golden hour light and enriches sky and warm bounce light."
    ),
    COOL_TONES(
        title = "Arctic Cool",
        category = "Cool",
        description = "Crisp cool shadows with clean, modern blue and cyan hues.",
        aiWhyReason = "Enhances architectural steel, snow, waterscapes, and clean modern lines."
    ),
    FILM_VINTAGE(
        title = "Film & Vintage",
        category = "Vintage",
        description = "Analog nostalgic emulsion curve with lifted blacks and warm organic greens.",
        aiWhyReason = "Softens digital sharpness and provides timeless retro character."
    ),
    BLACK_WHITE(
        title = "Monochrome",
        category = "B&W",
        description = "Fine-art black & white with calibrated spectral luminance.",
        aiWhyReason = "Removes distracting color clutter to emphasize shape, light, and geometry."
    ),
    LANDSCAPE(
        title = "Vivid Scenery",
        category = "Landscape",
        description = "De-hazed skies, punchy blues, and deep vibrant foliage greens.",
        aiWhyReason = "Accentuates natural outdoor scenery, clouds, and landscape depth."
    ),
    FOOD(
        title = "Gourmet",
        category = "Food",
        description = "Warm saturation boost with appetizing clarity and warm highlights.",
        aiWhyReason = "Increases appetizing vibrancy and warm tones on culinary dishes."
    ),
    NIGHT(
        title = "Night Owl",
        category = "Night",
        description = "Shadow lift with noise control and deep navy night sky tonality.",
        aiWhyReason = "Extracts shadow detail from low-light scenes without blowing out streetlamps."
    ),
    SOFT_SKIN(
        title = "Soft Skin",
        category = "Portrait",
        description = "Peachy luminance with delicate highlight softening for close-up portraits.",
        aiWhyReason = "Gentle luminance curve specifically tuned for natural beauty and selfies."
    ),
    VIBRANT(
        title = "Vibrant Pop",
        category = "Creative",
        description = "Energetic, dynamic color separation with enhanced chroma punch.",
        aiWhyReason = "Elevates flat lighting conditions into cheerful, high-energy imagery."
    ),
    MATTE(
        title = "Velvet Matte",
        category = "Artistic",
        description = "Crushed contrast with faded velvety deep shadows for an editorial vibe.",
        aiWhyReason = "Gives a high-fashion, editorial magazine look with subdued contrast."
    ),
    PRO_STUDIO(
        title = "Studio Master",
        category = "Pro",
        description = "Strictly calibrated tonal curve preserving 100% detail in extreme zones.",
        aiWhyReason = "Optimal baseline for manual post-processing and precision editing."
    );

    companion object {
        fun default(): FilterType = NONE
    }
}
