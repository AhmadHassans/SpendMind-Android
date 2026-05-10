package com.spendmindai.app.core.infrastructure.export

import com.spendmindai.app.core.domain.model.Expense
import com.spendmindai.app.core.infrastructure.pdf.CSVTextExtractor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataImportService @Inject constructor(
    private val csvTextExtractor: CSVTextExtractor
) {

    private val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
        SimpleDateFormat("MM/dd/yyyy", Locale.US),
        SimpleDateFormat("dd/MM/yyyy", Locale.US),
        SimpleDateFormat("dd-MM-yyyy", Locale.US),
        SimpleDateFormat("MM-dd-yyyy", Locale.US),
        SimpleDateFormat("yyyy/MM/dd", Locale.US)
    ).onEach { it.isLenient = false }

    /**
     * Parses a CSV string produced by [DataExportService] (or a compatible format) and
     * maps each row to an [Expense] domain object.
     *
     * Expected columns (case-insensitive): Date, Amount, Currency, Category, Note, Source.
     * Unknown/extra columns are ignored. Rows that cannot be parsed are skipped.
     *
     * @return [Result.success] with the list of imported expenses, or
     *         [Result.failure] if the CSV is malformed beyond recovery.
     */
    fun importFromCSV(csvContent: String): Result<List<Expense>> {
        if (csvContent.isBlank()) {
            return Result.failure(Exception("CSV content is empty"))
        }

        return try {
            val rows = csvTextExtractor.parseRows(csvContent)
            if (rows.isEmpty()) {
                return Result.failure(Exception("No rows found in CSV"))
            }

            // Detect header row and column indices
            val header = rows.first().map { it.trim().lowercase() }
            val dateIdx = header.indexOfFirst { it.contains("date") }
            val amountIdx = header.indexOfFirst { it.contains("amount") }
            val currencyIdx = header.indexOfFirst { it.contains("currency") }
            val categoryIdx = header.indexOfFirst { it.contains("category") }
            val noteIdx = header.indexOfFirst { it.contains("note") || it.contains("description") }
            val sourceIdx = header.indexOfFirst { it.contains("source") }

            if (amountIdx == -1) {
                return Result.failure(Exception("CSV must contain an 'Amount' column"))
            }

            val expenses = mutableListOf<Expense>()
            val dataRows = rows.drop(1) // skip header

            for (row in dataRows) {
                try {
                    if (row.size <= amountIdx) continue

                    val amountStr = row.getOrNull(amountIdx)?.trim()?.replace(",", "") ?: continue
                    val amount = amountStr.toDoubleOrNull() ?: continue
                    if (amount <= 0.0) continue

                    val date = if (dateIdx >= 0) {
                        parseDate(row.getOrNull(dateIdx)?.trim() ?: "") ?: Date()
                    } else {
                        Date()
                    }

                    val currency = row.getOrNull(currencyIdx)?.trim()?.uppercase()
                        ?.takeIf { it.length == 3 } ?: "USD"

                    val categoryId = row.getOrNull(categoryIdx)?.trim()?.takeIf { it.isNotEmpty() }

                    val note = row.getOrNull(noteIdx)?.trim()?.takeIf { it.isNotEmpty() }

                    val source = row.getOrNull(sourceIdx)?.trim()?.takeIf { it.isNotEmpty() } ?: "csv_import"

                    expenses.add(
                        Expense(
                            id = UUID.randomUUID().toString(),
                            amount = amount,
                            currency = currency,
                            categoryId = categoryId,
                            note = note,
                            date = date,
                            source = source,
                            importId = null
                        )
                    )
                } catch (e: Exception) {
                    // Skip malformed rows silently
                }
            }

            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to import CSV: ${e.message}"))
        }
    }

    private fun parseDate(dateString: String): Date? {
        if (dateString.isBlank()) return null
        for (format in dateFormats) {
            try {
                return format.parse(dateString)
            } catch (e: Exception) {
                // Try next format
            }
        }
        return null
    }
}
