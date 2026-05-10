package com.spendmindai.app.core.domain.model

import java.util.Date

data class ParsedExpense(
    val amount: Double,
    val currency: String,
    val categoryName: String?,
    val note: String?,
    val date: Date,
    val confidence: Double,
    val confidenceDetails: ConfidenceDetails = ConfidenceDetails()
)

data class ConfidenceDetails(
    val amountConfidence: Double = 1.0,
    val categoryConfidence: Double = 1.0,
    val overallConfidence: Double = 1.0
)
