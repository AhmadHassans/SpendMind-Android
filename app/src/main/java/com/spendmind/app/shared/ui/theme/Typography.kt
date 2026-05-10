package com.spendmindai.app.shared.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// iOS Artegra Soft scale → system font with matching sizes & weights
val SpendMindTypography = Typography(

    // iOS hero = 60pt bold
    displayLarge = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.Bold,
        fontSize    = 60.sp,
        lineHeight  = 66.sp,
        letterSpacing = (-0.5).sp
    ),

    // iOS display1 = 34pt bold
    displayMedium = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.Bold,
        fontSize    = 34.sp,
        lineHeight  = 40.sp,
        letterSpacing = (-0.25).sp
    ),

    // iOS display2 = 28pt semibold (used on mic icon)
    displaySmall = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 28.sp,
        lineHeight  = 34.sp,
        letterSpacing = 0.sp
    ),

    // iOS h1 = 22pt semibold
    headlineLarge = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 22.sp,
        lineHeight  = 28.sp,
        letterSpacing = 0.sp
    ),

    // iOS h2 = 20pt semibold
    headlineMedium = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 20.sp,
        lineHeight  = 26.sp,
        letterSpacing = 0.sp
    ),

    // iOS h3 = 18pt semibold
    headlineSmall = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 18.sp,
        lineHeight  = 24.sp,
        letterSpacing = 0.sp
    ),

    // iOS h1 alias for titleLarge (22pt bold)
    titleLarge = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.Bold,
        fontSize    = 22.sp,
        lineHeight  = 28.sp,
        letterSpacing = 0.sp
    ),

    // iOS bodyLarge = 17pt regular
    titleMedium = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.Medium,
        fontSize    = 17.sp,
        lineHeight  = 22.sp,
        letterSpacing = 0.sp
    ),

    // iOS body = 15pt medium
    titleSmall = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.Medium,
        fontSize    = 15.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.sp
    ),

    // iOS bodyLarge = 17pt regular
    bodyLarge = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.Normal,
        fontSize    = 17.sp,
        lineHeight  = 22.sp,
        letterSpacing = 0.sp
    ),

    // iOS body = 15pt regular
    bodyMedium = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.Normal,
        fontSize    = 15.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.sp
    ),

    // iOS caption = 13pt regular
    bodySmall = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.Normal,
        fontSize    = 13.sp,
        lineHeight  = 18.sp,
        letterSpacing = 0.sp
    ),

    // iOS captionSmall = 12pt medium (labels)
    labelLarge = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.Medium,
        fontSize    = 14.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.1.sp
    ),

    labelMedium = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.Medium,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.sp
    ),

    // iOS captionSmall = 12pt
    labelSmall = TextStyle(
        fontFamily  = FontFamily.Default,
        fontWeight  = FontWeight.Medium,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.sp
    )
)
