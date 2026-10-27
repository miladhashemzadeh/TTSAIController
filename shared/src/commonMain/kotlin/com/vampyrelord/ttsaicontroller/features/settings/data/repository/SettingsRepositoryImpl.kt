package com.vampyrelord.ttsaicontroller.features.settings.data.repository

import com.vampyrelord.ttsaicontroller.features.settings.data.datasource.InMemorySettingsDataSource
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppSettings
import com.vampyrelord.ttsaicontroller.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class SettingsRepositoryImpl(
    private val dataSource: InMemorySettingsDataSource
) : SettingsRepository {

    override val settings: StateFlow<AppSettings> = dataSource.settings

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        dataSource.update(transform)
    }

    override suspend fun resetDefaults() {
        dataSource.reset()
    }
}
