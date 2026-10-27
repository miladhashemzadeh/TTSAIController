package com.vampyrelord.ttsaicontroller.features.tts.domain.model

import com.vampyrelord.ttsaicontroller.core.rtk.currentTimeMillis

data class TtsVoice(
    val id: String,
    val name: String,
    val language: String,
    val gender: String,
    val isNeural: Boolean = true,
    val pitchRange: ClosedFloatingPointRange<Float> = 0.5f..2.0f,
    val rateRange: ClosedFloatingPointRange<Float> = 0.5f..2.5f
)

enum class SpeechJobStatus {
    QUEUED,
    SYNTHESIZING,
    PLAYING,
    PAUSED,
    COMPLETED,
    CANCELLED,
    ERROR
}

data class SpeechJob(
    val id: String,
    val text: String,
    val voice: TtsVoice,
    val pitch: Float = 1.0f,
    val rate: Float = 1.0f,
    val volume: Float = 1.0f,
    val status: SpeechJobStatus = SpeechJobStatus.QUEUED,
    val submittedAt: Long = currentTimeMillis(),
    val source: String = "USER", // "USER" or "AGENT"
    val durationSeconds: Float = 0f
)

data class TtsEngineStatus(
    val isSpeaking: Boolean = false,
    val isPaused: Boolean = false,
    val activeJob: SpeechJob? = null,
    val queueSize: Int = 0,
    val totalProcessedJobs: Int = 0
)
