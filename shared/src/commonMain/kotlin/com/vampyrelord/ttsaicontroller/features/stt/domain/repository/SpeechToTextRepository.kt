package com.vampyrelord.ttsaicontroller.features.stt.domain.repository

import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.AudioConfig
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.SttEngineCapability
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionResult
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Shared Speech-to-Text (STT) repository contract.
 * Exposes core speech-to-text shared functions ready for an AI model implementation.
 */
interface SpeechToTextRepository {

    /** Current transcription status (IDLE, LISTENING, PROCESSING, COMPLETED, ERROR) */
    val transcriptionStatus: StateFlow<TranscriptionStatus>

    /** Latest completed or active transcription result */
    val activeTranscription: StateFlow<TranscriptionResult?>

    /** Live streaming partial transcript text flow emitted while speaking */
    val liveTranscriptFlow: Flow<String>

    /**
     * Start live audio capture and speech recognition session.
     */
    suspend fun startListening(audioConfig: AudioConfig = AudioConfig()): AppResult<Unit>

    /**
     * Stop active audio capture, finalize recognition via the AI model, and return the transcript.
     */
    suspend fun stopListening(): AppResult<TranscriptionResult>

    /**
     * Cancel the active listening session and discard audio buffers.
     */
    suspend fun cancelListening()

    /**
     * Transcribe a raw audio buffer (e.g. PCM, WAV byte array) using the configured AI model.
     */
    suspend fun transcribeAudio(
        audioBytes: ByteArray,
        audioConfig: AudioConfig = AudioConfig()
    ): AppResult<TranscriptionResult>

    /**
     * Transcribe a local audio file at the specified file path.
     */
    suspend fun transcribeFile(
        filePath: String,
        audioConfig: AudioConfig = AudioConfig()
    ): AppResult<TranscriptionResult>

    /**
     * Query hardware and AI model capabilities.
     */
    fun checkCapability(): SttEngineCapability
}
