package com.vampyrelord.ttsaicontroller.features.tts.domain.usecase

import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.SpeechJob
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.TtsVoice
import com.vampyrelord.ttsaicontroller.features.tts.domain.repository.TtsRepository

class SpeakTextUseCase(private val repository: TtsRepository) {
    suspend operator fun invoke(
        text: String,
        voice: TtsVoice,
        pitch: Float = 1.0f,
        rate: Float = 1.0f,
        volume: Float = 1.0f,
        source: String = "USER"
    ): AppResult<SpeechJob> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return AppResult.Error(message = "Speech text cannot be empty")
        }
        return try {
            val job = repository.speak(
                text = trimmed,
                voice = voice,
                pitch = pitch.coerceIn(voice.pitchRange),
                rate = rate.coerceIn(voice.rateRange),
                volume = volume.coerceIn(0f, 1f),
                source = source
            )
            AppResult.Success(job)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}

class StopSpeechUseCase(private val repository: TtsRepository) {
    suspend operator fun invoke(): AppResult<Unit> {
        return try {
            repository.stop()
            AppResult.Success(Unit)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}

class PauseSpeechUseCase(private val repository: TtsRepository) {
    suspend operator fun invoke(): AppResult<Unit> {
        return try {
            repository.pause()
            AppResult.Success(Unit)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}

class ResumeSpeechUseCase(private val repository: TtsRepository) {
    suspend operator fun invoke(): AppResult<Unit> {
        return try {
            repository.resume()
            AppResult.Success(Unit)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}

class GetVoicesUseCase(private val repository: TtsRepository) {
    suspend operator fun invoke(): AppResult<List<TtsVoice>> {
        return try {
            val voices = repository.getAvailableVoices()
            AppResult.Success(voices)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}

class ClearQueueUseCase(private val repository: TtsRepository) {
    suspend operator fun invoke(): AppResult<Unit> {
        return try {
            repository.clearQueue()
            AppResult.Success(Unit)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}
