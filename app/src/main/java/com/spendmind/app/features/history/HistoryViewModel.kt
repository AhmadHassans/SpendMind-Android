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

enum class TimePeriod { DAY, WEEK, MONTH, YEAR, ALL }

private data class FilterState(
    val period: TimePeriod = TimePeriod.MONTH,
    val monthOffset: Int = 0,
    val categoryId: String? = null
)

data class HistoryUiState(
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val totalAmount: Double = 0.0,
    val categoryTotals: Map<String, Double> = emptyMap(),
    val currency: String = "USD",
    val selectedPeriod: TimePeriod = TimePeriod.MONTH,
    val monthOffset: Int = 0,
    val dateRangeLabel: String = "",
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    private val _searchQuery = MutableStateFlow("")

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                expenseRepository.getAllExpenses(),
                categoryRepository.getAllCategories(),
                userPreferencesRepository.getUserPreferences(),
                _filterState,
                _searchQuery
            ) { expenses, categories, prefs, fs, query ->
                // Period+search filtered, no category filter — used for per-category totals
                val periodFiltered = filterExpenses(expenses, fs.period, fs.monthOffset, query, null)
                val categoryTotals = categories.associate { cat ->
                    cat.id to periodFiltered.filter { it.categoryId == cat.id }.sumOf { it.amount }
                }
                val filtered = if (fs.categoryId != null)
                    periodFiltered.filter { it.categoryId == fs.categoryId }
                else periodFiltered
                HistoryUiState(
                    expenses = filtered,
                    categories = categories,
                    totalAmount = filtered.sumOf { it.amount },
                    categoryTotals = categoryTotals,
                    currency = prefs.currency,
                    selectedPeriod = fs.period,
                    monthOffset = fs.monthOffset,
                    dateRangeLabel = dateRangeLabel(fs.period, fs.monthOffset),
                    searchQuery = query,
                    selectedCategoryId = fs.categoryId
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    private fun filterExpenses(
        expenses: List<Expense>,
        period: TimePeriod,
        monthOffset: Int,
        query: String,
        categoryId: String?
    ): List<Expense> {
        val (startDate, endDate) = periodRange(period, monthOffset)
        return expenses
            .filter { e -> startDate == null || e.date >= startDate }
            .filter { e -> endDate == null || e.date <= endDate }
            .filter { e -> categoryId == null || e.categoryId == categoryId }
            .filter { e ->
                query.isBlank() ||
                    e.note?.contains(query, ignoreCase = true) == true ||
                    e.transcript?.contains(query, ignoreCase = true) == true
            }
            .sortedByDescending { it.date }
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
            val offset = monthOffset * 7
            val startCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6 + offset) }
            val endCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
            startCal.time to endCal.time
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
            val startCal = Calendar.getInstance().apply {
                add(Calendar.YEAR, monthOffset)
                set(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                add(Calendar.YEAR, monthOffset)
                set(Calendar.DAY_OF_YEAR, getActualMaximum(Calendar.DAY_OF_YEAR))
                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
            }
            startCal.time to endCal.time
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
                val offset = monthOffset * 7
                val endCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
                val startCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6 + offset) }
                "${dayMonFmt.format(startCal.time)} - ${dayMonFmt.format(endCal.time)}"
            }
            TimePeriod.YEAR -> {
                val cal = Calendar.getInstance().apply { add(Calendar.YEAR, monthOffset) }
                val year = cal.get(Calendar.YEAR)
                "Jan $year - Dec $year"
            }
            TimePeriod.ALL -> "All Time"
        }
    }

    fun selectPeriod(period: TimePeriod) {
        _filterState.value = FilterState(period = period, monthOffset = 0)
    }

    fun navigateMonth(delta: Int) {
        _filterState.value = _filterState.value.copy(monthOffset = _filterState.value.monthOffset + delta)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(categoryId: String?) {
        _filterState.value = _filterState.value.copy(categoryId = categoryId)
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
        }
    }
}
