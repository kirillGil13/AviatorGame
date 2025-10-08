package com.mycompany.aviatorgame.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mycompany.aviatorgame.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.PREFS_NAME
)

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val BALANCE_KEY = intPreferencesKey(Constants.KEY_BALANCE)
        val LAST_BONUS_KEY = longPreferencesKey(Constants.KEY_LAST_BONUS)
        val CONSECUTIVE_DAYS_KEY = intPreferencesKey(Constants.KEY_CONSECUTIVE_DAYS)
        val MUSIC_VOLUME_KEY = floatPreferencesKey(Constants.KEY_MUSIC_VOLUME)
        val SOUND_EFFECTS_VOLUME_KEY = floatPreferencesKey(Constants.KEY_SOUND_EFFECTS_VOLUME)
    }

    val balance: Flow<Int> = dataStore.data.map { preferences ->
        preferences[BALANCE_KEY] ?: Constants.INITIAL_BALANCE
    }

    val lastBonusDate: Flow<Long> = dataStore.data.map { preferences ->
        preferences[LAST_BONUS_KEY] ?: 0L
    }

    val consecutiveDays: Flow<Int> = dataStore.data.map { preferences ->
        preferences[CONSECUTIVE_DAYS_KEY] ?: 0
    }

    val musicVolume: Flow<Float> = dataStore.data.map { preferences ->
        preferences[MUSIC_VOLUME_KEY] ?: 0.5f
    }

    val soundEffectsVolume: Flow<Float> = dataStore.data.map { preferences ->
        preferences[SOUND_EFFECTS_VOLUME_KEY] ?: 0.7f
    }

    suspend fun updateBalance(balance: Int) {
        dataStore.edit { preferences ->
            preferences[BALANCE_KEY] = balance
        }
    }

    suspend fun updateDailyBonus(date: Long, days: Int) {
        dataStore.edit { preferences ->
            preferences[LAST_BONUS_KEY] = date
            preferences[CONSECUTIVE_DAYS_KEY] = days
        }
    }

    suspend fun addCoins(amount: Int) {
        dataStore.edit { preferences ->
            val currentBalance = preferences[BALANCE_KEY] ?: Constants.INITIAL_BALANCE
            preferences[BALANCE_KEY] = currentBalance + amount
        }
    }

    suspend fun updateMusicVolume(volume: Float) {
        dataStore.edit { preferences ->
            preferences[MUSIC_VOLUME_KEY] = volume
        }
    }

    suspend fun updateSoundEffectsVolume(volume: Float) {
        dataStore.edit { preferences ->
            preferences[SOUND_EFFECTS_VOLUME_KEY] = volume
        }
    }
}