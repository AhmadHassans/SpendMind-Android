package com.spendmindai.app.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendmindai.app.core.data.repository.CategoryRepository
import com.spendmindai.app.core.data.repository.ExpenseRepository
import com.spendmindai.app.core.data.repository.UserPreferencesRepository
import com.spendmindai.app.core.domain.model.UserPreferences
import com.spendmindai.app.core.infrastructure.billing.BillingService
import com.spendmindai.app.core.infrastructure.export.DataExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val dataExportService: DataExportService,
    private val billingService: BillingService
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences?> = userPreferencesRepository
        .getUserPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val isPremium: StateFlow<Boolean> = billingService.isPremium

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateCurrency(currency)
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateLanguage(language)
        }
    }

    fun updateMonthStartDay(day: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateMonthStartDay(day)
        }
    }

    fun updateNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val prefs = userPreferencesRepository.getUserPreferencesOnce()
            userPreferencesRepository.updateNotificationSettings(
                enabled = enabled,
                hour = prefs.notificationHour,
                minute = prefs.notificationMinute
            )
        }
    }

    fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val prefs = userPreferencesRepository.getUserPreferencesOnce()
            userPreferencesRepository.updateNotificationSettings(
                enabled = prefs.notificationEnabled,
                hour = hour,
                minute = minute
            )
        }
    }

    fun updateAutoSubmit(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateAutoSubmitTranscription(enabled)
        }
    }

    suspend fun exportExpensesAsCSV(): String = withContext(Dispatchers.IO) {
        val expenses = expenseRepository.getAllExpenses().first()
        val categories = categoryRepository.getAllCategoriesOnce()
        val categoryMap = categories.associateBy { it.id }
        dataExportService.exportToCSV(expenses, categoryMap)
    }

    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        expenseRepository.deleteAllExpenses()
    }
}
