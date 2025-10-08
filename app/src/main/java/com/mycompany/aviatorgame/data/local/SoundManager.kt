package com.mycompany.aviatorgame.data.local

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private var backgroundMusic: MediaPlayer? = null
    private var airplaneSound: MediaPlayer? = null
    private var fadeJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.Main)

    val musicVolume: Flow<Float> = preferencesManager.musicVolume
    val soundEffectsVolume: Flow<Float> = preferencesManager.soundEffectsVolume

    private var currentMusicVolume = 0.5f
    private var currentSoundEffectsVolume = 0.7f

    init {
        // Следим за изменениями громкости музыки
        scope.launch {
            musicVolume.collect { volume ->
                currentMusicVolume = volume
                backgroundMusic?.setVolume(volume, volume)
            }
        }

        // Следим за изменениями громкости звуковых эффектов
        scope.launch {
            soundEffectsVolume.collect { volume ->
                currentSoundEffectsVolume = volume
                // Применяем только если не идет fade
                if (fadeJob == null || fadeJob?.isActive == false) {
                    airplaneSound?.setVolume(volume, volume)
                }
            }
        }
    }

    fun initBackgroundMusic(resourceId: Int) {
        scope.launch {
            try {
                releaseBackgroundMusic()

                // Получаем сохраненную громкость БЕЗ блокировки
                val savedVolume = preferencesManager.musicVolume.first()
                currentMusicVolume = savedVolume

                // Создаем MediaPlayer с громкостью 0 сначала
                backgroundMusic = MediaPlayer.create(context, resourceId)?.apply {
                    setVolume(0f, 0f) // Сначала 0 чтобы не было звука
                    isLooping = true
                }

                // Небольшая задержка
                delay(50)

                // Теперь устанавливаем правильную громкость
                backgroundMusic?.setVolume(savedVolume, savedVolume)

                // ВАЖНО: Запускаем музыку сразу после инициализации
                if (savedVolume > 0f) {
                    backgroundMusic?.start()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun initAirplaneSound(resourceId: Int) {
        scope.launch {
            try {
                releaseAirplaneSound()

                // Получаем сохраненную громкость БЕЗ блокировки
                val savedVolume = preferencesManager.soundEffectsVolume.first()
                currentSoundEffectsVolume = savedVolume

                // Создаем MediaPlayer с громкостью 0 сначала
                airplaneSound = MediaPlayer.create(context, resourceId)?.apply {
                    setVolume(0f, 0f) // Сначала 0
                    isLooping = true
                }

                // Небольшая задержка
                delay(50)

                // Теперь устанавливаем правильную громкость
                airplaneSound?.setVolume(savedVolume, savedVolume)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playBackgroundMusic() {
        try {
            backgroundMusic?.let { player ->
                // Проверяем что громкость не 0
                if (currentMusicVolume > 0f && !player.isPlaying) {
                    player.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pauseBackgroundMusic() {
        try {
            backgroundMusic?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playAirplaneSound() {
        try {
            fadeJob?.cancel()
            airplaneSound?.let { player ->
                // Применяем текущую громкость
                player.setVolume(currentSoundEffectsVolume, currentSoundEffectsVolume)

                // Проверяем что громкость не 0
                if (currentSoundEffectsVolume > 0f) {
                    if (player.isPlaying) {
                        player.seekTo(0)
                    } else {
                        player.start()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fadeOutAirplaneSound(durationMs: Long = 600) {
        fadeJob?.cancel()

        fadeJob = scope.launch {
            airplaneSound?.let { player ->
                if (player.isPlaying) {
                    val steps = 20
                    val stepDuration = durationMs / steps
                    val volumeStep = currentSoundEffectsVolume / steps

                    repeat(steps) { step ->
                        val volume = currentSoundEffectsVolume - (volumeStep * (step + 1))
                        player.setVolume(volume.coerceAtLeast(0f), volume.coerceAtLeast(0f))
                        delay(stepDuration)
                    }

                    player.pause()
                    player.seekTo(0)
                }
            }
            fadeJob = null
        }
    }

    fun stopAirplaneSound() {
        fadeJob?.cancel()
        fadeJob = null
        try {
            airplaneSound?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    player.seekTo(0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun setMusicVolume(volume: Float) {
        preferencesManager.updateMusicVolume(volume.coerceIn(0f, 1f))
    }

    suspend fun setSoundEffectsVolume(volume: Float) {
        preferencesManager.updateSoundEffectsVolume(volume.coerceIn(0f, 1f))
    }

    private fun releaseBackgroundMusic() {
        try {
            backgroundMusic?.apply {
                if (isPlaying) stop()
                release()
            }
            backgroundMusic = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseAirplaneSound() {
        fadeJob?.cancel()
        fadeJob = null
        try {
            airplaneSound?.apply {
                if (isPlaying) stop()
                release()
            }
            airplaneSound = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun releaseAll() {
        releaseBackgroundMusic()
        releaseAirplaneSound()
    }
}