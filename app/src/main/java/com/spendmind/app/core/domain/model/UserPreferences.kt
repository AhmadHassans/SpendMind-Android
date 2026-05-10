package com.spendmindai.app.core.domain.model

import java.util.Date
import java.util.UUID

data class UserPreferences(
    val id: String = UUID.randomUUID().toString(),
    val currency: String = "USD",
    val language: String? = null,
    val autoSubmitTranscription: Boolean = false,
    val monthStartDay: Int = 1,
    val notificationEnabled: Boolean = false,
    val notificationHour: Int = 21,
    val notificationMinute: Int = 0,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    companion object {
        val DEFAULT = UserPreferences()

        val SUPPORTED_CURRENCIES = listOf(
            "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY",
            "INR", "MXN", "BRL", "KRW", "SGD", "HKD", "NOK", "SEK",
            "DKK", "NZD", "ZAR", "AED"
        )
    }
}
