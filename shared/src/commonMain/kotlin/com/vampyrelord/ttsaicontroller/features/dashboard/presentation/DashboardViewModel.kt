package com.vampyrelord.ttsaicontroller.features.dashboard.presentation

import androidx.lifecycle.viewModelScope
import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.core.architecture.BaseMviViewModel
import com.vampyrelord.ttsaicontroller.core.navigation.Navigator
import com.vampyrelord.ttsaicontroller.features.agent.domain.repository.AgentRepository
import com.vampyrelord.ttsaicontroller.features.dashboard.domain.usecase.GetDashboardMetricsUseCase
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.mvi.DashboardEffect
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.mvi.DashboardIntent
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.mvi.DashboardState
import com.vampyrelord.ttsaicontroller.features.tts.domain.repository.TtsRepository
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.SpeakTextUseCase
import com.vampyrelord.ttsaicontroller.features.tts.domain.usecase.StopSpeechUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getDashboardMetricsUseCase: GetDashboardMetricsUseCase,
    private val speakTextUseCase: SpeakTextUseCase,
    private val stopSpeechUseCase: StopSpeechUseCase,
    private val ttsRepository: TtsRepository,
    private val agentRepository: AgentRepository,
    private val navigator: Navigator
) : BaseMviViewModel<DashboardState, DashboardIntent, DashboardEffect>(DashboardState()) {

    init {
        // Automatically sync metrics when TTS status or Agent audit logs change
        combine(
            ttsRepository.engineStatus,
            ttsRepository.speechHistory,
            agentRepository.auditLogs
        ) { status, history, logs ->
            refreshMetrics()
            setState {
                copy(
                    recentJobs = history.take(3),
                    recentAgentLogs = logs.take(3)
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun refreshMetrics() {
        viewModelScope.launch {
            when (val res = getDashboardMetricsUseCase()) {
                is AppResult.Success -> setState { copy(metrics = res.data) }
                is AppResult.Error -> Unit
                is AppResult.Loading -> Unit
            }
        }
    }

    override suspend fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.Refresh -> {
                refreshMetrics()
            }
            is DashboardIntent.QuickSpeak -> {
                val voices = ttsRepository.getAvailableVoices()
                val voice = voices.firstOrNull()
                if (voice != null) {
                    speakTextUseCase(intent.text, voice, source = "DASHBOARD")
                } else {
                    sendEffect(DashboardEffect.ShowToast("No voice available"))
                }
            }
            is DashboardIntent.StopSpeech -> {
                stopSpeechUseCase()
            }
            is DashboardIntent.NavigateTo -> {
                navigator.navigateTo(intent.screen)
            }
        }
    }
}
