package com.spendmindai.app.features.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CalendarViewDay
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendmindai.app.core.domain.model.Category
import com.spendmindai.app.core.domain.model.Expense
import com.spendmindai.app.shared.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Locale
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

private fun categoryColor(name: String): Color =
    pastelPalette[Math.abs(name.hashCode()) % pastelPalette.size]

// ── Main screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    viewModel: ChartsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPeriodMenu by remember { mutableStateOf(false) }
    var showExpenseSheet by remember { mutableStateOf(false) }

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

        // Tappable chart area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(enabled = uiState.categoryChartData.isNotEmpty()) {
                    showExpenseSheet = true
                },
            contentAlignment = Alignment.Center
        ) {
            DonutChartWithCenter(
                data = uiState.categoryChartData,
                totalAmount = uiState.totalAmount,
                currency = uiState.currency,
                modifier = Modifier.size(300.dp)
            )

            // Tap hint when data present
            if (uiState.categoryChartData.isNotEmpty()) {
                Text(
                    text = "Tap to see breakdown",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFAAAAAA),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }

    if (showExpenseSheet) {
        ExpenseByCategorySheet(
            expenses   = uiState.filteredExpenses,
            categories = uiState.categories,
            currency   = uiState.currency,
            onDismiss  = { showExpenseSheet = false }
        )
    }
}

// ── Donut chart ───────────────────────────────────────────────────────────────

@Composable
private fun DonutChartWithCenter(
    data: List<CategoryChartData>,
    totalAmount: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {

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

        if (data.isNotEmpty()) {
            val total = data.sumOf { it.amount }
            var startAngleDeg = -90.0
            data.forEachIndexed { _, item ->
                val sweep = (item.amount / total) * 360.0
                val midAngleDeg = startAngleDeg + sweep / 2.0
                val midAngleRad = Math.toRadians(midAngleDeg)
                if (item.percentage >= 4.0) {
                    val iconRadius = 118.0
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
                            Text(text = item.emoji, fontSize = 18.sp)
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

// ── Expenses by category bottom sheet ────────────────────────────────────────

private data class CategoryGroup(
    val name: String,
    val emoji: String,
    val totalAmount: Double,
    val expenses: List<Expense>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseByCategorySheet(
    expenses:   List<Expense>,
    categories: List<Category>,
    currency:   String,
    onDismiss:  () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val groups = remember(expenses, categories) {
        val catMap = categories.associateBy { it.id }
        expenses
            .groupBy { e ->
                val cat = catMap[e.categoryId]
                Pair(cat?.name ?: "Other", cat?.emoji ?: "📝")
            }
            .map { (key, exps) ->
                CategoryGroup(
                    name        = key.first,
                    emoji       = key.second,
                    totalAmount = exps.sumOf { it.amount },
                    expenses    = exps.sortedByDescending { it.date }
                )
            }
            .sortedByDescending { it.totalAmount }
    }

    var expandedCategories by remember { mutableStateOf(setOf<String>()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape            = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor   = Color(0xFFF5F5F7),
        dragHandle       = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.88f)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = "Expenses by Category",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF555555))
                }
            }

            HorizontalDivider(color = Color(0xFFE5E5EA))

            if (groups.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text  = "No expenses in this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF888888)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp)
                ) {
                    items(groups, key = { it.name }) { group ->
                        val isExpanded = group.name in expandedCategories
                        CategorySection(
                            group      = group,
                            currency   = currency,
                            isExpanded = isExpanded,
                            onToggle   = {
                                expandedCategories = if (isExpanded)
                                    expandedCategories - group.name
                                else
                                    expandedCategories + group.name
                            }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

// ── Category section card ─────────────────────────────────────────────────────

@Composable
private fun CategorySection(
    group:      CategoryGroup,
    currency:   String,
    isExpanded: Boolean,
    onToggle:   () -> Unit
) {
    val bgColor = categoryColor(group.name)
    val count   = group.expenses.size

    Column {
        // Category header card
        Surface(
            modifier        = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle),
            shape           = RoundedCornerShape(16.dp),
            color           = Color.White,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier              = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Emoji circle
                Box(
                    modifier         = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(group.emoji, fontSize = 24.sp)
                }

                // Name + count
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = group.name,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        text  = "$count expense${if (count == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF888888)
                    )
                }

                // Total + chevron
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text       = CurrencyFormatter.format(group.totalAmount, currency),
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector        = if (isExpanded) Icons.Filled.KeyboardArrowUp
                                            else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint               = Color(0xFF888888),
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Expanded individual expenses
        AnimatedVisibility(
            visible = isExpanded,
            enter   = expandVertically(),
            exit    = shrinkVertically()
        ) {
            Column(
                modifier            = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.expenses.forEach { expense ->
                    ExpenseRow(expense = expense, currency = currency)
                }
            }
        }
    }
}

// ── Individual expense row ────────────────────────────────────────────────────

@Composable
private fun ExpenseRow(expense: Expense, currency: String) {
    val dayFmt   = remember { SimpleDateFormat("d",    Locale.getDefault()) }
    val monFmt   = remember { SimpleDateFormat("MMM",  Locale.getDefault()) }
    val timeFmt  = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(12.dp),
        color           = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date box
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF2F2F7)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = dayFmt.format(expense.date),
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color(0xFF1C1C1E)
                    )
                    Text(
                        text     = monFmt.format(expense.date),
                        fontSize = 10.sp,
                        color    = Color(0xFF8E8E93)
                    )
                }
            }

            // Note / description
            Text(
                text     = expense.note?.takeIf { it.isNotBlank() } ?: "Expense",
                style    = MaterialTheme.typography.bodyMedium,
                color    = if (expense.note.isNullOrBlank()) Color(0xFF8E8E93) else Color(0xFF1C1C1E),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Amount + time
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = CurrencyFormatter.format(expense.amount, currency),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF1C1C1E)
                )
                Text(
                    text     = timeFmt.format(expense.date),
                    fontSize = 11.sp,
                    color    = Color(0xFF8E8E93)
                )
            }
        }
    }
}

// ── Period label / icon extensions ───────────────────────────────────────────

private val TimePeriod.chartLabel: String
    get() = when (this) {
        TimePeriod.DAY   -> "Day"
        TimePeriod.WEEK  -> "Week"
        TimePeriod.MONTH -> "Month"
        TimePeriod.YEAR  -> "Year"
        TimePeriod.ALL   -> "All"
    }

private val TimePeriod.calendarIcon
    get() = when (this) {
        TimePeriod.DAY   -> Icons.Outlined.CalendarViewDay
        TimePeriod.WEEK  -> Icons.Outlined.CalendarViewWeek
        TimePeriod.MONTH -> Icons.Outlined.CalendarMonth
        TimePeriod.YEAR  -> Icons.Outlined.DateRange
        TimePeriod.ALL   -> Icons.Outlined.CalendarToday
    }
