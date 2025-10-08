package com.mycompany.aviatorgame.ui.screens.shop

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycompany.aviatorgame.data.local.BillingManager
import com.mycompany.aviatorgame.data.model.ShopItem
import com.mycompany.aviatorgame.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val repository: GameRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    val purchaseState: StateFlow<BillingManager.PurchaseState> = billingManager.purchaseState
    val shopItems = ShopItem.items

    fun purchaseItem(activity: Activity, item: ShopItem) {
        billingManager.purchaseItem(activity, item)
    }

    fun handlePurchaseSuccess(coins: Int) {
        viewModelScope.launch {
            repository.addPurchasedCoins(coins)
        }
    }

    fun resetPurchaseState() {
        billingManager.resetPurchaseState()
    }
}