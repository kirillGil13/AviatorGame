package com.mycompany.aviatorgame.ui.screens.main

import androidx.lifecycle.ViewModel
import com.mycompany.aviatorgame.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {
    val gameState = repository.gameState

    suspend fun claimDailyBonus(): Int? {
        return repository.claimDailyBonus()
    }
}