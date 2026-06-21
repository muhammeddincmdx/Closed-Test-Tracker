package com.mdstudio.closedtesttracker

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ProBillingState(
    val isReady: Boolean = false,
    val isPro: Boolean = false,
    val isLoading: Boolean = true,
    val price: String? = null,
    val message: String? = null
)

class ProManager(context: Context) {
    companion object {
        const val PRODUCT_ID = "pro_lifetime"
        private const val PREFS_NAME = "tester_settings"
        private const val KEY_PRO_CACHE = "pro_lifetime_owned"
    }

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(ProBillingState(isPro = prefs.getBoolean(KEY_PRO_CACHE, false)))
    val state: StateFlow<ProBillingState> = _state
    private var productDetails: ProductDetails? = null

    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases.orEmpty())
            } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                updateMessage(result.debugMessage)
            }
        }
        .enablePendingPurchases()
        .build()

    fun connect() {
        if (billingClient.isReady) {
            queryOwnership()
            queryProduct()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _state.value = _state.value.copy(isReady = true, isLoading = true, message = null)
                    queryOwnership()
                    queryProduct()
                } else {
                    _state.value = _state.value.copy(isLoading = false, message = result.debugMessage)
                }
            }

            override fun onBillingServiceDisconnected() {
                _state.value = _state.value.copy(isReady = false, isLoading = false)
            }
        })
    }

    fun launchPurchase(activity: Activity) {
        val details = productDetails
        if (!billingClient.isReady || details == null) {
            updateMessage("Google Play Billing is not ready yet.")
            connect()
            return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        billingClient.launchBillingFlow(activity, params)
    }

    fun queryOwnership() {
        if (!billingClient.isReady) return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases)
            } else {
                _state.value = _state.value.copy(isLoading = false, message = result.debugMessage)
            }
        }
    }

    private fun queryProduct() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        ) { result, products ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = products.firstOrNull()
                val price = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
                _state.value = _state.value.copy(price = price, isLoading = false)
            } else {
                _state.value = _state.value.copy(isLoading = false, message = result.debugMessage)
            }
        }
    }

    private fun processPurchases(purchases: List<com.android.billingclient.api.Purchase>) {
        val owned = purchases.firstOrNull { purchase ->
            purchase.purchaseState == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED &&
                PRODUCT_ID in purchase.products
        }
        if (owned == null) {
            setOwned(false)
            return
        }
        if (!owned.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(owned.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    setOwned(true)
                } else {
                    _state.value = _state.value.copy(isLoading = false, message = result.debugMessage)
                }
            }
        } else {
            setOwned(true)
        }
    }

    private fun setOwned(owned: Boolean) {
        prefs.edit().putBoolean(KEY_PRO_CACHE, owned).apply()
        _state.value = _state.value.copy(isPro = owned, isLoading = false, message = null)
    }

    private fun updateMessage(message: String?) {
        _state.value = _state.value.copy(isLoading = false, message = message)
    }

    fun close() {
        billingClient.endConnection()
    }
}
