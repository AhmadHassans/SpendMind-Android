package com.spendmindai.app.features.record

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class ManualExpenseUiState(
    val amountText: String = "",
    val note: String = "",
    val selectedCategory: Category? = null,
    val date: Date = Date(),
    val currency: String = "USD",
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ManualExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualExpenseUiState())
    val uiState: StateFlow<ManualExpenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.seedDefaultCategories()
        }
        viewModelScope.launch {
            userPreferencesRepository.getUserPreferences().collect { prefs ->
                _uiState.update { it.copy(currency = prefs.currency) }
            }
        }
        viewModelScope.launch {
            categoryRepository.getEnabledCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
    }

    fun setAmount(text: String) {
        _uiState.update { it.copy(amountText = text, error = null) }
    }

    fun setNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun setCategory(category: Category?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setDate(date: Date) {
        _uiState.update { it.copy(date = date) }
    }

    fun save() {
        val amount = _uiState.value.amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            expenseRepository.insertExpense(
                Expense(
                    id = UUID.randomUUID().toString(),
                    amount = amount,
                    currency = _uiState.value.currency,
                    date = _uiState.value.date,
                    note = _uiState.value.note.takeIf { it.isNotBlank() },
                    categoryId = _uiState.value.selectedCategory?.id,
                    source = "manual",
                    updatedAt = Date()
                )
            )
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}
