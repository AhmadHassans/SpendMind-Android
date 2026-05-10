package com.spendmindai.app.core.domain.model

import java.util.Date
import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val currency: String = "USD",
    val date: Date = Date(),
    val confidence: Double = 1.0,
    val source: String = "manual",
    val importId: String? = null,
    val transcript: String? = null,
    val note: String? = null,
    val updatedAt: Date = Date(),
    val categoryId: String? = null
) {
    companion object {
        fun create(
            amount: Double,
            currency: String = "USD",
            date: Date = Date(),
            note: String? = null,
            categoryId: String? = null,
            source: String = "manual",
            confidence: Double = 1.0,
            transcript: String? = null,
            importId: String? = null
        ): Expense = Expense(
            id = UUID.randomUUID().toString(),
            amount = amount,
            currency = currency,
            date = date,
            confidence = confidence,
            source = source,
            importId = importId,
            transcript = transcript,
            note = note,
            updatedAt = Date(),
            categoryId = categoryId
        )
    }
}
