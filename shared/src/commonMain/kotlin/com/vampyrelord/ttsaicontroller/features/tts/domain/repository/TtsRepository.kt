package com.vampyrelord.ttsaicontroller.features.tts.domain.repository

import com.vampyrelord.ttsaicontroller.features.tts.domain.model.SpeechJob
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.TtsEngineStatus
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.TtsVoice
import kotlinx.coroutines.flow.StateFlow

interface TtsRepository {
    val engineStatus: StateFlow<TtsEngineStatus>
    val speechQueue: StateFlow<List<SpeechJob>>
    val speechHistory: StateFlow<List<SpeechJob>>

    suspend fun getAvailableVoices(): List<TtsVoice>
    suspend fun speak(
        text: String,
        voice: TtsVoice,
        pitch: Float = 1.0f,
        rate: Float = 1.0f,
        volume: Float = 1.0f,
        source: String = "USER"
    ): SpeechJob

    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
    suspend fun clearQueue()
    suspend fun cancelJob(jobId: String)
}
