package com.spendmindai.app.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spendmindai.app.shared.ui.theme.AccentColor
import com.spendmindai.app.shared.ui.theme.DarkGrayColor
import com.spendmindai.app.shared.ui.theme.GradientEnd
import com.spendmindai.app.shared.ui.theme.GradientStart

// -------------------------------------------------------------------------
// iOS-matched button style tokens
// -------------------------------------------------------------------------

enum class ButtonStyle {
    Primary,   // #333333
    Accent,    // #E5534B
    Fancy,     // Gradient #F87171 → #A78BFA
    Success    // Green
}

enum class ButtonSize(val height: Dp, val cornerRadius: Dp, val padding: Dp) {
    Small(36.dp,   8.dp, 16.dp),
    Medium(50.dp, 14.dp, 32.dp),
    Large(56.dp,  14.dp, 32.dp)
}

// -------------------------------------------------------------------------
// PrimaryButton — iOS PrimaryButton equivalent
// -------------------------------------------------------------------------
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle    = ButtonStyle.Primary,
    size: ButtonSize      = ButtonSize.Medium,
    capsule: Boolean      = true,
    isLoading: Boolean    = false,
    enabled: Boolean      = true,
    icon: ImageVector?    = null,
    isCompact: Boolean    = false,
    showShadow: Boolean   = false
) {
    val shape: Shape = if (capsule) RoundedCornerShape(50)
                       else RoundedCornerShape(size.cornerRadius)

    val isActive  = enabled && !isLoading
    val isGradient = style == ButtonStyle.Fancy && isActive

    val solidColor: Color = when {
        !isActive            -> Color(0xFFB0B0B0)
        style == ButtonStyle.Primary -> DarkGrayColor
        style == ButtonStyle.Accent  -> AccentColor
        style == ButtonStyle.Success -> Color(0xFF43A047)
        else                 -> GradientStart
    }

    val widthMod = if (isCompact) Modifier else Modifier.fillMaxWidth()
    val shadowMod = if (showShadow && isGradient)
        Modifier.shadow(elevation = 10.dp, shape = shape)
    else Modifier

    val gradientBg = Modifier.background(
        brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
        shape = shape
    )

    Button(
        onClick          = onClick,
        modifier         = modifier
            .then(widthMod)
            .height(size.height)
            .then(shadowMod)
            .then(if (isGradient) gradientBg else Modifier),
        enabled          = isActive,
        shape            = shape,
        colors           = ButtonDefaults.buttonColors(
            containerColor         = if (isGradient) Color.Transparent else solidColor,
            disabledContainerColor = Color(0xFFB0B0B0)
        ),
        contentPadding   = PaddingValues(
            horizontal = if (isCompact) size.padding else 24.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(20.dp),
                color       = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text  = text,
                    style = when (size) {
                        ButtonSize.Small  -> MaterialTheme.typography.bodyMedium
                        ButtonSize.Medium -> MaterialTheme.typography.headlineSmall
                        ButtonSize.Large  -> MaterialTheme.typography.headlineMedium
                    },
                    color = Color.White
                )
            }
        }
    }
}
