package com.vampyrelord.ttsaicontroller.features.dashboard.domain.model

import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus

data class DashboardMetrics(
    val transcriptionStatus: TranscriptionStatus = TranscriptionStatus.IDLE,
    val isListening: Boolean = false,
    val lastTranscript: String? = null,
    val aiEngineName: String = "AI STT (Pending)",
    val isTouchRunning: Boolean = false,
    val totalSavedTouchConfigs: Int = 0
)
