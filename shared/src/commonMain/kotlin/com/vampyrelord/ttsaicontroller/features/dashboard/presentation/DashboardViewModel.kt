package com.vampyrelord.ttsaicontroller.features.dashboard.presentation

import androidx.lifecycle.viewModelScope
import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.core.architecture.BaseMviViewModel
import com.vampyrelord.ttsaicontroller.core.navigation.Navigator
import com.vampyrelord.ttsaicontroller.features.dashboard.domain.usecase.GetDashboardMetricsUseCase
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.mvi.DashboardEffect
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.mvi.DashboardIntent
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.mvi.DashboardState
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.AudioConfig
import com.vampyrelord.ttsaicontroller.features.stt.domain.repository.SpeechToTextRepository
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchConfigRepository
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchController
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getDashboardMetricsUseCase: GetDashboardMetricsUseCase,
    private val speechToTextRepository: SpeechToTextRepository,
    private val touchConfigRepository: TouchConfigRepository,
    private val touchController: TouchController,
    private val navigator: Navigator
) : BaseMviViewModel<DashboardState, DashboardIntent, DashboardEffect>(DashboardState()) {

    init {
        combine(
            speechToTextRepository.transcriptionStatus,
            speechToTextRepository.activeTranscription,
            touchController.executionState,
            touchConfigRepository.savedConfigurations
        ) { _, activeTranscription, _, _ ->
            refreshMetrics()
            setState {
                copy(
                    recentTranscriptions = if (activeTranscription != null) listOf(activeTranscription) else emptyList()
                )
            }
        }.launchIn(viewModelScope)
    }

    fun refreshMetrics() {
        viewModelScope.launch {
            when (val res = getDashboardMetricsUseCase()) {
                is AppResult.Success -> setState { copy(metrics = res.data) }
                is AppResult.Error -> Unit
                is AppResult.Loading -> Unit
            }
        }
    }

    public override suspend fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.Refresh -> {
                refreshMetrics()
            }
            is DashboardIntent.StartListening -> {
                when (val res = speechToTextRepository.startListening(AudioConfig())) {
                    is AppResult.Success -> {
                        setState { copy(metrics = metrics.copy(isListening = true, transcriptionStatus = com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus.LISTENING)) }
                        sendEffect(DashboardEffect.ShowToast("Listening started"))
                    }
                    is AppResult.Error -> sendEffect(DashboardEffect.ShowToast("Failed: ${res.message}"))
                    is AppResult.Loading -> Unit
                }
            }
            is DashboardIntent.StopListening -> {
                when (val res = speechToTextRepository.stopListening()) {
                    is AppResult.Success -> {
                        setState {
                            copy(
                                metrics = metrics.copy(
                                    isListening = false,
                                    transcriptionStatus = com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus.COMPLETED,
                                    lastTranscript = res.data.fullText
                                ),
                                recentTranscriptions = listOf(res.data)
                            )
                        }
                        sendEffect(DashboardEffect.ShowToast("Transcribed: ${res.data.fullText}"))
                    }
                    is AppResult.Error -> sendEffect(DashboardEffect.ShowToast("Failed: ${res.message}"))
                    is AppResult.Loading -> Unit
                }
            }
            is DashboardIntent.NavigateTo -> {
                navigator.navigateTo(intent.screen)
            }
        }
    }
}
