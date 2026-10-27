package com.vampyrelord.ttsaicontroller.features.dashboard.domain.usecase

import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.features.agent.domain.repository.AgentRepository
import com.vampyrelord.ttsaicontroller.features.dashboard.domain.model.DashboardMetrics
import com.vampyrelord.ttsaicontroller.features.settings.domain.repository.SettingsRepository
import com.vampyrelord.ttsaicontroller.features.tts.domain.repository.TtsRepository

class GetDashboardMetricsUseCase(
    private val ttsRepository: TtsRepository,
    private val agentRepository: AgentRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): AppResult<DashboardMetrics> {
        return try {
            val ttsStatus = ttsRepository.engineStatus.value
            val auditLogs = agentRepository.auditLogs.value
            val settings = settingsRepository.settings.value
            val tools = agentRepository.getAvailableTools()

            val metrics = DashboardMetrics(
                isSpeaking = ttsStatus.isSpeaking,
                isPaused = ttsStatus.isPaused,
                activeJobText = ttsStatus.activeJob?.text,
                queueCount = ttsStatus.queueSize,
                processedCount = ttsStatus.totalProcessedJobs,
                totalAgentTools = tools.size,
                totalAgentCommands = auditLogs.size,
                isAgentAutonomous = settings.agentAutonomousEnabled
            )
            AppResult.Success(metrics)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}
