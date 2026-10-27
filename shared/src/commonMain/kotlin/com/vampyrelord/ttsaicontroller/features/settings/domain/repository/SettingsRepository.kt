package com.vampyrelord.ttsaicontroller.features.settings.domain.repository

import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppSettings
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<AppSettings>

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings)
    suspend fun resetDefaults()
}
