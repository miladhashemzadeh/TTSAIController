package com.vampyrelord.ttsaicontroller.features.dashboard.domain.model

data class DashboardMetrics(
    val isSpeaking: Boolean = false,
    val isPaused: Boolean = false,
    val activeJobText: String? = null,
    val queueCount: Int = 0,
    val processedCount: Int = 0,
    val totalAgentTools: Int = 0,
    val totalAgentCommands: Int = 0,
    val isAgentAutonomous: Boolean = true
)
