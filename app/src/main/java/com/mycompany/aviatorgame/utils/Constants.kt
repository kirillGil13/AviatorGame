package com.mycompany.aviatorgame.utils

import kotlin.math.ln
import kotlin.random.Random

object Constants {
    // Betting limits
    const val MIN_BET = 10
    const val MAX_BET = 10000

    // Game mechanics
    const val MAX_MULTIPLIER = 100f
    const val MIN_MULTIPLIER = 1.0f

    // Настройки балансировки
    private const val HOUSE_EDGE = 0.1f // 3% преимущество казино (RTP = 97%)

    // НАСТРОЙКИ АЗАРТА И ДРАМЫ
    private const val HISTORY_SIZE = 10 // Сколько раундов учитываем
    private const val INSTANT_CRASH_CHANCE = 0.12f // 12% мгновенных крашей
    private const val COMPENSATION_THRESHOLD = 2 // Компенсация после 2 низких
    private const val LOW_MULTIPLIER_THRESHOLD = 2.5f // Более строгий порог
    private const val LUCKY_STREAK_CHANCE = 0.20f // 20% шанс везучей серии

    /**
     * Генерирует драматичный множитель краша с учетом истории
     */
    fun generateCrashMultiplier(history: List<Float>): Float {
        val random = Random.nextFloat()

        // 1. МГНОВЕННЫЙ КРАШ (драма!) - редко, но метко
        if (random < INSTANT_CRASH_CHANCE) {
            return generateInstantCrash()
        }

        // 2. Анализируем последние раунды
        val recentHistory = history.takeLast(HISTORY_SIZE)
        val lowMultiplierCount = recentHistory.count { it <= LOW_MULTIPLIER_THRESHOLD }
        val averageMultiplier = if (recentHistory.isNotEmpty()) {
            recentHistory.average().toFloat()
        } else {
            2.0f
        }

        // 3. КОМПЕНСАЦИЯ: После серии неудач - высокий множитель!
        if (lowMultiplierCount >= COMPENSATION_THRESHOLD) {
            return generateCompensationMultiplier()
        }

        // 4. ВЕЗУЧАЯ СЕРИЯ: Иногда начинается полоса удачи
        if (recentHistory.size >= 3 && random < LUCKY_STREAK_CHANCE) {
            val lastThree = recentHistory.takeLast(3)
            // Если последние 3 множителя были средние/высокие, продолжаем удачу
            if (lastThree.all { it > 2.0f }) {
                return generateLuckyMultiplier()
            }
        }

        // 5. АНТИ-СЕРИЯ: Если было несколько высоких подряд, следующий будет низкий
        if (recentHistory.size >= 2) {
            val lastTwo = recentHistory.takeLast(2)
            if (lastTwo.all { it > 5.0f }) {
                // После двух высоких - вероятнее низкий
                return generateLowMultiplier()
            }
        }

        // 6. ОБЫЧНАЯ ГЕНЕРАЦИЯ с house edge
        return generateNormalMultiplier()
    }

    /**
     * Мгновенный краш - самый драматичный момент!
     * Крашится почти сразу: 1.00x - 1.25x
     */
    private fun generateInstantCrash(): Float {
        val variants = listOf(
            1.00f,  // Краш на старте!
            1.01f,  // Почти сразу
            1.05f,  // Очень быстро
            1.10f,  // Быстро
            1.15f,  // Быстрее обычного
            1.20f,  // Неожиданно рано
            1.25f   // Рановато
        )
        return variants.random()
    }

    /**
     * Компенсационный множитель - награда за терпение
     * После серии неудач: 5.0x - 50.0x
     */
    private fun generateCompensationMultiplier(): Float {
        val random = Random.nextFloat()
        return when {
            random < 0.40f -> {
                // 40% шанс: 5.0x - 10.0x
                5.0f + Random.nextFloat() * 5.0f
            }
            random < 0.70f -> {
                // 30% шанс: 10.0x - 20.0x
                10.0f + Random.nextFloat() * 10.0f
            }
            random < 0.90f -> {
                // 20% шанс: 20.0x - 35.0x
                20.0f + Random.nextFloat() * 15.0f
            }
            else -> {
                // 10% шанс: 35.0x - 50.0x (джекпот!)
                35.0f + Random.nextFloat() * 15.0f
            }
        }.coerceIn(5.0f, MAX_MULTIPLIER).roundToTwoDecimals()
    }

    /**
     * Везучий множитель - серия удачи продолжается
     * 3.0x - 15.0x
     */
    private fun generateLuckyMultiplier(): Float {
        val random = Random.nextFloat()
        return when {
            random < 0.50f -> {
                // 50% шанс: 3.0x - 6.0x
                3.0f + Random.nextFloat() * 3.0f
            }
            random < 0.80f -> {
                // 30% шанс: 6.0x - 10.0x
                6.0f + Random.nextFloat() * 4.0f
            }
            else -> {
                // 20% шанс: 10.0x - 15.0x
                10.0f + Random.nextFloat() * 5.0f
            }
        }.coerceIn(3.0f, 15.0f).roundToTwoDecimals()
    }

