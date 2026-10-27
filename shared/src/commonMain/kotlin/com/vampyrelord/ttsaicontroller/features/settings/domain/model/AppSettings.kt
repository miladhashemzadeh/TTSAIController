package com.vampyrelord.ttsaicontroller.features.settings.domain.model

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AudioOutputRouting {
    DEFAULT_SPEAKERS,
    HEADPHONES,
    VIRTUAL_AUDIO_CABLE
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val autoPlayOnQueue: Boolean = true,
    val defaultPitch: Float = 1.0f,
    val defaultRate: Float = 1.0f,
    val defaultVolume: Float = 1.0f,
    val agentAutonomousEnabled: Boolean = true,
    val showWaveformAnimation: Boolean = true,
    val audioRouting: AudioOutputRouting = AudioOutputRouting.DEFAULT_SPEAKERS
)
