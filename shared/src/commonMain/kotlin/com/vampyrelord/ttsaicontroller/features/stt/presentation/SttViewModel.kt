package com.vampyrelord.ttsaicontroller.features.stt.presentation

import androidx.lifecycle.viewModelScope
import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.core.architecture.BaseMviViewModel
import com.vampyrelord.ttsaicontroller.features.stt.domain.repository.SpeechToTextRepository
import com.vampyrelord.ttsaicontroller.features.stt.domain.usecase.CancelSpeechToTextUseCase
import com.vampyrelord.ttsaicontroller.features.stt.domain.usecase.GetSttCapabilityUseCase
import com.vampyrelord.ttsaicontroller.features.stt.domain.usecase.StartSpeechToTextUseCase
import com.vampyrelord.ttsaicontroller.features.stt.domain.usecase.StopSpeechToTextUseCase
import com.vampyrelord.ttsaicontroller.features.stt.presentation.mvi.SttEffect
import com.vampyrelord.ttsaicontroller.features.stt.presentation.mvi.SttIntent
import com.vampyrelord.ttsaicontroller.features.stt.presentation.mvi.SttState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SttViewModel(
    private val repository: SpeechToTextRepository,
    private val startSpeechToTextUseCase: StartSpeechToTextUseCase,
    private val stopSpeechToTextUseCase: StopSpeechToTextUseCase,
    private val cancelSpeechToTextUseCase: CancelSpeechToTextUseCase,
    private val getSttCapabilityUseCase: GetSttCapabilityUseCase
) : BaseMviViewModel<SttState, SttIntent, SttEffect>(
    SttState(
        status = repository.transcriptionStatus.value,
        lastResult = repository.activeTranscription.value,
        capability = getSttCapabilityUseCase()
    )
) {

    init {
        repository.transcriptionStatus.onEach { stat ->
            setState { copy(status = stat, isRecording = stat == com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus.LISTENING) }
        }.launchIn(viewModelScope)

        repository.activeTranscription.onEach { res ->
            if (res != null) {
                setState { copy(lastResult = res, liveTranscript = res.fullText) }
            }
        }.launchIn(viewModelScope)

        repository.liveTranscriptFlow.onEach { partial ->
            setState { copy(liveTranscript = partial) }
        }.launchIn(viewModelScope)
    }

    public override suspend fun handleIntent(intent: SttIntent) {
        when (intent) {
            is SttIntent.StartListening -> {
                setState { copy(status = com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus.LISTENING, isRecording = true) }
                when (val res = startSpeechToTextUseCase(currentState.audioConfig)) {
                    is AppResult.Success -> {
                        sendEffect(SttEffect.ShowMessage("Listening started. Speak into the microphone."))
                    }
                    is AppResult.Error -> {
                        setState { copy(status = com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus.ERROR, isRecording = false, errorMessage = res.message) }
                        sendEffect(SttEffect.ShowMessage("STT Error: ${res.message}"))
                    }
                    is AppResult.Loading -> Unit
                }
            }
            is SttIntent.StopListening -> {
                when (val res = stopSpeechToTextUseCase()) {
                    is AppResult.Success -> {
                        setState {
                            copy(
                                status = com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus.COMPLETED,
                                isRecording = false,
                                lastResult = res.data,
                                liveTranscript = res.data.fullText
                            )
                        }
                        sendEffect(SttEffect.TranscriptionCompleted(res.data))
                        sendEffect(SttEffect.ShowMessage("Transcription finalized."))
                    }
                    is AppResult.Error -> {
                        setState { copy(status = com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus.ERROR, isRecording = false, errorMessage = res.message) }
                        sendEffect(SttEffect.ShowMessage("Transcription error: ${res.message}"))
                    }
                    is AppResult.Loading -> Unit
                }
            }
            is SttIntent.CancelListening -> {
                cancelSpeechToTextUseCase()
                setState { copy(status = com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus.IDLE, isRecording = false) }
                sendEffect(SttEffect.ShowMessage("Listening cancelled."))
            }
            is SttIntent.UpdateLanguage -> {
                setState { copy(audioConfig = audioConfig.copy(language = intent.language)) }
            }
            is SttIntent.ClearTranscript -> {
                setState { copy(liveTranscript = "", lastResult = null) }
            }
            is SttIntent.DismissError -> {
                setState { copy(errorMessage = null) }
            }
        }
    }
}
