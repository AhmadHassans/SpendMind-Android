package com.spendmindai.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.spendmindai.app.core.domain.model.Expense
import java.util.Date

@Entity(
    tableName = "expenses",
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
        Index(value = ["date"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val currency: String,
    val date: Long,
    val confidence: Double = 1.0,
    val source: String = "manual",
    val importId: String? = null,
    val transcript: String? = null,
    val note: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val categoryId: String? = null
)

fun ExpenseEntity.toDomainModel(): Expense = Expense(
    id = id,
    amount = amount,
    currency = currency,
    date = Date(date),
    confidence = confidence,
    source = source,
    importId = importId,
    transcript = transcript,
    note = note,
    updatedAt = Date(updatedAt),
    categoryId = categoryId
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    amount = amount,
    currency = currency,
    date = date.time,
    confidence = confidence,
    source = source,
    importId = importId,
    transcript = transcript,
    note = note,
    updatedAt = updatedAt.time,
    categoryId = categoryId
)
