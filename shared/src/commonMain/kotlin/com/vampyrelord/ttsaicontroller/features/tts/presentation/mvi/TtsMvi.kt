package com.vampyrelord.ttsaicontroller.features.tts.presentation.mvi

import com.vampyrelord.ttsaicontroller.core.architecture.MviEffect
import com.vampyrelord.ttsaicontroller.core.architecture.MviIntent
import com.vampyrelord.ttsaicontroller.core.architecture.MviState
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.SpeechJob
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.TtsEngineStatus
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.TtsVoice

data class TtsState(
    val inputText: String = "Welcome to TTS AI Controller. Layered clean architecture with MVI and Agent RTK is active.",
    val selectedVoice: TtsVoice? = null,
    val availableVoices: List<TtsVoice> = emptyList(),
    val pitch: Float = 1.0f,
    val rate: Float = 1.0f,
    val volume: Float = 1.0f,
    val engineStatus: TtsEngineStatus = TtsEngineStatus(),
    val queue: List<SpeechJob> = emptyList(),
    val history: List<SpeechJob> = emptyList(),
    val isLoadingVoices: Boolean = false
) : MviState

sealed interface TtsIntent : MviIntent {
    data class UpdateInputText(val text: String) : TtsIntent
    data class SelectVoice(val voice: TtsVoice) : TtsIntent
    data class UpdatePitch(val pitch: Float) : TtsIntent
    data class UpdateRate(val rate: Float) : TtsIntent
    data class UpdateVolume(val volume: Float) : TtsIntent
    data object SpeakCurrentText : TtsIntent
    data class QuickSpeak(val text: String) : TtsIntent
    data object PausePlayback : TtsIntent
    data object ResumePlayback : TtsIntent
    data object StopPlayback : TtsIntent
    data object ClearQueue : TtsIntent
    data class CancelJob(val jobId: String) : TtsIntent
}

sealed interface TtsEffect : MviEffect {
    data class ShowToast(val message: String) : TtsEffect
    data class SpeechQueued(val jobId: String) : TtsEffect
}
