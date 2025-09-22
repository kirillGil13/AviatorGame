package com.mycompany.aviatorgame.data.local

import android.app.Activity
import android.content.Context
import com.mycompany.aviatorgame.data.model.ShopItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    private val context: Context
) {

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState

    sealed class PurchaseState {
        object Idle : PurchaseState()
        object Processing : PurchaseState()
        data class Success(val coins: Int) : PurchaseState()
        data class Error(val message: String) : PurchaseState()
    }

    fun purchaseItem(activity: Activity, shopItem: ShopItem) {
        _purchaseState.value = PurchaseState.Processing
        // Для тестирования - симулируем успешную покупку
        // В реальном приложении здесь будет интеграция с Google Play Billing
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _purchaseState.value = PurchaseState.Success(shopItem.getTotalCoins())
        }, 1000)
    }

    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }
}