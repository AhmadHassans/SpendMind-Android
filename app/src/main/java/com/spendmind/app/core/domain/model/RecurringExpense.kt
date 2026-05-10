package com.spendmindai.app.core.domain.model

import java.util.Calendar
import java.util.Date
import java.util.UUID

enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    companion object {
        fun fromString(value: String): RecurrenceFrequency = try {
            valueOf(value.uppercase())
        } catch (e: IllegalArgumentException) {
            MONTHLY
        }
    }
}

data class RecurringExpense(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val currency: String = "USD",
    val frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    val isActive: Boolean = true,
    val isIncome: Boolean = false,
    val lastCreatedDate: Date? = null,
    val nextDueDate: Date? = null,
    val note: String? = null,
    val startDate: Date? = null,
    val updatedAt: Date = Date(),
    val categoryId: String? = null
) {
    fun calculateNextDueDate(fromDate: Date = Date()): Date {
        val calendar = Calendar.getInstance().apply { time = fromDate }
        return when (frequency) {
            RecurrenceFrequency.DAILY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.time
            }
            RecurrenceFrequency.WEEKLY -> {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.time
            }
            RecurrenceFrequency.MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)
                calendar.time
            }
            RecurrenceFrequency.YEARLY -> {
                calendar.add(Calendar.YEAR, 1)
                calendar.time
            }
        }
    }

    companion object {
        fun create(
            amount: Double,
            currency: String = "USD",
            frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
            isIncome: Boolean = false,
            note: String? = null,
            categoryId: String? = null,
            startDate: Date = Date()
        ): RecurringExpense {
            val instance = RecurringExpense(
                id = UUID.randomUUID().toString(),
                amount = amount,
                currency = currency,
                frequency = frequency,
                isActive = true,
                isIncome = isIncome,
                note = note,
                startDate = startDate,
                categoryId = categoryId
            )
            val nextDue = instance.calculateNextDueDate(startDate)
            return instance.copy(nextDueDate = nextDue)
        }
    }
}
