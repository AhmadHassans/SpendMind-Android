package com.spendmindai.app.features.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendmindai.app.core.data.repository.CategoryRepository
import com.spendmindai.app.core.data.repository.ExpenseRepository
import com.spendmindai.app.core.data.repository.UserPreferencesRepository
import com.spendmindai.app.core.domain.model.Expense
import com.spendmindai.app.core.domain.model.ParsedExpense
import com.spendmindai.app.core.infrastructure.ai.ExtractionPipeline
import com.spendmindai.app.core.infrastructure.speech.SpeechRecognitionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

enum class RecordState { IDLE, LISTENING, PROCESSING, REVIEW, ERROR }

data class RecordUiState(
    val state: RecordState = RecordState.IDLE,
    val transcript: String = "",
    val audioLevel: Float = 0f,
    val parsedExpenses: List<ParsedExpense> = emptyList(),
    val error: String? = null,
    val currency: String = "USD"
)

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val speechService: SpeechRecognitionService,
    private val extractionPipeline: ExtractionPipeline
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var currentLanguageCode: String = "en-US"

    init {
        viewModelScope.launch {
            userPreferencesRepository.getUserPreferences().collect { prefs ->
                _uiState.update { it.copy(currency = prefs.currency) }
                currentLanguageCode = prefs.language ?: "en-US"
            }
        }
        viewModelScope.launch {
            speechService.transcript.collect { transcript ->
                _uiState.update { it.copy(transcript = transcript) }
            }
        }
        viewModelScope.launch {
            speechService.audioLevel.collect { level ->
                _uiState.update { it.copy(audioLevel = level) }
            }
        }
    }

    fun startRecording() {
        _uiState.update { it.copy(state = RecordState.LISTENING, transcript = "", error = null) }
        speechService.startListening(
            languageCode = currentLanguageCode,
            onResult = { transcript -> processTranscript(transcript) },
            onError = { error -> _uiState.update { it.copy(state = RecordState.ERROR, error = error) } }
        )
    }

    fun stopRecording() {
        speechService.stopListening()
        val transcript = _uiState.value.transcript
        if (transcript.isNotEmpty()) {
            processTranscript(transcript)
        } else {
            _uiState.update { it.copy(state = RecordState.IDLE) }
        }
    }

    private fun processTranscript(transcript: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(state = RecordState.PROCESSING, transcript = transcript) }
            val availableCategories = categoryRepository.getEnabledCategories().let { flow ->
                // Collect once for the pipeline
                var result = emptyList<com.spendmindai.app.core.domain.model.Category>()
                val job = launch { flow.collect { result = it } }
                job.cancel()
                result
            }
            extractionPipeline.process(transcript, _uiState.value.currency, availableCategories).fold(
                onSuccess = { expenses ->
                    _uiState.update { it.copy(state = RecordState.REVIEW, parsedExpenses = expenses) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(state = RecordState.ERROR, error = error.message ?: "Unknown error") }
                }
            )
        }
    }

    fun confirmExpenses() {
        viewModelScope.launch {
            val categories = buildList {
                val job = launch {
                    categoryRepository.getAllCategories().collect { addAll(it) }
                }
                job.cancel()
            }
            _uiState.value.parsedExpenses.forEach { parsed ->
                val category = categories.find { it.name.equals(parsed.categoryName, ignoreCase = true) }
                val expense = Expense(
                    id = UUID.randomUUID().toString(),
                    amount = parsed.amount,
                    currency = parsed.currency,
                    date = parsed.date,
                    confidence = parsed.confidence,
                    source = "voice",
                    transcript = _uiState.value.transcript,
                    note = parsed.note,
                    categoryId = category?.id,
                    updatedAt = Date()
                )
                expenseRepository.insertExpense(expense)
            }
            _uiState.update {
                it.copy(
                    state = RecordState.IDLE,
                    parsedExpenses = emptyList(),
                    transcript = ""
                )
            }
        }
    }

    fun discardExpenses() {
        _uiState.update {
            it.copy(
                state = RecordState.IDLE,
                parsedExpenses = emptyList(),
                transcript = ""
            )
        }
    }

    fun resetError() {
        _uiState.update { it.copy(state = RecordState.IDLE, error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        speechService.destroy()
    }
}
