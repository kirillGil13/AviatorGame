package com.mycompany.aviatorgame.data.local

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.mycompany.aviatorgame.data.model.ShopItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    private val context: Context
) : PurchasesUpdatedListener {
    companion object {
        private const val USE_TEST_MODE = false // Включить для теста без billing
    }

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState

    private var billingClient: BillingClient? = null
    private var currentShopItem: ShopItem? = null

    sealed class PurchaseState {
        object Idle : PurchaseState()
        object Processing : PurchaseState()
        data class Success(val coins: Int) : PurchaseState()
        data class Error(val message: String) : PurchaseState()
    }

    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Подключение успешно
                    // Проверяем незавершенные покупки
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Попробуем переподключиться
                initializeBillingClient()
            }
        })
    }

    private fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Обработка незавершенных покупок
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(purchase)
                    }
                }
            }
        }
    }

    fun purchaseItem(activity: Activity, shopItem: ShopItem) {
        if (USE_TEST_MODE) {
            _purchaseState.value = PurchaseState.Processing
            // Симуляция покупки для теста
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                _purchaseState.value = PurchaseState.Success(shopItem.getTotalCoins())
            }, 2000) // 2 секунды задержки
        } else {
            currentShopItem = shopItem
            _purchaseState.value = PurchaseState.Processing

            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(shopItem.id)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                    val productDetails = productDetailsList[0]

                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                    billingClient?.launchBillingFlow(activity, billingFlowParams)
                } else {
                    _purchaseState.value = PurchaseState.Error("Не удалось загрузить информацию о товаре")
                }
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Error("Покупка отменена")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _purchaseState.value = PurchaseState.Error("Товар уже куплен")
            }
            else -> {
                _purchaseState.value = PurchaseState.Error("Ошибка покупки: ${billingResult.debugMessage}")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        // Подтверждаем покупку
        if (!purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Покупка подтверждена, начисляем монеты
                    val coins = currentShopItem?.getTotalCoins() ?: 0
                    _purchaseState.value = PurchaseState.Success(coins)
                }
            }
        } else {
            // Покупка уже подтверждена
            val coins = currentShopItem?.getTotalCoins() ?: 0
            _purchaseState.value = PurchaseState.Success(coins)
        }
    }

    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
        currentShopItem = null
    }
}