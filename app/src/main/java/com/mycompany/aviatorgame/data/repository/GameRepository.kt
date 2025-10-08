package com.mycompany.aviatorgame.data.repository

import com.mycompany.aviatorgame.data.local.BillingManager
import com.mycompany.aviatorgame.data.local.PreferencesManager
import com.mycompany.aviatorgame.data.model.Bet
import com.mycompany.aviatorgame.data.model.GameState
import com.mycompany.aviatorgame.utils.Constants
import com.mycompany.aviatorgame.utils.daysSince
import com.mycompany.aviatorgame.utils.isToday
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val billingManager: BillingManager
) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState

    val balance = preferencesManager.balance
    val purchaseState = billingManager.purchaseState

    init {
        // Load saved balance
        balance.onEach { savedBalance ->
            _gameState.update { it.copy(balance = savedBalance) }
        }.launchIn(GlobalScope)

        // Load daily bonus info
        combine(
            preferencesManager.lastBonusDate,
            preferencesManager.consecutiveDays
        ) { lastDate, days ->
            _gameState.update {
                it.copy(
                    lastDailyBonusDate = lastDate,
                    consecutiveDays = days
                )
            }
        }.launchIn(GlobalScope)
    }

    /**
     * Начинает новый раунд
     * Генерирует краш-множитель с учетом истории предыдущих крашей
     */
    fun startRound() {
        val state = _gameState.value

        // Генерируем краш-множитель с учетом истории
        val crashMultiplier = Constants.generateCrashMultiplier(state.crashHistory)

        _gameState.update {
            it.copy(
                isPlaying = true,
                isCrashed = false,
                currentMultiplier = 1.0f,
                roundId = it.roundId + 1,
                shouldPlayCrashAnimation = false,
                crashMultiplier = crashMultiplier
            )
        }
    }

    /**
     * Обновляет текущий множитель
     * Возвращает true если произошел краш
     */
    fun updateMultiplier(multiplier: Float): Boolean {
        val state = _gameState.value

        // Проверяем достигнут ли заранее вычисленный краш-множитель
        if (multiplier >= state.crashMultiplier) {
            crash()
            return true
        }

        _gameState.update { it.copy(currentMultiplier = multiplier) }

        // Auto cash out check
        state.activeBets.forEach { bet ->
            if (bet.isActive && !bet.cashedOut &&
                bet.autoCashOut != null && multiplier >= bet.autoCashOut) {
                cashOut(bet.id)
            }
        }

        return false
    }

    /**
     * Обрабатывает краш самолета
     * Добавляет текущий краш-множитель в историю
     */
    fun crash() {
        val state = _gameState.value

        // Добавляем текущий краш в историю (храним последние 20)
        val updatedHistory = (state.crashHistory + state.crashMultiplier).takeLast(20)

        _gameState.update {
            it.copy(
                isPlaying = false,
                isCrashed = true,
                activeBets = emptyList(),
                shouldPlayCrashAnimation = true,
                currentMultiplier = it.crashMultiplier, // Устанавливаем финальный множитель
                crashHistory = updatedHistory // Обновляем историю для следующих раундов
            )
        }
    }

    /**
     * Помечает что анимация краша проиграна
     */
    fun markCrashAnimationPlayed() {
        _gameState.update {
            it.copy(shouldPlayCrashAnimation = false)
        }
    }

    /**
     * Полный сброс состояния краша
     * Используется при уходе с игрового экрана
     * ВАЖНО: crashHistory НЕ сбрасываем - она нужна для генерации следующих раундов
     */
    fun resetCrashState() {
        _gameState.update {
            it.copy(
                isPlaying = false,
                isCrashed = false,
                shouldPlayCrashAnimation = false,
                currentMultiplier = 1.0f,
                activeBets = emptyList(),
                crashMultiplier = 0f
                // crashHistory сохраняем для следующих раундов!
            )
        }
    }

    /**
     * Размещает ставку
     * Возвращает true если ставка успешно размещена
     */
    fun placeBet(betId: Int, amount: Int, autoCashOut: Float? = null): Boolean {
        val state = _gameState.value
        if (state.isPlaying) return false
        if (amount > state.balance) return false
        if (state.activeBets.size >= 2) return false

        // Проверяем, что ставка с таким ID еще не существует
        if (state.activeBets.any { it.id == betId }) return false

        val newBet = Bet(
            id = betId,
            amount = amount,
            autoCashOut = autoCashOut,
            isActive = true
        )

        _gameState.update {
            it.copy(
                balance = it.balance - amount,
                activeBets = it.activeBets + newBet
            )
        }

        GlobalScope.launch {
            preferencesManager.updateBalance(_gameState.value.balance)
        }

        return true
    }

    /**
     * Отменяет ставку до начала раунда
     */
    fun cancelBet(betId: Int) {
        val state = _gameState.value
        if (state.isPlaying) return

        val bet = state.activeBets.find { it.id == betId } ?: return

        _gameState.update {
            it.copy(
                balance = it.balance + bet.amount,
                activeBets = it.activeBets.filter { it.id != betId }
            )
        }

        GlobalScope.launch {
            preferencesManager.updateBalance(_gameState.value.balance)
        }
    }

    /**
     * Забирает выигрыш по текущему множителю
     */
    fun cashOut(betId: Int) {
        val state = _gameState.value
        if (!state.isPlaying) return

        val bet = state.activeBets.find { it.id == betId } ?: return
        if (bet.cashedOut) return

        val winAmount = (bet.amount * state.currentMultiplier).toInt()

        _gameState.update {
            it.copy(
                balance = it.balance + winAmount,
                activeBets = it.activeBets.map {
                    if (it.id == betId) {
                        it.copy(
                            cashedOut = true,
                            cashOutMultiplier = state.currentMultiplier
                        )
                    } else it
                }
            )
        }

        GlobalScope.launch {
            preferencesManager.updateBalance(_gameState.value.balance)
        }
    }

    /**
     * Забирает ежедневный бонус
     * Возвращает сумму бонуса или null если уже забран сегодня
     */
    suspend fun claimDailyBonus(): Int? {
        val state = _gameState.value

        // Check if already claimed today
        if (state.lastDailyBonusDate.isToday()) return null

        val daysSinceLast = state.lastDailyBonusDate.daysSince()
        val consecutiveDays = if (daysSinceLast == 1) {
            (state.consecutiveDays + 1).coerceAtMost(7)
        } else {
            1
        }

        val bonusAmount = Constants.DAILY_BONUS[consecutiveDays - 1]

        _gameState.update {
            it.copy(
                balance = it.balance + bonusAmount,
                lastDailyBonusDate = System.currentTimeMillis(),
                consecutiveDays = consecutiveDays
            )
        }

        preferencesManager.updateBalance(_gameState.value.balance)
        preferencesManager.updateDailyBonus(System.currentTimeMillis(), consecutiveDays)

        return bonusAmount
    }

    /**
     * Добавляет купленные монеты к балансу
     */
    suspend fun addPurchasedCoins(coins: Int) {
        _gameState.update {
            it.copy(balance = it.balance + coins)
        }
        preferencesManager.addCoins(coins)
    }
}