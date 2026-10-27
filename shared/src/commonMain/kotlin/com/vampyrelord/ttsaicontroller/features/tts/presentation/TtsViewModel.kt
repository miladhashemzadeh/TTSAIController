package com.vampyrelord.ttsaicontroller.features.tts.presentation

import androidx.lifecycle.viewModelScope
import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.core.architecture.BaseMviViewModel
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.TtsVoice
import com.vampyrelord.ttsaicontroller.features.tts.domain.repository.TtsRepository
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.ClearQueueUseCase
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.GetVoicesUseCase
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.PauseSpeechUseCase
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.ResumeSpeechUseCase
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.SpeakTextUseCase
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.StopSpeechUseCase
import com.vampyrelord.ttsaicontroller.features.tts.presentation.mvi.TtsEffect
import com.vampyrelord.ttsaicontroller.features.tts.presentation.mvi.TtsIntent
import com.vampyrelord.ttsaicontroller.features.tts.presentation.mvi.TtsState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TtsViewModel(
    private val ttsRepository: TtsRepository,
    private val speakTextUseCase: SpeakTextUseCase,
    private val stopSpeechUseCase: StopSpeechUseCase,
    private val pauseSpeechUseCase: PauseSpeechUseCase,
    private val resumeSpeechUseCase: ResumeSpeechUseCase,
    private val getVoicesUseCase: GetVoicesUseCase,
    private val clearQueueUseCase: ClearQueueUseCase
) : BaseMviViewModel<TtsState, TtsIntent, TtsEffect>(TtsState()) {

    init {
        // Observe repository states
        ttsRepository.engineStatus.onEach { status ->
            setState { copy(engineStatus = status) }
        }.launchIn(viewModelScope)

        ttsRepository.speechQueue.onEach { q ->
            setState { copy(queue = q) }
        }.launchIn(viewModelScope)

        ttsRepository.speechHistory.onEach { hist ->
            setState { copy(history = hist) }
        }.launchIn(viewModelScope)

        loadVoices()
    }

    private fun loadVoices() {
        viewModelScope.launch {
            setState { copy(isLoadingVoices = true) }
            when (val res = getVoicesUseCase()) {
                is AppResult.Success -> {
                    val voices = res.data
                    setState {
                        copy(
                            isLoadingVoices = false,
                            availableVoices = voices,
                            selectedVoice = selectedVoice ?: voices.firstOrNull()
                        )
                    }
                }
                is AppResult.Error -> {
                    setState { copy(isLoadingVoices = false) }
                    sendEffect(TtsEffect.ShowToast("Failed to load voices: ${res.message}"))
                }
                is AppResult.Loading -> Unit
            }
        }
    }

    override suspend fun handleIntent(intent: TtsIntent) {
        when (intent) {
            is TtsIntent.UpdateInputText -> {
                setState { copy(inputText = intent.text) }
            }
            is TtsIntent.SelectVoice -> {
                setState { copy(selectedVoice = intent.voice) }
            }
            is TtsIntent.UpdatePitch -> {
                setState { copy(pitch = intent.pitch) }
            }
            is TtsIntent.UpdateRate -> {
                setState { copy(rate = intent.rate) }
            }
            is TtsIntent.UpdateVolume -> {
                setState { copy(volume = intent.volume) }
            }
            is TtsIntent.SpeakCurrentText -> {
                val voice = currentState.selectedVoice ?: currentState.availableVoices.firstOrNull()
                if (voice == null) {
                    sendEffect(TtsEffect.ShowToast("No voice selected"))
                    return
                }
                triggerSpeech(
                    text = currentState.inputText,
                    voice = voice,
                    pitch = currentState.pitch,
                    rate = currentState.rate,
                    volume = currentState.volume
                )
            }
            is TtsIntent.QuickSpeak -> {
                val voice = currentState.selectedVoice ?: currentState.availableVoices.firstOrNull()
                if (voice != null) {
                    triggerSpeech(
                        text = intent.text,
                        voice = voice,
                        pitch = currentState.pitch,
                        rate = currentState.rate,
                        volume = currentState.volume
                    )
                }
            }
            is TtsIntent.PausePlayback -> {
                pauseSpeechUseCase()
            }
            is TtsIntent.ResumePlayback -> {
                resumeSpeechUseCase()
            }
            is TtsIntent.StopPlayback -> {
                stopSpeechUseCase()
            }
            is TtsIntent.ClearQueue -> {
                clearQueueUseCase()
            }
            is TtsIntent.CancelJob -> {
                ttsRepository.cancelJob(intent.jobId)
            }
        }
    }

    private suspend fun triggerSpeech(
        text: String,
        voice: TtsVoice,
        pitch: Float,
        rate: Float,
        volume: Float
    ) {
        when (val res = speakTextUseCase(text, voice, pitch, rate, volume, source = "USER")) {
            is AppResult.Success -> {
                sendEffect(TtsEffect.SpeechQueued(res.data.id))
            }
            is AppResult.Error -> {
                sendEffect(TtsEffect.ShowToast("Speech error: ${res.message}"))
            }
            is AppResult.Loading -> Unit
        }
    }
}
