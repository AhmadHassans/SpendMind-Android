package com.spendmindai.app.shared.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {

    /**
     * Formats [amount] with the currency symbol for [currencyCode].
     *
     * Example: `format(1234.5, "USD")` → `"$1,234.50"`
     */
    fun format(amount: Double, currencyCode: String): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val symbols = DecimalFormatSymbols(Locale.US).apply { this.currency = currency }
            val decimalFormat = DecimalFormat("#,##0.00", symbols)
            "${currency.symbol}${decimalFormat.format(amount)}"
        } catch (e: Exception) {
            "$currencyCode ${String.format(Locale.US, "%.2f", amount)}"
        }
    }

    /**
     * Formats [amount] in a compact, human-readable form.
     *
     * - >= 1,000,000 → e.g. `"$1.2M"`
     * - >= 1,000     → e.g. `"$1.2K"`
     * - otherwise    → delegates to [format]
     */
    fun formatCompact(amount: Double, currencyCode: String): String {
        val symbol = try {
            Currency.getInstance(currencyCode).symbol
        } catch (e: Exception) {
            currencyCode
        }
        return when {
            amount >= 1_000_000 -> "$symbol${String.format(Locale.US, "%.1f", amount / 1_000_000)}M"
            amount >= 1_000 -> "$symbol${String.format(Locale.US, "%.1f", amount / 1_000)}K"
            else -> format(amount, currencyCode)
        }
    }

    /**
     * Returns just the currency symbol for [currencyCode].
     *
     * Falls back to the ISO code itself if the code is unrecognised.
     */
    fun symbol(currencyCode: String): String {
        return try {
            Currency.getInstance(currencyCode).symbol
        } catch (e: Exception) {
            currencyCode
        }
    }

    /**
     * Returns the display name for [currencyCode] in the given [locale].
     *
     * Example: `displayName("USD")` → `"US Dollar"`
     */
    fun displayName(currencyCode: String, locale: Locale = Locale.getDefault()): String {
        return try {
            Currency.getInstance(currencyCode).getDisplayName(locale)
        } catch (e: Exception) {
            currencyCode
        }
    }
}
