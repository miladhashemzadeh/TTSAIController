package com.vampyrelord.ttsaicontroller.features.tts.data.datasource

import com.vampyrelord.ttsaicontroller.features.tts.domain.model.SpeechJob
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.SpeechJobStatus
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.TtsEngineStatus
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.TtsVoice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Text-to-Speech Engine Data Source.
 * Provides multiplatform voice catalog and simulated audio rendering pipeline.
 */
class TtsEngineDataSource(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val availableVoices = listOf(
        TtsVoice("voice-aria", "Aria Neural", "en-US", "Female", true),
        TtsVoice("voice-echo", "Echo AI", "en-US", "Male", true),
        TtsVoice("voice-nova", "Nova Smooth", "en-GB", "Female", true),
        TtsVoice("voice-orion", "Orion Deep", "en-US", "Male", false),
        TtsVoice("voice-serena", "Serena Calm", "en-AU", "Female", true)
    )

    private val _engineStatus = MutableStateFlow(TtsEngineStatus())
    val engineStatus: StateFlow<TtsEngineStatus> = _engineStatus.asStateFlow()

    private var playbackJob: Job? = null
    private var onJobCompletedListener: ((SpeechJob) -> Unit)? = null

    fun setOnJobCompletedListener(listener: (SpeechJob) -> Unit) {
        onJobCompletedListener = listener
    }

    fun getVoices(): List<TtsVoice> = availableVoices

    fun playJob(job: SpeechJob) {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            val estimatedDurationSeconds = ((job.text.length / 15f) / job.rate).coerceAtLeast(1.5f)
            val updatedJob = job.copy(
                status = SpeechJobStatus.PLAYING,
                durationSeconds = estimatedDurationSeconds
            )

            _engineStatus.update {
                it.copy(
                    isSpeaking = true,
                    isPaused = false,
                    activeJob = updatedJob
                )
            }

            val totalSteps = (estimatedDurationSeconds * 10).toInt()
            for (step in 0 until totalSteps) {
                while (_engineStatus.value.isPaused) {
                    delay(100)
                }
                delay(100)
            }

            val completedJob = updatedJob.copy(status = SpeechJobStatus.COMPLETED)
            _engineStatus.update {
                it.copy(
                    isSpeaking = false,
                    isPaused = false,
                    activeJob = null,
                    totalProcessedJobs = it.totalProcessedJobs + 1
                )
            }
            onJobCompletedListener?.invoke(completedJob)
        }
    }

    fun pause() {
        if (_engineStatus.value.isSpeaking && !_engineStatus.value.isPaused) {
            _engineStatus.update {
                it.copy(
                    isPaused = true,
                    activeJob = it.activeJob?.copy(status = SpeechJobStatus.PAUSED)
                )
            }
        }
    }

    fun resume() {
        if (_engineStatus.value.isPaused) {
            _engineStatus.update {
                it.copy(
                    isPaused = false,
                    activeJob = it.activeJob?.copy(status = SpeechJobStatus.PLAYING)
                )
            }
        }
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        val active = _engineStatus.value.activeJob
        if (active != null) {
            onJobCompletedListener?.invoke(active.copy(status = SpeechJobStatus.CANCELLED))
        }
        _engineStatus.update {
            it.copy(
                isSpeaking = false,
                isPaused = false,
                activeJob = null
            )
        }
    }
}
