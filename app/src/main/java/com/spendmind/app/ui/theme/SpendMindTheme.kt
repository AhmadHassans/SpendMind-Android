package com.spendmindai.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Brand palette — matches shared/ui/theme/Theme.kt
private val AccentRed      = Color(0xFFE5534B)
private val AccentPurple   = Color(0xFFA78BFA)
private val DarkGray       = Color(0xFF1C1C1E)

private val LightColorScheme = lightColorScheme(
    primary              = AccentRed,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFFFDAD7),
    onPrimaryContainer   = Color(0xFF5C0007),
    secondary            = Color(0xFF6C6C70),
    onSecondary          = Color.White,
    tertiary             = AccentPurple,
    onTertiary           = Color.White,
    background           = Color(0xFFFFFFFF),
    onBackground         = DarkGray,
    surface              = Color(0xFFF2F2F7),
    onSurface            = DarkGray,
    surfaceVariant       = Color(0xFFE5E5EA),
    onSurfaceVariant     = Color(0xFF48484A),
    error                = Color(0xFFE53935),
    onError              = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary              = AccentRed,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFF7A1F1A),
    onPrimaryContainer   = Color(0xFFFFDAD7),
    secondary            = Color(0xFF9E9EA8),
    onSecondary          = Color(0xFF1C1C1E),
    tertiary             = AccentPurple,
    onTertiary           = Color.White,
    background           = Color(0xFF000000),
    onBackground         = Color.White,
    surface              = Color(0xFF1C1C1E),
    onSurface            = Color.White,
    surfaceVariant       = Color(0xFF3A3A3C),
    onSurfaceVariant     = Color(0xFFAEAEB2),
    error                = Color(0xFFE53935),
    onError              = Color.White
)

@Composable
fun SpendMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Transparent status bar — Compose content draws edge-to-edge
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
