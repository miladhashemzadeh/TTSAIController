package com.vampyrelord.ttsaicontroller.features.dashboard.domain.usecase

import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.features.dashboard.domain.model.DashboardMetrics
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus
import com.vampyrelord.ttsaicontroller.features.stt.domain.repository.SpeechToTextRepository
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchConfigRepository
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchController
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchExecutionStatus

class GetDashboardMetricsUseCase(
    private val speechToTextRepository: SpeechToTextRepository,
    private val touchConfigRepository: TouchConfigRepository,
    private val touchController: TouchController
) {
    suspend operator fun invoke(): AppResult<DashboardMetrics> {
        return try {
            val sttStatus = speechToTextRepository.transcriptionStatus.value
            val active = speechToTextRepository.activeTranscription.value
            val capability = speechToTextRepository.checkCapability()
            val savedConfigs = touchConfigRepository.savedConfigurations.value
            val isTouchRunning = touchController.executionState.value.status == TouchExecutionStatus.RUNNING

            val metrics = DashboardMetrics(
                transcriptionStatus = sttStatus,
                isListening = sttStatus == TranscriptionStatus.LISTENING,
                lastTranscript = active?.fullText,
                aiEngineName = capability.engineName,
                isTouchRunning = isTouchRunning,
                totalSavedTouchConfigs = savedConfigs.size
            )
            AppResult.Success(metrics)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}
