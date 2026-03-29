package com.osnordev.abaco.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = GreenContainer40,
    onPrimaryContainer = OnGreenContainer40,

    secondary = Amber40,
    onSecondary = Color.White,
    secondaryContainer = AmberContainer40,
    onSecondaryContainer = OnAmberContainer40,

    tertiary = SlateBlue40,
    onTertiary = Color.White,
    tertiaryContainer = SlateBlueContainer40,
    onTertiaryContainer = OnSlateBlueContainer40,

    error = Error40,
    errorContainer = ErrorContainer40,
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),

    background = Background40,
    onBackground = OnSurface40,
    surface = Surface40,
    onSurface = OnSurface40,
    surfaceVariant = SurfaceVariant40,
    onSurfaceVariant = OnSurfaceVariant40,
    outline = Outline40,
)

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Color(0xFF003823),
    primaryContainer = GreenContainer80,
    onPrimaryContainer = OnGreenContainer80,

    secondary = Amber80,
    onSecondary = Color(0xFF3F2D00),
    secondaryContainer = AmberContainer80,
    onSecondaryContainer = OnAmberContainer80,

    tertiary = SlateBlue80,
    onTertiary = Color(0xFF002D6E),
    tertiaryContainer = SlateBlueContainer80,
    onTertiaryContainer = OnSlateBlueContainer80,

    error = Error80,
    errorContainer = ErrorContainer80,
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Background80,
    onBackground = OnSurface80,
    surface = Surface80,
    onSurface = OnSurface80,
    surfaceVariant = SurfaceVariant80,
    onSurfaceVariant = OnSurfaceVariant80,
    outline = Outline80,
)

@Composable
fun AbacoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color available on Android 12+ — disabled so our brand palette is always used
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Update status bar color to match the surface
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
