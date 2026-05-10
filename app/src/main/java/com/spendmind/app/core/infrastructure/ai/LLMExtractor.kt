package com.spendmindai.app.core.infrastructure.ai

import com.spendmindai.app.core.data.network.OpenAIApi
import com.spendmindai.app.core.data.network.ChatRequest
import com.spendmindai.app.core.data.network.Message
import com.spendmindai.app.core.domain.model.ParsedExpense
import com.spendmindai.app.core.domain.model.ConfidenceDetails
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LLMExtractor @Inject constructor(
    private val openAIApi: OpenAIApi
) {
    suspend fun extractExpenses(transcript: String, currency: String = "USD"): List<ParsedExpense> {
        val systemPrompt = """You are an expense extraction assistant. Extract expenses from the user's voice transcript.
Return a JSON array of expenses with fields: amount (number), currency (string, default $currency),
category (string), note (string), confidence (0.0-1.0).
Example: [{"amount": 50.0, "currency": "$currency", "category": "Food", "note": "lunch", "confidence": 0.9}]
Only return the JSON array, nothing else."""

        val request = ChatRequest(
            model = "gpt-4o",
            messages = listOf(
                Message("system", systemPrompt),
                Message("user", transcript)
            ),
            temperature = 0.1,
            maxTokens = 500
        )

        return try {
            val response = openAIApi.chatCompletion(request)
            val content = response.choices?.firstOrNull()?.message?.content ?: return emptyList()
            parseExpensesFromJson(content, currency)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseExpensesFromJson(json: String, defaultCurrency: String): List<ParsedExpense> {
        return try {
            val cleanJson = json.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val array = JSONArray(cleanJson)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.getJSONObject(i)
                ParsedExpense(
                    amount = obj.optDouble("amount", 0.0),
                    currency = obj.optString("currency", defaultCurrency),
                    categoryName = obj.optString("category").takeIf { it.isNotEmpty() },
                    note = obj.optString("note").takeIf { it.isNotEmpty() },
                    date = Date(),
                    confidence = obj.optDouble("confidence", 0.8),
                    confidenceDetails = ConfidenceDetails(
                        amountConfidence = obj.optDouble("confidence", 0.8),
                        categoryConfidence = obj.optDouble("confidence", 0.8),
                        overallConfidence = obj.optDouble("confidence", 0.8)
                    )
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
