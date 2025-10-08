package com.mycompany.aviatorgame.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycompany.aviatorgame.data.local.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val soundManager: SoundManager
) : ViewModel() {

    val musicVolume: StateFlow<Float> = soundManager.musicVolume.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.5f
    )

    val soundEffectsVolume: StateFlow<Float> = soundManager.soundEffectsVolume.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.7f
    )

    fun updateMusicVolume(volume: Float) {
        viewModelScope.launch {
            soundManager.setMusicVolume(volume)
        }
    }

    fun updateSoundEffectsVolume(volume: Float) {
        viewModelScope.launch {
            soundManager.setSoundEffectsVolume(volume)
        }
    }
}