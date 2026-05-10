package com.spendmindai.app.features.record

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.spendmindai.app.features.settings.SettingsScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.spendmindai.app.core.domain.model.Category
import com.spendmindai.app.features.history.ChartsScreen
import com.spendmindai.app.features.history.ChartsViewModel
import com.spendmindai.app.features.history.HistoryViewModel
import com.spendmindai.app.features.history.TimePeriod
import com.spendmindai.app.features.history.components.ExpenseListItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Curved tab bar shape — center cutout for floating mic (matches iOS TabBarCurve)
// ─────────────────────────────────────────────────────────────────────────────
private object TabBarCurveShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val h    = size.height * 0.72f
        val midX = size.width / 2f
        val path = Path()
        path.moveTo(0f, 0f)
        path.lineTo(midX * 0.50f, 0f)
        path.cubicTo(midX * 0.80f, 0f, midX * 0.72f, h, midX, h)
        path.cubicTo(midX * 1.28f, h, midX * 1.20f, 0f, midX * 1.50f, 0f)
        path.lineTo(size.width, 0f)
        path.lineTo(size.width, size.height)
        path.lineTo(0f, size.height)
        path.close()
        return Outline.Generic(path)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab bar nav button
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TabBarButton(
    icon: ImageVector,
    iconSelected: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (isSelected) Color(0xFF1A1A1A) else Color(0xFF9E9E9E)
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(60.dp)
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector        = if (isSelected) iconSelected else icon,
            contentDescription = label,
            tint               = tint,
            modifier           = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = tint,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
        Spacer(Modifier.weight(1f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Center mic button — purple gradient idle, red recording, 3 sonar rings
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CenterMicButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue   = if (isRecording) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "mic_scale"
    )

    val micBrush = if (isRecording) {
        Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFE53935)))
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF9C77F5), Color(0xFF6366F1)),
            start  = Offset(0f, 0f),
            end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }
    val ringColor = if (isRecording) Color(0xFFE53935) else Color(0xFF8B5CF6)

    val pulse = rememberInfiniteTransition(label = "mic_pulse")
    val ring1 by pulse.animateFloat(
        initialValue  = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label         = "r1"
    )
    val ring2 by pulse.animateFloat(
        initialValue  = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 667, easing = LinearEasing), RepeatMode.Restart),
        label         = "r2"
    )
    val ring3 by pulse.animateFloat(
        initialValue  = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 1334, easing = LinearEasing), RepeatMode.Restart),
        label         = "r3"
    )

    Box(modifier = modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center    = Offset(size.width / 2f, size.height / 2f)
            val btnRadius = 32.dp.toPx()
            val maxRadius = size.minDimension / 2f
            listOf(ring1, ring2, ring3).forEach { progress ->
                val radius = btnRadius + (maxRadius - btnRadius) * progress
                val alpha  = 0.30f * (1f - progress)
                if (alpha > 0.005f)
                    drawCircle(color = ringColor.copy(alpha = alpha), radius = radius, center = center)
            }
        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(brush = micBrush)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (isRecording) "Stop" else "Record",
                tint               = Color.White,
                modifier           = Modifier.size(28.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Custom curved bottom tab bar (matches iOS CustomTabBar)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CustomTabBar(
    selectedTab: Int,
    isRecording: Boolean,
    onTabSelected: (Int) -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier          = modifier.fillMaxWidth().navigationBarsPadding(),
        contentAlignment  = Alignment.TopCenter
    ) {
        // Bar background with curve cutout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .align(Alignment.BottomCenter)
                .background(color = Color(0xFFF7F7F7), shape = TabBarCurveShape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .height(90.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabBarButton(
                    icon         = Icons.Outlined.Home,
                    iconSelected = Icons.Filled.Home,
                    label        = "Home",
                    isSelected   = selectedTab == 0,
                    onClick      = { onTabSelected(0) },
                    modifier     = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(120.dp))
                TabBarButton(
                    icon         = Icons.Outlined.PieChart,
                    iconSelected = Icons.Filled.PieChart,
                    label        = "Charts",
                    isSelected   = selectedTab == 1,
                    onClick      = { onTabSelected(1) },
                    modifier     = Modifier.weight(1f)
                )
            }
        }

        // Mic floats above bar in the curve cutout
        CenterMicButton(
            isRecording = isRecording,
            onClick     = onMicClick,
            modifier    = Modifier.offset(y = (-20).dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Date range nav bar at top (arrows + label, shared across tabs)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DateNavBar(
    label: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 4.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(
                imageVector        = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous",
                tint               = Color(0xFFAAAAAA),
                modifier           = Modifier.size(28.dp)
            )
        }
        Text(
            text       = label,
            fontWeight = FontWeight.Bold,
            fontSize   = 20.sp,
            color      = Color.Black,
            textAlign  = TextAlign.Center,
            maxLines   = 1,
            modifier   = Modifier.weight(1f)
        )
        IconButton(onClick = onNext) {
            Icon(
                imageVector        = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next",
                tint               = Color(0xFFAAAAAA),
                modifier           = Modifier.size(28.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main screen shell — matches iOS ContentView structure
//   TOP: date nav arrows
//   MIDDLE: tab content
//   BOTTOM: curved tab bar + floating mic
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    onNavigateToManualExpense: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToBankImport: () -> Unit = {}
) {
    var selectedTab      by remember { mutableStateOf(0) }
    var showRecord       by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    // Hoist both VMs here so date nav can read/write either one
    val historyVm: HistoryViewModel = hiltViewModel()
    val chartsVm: ChartsViewModel   = hiltViewModel()
    val historyState by historyVm.uiState.collectAsState()
    val chartsState  by chartsVm.uiState.collectAsState()

    val dateLabel = if (selectedTab == 0) historyState.dateRangeLabel else chartsState.dateRangeLabel
    val onPrev: () -> Unit = if (selectedTab == 0) ({ historyVm.navigateMonth(-1) }) else ({ chartsVm.navigateMonth(-1) })
    val onNext: () -> Unit = if (selectedTab == 0) ({ historyVm.navigateMonth(1) })  else ({ chartsVm.navigateMonth(1) })

    Scaffold(
        bottomBar = {
            CustomTabBar(
                selectedTab  = selectedTab,
                isRecording  = showRecord,
                onTabSelected = { selectedTab = it },
                onMicClick   = { showRecord = !showRecord }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                // MIDDLE: tab content fills remaining space
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> HomeContent(
                            viewModel                 = historyVm,
                            onNavigateToManualExpense = onNavigateToManualExpense,
                            onNavigateToSettings      = { showSettingsSheet = true }
                        )
                        1 -> ChartsScreen(viewModel = chartsVm)
                    }
                }

                // ABOVE BOTTOM NAV: date range with arrows (matches iOS ChartsScene bottom HStack)
                DateNavBar(label = dateLabel, onPrev = onPrev, onNext = onNext)
            }

            // RecordScreen full-screen overlay
            if (showRecord) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    RecordScreen(onDismiss = { showRecord = false })
                }
            }
        }
    }

    // Settings sheet — opens as bottom sheet instead of full navigation
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            SettingsScreen(onNavigateBack = { showSettingsSheet = false })
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Home tab content (iOS RecordScene / LandingExpenseView)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HomeContent(
    onNavigateToManualExpense: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState        by viewModel.uiState.collectAsState()
    var showPeriodMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Top-right: + and Settings circular buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp).clip(CircleShape).background(Color.Black)
                    .clickable { onNavigateToManualExpense() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, "Add expense", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp).clip(CircleShape).background(Color.Black)
                    .clickable { onNavigateToSettings() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Settings, "Settings", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // Large amount + currency
        Row(
            modifier          = Modifier.padding(start = 20.dp, top = 4.dp, end = 20.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text       = formatAmount(uiState.totalAmount),
                fontSize   = 68.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.Black,
                lineHeight = 72.sp
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = uiState.currency,
                fontSize   = 20.sp,
                color      = Color.Gray,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.padding(bottom = 10.dp)
            )
        }

        // Period chip "This Month ◇"
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.padding(start = 16.dp)) {
            Surface(
                shape    = RoundedCornerShape(20.dp),
                color    = Color(0xFFF2F2F7),
                modifier = Modifier.clickable { showPeriodMenu = true }
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = uiState.selectedPeriod.homeLabel,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color      = Color.Black
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.UnfoldMore, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                }
            }
            DropdownMenu(expanded = showPeriodMenu, onDismissRequest = { showPeriodMenu = false }) {
                TimePeriod.values().forEach { period ->
                    DropdownMenuItem(
                        text    = { Text(period.homeLabel) },
                        onClick = { viewModel.selectPeriod(period); showPeriodMenu = false }
                    )
                }
            }
        }

        // Category summary cards — horizontal scroll filter, matches iOS CategorySummaryCards
        if (uiState.categories.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            CategorySummaryCards(
                categories         = uiState.categories,
                categoryTotals     = uiState.categoryTotals,
                selectedCategoryId = uiState.selectedCategoryId,
                onCategorySelected = { id -> viewModel.setSelectedCategory(id) }
            )
        }

        Spacer(Modifier.height(8.dp))

        // Expense list or empty state
        if (uiState.expenses.isEmpty()) {
            HomeEmptyState()
        } else {
            val grouped = uiState.expenses.groupBy { dateGroupKey(it.date) }
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                grouped.forEach { (dateLabel, dayExpenses) ->
                    item(key = "header_$dateLabel") {
                        Text(
                            text       = dateLabel,
                            modifier   = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style      = MaterialTheme.typography.labelMedium,
                            color      = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(dayExpenses, key = { it.id }) { expense ->
                        val category = uiState.categories.find { it.id == expense.categoryId }
                        ExpenseListItem(
                            expense  = expense,
                            category = category,
                            currency = uiState.currency,
                            onClick  = {},
                            onDelete = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category colour palette — matches iOS Color.CategoryColors defaults
// ─────────────────────────────────────────────────────────────────────────────
private fun categoryCardColor(name: String): Color {
    val palette = listOf(
        Color(0xFFFF9500), Color(0xFFFF6584), Color(0xFF34C759),
        Color(0xFFAF52DE), Color(0xFF007AFF), Color(0xFFE5534B), Color(0xFF9E9E9E)
    )
    return when (name.lowercase().trim()) {
        "car", "transport", "transportation" -> Color(0xFFFF9500)
        "eating out", "food", "dining", "restaurant" -> Color(0xFFFF6584)
        "groceries", "grocery" -> Color(0xFF34C759)
        "luxury" -> Color(0xFFAF52DE)
        "shopping" -> Color(0xFF007AFF)
        "medicines", "medicine", "health", "pharmacy" -> Color(0xFFE5534B)
        else -> palette[Math.abs(name.hashCode()) % palette.size]
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Compact amount formatter for category cards: 1500 → "1.5K", 500 → "500", 0 → "—"
// ─────────────────────────────────────────────────────────────────────────────
private fun formatCompactAmount(amount: Double): String {
    if (amount == 0.0) return "—"
    return if (amount >= 1000) String.format("%.1fK", amount / 1000)
    else String.format("%.0f", amount)
}

// ─────────────────────────────────────────────────────────────────────────────
// Horizontal scrollable category filter cards — matches iOS CategorySummaryCards
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CategorySummaryCards(
    categories: List<Category>,
    categoryTotals: Map<String, Double>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit
) {
    // Sort by amount descending (categories with most spending first)
    val sorted = categories.sortedByDescending { categoryTotals[it.id] ?: 0.0 }

    LazyRow(
        contentPadding        = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sorted, key = { it.id }) { category ->
            val amount     = categoryTotals[category.id] ?: 0.0
            val isSelected = selectedCategoryId == category.id
            val color      = categoryCardColor(category.name)

            CategorySummaryCard(
                category   = category,
                amount     = amount,
                isSelected = isSelected,
                color      = color,
                onClick    = { onCategorySelected(if (isSelected) null else category.id) }
            )
        }
    }
}

@Composable
private fun CategorySummaryCard(
    category: Category,
    amount: Double,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue   = if (isSelected) 1.09f else 1f,
        animationSpec = tween(durationMillis = 200),
        label         = "cat_card_scale"
    )

    val bgColor     = if (isSelected) color.copy(alpha = 0.10f) else Color(0xFFF2F2F7)
    val borderColor = if (isSelected) color else Color.Transparent

    Column(
        modifier = Modifier
            .scale(scale)
            .width(104.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(width = if (isSelected) 2.dp else 0.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = if (isSelected) 0.40f else 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.emoji, fontSize = 24.sp)
        }
        Text(
            text      = category.name,
            fontSize  = 11.sp,
            fontWeight = FontWeight.Medium,
            color     = Color.Black,
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier  = Modifier.width(80.dp)
        )
        Text(
            text       = formatCompactAmount(amount),
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color.Black
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state illustration
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HomeEmptyState() {
    Column(
        modifier              = Modifier.fillMaxSize(),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        SpendingIllustration(modifier = Modifier.size(220.dp))
        Spacer(Modifier.height(24.dp))
        Text(
            text       = "Peace of mind starts here",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = Color.Black,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text      = "Tap the 🎤 mic button to record your first transaction.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = Color.Gray,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 40.dp)
        )
    }
}

@Composable
private fun SpendingIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height

        rotate(degrees = -35f, pivot = Offset(w * 0.17f, h * 0.65f)) {
            drawOval(color = Color(0xFFD81B60), topLeft = Offset(w * 0.02f, h * 0.28f), size = Size(w * 0.22f, h * 0.60f))
        }
        rotate(degrees = 18f, pivot = Offset(w * 0.24f, h * 0.62f)) {
            drawOval(color = Color(0xFF8E24AA), topLeft = Offset(w * 0.10f, h * 0.38f), size = Size(w * 0.20f, h * 0.46f))
        }

        val docL = w * 0.28f; val docT = h * 0.08f; val docW = w * 0.66f; val docH = h * 0.72f
        drawRoundRect(color = Color(0xFF29B6F6), topLeft = Offset(docL, docT), size = Size(docW, docH), cornerRadius = CornerRadius(18f))
        drawRoundRect(color = Color(0xFF4DD0E1), topLeft = Offset(docL, docT), size = Size(docW, docH * 0.22f), cornerRadius = CornerRadius(18f))
        val lineColor = Color(0xFF0288D1).copy(alpha = 0.35f)
        for (i in 1..4) {
            val rowY = docT + docH * (0.26f + i * 0.13f)
            drawLine(color = lineColor, start = Offset(docL + 14f, rowY), end = Offset(docL + docW - 14f, rowY), strokeWidth = 1.5f)
        }
        drawLine(color = lineColor, start = Offset(docL + docW * 0.52f, docT + docH * 0.28f), end = Offset(docL + docW * 0.52f, docT + docH - 14f), strokeWidth = 1.5f)

        val coinR = w * 0.085f
        listOf(Offset(w * 0.74f, h * 0.13f), Offset(w * 0.64f, h * 0.20f), Offset(w * 0.83f, h * 0.22f)).forEach { c ->
            drawCircle(color = Color(0xFFE65100), radius = coinR + 1f, center = c)
            drawCircle(color = Color(0xFFFFB300), radius = coinR, center = c)
            drawCircle(color = Color(0xFFFFCA28), radius = coinR * 0.72f, center = c)
            drawCircle(color = Color(0xFFFFB300), radius = coinR * 0.35f, center = c, style = Stroke(width = 3f))
        }

        val calcL = w * 0.04f; val calcT = h * 0.40f; val calcW = w * 0.46f; val calcH = h * 0.57f
        drawRoundRect(color = Color(0xFF1A237E), topLeft = Offset(calcL, calcT), size = Size(calcW, calcH), cornerRadius = CornerRadius(20f))
        drawRoundRect(color = Color(0xFFE53935), topLeft = Offset(calcL + calcW * 0.10f, calcT + calcH * 0.07f), size = Size(calcW * 0.80f, calcH * 0.17f), cornerRadius = CornerRadius(6f))
        drawRoundRect(color = Color(0xFFEF9A9A), topLeft = Offset(calcL + calcW * 0.54f, calcT + calcH * 0.09f), size = Size(calcW * 0.30f, calcH * 0.13f), cornerRadius = CornerRadius(4f))
        val bW = calcW * 0.16f; val bH = calcH * 0.09f; val bGapX = (calcW * 0.78f - 3 * bW) / 2f
        val bSX = calcL + calcW * 0.11f; val bSY = calcT + calcH * 0.30f; val bGY = calcH * 0.145f
        for (row in 0..3) for (col in 0..2) {
            drawRoundRect(
                color     = if (row == 3 && col == 2) Color(0xFFE53935) else Color(0xFF283593),
                topLeft   = Offset(bSX + col * (bW + bGapX), bSY + row * bGY),
                size      = Size(bW, bH),
                cornerRadius = CornerRadius(4f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun formatAmount(amount: Double): String {
    if (amount == 0.0) return "0"
    val l = amount.toLong()
    return if (l.toDouble() == amount) l.toString() else String.format(Locale.US, "%.2f", amount)
}

private fun dateGroupKey(date: Date): String {
    val cal   = Calendar.getInstance().apply { time = date }
    val today = Calendar.getInstance()
    return when {
        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1 -> "Yesterday"
        else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date)
    }
}

private val TimePeriod.homeLabel: String
    get() = when (this) {
        TimePeriod.DAY   -> "Today"
        TimePeriod.WEEK  -> "This Week"
        TimePeriod.MONTH -> "This Month"
        TimePeriod.YEAR  -> "This Year"
        TimePeriod.ALL   -> "All Time"
    }
