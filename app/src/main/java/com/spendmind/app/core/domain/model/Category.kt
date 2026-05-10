package com.spendmindai.app.core.domain.model

import java.util.Date
import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = "📝",
    val colorName: String = "gray",
    val isCustom: Boolean = false,
    val isEnabled: Boolean = true,
    val order: Int = 0,
    val updatedAt: Date = Date()
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category(id = "food", name = "Food & Dining", emoji = "🍔", colorName = "orange", order = 0),
            Category(id = "transport", name = "Transport", emoji = "🚗", colorName = "blue", order = 1),
            Category(id = "shopping", name = "Shopping", emoji = "🛍️", colorName = "pink", order = 2),
            Category(id = "bills", name = "Bills & Utilities", emoji = "💡", colorName = "yellow", order = 3),
            Category(id = "health", name = "Health & Medical", emoji = "🏥", colorName = "red", order = 4),
            Category(id = "entertainment", name = "Entertainment", emoji = "🎬", colorName = "purple", order = 5),
            Category(id = "education", name = "Education", emoji = "📚", colorName = "teal", order = 6),
            Category(id = "travel", name = "Travel", emoji = "✈️", colorName = "cyan", order = 7),
            Category(id = "fitness", name = "Fitness", emoji = "💪", colorName = "green", order = 8),
            Category(id = "subscriptions", name = "Subscriptions", emoji = "📱", colorName = "indigo", order = 9),
            Category(id = "other", name = "Other", emoji = "📝", colorName = "gray", order = 10)
        )
    }
}
