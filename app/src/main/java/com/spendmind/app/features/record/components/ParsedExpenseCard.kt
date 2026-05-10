package com.spendmindai.app.features.record.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendmindai.app.core.domain.model.ParsedExpense
import com.spendmindai.app.shared.utils.CurrencyFormatter

@Composable
fun ParsedExpenseCard(
    parsedExpense: ParsedExpense,
    currency: String,
    isAccepted: Boolean,
    onToggleAccepted: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(0xFFE5534B)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAccepted)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isAccepted) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category emoji
            val emoji = parsedExpense.categoryName?.let { resolveCategoryEmoji(it) } ?: "📝"
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Amount + note
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = CurrencyFormatter.format(parsedExpense.amount, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAccepted) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                if (!parsedExpense.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = parsedExpense.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                if (!parsedExpense.categoryName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = parsedExpense.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                ConfidenceChip(confidence = parsedExpense.confidence)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Accept / reject checkbox
            Checkbox(
                checked = isAccepted,
                onCheckedChange = onToggleAccepted,
                colors = CheckboxDefaults.colors(checkedColor = accentColor)
            )
        }
    }
}

@Composable
private fun ConfidenceChip(confidence: Double) {
    val (label, bgColor, textColor) = when {
        confidence > 0.8 -> Triple("High", Color(0xFF4CAF50).copy(alpha = 0.15f), Color(0xFF2E7D32))
        confidence > 0.5 -> Triple("Medium", Color(0xFFFF9800).copy(alpha = 0.15f), Color(0xFFE65100))
        else -> Triple("Low", Color(0xFFF44336).copy(alpha = 0.15f), Color(0xFFC62828))
    }
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$label confidence",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun resolveCategoryEmoji(categoryName: String): String {
    return when (categoryName.lowercase()) {
        "food & dining", "food" -> "🍔"
        "transport", "transportation" -> "🚗"
        "shopping" -> "🛍️"
        "bills & utilities", "bills" -> "💡"
        "health & medical", "health" -> "🏥"
        "entertainment" -> "🎬"
        "education" -> "📚"
        "travel" -> "✈️"
        "fitness" -> "💪"
        "subscriptions" -> "📱"
        else -> "📝"
    }
}
