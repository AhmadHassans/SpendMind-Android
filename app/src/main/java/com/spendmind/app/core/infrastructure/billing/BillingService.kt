package com.spendmindai.app.core.infrastructure.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Product ID configured in Google Play Console. */
private const val PREMIUM_PRODUCT_ID = "spendmind_premium"

@Singleton
class BillingService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState

    private var billingClient: BillingClient? = null
    private var premiumProductDetails: ProductDetails? = null

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Initialises the BillingClient and starts a service connection.
     * Call this from your Activity's onCreate (or the Hilt entry point).
     */
    fun init(activity: Activity) {
        val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            handlePurchasesUpdated(billingResult, purchases)
        }

        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                    queryProductDetails()
                } else {
                    _billingState.value = BillingState.Error(
                        "Billing setup failed: ${billingResult.debugMessage}"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingState.value = BillingState.Disconnected
            }
        })
    }

    /** Releases billing client resources. Call from your Activity's onDestroy. */
    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /** Checks Google Play for active subscriptions or in-app purchases. */
    fun queryPurchases() {
        val client = billingClient ?: return

        // Query subscriptions
        val subParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(subParams) { _, purchases ->
            val hasSub = purchases.any { it.isActive() }
            if (hasSub) {
                _isPremium.value = true
                return@queryPurchasesAsync
            }

            // Fallback: query one-time purchases
            val inapParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            client.queryPurchasesAsync(inapParams) { _, inAppPurchases ->
                _isPremium.value = inAppPurchases.any { it.isActive() }
            }
        }
    }

    private fun queryProductDetails() {
        val client = billingClient ?: return
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { _, productDetailsList ->
            premiumProductDetails = productDetailsList.firstOrNull()
        }
    }

    // -------------------------------------------------------------------------
    // Purchase flow
    // -------------------------------------------------------------------------

    /**
     * Launches the Google Play billing flow for the premium subscription.
     * Requires a valid [Activity] context (not Application context).
     */
    fun purchasePremium(activity: Activity) {
        val client = billingClient ?: run {
            _billingState.value = BillingState.Error("Billing client not initialised")
            return
        }
        val productDetails = premiumProductDetails ?: run {
            // Try re-fetching product details
            queryProductDetails()
            _billingState.value = BillingState.Error("Product details not available yet. Please try again.")
            return
        }

        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken

        val productDetailsParams = if (offerToken != null) {
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        } else {
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        _billingState.value = BillingState.Purchasing
        client.launchBillingFlow(activity, billingFlowParams)
    }

    /** Restores previous purchases — useful for users reinstalling the app. */
    fun restorePurchases() {
        queryPurchases()
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun handlePurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    acknowledgePurchaseIfNeeded(purchase)
                    if (purchase.isActive()) {
                        _isPremium.value = true
                        _billingState.value = BillingState.PurchaseSuccess
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingState.value = BillingState.Cancelled
            }
            else -> {
                _billingState.value = BillingState.Error(
                    "Purchase failed: ${billingResult.debugMessage}"
                )
            }
        }
    }

    private fun acknowledgePurchaseIfNeeded(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient?.acknowledgePurchase(params) { _ -> /* no-op */ }
        }
    }

    private fun Purchase.isActive(): Boolean =
        purchaseState == Purchase.PurchaseState.PURCHASED ||
                purchaseState == Purchase.PurchaseState.PENDING
}

// -------------------------------------------------------------------------
// State sealed class
// -------------------------------------------------------------------------

sealed class BillingState {
    object Idle : BillingState()
    object Purchasing : BillingState()
    object PurchaseSuccess : BillingState()
    object Cancelled : BillingState()
    object Disconnected : BillingState()
    data class Error(val message: String) : BillingState()
}
