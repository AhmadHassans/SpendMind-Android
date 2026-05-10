package com.spendmindai.app.core.infrastructure.recurring

import com.spendmindai.app.core.data.repository.ExpenseRepository
import com.spendmindai.app.core.data.repository.RecurringExpenseRepository
import com.spendmindai.app.core.domain.model.Expense
import com.spendmindai.app.core.domain.model.RecurrenceFrequency
import com.spendmindai.app.core.domain.model.RecurringExpense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringExpenseService @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val recurringExpenseRepository: RecurringExpenseRepository
) {

    suspend fun generateDueExpenses(now: Date = Date()): List<Expense> =
        withContext(Dispatchers.IO) {
            val dueTemplates = recurringExpenseRepository.getAllOnce()
                .filter { template ->
                    template.isActive &&
                    template.nextDueDate != null &&
                    !template.nextDueDate.after(now)
                }

            val created = mutableListOf<Expense>()

            for (template in dueTemplates) {
                val expense = Expense(
                    id = UUID.randomUUID().toString(),
                    amount = template.amount,
                    currency = template.currency,
                    categoryId = template.categoryId,
                    note = template.note,
                    date = template.nextDueDate ?: now,
                    source = "recurring"
                )

                expenseRepository.insertExpense(expense)
                created.add(expense)

                val newLastCreated = template.nextDueDate ?: now
                val newNextDue = advanceDate(newLastCreated, template.frequency)

                val updated = template.copy(
                    lastCreatedDate = newLastCreated,
                    nextDueDate = newNextDue
                )
                recurringExpenseRepository.update(updated)
            }

            created
        }

    fun advanceDate(from: Date, frequency: RecurrenceFrequency): Date {
        val cal = Calendar.getInstance().apply { time = from }
        when (frequency) {
            RecurrenceFrequency.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            RecurrenceFrequency.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            RecurrenceFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
            RecurrenceFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        return cal.time
    }
}
