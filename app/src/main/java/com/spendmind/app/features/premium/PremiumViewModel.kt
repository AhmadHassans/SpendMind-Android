package com.spendmindai.app.features.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.spendmindai.app.core.infrastructure.billing.BillingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingService: BillingService
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = billingService.isPremium

    fun purchasePremium(activity: Activity) {
        billingService.purchasePremium(activity)
    }

    fun restorePurchases() {
        billingService.queryPurchases()
    }
}
