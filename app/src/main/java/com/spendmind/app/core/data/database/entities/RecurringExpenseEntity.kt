package com.spendmindai.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.spendmindai.app.core.domain.model.RecurrenceFrequency
import com.spendmindai.app.core.domain.model.RecurringExpense
import java.util.Date

@Entity(
    tableName = "recurring_expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["nextDueDate"])
    ]
)
data class RecurringExpenseEntity(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val currency: String,
    val frequency: String,
    val isActive: Boolean = true,
    val isIncome: Boolean = false,
    val lastCreatedDate: Long? = null,
    val nextDueDate: Long? = null,
    val note: String? = null,
    val startDate: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val categoryId: String? = null
)

fun RecurringExpenseEntity.toDomainModel(): RecurringExpense = RecurringExpense(
    id = id,
    amount = amount,
    currency = currency,
    frequency = RecurrenceFrequency.fromString(frequency),
    isActive = isActive,
    isIncome = isIncome,
    lastCreatedDate = lastCreatedDate?.let { Date(it) },
    nextDueDate = nextDueDate?.let { Date(it) },
    note = note,
    startDate = startDate?.let { Date(it) },
    updatedAt = Date(updatedAt),
    categoryId = categoryId
)

fun RecurringExpense.toEntity(): RecurringExpenseEntity = RecurringExpenseEntity(
    id = id,
    amount = amount,
    currency = currency,
    frequency = frequency.name,
    isActive = isActive,
    isIncome = isIncome,
    lastCreatedDate = lastCreatedDate?.time,
    nextDueDate = nextDueDate?.time,
    note = note,
    startDate = startDate?.time,
    updatedAt = updatedAt.time,
    categoryId = categoryId
)