    /**
     * Низкий множитель - после серии везения
     * 1.2x - 2.5x
     */
    private fun generateLowMultiplier(): Float {
        val random = Random.nextFloat()
        return when {
            random < 0.30f -> {
                // 30% шанс: совсем низкий 1.2x - 1.5x
                1.2f + Random.nextFloat() * 0.3f
            }
            random < 0.70f -> {
                // 40% шанс: низкий 1.5x - 2.0x
                1.5f + Random.nextFloat() * 0.5f
            }
            else -> {
                // 30% шанс: средне-низкий 2.0x - 2.5x
                2.0f + Random.nextFloat() * 0.5f
            }
        }.roundToTwoDecimals()
    }

    /**
     * Обычная генерация - стандартное распределение
     */
    private fun generateNormalMultiplier(): Float {
        val random = Random.nextFloat()
        val adjustedRandom = random * (1 - HOUSE_EDGE)

        val crashPoint = if (adjustedRandom >= 0.99f) {
            MAX_MULTIPLIER
        } else {
            (1.0f / (1.0f - adjustedRandom)).coerceIn(MIN_MULTIPLIER, MAX_MULTIPLIER)
        }

        return crashPoint.roundToTwoDecimals()
    }

    /**
     * Округление до 2 знаков после запятой
     */
    private fun Float.roundToTwoDecimals(): Float {
        return (this * 100).toInt() / 100f
    }

    /**
     * АЛЬТЕРНАТИВА: Простая система с фиксированными вероятностями
     * Используйте эту функцию если хотите более предсказуемое распределение
     */
    fun generateSimpleCrashMultiplier(history: List<Float>): Float {
        val random = Random.nextFloat()
        val recentHistory = history.takeLast(5)
        val lowCount = recentHistory.count { it < 2.0f }

        // Компенсация: после 3 низких множителей гарантирован высокий
        if (lowCount >= 3) {
            return when (Random.nextInt(0, 100)) {
                in 0..39 -> (5.0f + Random.nextFloat() * 5.0f)   // 40%: 5-10x
                in 40..69 -> (10.0f + Random.nextFloat() * 10.0f) // 30%: 10-20x
                in 70..89 -> (20.0f + Random.nextFloat() * 15.0f) // 20%: 20-35x
                else -> (35.0f + Random.nextFloat() * 15.0f)      // 10%: 35-50x
            }.roundToTwoDecimals()
        }

        // Мгновенный краш: 8% шанс
        if (random < 0.08f) {
            return listOf(1.00f, 1.01f, 1.05f, 1.10f, 1.15f, 1.20f).random()
        }

        // Обычное распределение
        return when (Random.nextInt(0, 100)) {
            in 0..39 -> (1.2f + Random.nextFloat() * 1.3f)   // 40%: 1.2-2.5x
            in 40..64 -> (2.5f + Random.nextFloat() * 2.5f)  // 25%: 2.5-5.0x
            in 65..84 -> (5.0f + Random.nextFloat() * 5.0f)  // 20%: 5.0-10.0x
            in 85..94 -> (10.0f + Random.nextFloat() * 10.0f) // 10%: 10-20x
            else -> (20.0f + Random.nextFloat() * 30.0f)     // 5%: 20-50x
        }.coerceIn(MIN_MULTIPLIER, MAX_MULTIPLIER).roundToTwoDecimals()
    }

    // Скорость роста множителя
    fun getMultiplierSpeed(currentMultiplier: Float): Float {
        return when {
            currentMultiplier < 2f -> 0.015f
            currentMultiplier < 5f -> 0.045f
            currentMultiplier < 10f -> 0.065f
            currentMultiplier < 20f -> 0.5f
            else -> 1f
        }
    }

    // Balance
    const val INITIAL_BALANCE = 2000

    // Daily bonus amounts (7 days)
    val DAILY_BONUS = listOf(100, 150, 200, 300, 400, 500, 1000)

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
    const val KEY_MUSIC_VOLUME = "music_volume"
    const val KEY_SOUND_EFFECTS_VOLUME = "sound_effects_volume"

    // UI Animation speeds
    const val PLANE_ANIMATION_DURATION = 50L
    const val CRASH_ANIMATION_DURATION = 500L
    const val WIN_ANIMATION_DURATION = 1000L

    // Colors for multiplier ranges
    fun getMultiplierColor(multiplier: Float): Long {
        return when {
            multiplier < 1.5f -> 0xFF00d4ff
            multiplier < 2f -> 0xFF00ff88
            multiplier < 3f -> 0xFF00ff00
            multiplier < 5f -> 0xFFffff00
            multiplier < 10f -> 0xFFffa500
            multiplier < 20f -> 0xFFff6b6b
            else -> 0xFFff0000
        }
    }
}