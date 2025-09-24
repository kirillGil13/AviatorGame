package com.mycompany.aviatorgame.data.model

import kotlinx.serialization.Serializable
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class GameState(
    val balance: Int = 2000,
    val currentMultiplier: Float = 1.0f,
    val isPlaying: Boolean = false,
    val isCrashed: Boolean = false,
    val activeBets: List<Bet> = emptyList(),
    val lastDailyBonusDate: Long = 0L,
    val consecutiveDays: Int = 0
)