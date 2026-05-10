package com.spendmindai.app.core.infrastructure.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    fun logEvent(name: String, params: Map<String, Any> = emptyMap()) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Float -> putFloat(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    fun logExpenseAdded(source: String, amount: Double, currency: String) =
        logEvent("expense_added", mapOf("source" to source, "amount" to amount, "currency" to currency))

    fun logOnboardingStep(step: String) = logEvent("onboarding_step", mapOf("step" to step))

    fun logScreenView(screenName: String) =
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, mapOf(FirebaseAnalytics.Param.SCREEN_NAME to screenName))

    fun logBankImport(fileType: String, expenseCount: Int) =
        logEvent("bank_import", mapOf("file_type" to fileType, "expense_count" to expenseCount))

    fun logPremiumPurchase(productId: String) =
        logEvent(FirebaseAnalytics.Event.PURCHASE, mapOf(FirebaseAnalytics.Param.ITEM_ID to productId))

    fun logCategoryCreated(isCustom: Boolean) = logEvent("category_created", mapOf("is_custom" to isCustom))

    fun setUserId(userId: String) { firebaseAnalytics.setUserId(userId) }

    fun setUserProperty(name: String, value: String) { firebaseAnalytics.setUserProperty(name, value) }
}
