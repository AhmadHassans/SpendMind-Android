package com.spendmindai.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spendmindai.app.core.domain.model.Category
import java.util.Date

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val emoji: String = "📝",
    val colorName: String,
    val isCustom: Boolean = false,
    val isEnabled: Boolean = true,
    val order: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

fun CategoryEntity.toDomainModel(): Category = Category(
    id = id,
    name = name,
    emoji = emoji,
    colorName = colorName,
    isCustom = isCustom,
    isEnabled = isEnabled,
    order = order,
    updatedAt = Date(updatedAt)
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    emoji = emoji,
    colorName = colorName,
    isCustom = isCustom,
    isEnabled = isEnabled,
    order = order,
    updatedAt = updatedAt.time
)
