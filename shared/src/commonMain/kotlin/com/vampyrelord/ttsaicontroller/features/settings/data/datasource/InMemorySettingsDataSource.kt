package com.vampyrelord.ttsaicontroller.features.settings.data.datasource

import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemorySettingsDataSource {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun update(transform: (AppSettings) -> AppSettings) {
        _settings.update(transform)
    }

    fun reset() {
        _settings.value = AppSettings()
    }
}
