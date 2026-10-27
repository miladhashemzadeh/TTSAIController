package com.vampyrelord.ttsaicontroller.features.stt.domain.usecase

import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.AudioConfig
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.SttEngineCapability
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionResult
import com.vampyrelord.ttsaicontroller.features.stt.domain.repository.SpeechToTextRepository

class StartSpeechToTextUseCase(
    private val repository: SpeechToTextRepository
) {
    suspend operator fun invoke(audioConfig: AudioConfig = AudioConfig()): AppResult<Unit> {
        return repository.startListening(audioConfig)
    }
}

class StopSpeechToTextUseCase(
    private val repository: SpeechToTextRepository
) {
    suspend operator fun invoke(): AppResult<TranscriptionResult> {
        return repository.stopListening()
    }
}

class CancelSpeechToTextUseCase(
    private val repository: SpeechToTextRepository
) {
    suspend operator fun invoke() {
        repository.cancelListening()
    }
}

class TranscribeAudioUseCase(
    private val repository: SpeechToTextRepository
) {
    suspend operator fun invoke(
        audioBytes: ByteArray,
        audioConfig: AudioConfig = AudioConfig()
    ): AppResult<TranscriptionResult> {
        return repository.transcribeAudio(audioBytes, audioConfig)
    }
}

class TranscribeFileUseCase(
    private val repository: SpeechToTextRepository
) {
    suspend operator fun invoke(
        filePath: String,
        audioConfig: AudioConfig = AudioConfig()
    ): AppResult<TranscriptionResult> {
        return repository.transcribeFile(filePath, audioConfig)
    }
}

class GetSttCapabilityUseCase(
    private val repository: SpeechToTextRepository
) {
    operator fun invoke(): SttEngineCapability {
        return repository.checkCapability()
    }
}
