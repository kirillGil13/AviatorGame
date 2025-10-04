package com.mycompany.aviatorgame.utils

object Constants {
    // Betting limits
    const val MIN_BET = 10
    const val MAX_BET = 10000

    // Game mechanics
    const val MAX_MULTIPLIER = 100f
    const val MIN_MULTIPLIER = 1.0f

    // Вероятность краша увеличивается с множителем
    // Базовая вероятность низкая, но растет экспоненциально
    fun getCrashProbability(multiplier: Float): Float {
        return 0.01f
    }

    // Скорость роста множителя (увеличивается со временем)
    fun getMultiplierSpeed(currentMultiplier: Float): Float {
        return when {
            currentMultiplier < 2f -> 0.015f   // Медленный старт
            currentMultiplier < 5f -> 0.025f   // Средняя скорость
            currentMultiplier < 10f -> 0.035f  // Быстрее
            currentMultiplier < 20f -> 0.05f   // Еще быстрее
            else -> 0.08f                       // Очень быстро
        }
    }

    // Balance
    const val INITIAL_BALANCE = 2000

    // Daily bonus amounts (7 days)
    val DAILY_BONUS = listOf(
        100,  // Day 1
        150,  // Day 2
        200,  // Day 3
        300,  // Day 4
        400,  // Day 5
        500,  // Day 6
        1000  // Day 7 - Big bonus!
    )

    // Quick bet amounts for UI
    val QUICK_BET_AMOUNTS = listOf(10, 50, 100, 200, 500, 1000)

    // Auto cash out presets
    val AUTO_CASHOUT_PRESETS = listOf(1.5f, 2f, 3f, 5f, 10f)

    // Preferences keys
    const val PREFS_NAME = "aviator_prefs"
    const val KEY_BALANCE = "balance"
    const val KEY_LAST_BONUS = "last_bonus"
    const val KEY_CONSECUTIVE_DAYS = "consecutive_days"
    const val KEY_TOTAL_WINS = "total_wins"
    const val KEY_BIGGEST_WIN = "biggest_win"
    const val KEY_TOTAL_GAMES = "total_games"

    // UI Animation speeds
    const val PLANE_ANIMATION_DURATION = 50L // milliseconds between updates
    const val CRASH_ANIMATION_DURATION = 500L
    const val WIN_ANIMATION_DURATION = 1000L

    // Colors for multiplier ranges
    fun getMultiplierColor(multiplier: Float): Long {
        return when {
            multiplier < 1.5f -> 0xFF00d4ff  // Cyan
            multiplier < 2f -> 0xFF00ff88    // Green
            multiplier < 3f -> 0xFF00ff00    // Bright Green
            multiplier < 5f -> 0xFFffff00    // Yellow
            multiplier < 10f -> 0xFFffa500   // Orange
            multiplier < 20f -> 0xFFff6b6b   // Red-Orange
            else -> 0xFFff0000               // Red
        }
    }
}