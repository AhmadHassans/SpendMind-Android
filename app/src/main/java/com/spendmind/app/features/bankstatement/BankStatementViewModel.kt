package com.spendmindai.app.features.bankstatement

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendmindai.app.core.data.network.OpenAIApi
import com.spendmindai.app.core.data.repository.CategoryRepository
import com.spendmindai.app.core.data.repository.ExpenseRepository
import com.spendmindai.app.core.infrastructure.deduplication.DeduplicationService
import com.spendmindai.app.core.infrastructure.pdf.CSVTextExtractor
import com.spendmindai.app.core.infrastructure.pdf.PDFTextExtractor
import com.spendmindai.app.core.domain.model.Expense
import com.spendmindai.app.core.data.network.ChatRequest
import com.spendmindai.app.core.data.network.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Date
import java.util.UUID
import javax.inject.Inject

enum class ImportStep { IDLE, EXTRACTING_TEXT, DISCOVERING_SCHEMA, EXTRACTING_TRANSACTIONS, MAPPING_CATEGORIES, REVIEWING, IMPORTING, COMPLETE, ERROR }

data class ImportableTransaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val currency: String,
    val date: Date,
    val description: String,
    val categoryName: String?,
    val isDebit: Boolean = true,
    var isSelected: Boolean = true
)

data class BankImportUiState(
    val step: ImportStep = ImportStep.IDLE,
    val progress: Float = 0f,
    val statusMessage: String = "",
    val transactions: List<ImportableTransaction> = emptyList(),
    val importedCount: Int = 0,
    val error: String? = null,
    val fileName: String = ""
)

