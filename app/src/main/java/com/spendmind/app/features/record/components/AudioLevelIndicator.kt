package com.spendmindai.app.features.record.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun AudioLevelIndicator(
    isRecording: Boolean,
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio")
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_scale"
    )
    val accentColor = Color(0xFFE5534B)

    Canvas(modifier = modifier) {
        if (!isRecording) return@Canvas
        val centerX = size.width / 2
        val centerY = size.height / 2
        val baseRadius = size.minDimension / 2 * animatedScale * (0.7f + audioLevel * 0.3f)

        listOf(
            0.5f to 0.15f,
            0.7f to 0.10f,
            0.9f to 0.05f
        ).forEach { (scale, alpha) ->
            drawCircle(
                color = accentColor.copy(alpha = alpha),
                radius = baseRadius * scale,
                center = Offset(centerX, centerY)
            )
        }
    }
}
