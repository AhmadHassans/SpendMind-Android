package com.spendmindai.app.features.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendmindai.app.core.data.repository.CategoryRepository
import com.spendmindai.app.core.data.repository.ExpenseRepository
import com.spendmindai.app.core.domain.model.Expense
import com.spendmindai.app.core.domain.model.ParsedExpense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class ReviewItem(
    val parsed: ParsedExpense,
    val isAccepted: Boolean = true
)

data class ReviewUiState(
    val items: List<ReviewItem> = emptyList(),
    val currency: String = "USD",
    val isSaving: Boolean = false,
    val isDone: Boolean = false
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun setPendingExpenses(expenses: List<ParsedExpense>, currency: String) {
        _uiState.value = ReviewUiState(
            items = expenses.map { ReviewItem(it) },
            currency = currency
        )
    }

    fun toggleItem(index: Int) {
        val items = _uiState.value.items.toMutableList()
        if (index in items.indices) {
            items[index] = items[index].copy(isAccepted = !items[index].isAccepted)
            _uiState.update { it.copy(items = items) }
        }
    }

    fun acceptAll() {
        _uiState.update { it.copy(items = it.items.map { item -> item.copy(isAccepted = true) }) }
    }

    fun rejectAll() {
        _uiState.update { it.copy(items = it.items.map { item -> item.copy(isAccepted = false) }) }
    }

    fun saveAccepted() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val categories = categoryRepository.getAllCategoriesOnce()
            _uiState.value.items.filter { it.isAccepted }.forEach { item ->
                val cat = categories.find { it.name.equals(item.parsed.categoryName, ignoreCase = true) }
                expenseRepository.insertExpense(
                    Expense(
                        id = UUID.randomUUID().toString(),
                        amount = item.parsed.amount,
                        currency = _uiState.value.currency,
                        date = item.parsed.date,
                        confidence = item.parsed.confidence,
                        source = "voice",
                        note = item.parsed.note,
                        categoryId = cat?.id,
                        updatedAt = Date()
                    )
                )
            }
            _uiState.update { it.copy(isSaving = false, isDone = true) }
        }
    }
}
