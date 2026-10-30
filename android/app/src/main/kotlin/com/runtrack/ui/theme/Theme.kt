package com.runtrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.runtrack.R

object RunTrackColors {
    val Accent = Color(0xFF2ADAA5)
    val Surface = Color.White
    val Text = Color(0xFF171717)
    val Muted = Color(0xFF727272)
}

private val Inter = FontFamily(Font(R.font.inter_regular), Font(R.font.inter_medium, FontWeight.Medium), Font(R.font.inter_bold, FontWeight.Bold))
private val Base = Typography()
private val TrackTypography = Typography(
    headlineLarge = Base.headlineLarge.copy(fontFamily = Inter), headlineMedium = Base.headlineMedium.copy(fontFamily = Inter),
    headlineSmall = Base.headlineSmall.copy(fontFamily = Inter), titleLarge = Base.titleLarge.copy(fontFamily = Inter),
    titleMedium = Base.titleMedium.copy(fontFamily = Inter), titleSmall = Base.titleSmall.copy(fontFamily = Inter),
    bodyLarge = Base.bodyLarge.copy(fontFamily = Inter), bodyMedium = Base.bodyMedium.copy(fontFamily = Inter),
    bodySmall = Base.bodySmall.copy(fontFamily = Inter), labelLarge = Base.labelLarge.copy(fontFamily = Inter),
    labelMedium = Base.labelMedium.copy(fontFamily = Inter), labelSmall = Base.labelSmall.copy(fontFamily = Inter)
)

@Composable
fun RunTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = RunTrackColors.Text, onPrimary = Color.White,
            background = RunTrackColors.Surface, surface = RunTrackColors.Surface, onSurface = RunTrackColors.Text,
            onBackground = RunTrackColors.Text, onSurfaceVariant = RunTrackColors.Muted,
            outline = Color(0xFF79747E), outlineVariant = Color(0xFFDDDDDD)),
        typography = TrackTypography, content = content
    )
}
