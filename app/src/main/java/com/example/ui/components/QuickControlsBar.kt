package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Timer3
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioMode
import com.example.model.CameraSettings
import com.example.model.FlashMode
import com.example.model.TimerMode
import com.example.ui.theme.CameraGlass
import com.example.ui.theme.CameraObsidian
import com.example.ui.theme.CameraSurfaceCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun QuickControlsBar(
    settings: CameraSettings,
    onFlashToggle: () -> Unit,
    onTimerToggle: () -> Unit,
    onAspectRatioToggle: () -> Unit,
    onOpenGridSheet: () -> Unit,
    onOpenPoseCoach: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(CameraGlass)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flash Mode Button
        IconButton(onClick = onFlashToggle) {
            when (settings.flashMode) {
                FlashMode.AUTO -> Icon(Icons.Default.FlashAuto, contentDescription = "Flash Auto", tint = GoldAccent)
                FlashMode.ON -> Icon(Icons.Default.FlashOn, contentDescription = "Flash On", tint = GoldAccent)
                FlashMode.OFF -> Icon(Icons.Default.FlashOff, contentDescription = "Flash Off", tint = TextSecondary)
                FlashMode.TORCH -> Icon(Icons.Default.Highlight, contentDescription = "Torch", tint = GoldAccent)
            }
        }

        // Timer Button
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onTimerToggle() }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer",
                    tint = if (settings.timerMode != TimerMode.OFF) GoldAccent else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                if (settings.timerMode != TimerMode.OFF) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = settings.timerMode.label,
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Aspect Ratio Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(CameraSurfaceCard)
                .clickable { onAspectRatioToggle() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = settings.aspectRatio.label,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Grid Sheet Button
        IconButton(onClick = onOpenGridSheet) {
            Icon(
                imageVector = Icons.Default.GridOn,
                contentDescription = "Grids",
                tint = if (settings.gridSettings.isVisible) GoldAccent else TextSecondary
            )
        }

        // Pose Coach Button
        IconButton(onClick = onOpenPoseCoach) {
            Icon(
                imageVector = Icons.Default.Accessibility,
                contentDescription = "Pose Coach",
                tint = if (settings.isPoseCoachActive) CyanAccent else TextSecondary
            )
        }

        // Settings Button
        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = TextSecondary
            )
        }
    }
}
