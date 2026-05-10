package com.spendmindai.app.features.record

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendmindai.app.core.domain.model.ParsedExpense
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

// Purple matching the tab-bar mic button gradient
private val MicPurple  = Color(0xFF6C63FF)
private val MicViolet  = Color(0xFF9C77F5)
private val RecordRed  = Color(0xFFE53935)

@Composable
fun RecordScreen(
    onDismiss: () -> Unit = {},
    viewModel: RecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            snackbarHostState.showSnackbar(uiState.error ?: "An error occurred")
            viewModel.resetError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Record Expense",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (uiState.state) {
                    RecordState.IDLE       -> "Tap the mic to start recording"
                    RecordState.LISTENING  -> "Listening… tap to stop"
                    RecordState.PROCESSING -> "Processing your expense…"
                    RecordState.REVIEW     -> "Review detected expenses"
                    RecordState.ERROR      -> "Something went wrong"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ----------------------------------------------------------------
            // Mic button area — 200dp host for rings + button
            // ----------------------------------------------------------------
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Sonar rings: slow idle pulse / fast energetic recording ripple
                SonarRings(
                    isRecording = uiState.state == RecordState.LISTENING,
                    isVisible   = uiState.state == RecordState.IDLE ||
                                  uiState.state == RecordState.LISTENING,
                    modifier    = Modifier.fillMaxSize()
                )

                // Processing spinner
                if (uiState.state == RecordState.PROCESSING) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(180.dp),
                        color       = MicPurple,
                        strokeWidth = 4.dp
                    )
                }

                MicButton(
                    isRecording  = uiState.state == RecordState.LISTENING,
                    isProcessing = uiState.state == RecordState.PROCESSING,
                    onClick      = {
                        when (uiState.state) {
                            RecordState.IDLE, RecordState.ERROR -> viewModel.startRecording()
                            RecordState.LISTENING               -> viewModel.stopRecording()
                            else                                -> {}
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Transcript card
            AnimatedVisibility(
                visible = uiState.transcript.isNotEmpty(),
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text     = "\"${uiState.transcript}\"",
                        modifier = Modifier.padding(16.dp),
                        style    = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Parsed expenses review
            AnimatedVisibility(
                visible = uiState.state == RecordState.REVIEW && uiState.parsedExpenses.isNotEmpty(),
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Text(
                        text      = "Detected Expenses",
                        style     = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier  = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.parsedExpenses) { expense ->
                            ParsedExpenseCard(expense = expense, currency = uiState.currency)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick  = { viewModel.discardExpenses(); onDismiss() },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape    = RoundedCornerShape(14.dp)
                        ) { Text("Discard") }

                        Button(
                            onClick  = { viewModel.confirmExpenses(); onDismiss() },
                            modifier = Modifier.weight(2f).height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = MicPurple)
                        ) {
                            Text("Save All", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Idle hint
            AnimatedVisibility(
                visible = uiState.state == RecordState.IDLE && uiState.transcript.isEmpty(),
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text  = "Try saying:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "\"Coffee 5 dollars\"",
                        "\"Lunch 12.50 at McDonald's\"",
                        "\"Uber 8 bucks transport\""
                    ).forEach { hint ->
                        Text(
                            text      = hint,
                            style     = MaterialTheme.typography.bodySmall,
                            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SonarRings — 3 concentric expanding rings with staggered delays
//
//   isVisible  = false → nothing drawn (processing / review states)
//   isRecording = false → slow idle pulse (1800 ms, low alpha)
//   isRecording = true  → fast energetic ripple (700 ms, high alpha)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SonarRings(
    isRecording: Boolean,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isVisible) {
        Canvas(modifier = modifier) {}   // keep layout slot, draw nothing
        return
    }

    val duration   = if (isRecording) 700  else 1800
    val maxAlpha   = if (isRecording) 0.45f else 0.18f
    val ringColor  = if (isRecording) RecordRed else MicPurple

    val pulse = rememberInfiniteTransition(label = "sonar_rings")

    val r1 by pulse.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation    = tween(duration, easing = LinearEasing),
            repeatMode   = RepeatMode.Restart
        ), label = "ring1"
    )
    val r2 by pulse.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation    = tween(duration, delayMillis = duration / 3, easing = LinearEasing),
            repeatMode   = RepeatMode.Restart
        ), label = "ring2"
    )
    val r3 by pulse.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation    = tween(duration, delayMillis = (duration * 2) / 3, easing = LinearEasing),
            repeatMode   = RepeatMode.Restart
        ), label = "ring3"
    )

    Canvas(modifier = modifier) {
        val center     = Offset(size.width / 2f, size.height / 2f)
        // rings start just outside the button (48dp radius) and expand to fill box
        val btnRadius  = 48.dp.toPx()
        val maxRadius  = size.minDimension / 2f

        listOf(r1, r2, r3).forEach { progress ->
            val radius = btnRadius + (maxRadius - btnRadius) * progress
            val alpha  = maxAlpha * (1f - progress)
            if (alpha > 0.005f) {
                drawCircle(
                    color  = ringColor.copy(alpha = alpha),
                    radius = radius,
                    center = center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MicButton — purple gradient idle, red recording, grey processing
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MicButton(
    isRecording: Boolean,
    isProcessing: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue   = if (isRecording) 1.10f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "mic_btn_scale"
    )

    val brush = when {
        isRecording  -> Brush.linearGradient(listOf(Color(0xFFEF4444), RecordRed))
        isProcessing -> Brush.linearGradient(listOf(Color(0xFF9E9E9E), Color(0xFF757575)))
        else         -> Brush.linearGradient(
            colors = listOf(MicViolet, MicPurple),
            start  = Offset(0f, 0f),
            end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    Box(
        modifier = Modifier
            .size(96.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(brush = brush)
            .clickable(enabled = !isProcessing) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                color       = Color.White,
                modifier    = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
        } else {
            Icon(
                imageVector     = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (isRecording) "Stop recording" else "Start recording",
                modifier        = Modifier.size(48.dp),
                tint            = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Parsed expense card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ParsedExpenseCard(expense: ParsedExpense, currency: String) {
    val formattedAmount = remember(expense.amount, currency) {
        try {
            val fmt = NumberFormat.getCurrencyInstance(Locale.getDefault())
            fmt.currency = Currency.getInstance(expense.currency.ifEmpty { currency })
            fmt.format(expense.amount)
        } catch (e: Exception) {
            "${expense.currency.ifEmpty { currency }} ${"%.2f".format(expense.amount)}"
        }
    }

    val confidenceColor = when {
        expense.confidence >= 0.8 -> Color(0xFF4CAF50)
        expense.confidence >= 0.5 -> Color(0xFFFF9800)
        else                      -> Color(0xFFF44336)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = getCategoryEmoji(expense.categoryName), fontSize = 28.sp, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text      = expense.categoryName ?: "Uncategorized",
                    style     = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (!expense.note.isNullOrEmpty()) {
                    Text(
                        text    = expense.note,
                        style   = MaterialTheme.typography.bodySmall,
                        color   = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text      = formattedAmount,
                    style     = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color     = MicPurple
                )
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(confidenceColor))
                    Text(
                        text  = "${(expense.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = confidenceColor
                    )
                }
            }
        }
    }
}

private fun getCategoryEmoji(categoryName: String?): String = when {
    categoryName == null                                                    -> "📝"
    categoryName.contains("food", true) ||
        categoryName.contains("dining", true)                              -> "🍔"
    categoryName.contains("transport", true) ||
        categoryName.contains("travel", true)                              -> "🚗"
    categoryName.contains("shopping", true)                                -> "🛍️"
    categoryName.contains("bill", true) ||
        categoryName.contains("util", true)                                -> "💡"
    categoryName.contains("health", true) ||
        categoryName.contains("medical", true)                             -> "🏥"
    categoryName.contains("entertainment", true)                           -> "🎬"
    categoryName.contains("education", true)                               -> "📚"
    categoryName.contains("fitness", true)                                 -> "💪"
    categoryName.contains("subscription", true)                            -> "📱"
    else                                                                   -> "📝"
}
