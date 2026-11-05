package com.parkpulse.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parkpulse.app.presentation.theme.ParksColors

private val ParkPulseColors = lightColorScheme(
    primary = ParksColors.PrimaryGreen,
    onPrimary = ParksColors.Black,
    primaryContainer = ParksColors.BackgroundMint,
    onPrimaryContainer = ParksColors.DarkGreen,
    secondary = ParksColors.SecondaryGreen,
    onSecondary = ParksColors.White,
    tertiary = ParksColors.DarkGreen,
    onTertiary = ParksColors.White,
    background = ParksColors.BackgroundMint,
    onBackground = ParksColors.Black,
    surface = ParksColors.White,
    onSurface = ParksColors.Black,
    surfaceVariant = ParksColors.White,
    onSurfaceVariant = ParksColors.SecondaryGreen,
    outline = ParksColors.SecondaryGreen,
    outlineVariant = ParksColors.SecondaryGreen,
    error = ParksColors.ErrorRed,
    onError = ParksColors.White
)

private val ParkPulseTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp)
)

private val ParkPulseShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun ParkPulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ParkPulseColors,
        typography = ParkPulseTypography,
        shapes = ParkPulseShapes,
        content = content
    )
}
