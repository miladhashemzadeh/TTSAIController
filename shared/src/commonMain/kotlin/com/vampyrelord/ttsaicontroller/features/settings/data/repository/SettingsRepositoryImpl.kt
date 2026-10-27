package com.vampyrelord.ttsaicontroller.features.settings.data.repository

import com.vampyrelord.ttsaicontroller.core.rtk.RtkAction
import com.vampyrelord.ttsaicontroller.core.rtk.RtkStore
import com.vampyrelord.ttsaicontroller.features.settings.data.datasource.InMemorySettingsDataSource
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppSettings
import com.vampyrelord.ttsaicontroller.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class SettingsRepositoryImpl(
    private val dataSource: InMemorySettingsDataSource,
    private val rtkStore: RtkStore
) : SettingsRepository {

    override val settings: StateFlow<AppSettings> = dataSource.settings

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        dataSource.update(transform)
        val updated = dataSource.settings.value
        rtkStore.dispatch(
            RtkAction(
                type = "settings/updated",
                payload = mapOf(
                    "themeMode" to updated.themeMode.name,
                    "agentAutonomous" to updated.agentAutonomousEnabled,
                    "audioRouting" to updated.audioRouting.name
                )
            )
        )
    }

    override suspend fun resetDefaults() {
        dataSource.reset()
        rtkStore.dispatch(
            RtkAction(type = "settings/reset_defaults", payload = emptyMap<String, String>())
        )
    }
}
