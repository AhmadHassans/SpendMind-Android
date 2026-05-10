package com.spendmindai.app.features.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendmindai.app.core.domain.model.Category
import com.spendmindai.app.core.domain.model.Expense
import com.spendmindai.app.shared.utils.CurrencyFormatter

// ─────────────────────────────────────────────────────────────────────────────
// Category colour palette — matches iOS Color.CategoryColors defaults
// ─────────────────────────────────────────────────────────────────────────────
private fun categoryColor(name: String): Color {
    val palette = listOf(
        Color(0xFFFF9500), Color(0xFFFF6584), Color(0xFF34C759),
        Color(0xFFAF52DE), Color(0xFF007AFF), Color(0xFFE5534B), Color(0xFF9E9E9E)
    )
    return when (name.lowercase().trim()) {
        "car", "transport", "transportation" -> Color(0xFFFF9500)
        "eating out", "food", "dining", "restaurant" -> Color(0xFFFF6584)
        "groceries", "grocery" -> Color(0xFF34C759)
        "luxury" -> Color(0xFFAF52DE)
        "shopping" -> Color(0xFF007AFF)
        "medicines", "medicine", "health", "pharmacy" -> Color(0xFFE5534B)
        else -> palette[Math.abs(name.hashCode()) % palette.size]
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListItem(
    expense: Expense,
    category: Category?,
    currency: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE53935))
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White)
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        // iOS-style row: [emoji circle] | [category (small) + note (bold)] | [amount pill]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji circle — category-specific colour at 25% opacity
            val circleColor = categoryColor(category?.name ?: "")
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(circleColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = category?.emoji ?: "📝",
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text column — note (prominent) on top, category name (secondary) below
            Column(modifier = Modifier.weight(1f)) {
                val displayText = expense.note?.takeIf { it.isNotBlank() }
                    ?: category?.name
                    ?: "Expense"
                Text(
                    text       = displayText,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.Black,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                if (expense.note?.isNotBlank() == true) {
                    Text(
                        text     = category?.name ?: "Uncategorized",
                        fontSize = 12.sp,
                        color    = Color(0xFF8E8E93),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount — gray rounded rect, matching iOS .systemGray5 pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE5E5EA))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text       = CurrencyFormatter.format(expense.amount, currency),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.Black
                )
            }
        }
    }
}
