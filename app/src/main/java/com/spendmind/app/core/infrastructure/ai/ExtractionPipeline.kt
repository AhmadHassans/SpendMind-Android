package com.spendmindai.app.core.infrastructure.ai

import com.spendmindai.app.core.domain.model.Category
import com.spendmindai.app.core.domain.model.ParsedExpense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtractionPipeline @Inject constructor(
    private val llmExtractor: LLMExtractor,
    private val categoryMatchingService: CategoryMatchingService
) {
    /**
     * Orchestrates the full pipeline:
     * transcript → LLMExtractor → CategoryMatchingService → List<ParsedExpense>
     *
     * @param transcript Raw voice transcript to extract expenses from.
     * @param currency   ISO 4217 currency code used as the default (e.g. "USD").
     * @param availableCategories List of Category objects the user has configured.
     * @return [Result] wrapping the list of parsed expenses, or an exception on failure.
     */
    suspend fun process(
        transcript: String,
        currency: String = "USD",
        availableCategories: List<Category> = emptyList()
    ): Result<List<ParsedExpense>> = withContext(Dispatchers.IO) {
        runCatching {
            if (transcript.isBlank()) {
                return@runCatching emptyList()
            }

            // Step 1 – extract raw expenses via the LLM
            val rawExpenses = llmExtractor.extractExpenses(transcript, currency)

            // Step 2 – resolve each expense's categoryName to an actual Category
            rawExpenses.map { expense ->
                if (availableCategories.isNotEmpty() && expense.categoryName != null) {
                    val matchedCategory = categoryMatchingService.findBestMatch(
                        categoryName = expense.categoryName,
                        availableCategories = availableCategories
                    )
                    expense.copy(categoryName = matchedCategory?.name)
                } else {
                    expense
                }
            }
        }
    }
}
