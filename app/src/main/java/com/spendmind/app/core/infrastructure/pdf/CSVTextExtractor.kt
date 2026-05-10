package com.spendmindai.app.core.infrastructure.pdf

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CSVTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Reads the CSV file at [uri], auto-detects the delimiter, and returns the raw
     * string content along with the detected delimiter character.
     *
     * Supported delimiters (in detection priority): comma, semicolon, tab, pipe.
     */
    fun extractText(uri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot open CSV file"))

            val rawContent = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            inputStream.close()

            if (rawContent.isBlank()) {
                return Result.failure(Exception("CSV file is empty"))
            }

            Result.success(rawContent)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to read CSV: ${e.message}"))
        }
    }

    /**
     * Parses the CSV string into a list of rows, where each row is a list of field strings.
     * Handles quoted fields (including quoted fields that contain the delimiter or newlines).
     *
     * @param content  Raw CSV text.
     * @param delimiter Auto-detected when `null`; otherwise use the provided character.
     */
    fun parseRows(content: String, delimiter: Char? = null): List<List<String>> {
        val detectedDelimiter = delimiter ?: detectDelimiter(content)
        val rows = mutableListOf<List<String>>()
        var currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var insideQuotes = false
        var i = 0

        while (i < content.length) {
            val ch = content[i]
            when {
                ch == '"' -> {
                    if (insideQuotes && i + 1 < content.length && content[i + 1] == '"') {
                        // Escaped double-quote inside a quoted field
                        currentField.append('"')
                        i++ // skip second quote
                    } else {
                        insideQuotes = !insideQuotes
                    }
                }
                ch == detectedDelimiter && !insideQuotes -> {
                    currentRow.add(currentField.toString())
                    currentField.clear()
                }
                (ch == '\n' || ch == '\r') && !insideQuotes -> {
                    // Handle \r\n as a single line ending
                    if (ch == '\r' && i + 1 < content.length && content[i + 1] == '\n') {
                        i++
                    }
                    currentRow.add(currentField.toString())
                    currentField.clear()
                    if (currentRow.any { it.isNotEmpty() }) {
                        rows.add(currentRow)
                    }
                    currentRow = mutableListOf()
                }
                else -> currentField.append(ch)
            }
            i++
        }

        // Flush remaining content
        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString())
            if (currentRow.any { it.isNotEmpty() }) {
                rows.add(currentRow)
            }
        }

        return rows
    }

    /**
     * Scores each candidate delimiter by counting occurrences in the first 20 lines
     * and returns the one with the highest consistent count.
     */
    fun detectDelimiter(content: String): Char {
        val candidates = listOf(',', ';', '\t', '|')
        val sampleLines = content.lines().take(20).filter { it.isNotBlank() }

        data class Score(val delimiter: Char, val avgCount: Double, val consistency: Double)

        val scores = candidates.map { delim ->
            val counts = sampleLines.map { line -> line.count { it == delim } }
            val avg = if (counts.isEmpty()) 0.0 else counts.average()
            val variance = if (counts.size < 2) 0.0 else {
                counts.map { (it - avg) * (it - avg) }.average()
            }
            // Prefer delimiters with a higher average count and lower variance
            Score(delim, avg, variance)
        }

        // Pick delimiter with the highest average (tie-break: lowest variance)
        val best = scores
            .filter { it.avgCount > 0 }
            .maxWithOrNull(compareBy({ it.avgCount }, { -it.consistency }))

        return best?.delimiter ?: ','
    }
}
