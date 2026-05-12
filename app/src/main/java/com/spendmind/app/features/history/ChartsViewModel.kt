package com.spendmindai.app.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendmindai.app.core.data.repository.CategoryRepository
import com.spendmindai.app.core.data.repository.ExpenseRepository
import com.spendmindai.app.core.data.repository.UserPreferencesRepository
import com.spendmindai.app.core.domain.model.Category
import com.spendmindai.app.core.domain.model.Expense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class CategoryChartData(
    val name: String,
    val emoji: String,
    val amount: Double,
    val percentage: Double
)

data class ChartsUiState(
    val categoryChartData: List<CategoryChartData> = emptyList(),
    val expensesByCategory: Map<String, Double> = emptyMap(),
    val filteredExpenses: List<Expense> = emptyList(),
    val weeklyTotals: List<Pair<String, Double>> = emptyList(),
    val totalAmount: Double = 0.0,
    val currency: String = "USD",
    val selectedPeriod: TimePeriod = TimePeriod.MONTH,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val monthOffset: Int = 0,
    val dateRangeLabel: String = ""
)

@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(TimePeriod.MONTH)
    private val _monthOffset = MutableStateFlow(0)

    private val _uiState = MutableStateFlow(ChartsUiState())
    val uiState: StateFlow<ChartsUiState> = _uiState.asStateFlow()

    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                expenseRepository.getAllExpenses(),
                categoryRepository.getAllCategories(),
                userPreferencesRepository.getUserPreferences(),
                _selectedPeriod,
                _monthOffset
            ) { expenses, categories, prefs, period, monthOffset ->
                val (startDate, endDate) = periodRange(period, monthOffset)
                val filtered = expenses.filter { e ->
                    (startDate == null || e.date >= startDate) &&
                    (endDate == null || e.date <= endDate)
                }

                val categoryMap = categories.associateBy { it.id }
                val byCategory = filtered
                    .groupBy { e -> categoryMap[e.categoryId]?.name ?: "Other" }
                    .mapValues { (_, list) -> list.sumOf { it.amount } }
                    .entries
                    .sortedByDescending { it.value }
                    .associate { it.key to it.value }

                val total = filtered.sumOf { it.amount }

                val chartData = byCategory.entries.map { (name, amount) ->
                    val cat = categories.find { it.name == name }
                    CategoryChartData(
                        name = name,
                        emoji = cat?.emoji ?: "📝",
                        amount = amount,
                        percentage = if (total > 0) (amount / total) * 100.0 else 0.0
                    )
                }

                val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
                val weekly = buildList {
                    repeat(7) { daysAgo ->
                        val dayStart = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -6 + daysAgo)
                            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }.time
                        val dayEnd = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -6 + daysAgo)
                            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                        }.time
                        val dayTotal = expenses
                            .filter { e -> e.date >= dayStart && e.date <= dayEnd }
                            .sumOf { it.amount }
                        add(dayFormatter.format(dayStart) to dayTotal)
                    }
                }

                ChartsUiState(
                    categoryChartData = chartData,
                    expensesByCategory = byCategory,
                    filteredExpenses = filtered,
                    weeklyTotals = weekly,
                    totalAmount = total,
                    currency = prefs.currency,
                    selectedPeriod = period,
                    categories = categories,
                    isLoading = false,
                    monthOffset = monthOffset,
                    dateRangeLabel = dateRangeLabel(period, monthOffset)
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    private fun periodRange(period: TimePeriod, monthOffset: Int): Pair<Date?, Date?> = when (period) {
        TimePeriod.DAY -> {
            val startCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, monthOffset)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, monthOffset)
                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
            }
            startCal.time to endCal.time
        }
        TimePeriod.WEEK -> {
            val start = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
            start to null
        }
        TimePeriod.MONTH -> {
            val startCal = Calendar.getInstance().apply {
                add(Calendar.MONTH, monthOffset)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                add(Calendar.MONTH, monthOffset)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
            }
            startCal.time to endCal.time
        }
        TimePeriod.YEAR -> {
            val start = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.time
            start to null
        }
        TimePeriod.ALL -> null to null
    }

    private fun dateRangeLabel(period: TimePeriod, monthOffset: Int): String {
        val dayMonFmt = SimpleDateFormat("dd MMM", Locale.getDefault())
        return when (period) {
            TimePeriod.DAY -> {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, monthOffset) }
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
            }
            TimePeriod.MONTH -> {
                val cal = Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }
                val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                "01 $monthName - $maxDay $monthName"
            }
            TimePeriod.WEEK -> {
                val endCal = Calendar.getInstance()
                val startCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }
                "${dayMonFmt.format(startCal.time)} - ${dayMonFmt.format(endCal.time)}"
            }
            TimePeriod.YEAR -> {
                val year = Calendar.getInstance().get(Calendar.YEAR)
                "Jan $year - Dec $year"
            }
            TimePeriod.ALL -> "All Time"
        }
    }

    fun selectPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
        _monthOffset.value = 0
    }

    fun navigateMonth(delta: Int) {
        _monthOffset.value += delta
    }
}
