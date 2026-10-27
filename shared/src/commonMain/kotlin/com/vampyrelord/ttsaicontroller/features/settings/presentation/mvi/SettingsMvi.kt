package com.vampyrelord.ttsaicontroller.features.settings.presentation.mvi

import com.vampyrelord.ttsaicontroller.core.architecture.MviEffect
import com.vampyrelord.ttsaicontroller.core.architecture.MviIntent
import com.vampyrelord.ttsaicontroller.core.architecture.MviState
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppSettings
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppThemeMode
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AudioOutputRouting

data class SettingsState(
    val settings: AppSettings = AppSettings(),
    val platformName: String = "",
    val isSaving: Boolean = false
) : MviState

sealed interface SettingsIntent : MviIntent {
    data class SetThemeMode(val mode: AppThemeMode) : SettingsIntent
    data class ToggleAutoPlay(val enabled: Boolean) : SettingsIntent
    data class ToggleAgentAutonomous(val enabled: Boolean) : SettingsIntent
    data class ToggleWaveform(val enabled: Boolean) : SettingsIntent
    data class SetAudioRouting(val routing: AudioOutputRouting) : SettingsIntent
    data object ResetToDefaults : SettingsIntent
}

sealed interface SettingsEffect : MviEffect {
    data class ShowToast(val message: String) : SettingsEffect
}
