package com.vampyrelord.ttsaicontroller.features.touch.domain

import com.vampyrelord.ttsaicontroller.features.touch.domain.model.RushwulfSettings
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.SavedTouchConfig
import kotlinx.coroutines.flow.StateFlow

interface TouchConfigRepository {
    val savedConfigurations: StateFlow<List<SavedTouchConfig>>
    val activeConfiguration: StateFlow<SavedTouchConfig?>
    val rushwulfSettings: StateFlow<RushwulfSettings>

    suspend fun saveConfiguration(config: SavedTouchConfig)
    suspend fun loadConfiguration(configId: String): SavedTouchConfig?
    suspend fun deleteConfiguration(configId: String)
    suspend fun setActiveConfiguration(config: SavedTouchConfig?)
    suspend fun updateRushwulfSettings(transform: (RushwulfSettings) -> RushwulfSettings)
}
