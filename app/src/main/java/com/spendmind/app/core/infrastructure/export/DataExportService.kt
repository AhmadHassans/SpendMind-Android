package com.spendmindai.app.core.infrastructure.export

import com.spendmindai.app.core.domain.model.Category
import com.spendmindai.app.core.domain.model.Expense
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExportService @Inject constructor() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Exports [expenses] to a CSV-formatted string.
     *
     * Columns: Date, Amount, Currency, Category, Note, Source
     *
     * @param expenses   The list of expenses to export.
     * @param categories A map of categoryId → Category used to resolve category names.
     * @return A well-formed CSV string including a header row.
     */
    fun exportToCSV(
        expenses: List<Expense>,
        categories: Map<String, Category>
    ): String {
        val sb = StringBuilder()

        // Header row
        sb.appendLine("Date,Amount,Currency,Category,Note,Source")

        // Data rows
        expenses.forEach { expense ->
            val date = dateFormat.format(expense.date)
            val amount = String.format(Locale.US, "%.2f", expense.amount)
            val currency = escapeCsvField(expense.currency)
            val categoryName = escapeCsvField(
                categories[expense.categoryId]?.name ?: expense.categoryId.orEmpty()
            )
            val note = escapeCsvField(expense.note.orEmpty())
            val source = escapeCsvField(expense.source.orEmpty())

            sb.appendLine("$date,$amount,$currency,$categoryName,$note,$source")
        }

        return sb.toString()
    }

    /**
     * Wraps [field] in double-quotes if it contains a comma, double-quote, or newline,
     * escaping any internal double-quotes by doubling them.
     */
    private fun escapeCsvField(field: String): String {
        return if (field.contains(',') || field.contains('"') || field.contains('\n')) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
}