@HiltViewModel
class BankStatementViewModel @Inject constructor(
    private val pdfExtractor: PDFTextExtractor,
    private val csvExtractor: CSVTextExtractor,
    private val openAIApi: OpenAIApi,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val deduplicationService: DeduplicationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BankImportUiState())
    val uiState: StateFlow<BankImportUiState> = _uiState.asStateFlow()

    fun processFile(uri: Uri, fileName: String, mimeType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(step = ImportStep.EXTRACTING_TEXT, statusMessage = "Reading file...", fileName = fileName, progress = 0.1f) }

            val textResult = when {
                mimeType.contains("pdf") || fileName.endsWith(".pdf", true) -> pdfExtractor.extractText(uri)
                mimeType.contains("csv") || fileName.endsWith(".csv", true) -> csvExtractor.extractText(uri)
                else -> Result.failure(Exception("Unsupported file format. Please use PDF or CSV."))
            }

            textResult.fold(
                onSuccess = { text -> discoverSchema(text) },
                onFailure = { error -> _uiState.update { it.copy(step = ImportStep.ERROR, error = error.message) } }
            )
        }
    }

    private suspend fun discoverSchema(text: String) {
        _uiState.update { it.copy(step = ImportStep.DISCOVERING_SCHEMA, statusMessage = "Analyzing statement format...", progress = 0.3f) }

        val schemaPrompt = """Analyze this bank statement text and return a JSON object with:
{
  "dateColumn": "column name or index for dates",
  "amountColumn": "column name for amounts",
  "descriptionColumn": "column name for descriptions",
  "currency": "detected currency code",
  "dateFormat": "detected date format"
}
Text sample (first 2000 chars): ${text.take(2000)}
Return only the JSON object."""

        try {
            val schemaResponse = openAIApi.chatCompletion(
                ChatRequest(
                    model = "gpt-4o",
                    messages = listOf(Message("user", schemaPrompt)),
                    temperature = 0.1,
                    maxTokens = 300
                )
            )
            val schemaContent = schemaResponse.choices?.firstOrNull()?.message?.content ?: ""
            extractTransactions(text, schemaContent)
        } catch (e: Exception) {
            _uiState.update { it.copy(step = ImportStep.ERROR, error = "Failed to analyze statement: ${e.message}") }
        }
    }

    private suspend fun extractTransactions(text: String, schema: String) {
        _uiState.update { it.copy(step = ImportStep.EXTRACTING_TRANSACTIONS, statusMessage = "Extracting transactions...", progress = 0.5f) }

        val chunks = text.chunked(3000)
        val allTransactions = mutableListOf<ImportableTransaction>()

        chunks.take(5).forEachIndexed { index, chunk ->
            _uiState.update { it.copy(progress = 0.5f + (index.toFloat() / chunks.size.coerceAtLeast(1)) * 0.3f) }

            val extractPrompt = """Extract transactions from this bank statement chunk. Schema: $schema
Return a JSON array of objects: [{"amount": number, "date": "YYYY-MM-DD", "description": "text", "isDebit": boolean, "currency": "code"}]
Only debits/expenses. Skip credits/income. Chunk: $chunk
Return only the JSON array."""

            try {
                val response = openAIApi.chatCompletion(
                    ChatRequest(
                        model = "gpt-4o",
                        messages = listOf(Message("user", extractPrompt)),
                        temperature = 0.1,
                        maxTokens = 1000
                    )
                )
                val content = response.choices?.firstOrNull()?.message?.content?.trim() ?: "[]"
                val clean = content.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val arr = JSONArray(clean)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    allTransactions.add(
                        ImportableTransaction(
                            amount = obj.optDouble("amount", 0.0),
                            currency = obj.optString("currency", "USD"),
                            date = try {
                                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                    .parse(obj.optString("date")) ?: Date()
                            } catch (e: Exception) {
                                Date()
                            },
                            description = obj.optString("description", ""),
                            categoryName = null,
                            isDebit = obj.optBoolean("isDebit", true)
                        )
                    )
                }
            } catch (e: Exception) { /* skip bad chunk */ }
        }

        mapCategories(allTransactions)
    }

    private suspend fun mapCategories(transactions: List<ImportableTransaction>) {
        _uiState.update { it.copy(step = ImportStep.MAPPING_CATEGORIES, statusMessage = "Assigning categories...", progress = 0.85f) }

        val categories = categoryRepository.getAllCategoriesOnce()
        val categoryNames = categories.map { it.name }

        val descriptions = transactions.take(50).joinToString("\n") { "${it.id}: ${it.description}" }
        val mapPrompt = """Map these transaction descriptions to categories from this list: ${categoryNames.joinToString(", ")}
Descriptions:
$descriptions
Return JSON: {"transactionId": "categoryName"} for each item. Use closest matching category."""

        val mapped = try {
            val response = openAIApi.chatCompletion(
                ChatRequest(
                    model = "gpt-4o",
                    messages = listOf(Message("user", mapPrompt)),
                    temperature = 0.1,
                    maxTokens = 800
                )
            )
            val content = response.choices?.firstOrNull()?.message?.content?.trim() ?: "{}"
            val clean = content.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = org.json.JSONObject(clean)
            transactions.map { t ->
                t.copy(categoryName = obj.optString(t.id).takeIf { it.isNotEmpty() })
            }
        } catch (e: Exception) {
            transactions
        }

        _uiState.update {
            it.copy(
                step = ImportStep.REVIEWING,
                transactions = mapped,
                progress = 1f,
                statusMessage = "Review ${mapped.size} transactions"
            )
        }
    }

    fun toggleTransaction(id: String) {
        _uiState.update { state ->
            state.copy(transactions = state.transactions.map {
                if (it.id == id) it.copy(isSelected = !it.isSelected) else it
            })
        }
    }

    fun selectAll() {
        _uiState.update { state ->
            state.copy(transactions = state.transactions.map { it.copy(isSelected = true) })
        }
    }

    fun deselectAll() {
        _uiState.update { state ->
            state.copy(transactions = state.transactions.map { it.copy(isSelected = false) })
        }
    }

    fun confirmImport() {
        viewModelScope.launch {
            _uiState.update { it.copy(step = ImportStep.IMPORTING, statusMessage = "Saving transactions...") }
            val categories = categoryRepository.getAllCategoriesOnce()
            var imported = 0

            _uiState.value.transactions.filter { it.isSelected }.forEach { tx ->
                val importId = "${tx.amount}_${tx.date.time}_${tx.description.hashCode()}"
                if (!deduplicationService.isDuplicate(tx.amount, tx.date, tx.categoryName, importId = importId)) {
                    val category = categories.find { it.name.equals(tx.categoryName, ignoreCase = true) }
                    expenseRepository.insertExpense(
                        Expense(
                            id = UUID.randomUUID().toString(),
                            amount = tx.amount,
                            currency = tx.currency,
                            date = tx.date,
                            confidence = 0.8,
                            source = "bank_import",
                            note = tx.description,
                            categoryId = category?.id,
                            importId = importId,
                            updatedAt = Date()
                        )
                    )
                    imported++
                }
            }
            _uiState.update {
                it.copy(
                    step = ImportStep.COMPLETE,
                    importedCount = imported,
                    statusMessage = "Imported $imported transactions"
                )
            }
        }
    }

    fun reset() {
        _uiState.value = BankImportUiState()
    }
}
