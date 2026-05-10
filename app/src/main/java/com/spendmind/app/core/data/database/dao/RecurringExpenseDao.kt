package com.spendmindai.app.core.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.spendmindai.app.core.data.database.entities.RecurringExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {

    @Query("SELECT * FROM recurring_expenses ORDER BY nextDueDate ASC")
    fun getAllRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE isIncome = 1 AND isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveIncomes(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE isIncome = 0 AND isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveExpenses(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    suspend fun getRecurringExpenseById(id: String): RecurringExpenseEntity?

    @Query("SELECT * FROM recurring_expenses WHERE nextDueDate <= :timestamp AND isActive = 1")
    suspend fun getDueRecurringExpenses(timestamp: Long): List<RecurringExpenseEntity>

    @Query("SELECT * FROM recurring_expenses WHERE categoryId = :categoryId ORDER BY nextDueDate ASC")
    fun getRecurringExpensesByCategory(categoryId: String): Flow<List<RecurringExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpense(recurringExpense: RecurringExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpenses(recurringExpenses: List<RecurringExpenseEntity>)

    @Update
    suspend fun updateRecurringExpense(recurringExpense: RecurringExpenseEntity)

    @Delete
    suspend fun deleteRecurringExpense(recurringExpense: RecurringExpenseEntity)

    @Query("DELETE FROM recurring_expenses WHERE id = :id")
    suspend fun deleteRecurringExpenseById(id: String)

    @Query("UPDATE recurring_expenses SET isActive = 0 WHERE id = :id")
    suspend fun deactivateRecurringExpense(id: String)

    @Query("SELECT COUNT(*) FROM recurring_expenses WHERE isActive = 1")
    suspend fun getActiveRecurringExpenseCount(): Int

    @Query("SELECT COUNT(*) FROM recurring_expenses")
    suspend fun getTotalRecurringExpenseCount(): Int
}
