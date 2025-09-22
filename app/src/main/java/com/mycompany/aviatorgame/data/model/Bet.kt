package com.mycompany.aviatorgame.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Bet(
    val id: Int,
    val amount: Int,
    val autoCashOut: Float? = null,
    val isActive: Boolean = false,
    val cashedOut: Boolean = false,
    val cashOutMultiplier: Float? = null
) {
    fun getWinAmount(): Int {
        return if (cashedOut && cashOutMultiplier != null) {
            (amount * cashOutMultiplier).toInt()
        } else {
            0
        }
    }
}