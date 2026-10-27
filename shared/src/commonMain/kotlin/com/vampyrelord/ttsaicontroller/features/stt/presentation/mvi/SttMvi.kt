package com.vampyrelord.ttsaicontroller.features.stt.presentation.mvi

import com.vampyrelord.ttsaicontroller.core.architecture.MviEffect
import com.vampyrelord.ttsaicontroller.core.architecture.MviIntent
import com.vampyrelord.ttsaicontroller.core.architecture.MviState
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.AudioConfig
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.SttEngineCapability
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionResult
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus

data class SttState(
    val status: TranscriptionStatus = TranscriptionStatus.IDLE,
    val liveTranscript: String = "",
    val lastResult: TranscriptionResult? = null,
    val audioConfig: AudioConfig = AudioConfig(),
    val isRecording: Boolean = false,
    val capability: SttEngineCapability = SttEngineCapability(),
    val errorMessage: String? = null
) : MviState

sealed interface SttIntent : MviIntent {
    data object StartListening : SttIntent
    data object StopListening : SttIntent
    data object CancelListening : SttIntent
    data class UpdateLanguage(val language: String) : SttIntent
    data object ClearTranscript : SttIntent
    data object DismissError : SttIntent
}

sealed interface SttEffect : MviEffect {
    data class ShowMessage(val message: String) : SttEffect
    data class TranscriptionCompleted(val result: TranscriptionResult) : SttEffect
}
