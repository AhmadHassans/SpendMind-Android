package com.spendmindai.app.features.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendmindai.app.core.data.repository.CategoryRepository
import com.spendmindai.app.core.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.seedDefaultCategories()
        }
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
    }

    fun addCategory(name: String, emoji: String, colorName: String) {
        viewModelScope.launch {
            categoryRepository.insertCategory(
                Category(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    emoji = emoji,
                    colorName = colorName,
                    isCustom = true
                )
            )
        }
    }

    fun toggleEnabled(category: Category) {
        viewModelScope.launch {
            categoryRepository.updateCategory(
                category.copy(isEnabled = !category.isEnabled, updatedAt = Date())
            )
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { categoryRepository.deleteCategory(category) }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category.copy(updatedAt = Date()))
        }
    }

    fun saveEnabledStates(enabledIds: Set<String>) {
        viewModelScope.launch {
            _uiState.value.categories.forEach { cat ->
                val shouldBeEnabled = cat.id in enabledIds
                if (cat.isEnabled != shouldBeEnabled) {
                    categoryRepository.updateCategory(cat.copy(isEnabled = shouldBeEnabled, updatedAt = Date()))
                }
            }
        }
    }
}
