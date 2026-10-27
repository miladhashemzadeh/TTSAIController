package com.vampyrelord.ttsaicontroller.features.settings.presentation

import androidx.lifecycle.viewModelScope
import com.vampyrelord.ttsaicontroller.core.architecture.BaseMviViewModel
import com.vampyrelord.ttsaicontroller.features.settings.domain.repository.SettingsRepository
import com.vampyrelord.ttsaicontroller.features.settings.domain.usecase.ResetSettingsUseCase
import com.vampyrelord.ttsaicontroller.features.settings.domain.usecase.UpdateSettingsUseCase
import com.vampyrelord.ttsaicontroller.features.settings.presentation.mvi.SettingsEffect
import com.vampyrelord.ttsaicontroller.features.settings.presentation.mvi.SettingsIntent
import com.vampyrelord.ttsaicontroller.features.settings.presentation.mvi.SettingsState
import com.vampyrelord.ttsaicontroller.getPlatform
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase
) : BaseMviViewModel<SettingsState, SettingsIntent, SettingsEffect>(
    SettingsState(platformName = getPlatform().name)
) {

    init {
        settingsRepository.settings.onEach { updated ->
            setState { copy(settings = updated) }
        }.launchIn(viewModelScope)
    }

    override suspend fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetThemeMode -> {
                updateSettingsUseCase { it.copy(themeMode = intent.mode) }
            }
            is SettingsIntent.ToggleAutoPlay -> {
                updateSettingsUseCase { it.copy(autoPlayOnQueue = intent.enabled) }
            }
            is SettingsIntent.ToggleAgentAutonomous -> {
                updateSettingsUseCase { it.copy(agentAutonomousEnabled = intent.enabled) }
            }
            is SettingsIntent.ToggleWaveform -> {
                updateSettingsUseCase { it.copy(showWaveformAnimation = intent.enabled) }
            }
            is SettingsIntent.SetAudioRouting -> {
                updateSettingsUseCase { it.copy(audioRouting = intent.routing) }
            }
            is SettingsIntent.ResetToDefaults -> {
                resetSettingsUseCase()
                sendEffect(SettingsEffect.ShowToast("Settings reset to defaults"))
            }
        }
    }
}
