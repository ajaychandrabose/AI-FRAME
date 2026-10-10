# AI Frame – Smart Camera

**AI Frame – Smart Camera** is an intelligent, professional photography camera application for Android built with Kotlin, Jetpack Compose, CameraX, on-device ML Kit vision heuristics, and optional Google Gemini cloud AI critique.

Instead of only capturing images, AI Frame acts as your personal AI photography coach—guiding you on framing, headroom, horizon alignment, optimal composition grids, poses, and color-graded filters.

---

## Key Features

### 1. 12 Intelligent Composition Grids
Rendered with mathematical precision on the live CameraX viewfinder:
1. **Rule of Thirds**: Classic 3×3 grid with 4 highlighted power intersection points.
2. **Golden Ratio (Phi Grid)**: Harmonic proportions (1:0.618:1).
3. **Golden Spiral (Fibonacci Flow)**: Logarithmic curve leading the viewer's eye to the focal subject.
4. **Golden Triangle**: Diagonal line with intersecting perpendiculars.
5. **Diagonal Composition**: Dynamic tension lines connecting corners.
6. **Center Composition**: Concentric focus circles and crosshairs for bullseye framing.
7. **Symmetry Grid**: Plumb mirror axes with quadrant dividers.
8. **Leading Lines**: Converging perspective guides toward a vanishing horizon.
9. **Portrait Guide**: Soft head oval, eye-level line, and shoulder slope guides.
10. **Landscape Guide**: Dual horizon levels (1/3 sky or 2/3 sky) with foreground anchors.
11. **Headroom & Eye-line Guide**: Safe headroom margin and upper third eye marker.
12. **Architecture & Horizon Alignment**: Vertical plumb lines and dual-axis leveling.
13. **Minimal Guide**: Corner framing brackets and subtle center reticle.

*Customization*: Instant opacity slider (20%–100%), line thickness (1–3.5 dp), and 5 distinct line colors (Electric Gold, Crisp White, Pro Cyan, Neon Green, Warm Coral).

### 2. AI Scene Detection & Real-Time Photography Coach
- **On-Device Vision Analysis**: Analyzes frame luminance, dynamic range, contrast, and edge orientation every 280ms without dropping preview frames.
- **ML Kit Face & Pose Detection**: Real-time face tracking, head tilt (Euler angles pitch, yaw, roll), smiling probability, and eye alignment.
- **Physical Horizon Level**: Real accelerometer-assisted tilt angle readout (e.g. `+0.8°`) with tactile haptic lock and emerald level indicator when within ±1.0°.
- **AI Composition Score**: Real-time score (0–100%) factoring rule-of-thirds sweetspots, headroom, horizon level, and lighting balance.
- **Actionable Hints**: Actionable banner over preview (e.g., *"Tilt camera up slightly for more headroom"*, *"Rotate phone by 1.5° to level the horizon"*, *"Move subject toward the golden intersection"*).
- **Gemini Cloud AI Critique**: Optional multimodal analysis powered by `gemini-3.5-flash` for critique and pro tips.

### 3. AI Pose Coach
- Interactive pose guide overlays for portraits:
  - **Headshot**: Professional bust framing and chin tilt guidance.
  - **Standing 3/4**: Slimming 45° body angle with shoulders turned.
  - **Casual Sitting**: Relaxed seated posture with natural arms.
  - **Golden Selfie**: High-angle perspective catching soft catchlights.
  - **Full Body**: Elongated proportions with headroom and feet margins.
  - **Editorial Pose**: Dynamic asymmetry and fashion model shoulder tilt.

### 4. 15 Cinematic Smart Filters & GPU Pipeline
- **Real ColorMatrix Processing**: Hardware-accelerated color grading on preview and high-resolution captured Bitmaps:
  - *Natural Pro*, *Cinematic*, *Portrait Glow*, *Golden Hour*, *Arctic Cool*, *Film & Vintage*, *Monochrome*, *Vivid Scenery*, *Gourmet*, *Night Owl*, *Soft Skin*, *Vibrant Pop*, *Velvet Matte*, *Studio Master*, and *Original*.
- **AI Top 3 Recommendations**: Contextually recommends top 3 filters with explanations why.
- **Real-Time Intensity Slider**: Tune filter strength from 0% to 100%.
- **Live Before/After Compare**: One-tap comparison between filtered and original preview.

### 5. Pro Camera Controls
- Front/rear camera toggle with smooth transition.
- Tap-to-focus with animated reticle and vertical drag exposure compensation (-2 to +2 EV).
- Pinch-to-zoom (1.0x to 8.0x).
- Flash modes: Auto, On, Off, Torch.
- Shutter timer: Off, 3s, 5s, 10s with on-screen countdown and haptic feedback.
- Aspect ratio switcher: 4:3, 16:9, and 1:1 with viewfinder masking and automatic crop.
- Shutter button with haptic feedback and capture flash effect.

### 6. Photo Editor & MediaStore Gallery
- **Built-in Editor**:
  - Filter application with fine intensity control.
  - Sliders for Brightness, Contrast, Saturation, and Warmth.
  - 90° rotation, horizontal flipping, and aspect ratio crop (1:1, 4:3, 16:9).
  - Undo and redo history stack.
  - Non-destructive export to Android `MediaStore` (`Pictures/AIFrame`).
  - Direct share to social apps and messaging.
- **Gallery**:
  - Grid view with EXIF and composition score badges.
  - Full-resolution photo inspection and deletion confirmation.

---

## Build & Run Instructions

### Prerequisites
- Android SDK 36 (minSdk 24)
- JDK 17 / 21
- Gradle with Kotlin DSL

### 1. Build Debug APK
To build the installable debug APK:
```bash
gradle assembleDebug
```
The output APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 2. Run Local Tests
To execute unit tests and Robolectric tests:
```bash
gradle :app:testDebugUnitTest
```

### 3. Build Signed Release APK
The project is configured with a release signing configuration:
```bash
gradle assembleRelease
```
The release APK is generated at:
```
app/build/outputs/apk/release/app-release-unsigned.apk
```
Or signed with your production keystore by exporting environment variables:
```bash
export KEYSTORE_PATH="/path/to/my-upload-key.jks"
export STORE_PASSWORD="your-keystore-password"
export KEY_PASSWORD="your-key-password"
gradle assembleRelease
```

---

## Permissions & Privacy
- `android.permission.CAMERA`: Required for camera preview, composition analysis, and photo capture.
- `android.permission.VIBRATE`: Haptic feedback on shutter click and horizon leveling.
- `android.permission.INTERNET`: Required exclusively for optional Gemini Cloud AI photo critique.
- Photos are stored locally on the device using Android's standard `MediaStore` API without any background cloud uploading.
