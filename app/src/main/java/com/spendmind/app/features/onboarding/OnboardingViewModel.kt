package com.spendmindai.app.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendmindai.app.core.data.repository.CategoryRepository
import com.spendmindai.app.core.data.repository.OnboardingRepository
import com.spendmindai.app.core.data.repository.UserPreferencesRepository
import com.spendmindai.app.core.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

enum class OnboardingStep { WELCOME, CATEGORIES, LANGUAGE, PERMISSIONS, COMPLETE }

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _currentStep = MutableStateFlow(OnboardingStep.WELCOME)
    val currentStep: StateFlow<OnboardingStep> = _currentStep

    private val _isComplete = MutableStateFlow(false)
    val isComplete: StateFlow<Boolean> = _isComplete

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories

    val availableCategories: List<Category> = Category.DEFAULT_CATEGORIES

    val isOnboardingComplete: Boolean get() = onboardingRepository.hasCompletedOnboarding()

    init {
        if (isOnboardingComplete) {
            _isComplete.value = true
        } else {
            determineStartStep()
        }
    }

    private fun determineStartStep() {
        _currentStep.value = when {
            !onboardingRepository.hasSeenWelcome() -> OnboardingStep.WELCOME
            !onboardingRepository.hasSeenCategories() -> OnboardingStep.CATEGORIES
            !onboardingRepository.hasSeenLanguage() -> OnboardingStep.LANGUAGE
            !onboardingRepository.hasSeenPermissions() -> OnboardingStep.PERMISSIONS
            else -> {
                completeOnboarding()
                OnboardingStep.COMPLETE
            }
        }
    }

    fun nextStep() {
        viewModelScope.launch {
            when (_currentStep.value) {
                OnboardingStep.WELCOME -> {
                    onboardingRepository.setHasSeenWelcome(true)
                    _currentStep.value = OnboardingStep.CATEGORIES
                }
                OnboardingStep.CATEGORIES -> {
                    onboardingRepository.setHasSeenCategories(true)
                    saveSelectedCategories()
                    applyCurrencyDefault()
                    _currentStep.value = OnboardingStep.LANGUAGE
                }
                OnboardingStep.LANGUAGE -> {
                    onboardingRepository.setHasSeenLanguage(true)
                    _currentStep.value = OnboardingStep.PERMISSIONS
                }
                OnboardingStep.PERMISSIONS -> {
                    onboardingRepository.setHasSeenPermissions(true)
                    completeOnboarding()
                }
                OnboardingStep.COMPLETE -> {}
            }
        }
    }

    fun previousStep() {
        _currentStep.value = when (_currentStep.value) {
            OnboardingStep.CATEGORIES -> OnboardingStep.WELCOME
            OnboardingStep.LANGUAGE -> OnboardingStep.CATEGORIES
            OnboardingStep.PERMISSIONS -> OnboardingStep.LANGUAGE
            else -> _currentStep.value
        }
    }

    fun toggleCategory(categoryName: String) {
        val current = _selectedCategories.value.toMutableSet()
        if (current.contains(categoryName)) current.remove(categoryName) else current.add(categoryName)
        _selectedCategories.value = current
    }

    fun saveSelectedCategories() {
        viewModelScope.launch {
            val toSeed = if (_selectedCategories.value.isEmpty()) {
                Category.DEFAULT_CATEGORIES
            } else {
                Category.DEFAULT_CATEGORIES.filter { it.name in _selectedCategories.value }
            }
            categoryRepository.insertCategories(toSeed)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateLanguage(language)
        }
    }

    private fun applyCurrencyDefault() {
        viewModelScope.launch {
            val currencyCode = try {
                java.util.Currency.getInstance(Locale.getDefault()).currencyCode
            } catch (e: Exception) {
                "USD"
            }
            userPreferencesRepository.updateCurrency(currencyCode)
        }
    }

    private fun completeOnboarding() {
        onboardingRepository.setHasCompletedOnboarding(true)
        _isComplete.value = true
        _currentStep.value = OnboardingStep.COMPLETE
    }
}
