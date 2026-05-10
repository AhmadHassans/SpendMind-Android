package com.spendmindai.app.core.data.repository

import com.spendmindai.app.core.data.database.dao.CategoryDao
import com.spendmindai.app.core.data.database.dao.ExpenseDao
import com.spendmindai.app.core.data.database.entities.toEntity
import com.spendmindai.app.core.data.database.entities.toDomainModel
import com.spendmindai.app.core.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao
) {

    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses().map { list ->
        list.map { it.toDomainModel() }
    }

    fun getExpensesByDateRange(startDate: Date, endDate: Date): Flow<List<Expense>> =
        expenseDao.getExpensesByDateRange(startDate.time, endDate.time).map { list ->
            list.map { it.toDomainModel() }
        }

    fun getExpensesByCategory(categoryId: String): Flow<List<Expense>> =
        expenseDao.getExpensesByCategory(categoryId).map { list ->
            list.map { it.toDomainModel() }
        }

    suspend fun getExpenseById(id: String): Expense? =
        expenseDao.getExpenseById(id)?.toDomainModel()

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense.toEntity())
    }

    suspend fun insertExpenses(expenses: List<Expense>) {
        expenseDao.insertExpenses(expenses.map { it.toEntity() })
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense.toEntity())
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense.toEntity())
    }

    suspend fun deleteAllExpenses() {
        expenseDao.deleteAllExpenses()
    }

    suspend fun getTotalByDateRange(startDate: Date, endDate: Date): Double =
        expenseDao.getTotalByDateRange(startDate.time, endDate.time) ?: 0.0

    suspend fun checkDuplicate(importId: String): Boolean =
        expenseDao.getExpenseByImportId(importId) != null

    suspend fun getExpenseCount(): Int =
        expenseDao.getExpenseCount()
}
