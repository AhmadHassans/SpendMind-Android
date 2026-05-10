package com.spendmindai.app.core.infrastructure.pdf

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PDFTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Android's built-in PdfRenderer only renders pages as bitmaps (no text extraction).
    // For text extraction we use iText7 which handles text-based PDFs.

    fun extractText(uri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot open PDF"))

            val bytes = inputStream.readBytes()
            inputStream.close()

            // Use iText7 for text extraction
            val reader = com.itextpdf.kernel.pdf.PdfReader(bytes.inputStream())
            val document = com.itextpdf.kernel.pdf.PdfDocument(reader)
            val strategy = com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy()

            val sb = StringBuilder()
            for (i in 1..document.numberOfPages) {
                val page = document.getPage(i)
                val text = com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor.getTextFromPage(
                    page,
                    strategy
                )
                sb.append(text).append("\n")
            }
            document.close()

            val text = sb.toString().trim()
            if (text.length < 50) {
                Result.failure(
                    Exception("PDF appears to be scanned/image-only. Text extraction not supported.")
                )
            } else {
                Result.success(text)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to extract PDF text: ${e.message}"))
        }
    }

    /** Returns the number of pages in the PDF without extracting all text. */
    fun getPageCount(uri: Uri): Result<Int> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot open PDF"))
            val bytes = inputStream.readBytes()
            inputStream.close()
            val reader = com.itextpdf.kernel.pdf.PdfReader(bytes.inputStream())
            val document = com.itextpdf.kernel.pdf.PdfDocument(reader)
            val count = document.numberOfPages
            document.close()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to read PDF page count: ${e.message}"))
        }
    }
}
