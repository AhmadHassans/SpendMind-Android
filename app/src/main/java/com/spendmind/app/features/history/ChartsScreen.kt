package com.spendmindai.app.features.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CalendarViewDay
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendmindai.app.shared.utils.CurrencyFormatter
import kotlin.math.cos
import kotlin.math.sin

// Soft pastel palette matching iOS design
private val pastelPalette = listOf(
    Color(0xFFFFB3BA), // soft pink
    Color(0xFFFFE4A0), // soft yellow
    Color(0xFFB5EAD7), // soft green
    Color(0xFFC7CEEA), // soft blue
    Color(0xFFFFDAC1), // soft peach
    Color(0xFFE2C5F0), // soft lavender
    Color(0xFFB5D5E8), // soft sky
    Color(0xFFFFCBA4), // soft apricot
    Color(0xFFD4F0C0), // soft mint
    Color(0xFFF0C5D4)  // soft rose
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    viewModel: ChartsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPeriodMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header: period picker (left) | title (center)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Box(modifier = Modifier.align(Alignment.CenterStart)) {
                TextButton(
                    onClick = { showPeriodMenu = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = uiState.selectedPeriod.chartLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.UnfoldMore,
                        contentDescription = "Select period",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded = showPeriodMenu,
                    onDismissRequest = { showPeriodMenu = false }
                ) {
                    TimePeriod.values().forEach { period ->
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = period.calendarIcon,
                                    contentDescription = null,
                                    tint = Color(0xFF444444)
                                )
                            },
                            text = {
                                Text(
                                    text = period.chartLabel,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black
                                )
                            },
                            onClick = {
                                viewModel.selectPeriod(period)
                                showPeriodMenu = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "Expense Charts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Donut chart centered in remaining space
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            DonutChartWithCenter(
                data = uiState.categoryChartData,
                totalAmount = uiState.totalAmount,
                currency = uiState.currency,
                modifier = Modifier.size(300.dp)
            )
        }
    }
}

@Composable
private fun DonutChartWithCenter(
    data: List<CategoryChartData>,
    totalAmount: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    // Outer box: 300dp — icons float outside the 200dp ring canvas
    Box(modifier = modifier, contentAlignment = Alignment.Center) {

        // Ring canvas (smaller than outer box so icons fit around it)
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = size.width * 0.20f
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

            if (data.isEmpty()) {
                drawArc(
                    color = Color(0xFFE5E5EA),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
            } else {
                val total = data.sumOf { it.amount }
                var startAngle = -90f
                data.forEachIndexed { index, item ->
                    val sweepAngle = ((item.amount / total) * 360f).toFloat()
                    drawArc(
                        color = pastelPalette[index % pastelPalette.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle - 1.5f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (data.isEmpty()) {
                Text(
                    text = "No Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = CurrencyFormatter.format(0.0, currency),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFFAAAAAA)
                )
            } else {
                Text(
                    text = "Total Expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = CurrencyFormatter.formatCompact(totalAmount, currency),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111),
                    fontSize = 22.sp
                )
            }
        }

        // Emoji icons floating outside the ring with percentage
        if (data.isNotEmpty()) {
            val total = data.sumOf { it.amount }
            var startAngleDeg = -90.0
            data.forEachIndexed { index, item ->
                val sweep = (item.amount / total) * 360.0
                val midAngleDeg = startAngleDeg + sweep / 2.0
                val midAngleRad = Math.toRadians(midAngleDeg)
                // Only show icon if slice is large enough to avoid overlap
                if (item.percentage >= 4.0) {
                    val iconRadius = 118.0 // dp from center of 300dp box
                    val offsetX = (iconRadius * cos(midAngleRad)).dp
                    val offsetY = (iconRadius * sin(midAngleRad)).dp
                    Box(
                        modifier = Modifier.offset(x = offsetX, y = offsetY),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = item.emoji,
                                fontSize = 18.sp
                            )
                            Text(
                                text = if (item.percentage < 1.0)
                                    String.format("%.1f%%", item.percentage)
                                else
                                    "${item.percentage.toInt()}%",
                                fontSize = 9.sp,
                                color = Color(0xFF555555),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                startAngleDeg += sweep
            }
        }
    }
}

private val TimePeriod.chartLabel: String
    get() = when (this) {
        TimePeriod.DAY -> "Day"
        TimePeriod.WEEK -> "Week"
        TimePeriod.MONTH -> "Month"
        TimePeriod.YEAR -> "Year"
        TimePeriod.ALL -> "All"
    }

private val TimePeriod.calendarIcon
    get() = when (this) {
        TimePeriod.DAY -> Icons.Outlined.CalendarViewDay
        TimePeriod.WEEK -> Icons.Outlined.CalendarViewWeek
        TimePeriod.MONTH -> Icons.Outlined.CalendarMonth
        TimePeriod.YEAR -> Icons.Outlined.DateRange
        TimePeriod.ALL -> Icons.Outlined.CalendarToday
    }
