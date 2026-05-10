package com.spendmindai.app.core.data.repository

import com.spendmindai.app.core.data.database.dao.RecurringExpenseDao
import com.spendmindai.app.core.data.database.entities.toDomainModel
import com.spendmindai.app.core.data.database.entities.toEntity
import com.spendmindai.app.core.domain.model.RecurringExpense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringExpenseRepository @Inject constructor(
    private val recurringExpenseDao: RecurringExpenseDao
) {
    fun getAll(): Flow<List<RecurringExpense>> =
        recurringExpenseDao.getAllRecurringExpenses()
            .map { list -> list.map { it.toDomainModel() } }

    suspend fun getAllOnce(): List<RecurringExpense> =
        recurringExpenseDao.getAllRecurringExpenses()
            .first()
            .map { it.toDomainModel() }

    suspend fun getDue(): List<RecurringExpense> =
        recurringExpenseDao.getDueRecurringExpenses(Date().time)
            .map { it.toDomainModel() }

    suspend fun insert(expense: RecurringExpense) {
        recurringExpenseDao.insertRecurringExpense(expense.toEntity())
    }

    suspend fun update(expense: RecurringExpense) {
        recurringExpenseDao.updateRecurringExpense(expense.toEntity())
    }

    suspend fun delete(expense: RecurringExpense) {
        recurringExpenseDao.deleteRecurringExpense(expense.toEntity())
    }
}
