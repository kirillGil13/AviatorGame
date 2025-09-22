package com.mycompany.aviatorgame.data.model

data class ShopItem(
    val id: String,
    val coins: Int,
    val price: String,
    val bonus: Int = 0 // Bonus percentage
) {
    companion object {
        val items = listOf(
            ShopItem("coins_1000", 1000, "$0.99"),
            ShopItem("coins_2000", 2000, "$1.99"),
            ShopItem("coins_5000", 5000, "$4.99", 10),
            ShopItem("coins_10000", 10000, "$9.99", 20),
            ShopItem("coins_20000", 20000, "$19.99", 30)
        )
    }

    fun getTotalCoins(): Int = coins + (coins * bonus / 100)
}