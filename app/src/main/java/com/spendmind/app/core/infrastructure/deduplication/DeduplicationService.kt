package com.spendmindai.app.core.infrastructure.deduplication

import com.spendmindai.app.core.data.repository.ExpenseRepository
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeduplicationService @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend fun isDuplicate(
        amount: Double,
        date: Date,
        categoryId: String?,
        importId: String? = null
    ): Boolean {
        if (importId != null && expenseRepository.checkDuplicate(importId)) return true
        return false
    }

    fun computeContentHash(amount: Double, date: Date, categoryId: String?): String {
        val dayString = dateFormat.format(date)
        val amountString = String.format(Locale.US, "%.2f", amount)
        val input = "$amountString|$dayString|${categoryId.orEmpty()}"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
