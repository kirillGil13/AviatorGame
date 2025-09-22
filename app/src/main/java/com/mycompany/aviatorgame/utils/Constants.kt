package com.mycompany.aviatorgame.utils

object Constants {
    const val MIN_BET = 10
    const val MAX_BET = 10000
    const val MAX_MULTIPLIER = 100f
    const val CRASH_PROBABILITY = 0.03f // 3% chance to crash each tick
    const val MULTIPLIER_SPEED = 0.02f // How fast multiplier grows
    const val INITIAL_BALANCE = 2000

    // Daily bonus amounts
    val DAILY_BONUS = listOf(100, 150, 200, 250, 300, 400, 500)

    // Preferences keys
    const val PREFS_NAME = "aviator_prefs"
    const val KEY_BALANCE = "balance"
    const val KEY_LAST_BONUS = "last_bonus"
    const val KEY_CONSECUTIVE_DAYS = "consecutive_days"
}