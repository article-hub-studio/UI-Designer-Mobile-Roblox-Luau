package com.robloxui.designer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = StudioColors.Primary,
    onPrimary = StudioColors.OnPrimary,
    primaryContainer = StudioColors.PrimaryDim,
    onPrimaryContainer = StudioColors.Primary,
    secondary = StudioColors.Secondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00391A),
    onSecondaryContainer = StudioColors.Secondary,
    tertiary = StudioColors.AccentPurple,
    onTertiary = Color.Black,
    background = StudioColors.Background,
    onBackground = StudioColors.TextPrimary,
    surface = StudioColors.Surface,
    onSurface = StudioColors.TextPrimary,
    surfaceVariant = StudioColors.SurfaceVariant,
    onSurfaceVariant = StudioColors.TextSecondary,
    outline = Color(0xFF444466),
    outlineVariant = Color(0xFF333355),
    error = StudioColors.AccentRed,
    onError = Color.Black,
    errorContainer = Color(0xFF4A0010),
    onErrorContainer = StudioColors.AccentRed,
    inverseSurface = Color(0xFFE8E8F0),
    inverseOnSurface = Color(0xFF1A1A2E),
    inversePrimary = StudioColors.PrimaryVariant,
    scrim = StudioColors.Scrim
)

private val LightColorScheme = lightColorScheme(
    primary = StudioColors.PrimaryVariant,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCF0FF),
    onPrimaryContainer = Color(0xFF003549),
    secondary = Color(0xFF006B3E),
    onSecondary = Color.White,
    background = Color(0xFFF8F8FF),
    onBackground = Color(0xFF1A1A2E),
    surface = Color.White,
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFE8E8F0),
    onSurfaceVariant = Color(0xFF444466),
    outline = Color(0xFFB0B0C0),
    outlineVariant = Color(0xFFD0D0E0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    scrim = Color(0x66000000)
)

@Composable
fun RobloxUIDesignerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = StudioColors.BackgroundDarker.toArgb()
            window.navigationBarColor = StudioColors.BackgroundDarker.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StudioTypography.Default,
        content = content
    )
}
