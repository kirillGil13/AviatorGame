package com.mycompany.aviatorgame.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycompany.aviatorgame.data.local.SoundManager
import com.mycompany.aviatorgame.data.repository.GameRepository
import com.mycompany.aviatorgame.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository,
    private val soundManager: SoundManager
) : ViewModel() {

    val gameState = repository.gameState

    private val _bet1Amount = MutableStateFlow(100)
    val bet1Amount: StateFlow<Int> = _bet1Amount.asStateFlow()

    private val _bet2Amount = MutableStateFlow(100)
    val bet2Amount: StateFlow<Int> = _bet2Amount.asStateFlow()

    private val _bet1AutoCashOut = MutableStateFlow<Float?>(null)
    val bet1AutoCashOut: StateFlow<Float?> = _bet1AutoCashOut.asStateFlow()

    private val _bet2AutoCashOut = MutableStateFlow<Float?>(null)
    val bet2AutoCashOut: StateFlow<Float?> = _bet2AutoCashOut.asStateFlow()

    private var gameJob: Job? = null

    fun startGame() {
        if (gameState.value.isPlaying) return
        if (gameState.value.activeBets.isEmpty()) return

        repository.startRound()

        // Запускаем звук самолета
        soundManager.playAirplaneSound()

        gameJob = viewModelScope.launch {
            var multiplier = 1.0f
            while (multiplier < Constants.MAX_MULTIPLIER) {
                delay(Constants.PLANE_ANIMATION_DURATION)

                val speed = Constants.getMultiplierSpeed(multiplier)
                multiplier += speed

                val crashed = repository.updateMultiplier(multiplier)
                if (crashed) {
                    // Плавно затухаем звук самолета при краше
                    soundManager.fadeOutAirplaneSound(Constants.CRASH_ANIMATION_DURATION)
                    delay(Constants.CRASH_ANIMATION_DURATION)
                    break
                }
            }
        }
    }

    fun forceGameEnd() {
        gameJob?.cancel()
        gameJob = null

        // Останавливаем звук самолета
        soundManager.stopAirplaneSound()

        repository.resetCrashState()
    }

    fun markCrashAnimationPlayed() {
        repository.markCrashAnimationPlayed()
    }

    fun resetCrashState() {
        repository.resetCrashState()
    }

    fun placeBet1() {
        repository.placeBet(1, _bet1Amount.value, _bet1AutoCashOut.value)
    }

    fun placeBet2() {
        repository.placeBet(2, _bet2Amount.value, _bet2AutoCashOut.value)
    }

    fun cancelBet(betId: Int) {
        repository.cancelBet(betId)
    }

    fun cashOut(betId: Int) {
        repository.cashOut(betId)
    }

    fun updateBet1Amount(amount: Int) {
        _bet1Amount.value = amount.coerceIn(Constants.MIN_BET, Constants.MAX_BET)
    }

    fun updateBet2Amount(amount: Int) {
        _bet2Amount.value = amount.coerceIn(Constants.MIN_BET, Constants.MAX_BET)
    }

    fun updateBet1AutoCashOut(value: Float?) {
        _bet1AutoCashOut.value = value?.coerceIn(1.01f, Constants.MAX_MULTIPLIER)
    }

    fun updateBet2AutoCashOut(value: Float?) {
        _bet2AutoCashOut.value = value?.coerceIn(1.01f, Constants.MAX_MULTIPLIER)
    }

    fun doubleBet1() {
        updateBet1Amount(_bet1Amount.value * 2)
    }

    fun halveBet1() {
        updateBet1Amount(_bet1Amount.value / 2)
    }

    fun doubleBet2() {
        updateBet2Amount(_bet2Amount.value * 2)
    }

    fun halveBet2() {
        updateBet2Amount(_bet2Amount.value / 2)
    }

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
        soundManager.stopAirplaneSound()
    }
}