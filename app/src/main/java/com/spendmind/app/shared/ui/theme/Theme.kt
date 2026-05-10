package com.spendmindai.app.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// -------------------------------------------------------------------------
// iOS-matched brand palette (extracted from .colorset files)
// -------------------------------------------------------------------------

// Primary accent — iOS PrimaryAccentColor (R=0.898, G=0.325, B=0.294)
val AccentColor      = Color(0xFFE5534B)
val AccentColorLight = Color(0xFFEE8080)

// iOS AppDarkGrayColor (R=0.200)
val DarkGrayColor    = Color(0xFF333333)

// iOS FancyGradient
val GradientStart    = Color(0xFFF87171)   // R=0.973, G=0.443, B=0.443
val GradientEnd      = Color(0xFFA78BFA)   // R=0.655, G=0.545, B=0.980

// Semantic
val ErrorColor       = Color(0xFFE53935)
val SuccessColor     = Color(0xFF43A047)

// Category fill colors — iOS light-mode values
val CategoryCar          = Color(0xFFF3C5C2)
val CategoryEatingOut    = Color(0xFFFFDFAE)
val CategoryGroceries    = Color(0xFFD9D9D9)
val CategoryLuxury       = Color(0xFFAED9E6)
val CategoryShopping     = Color(0xFFCDEEDC)
val CategoryMedicines    = Color(0xFFD1C7E3)
val CategoryUncategorized = Color(0xFFCCCCCC)

// iOS secondarySystemBackground equivalents
val TabBarBackgroundLight = Color(0xFFF2F2F7)
val TabBarBackgroundDark  = Color(0xFF1C1C1E)

// -------------------------------------------------------------------------
// Colour schemes
// -------------------------------------------------------------------------

private val DarkColorScheme = darkColorScheme(
    primary              = AccentColor,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFF7A1F1A),
    onPrimaryContainer   = Color(0xFFFFDAD7),
    secondary            = Color(0xFF9E9EA8),
    onSecondary          = Color(0xFF1C1C1E),
    tertiary             = GradientEnd,
    onTertiary           = Color.White,
    background           = Color(0xFF000000),
    onBackground         = Color.White,
    surface              = TabBarBackgroundDark,
    onSurface            = Color.White,
    surfaceVariant       = Color(0xFF3A3A3C),
    onSurfaceVariant     = Color(0xFFAEAEB2),
    error                = ErrorColor,
    onError              = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary              = AccentColor,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFFFDAD7),
    onPrimaryContainer   = Color(0xFF5C0007),
    secondary            = Color(0xFF6C6C70),
    onSecondary          = Color.White,
    tertiary             = GradientEnd,
    onTertiary           = Color.White,
    background           = Color(0xFFFFFFFF),
    onBackground         = Color(0xFF1C1C1E),
    surface              = TabBarBackgroundLight,
    onSurface            = Color(0xFF1C1C1E),
    surfaceVariant       = Color(0xFFE5E5EA),
    onSurfaceVariant     = Color(0xFF48484A),
    error                = ErrorColor,
    onError              = Color.White
)

// -------------------------------------------------------------------------
// Theme entry point
// -------------------------------------------------------------------------

@Composable
fun SpendMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = SpendMindTypography,
        content     = content
    )
}
