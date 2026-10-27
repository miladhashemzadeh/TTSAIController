package com.vampyrelord.ttsaicontroller.features.tts.data.repository

import com.vampyrelord.ttsaicontroller.core.rtk.RtkAction
import com.vampyrelord.ttsaicontroller.core.rtk.RtkStore
import com.vampyrelord.ttsaicontroller.core.rtk.currentTimeMillis
import com.vampyrelord.ttsaicontroller.features.tts.data.datasource.InMemoryTtsQueueDataSource
import com.vampyrelord.ttsaicontroller.features.tts.data.datasource.TtsEngineDataSource
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.SpeechJob
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.SpeechJobStatus
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.TtsEngineStatus
import com.vampyrelord.ttsaicontroller.features.tts.domain.model.TtsVoice
import com.vampyrelord.ttsaicontroller.features.tts.domain.repository.TtsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class TtsRepositoryImpl(
    private val engineDataSource: TtsEngineDataSource,
    private val queueDataSource: InMemoryTtsQueueDataSource,
    private val rtkStore: RtkStore,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : TtsRepository {

    private val _engineStatus = MutableStateFlow(TtsEngineStatus())
    override val engineStatus: StateFlow<TtsEngineStatus> = _engineStatus.asStateFlow()

    override val speechQueue: StateFlow<List<SpeechJob>> = queueDataSource.queue
    override val speechHistory: StateFlow<List<SpeechJob>> = queueDataSource.history

    init {
        // Sync engine data source status with repository status
        engineDataSource.engineStatus.onEach { engStat ->
            _engineStatus.update {
                it.copy(
                    isSpeaking = engStat.isSpeaking,
                    isPaused = engStat.isPaused,
                    activeJob = engStat.activeJob,
                    queueSize = queueDataSource.queue.value.size,
                    totalProcessedJobs = engStat.totalProcessedJobs
                )
            }
        }.launchIn(scope)

        queueDataSource.queue.onEach { q ->
            _engineStatus.update { it.copy(queueSize = q.size) }
        }.launchIn(scope)

        engineDataSource.setOnJobCompletedListener { completedJob ->
            queueDataSource.addToHistory(completedJob)
            rtkStore.dispatch(
                RtkAction(
                    type = "tts/job_completed",
                    payload = mapOf("id" to completedJob.id, "status" to completedJob.status.name),
                    meta = mapOf("source" to completedJob.source)
                )
            )
            // Play next in queue if available
            val next = queueDataSource.poll()
            if (next != null) {
                engineDataSource.playJob(next)
            }
        }
    }

    override suspend fun getAvailableVoices(): List<TtsVoice> = engineDataSource.getVoices()

    override suspend fun speak(
        text: String,
        voice: TtsVoice,
        pitch: Float,
        rate: Float,
        volume: Float,
        source: String
    ): SpeechJob {
        val job = SpeechJob(
            id = "tts-${currentTimeMillis()}-${Random.nextInt(100, 999)}",
            text = text,
            voice = voice,
            pitch = pitch,
            rate = rate,
            volume = volume,
            status = SpeechJobStatus.QUEUED,
            source = source
        )

        rtkStore.dispatch(
            RtkAction(
                type = "tts/speak_requested",
                payload = mapOf(
                    "id" to job.id,
                    "text" to job.text,
                    "voice" to job.voice.name,
                    "pitch" to job.pitch,
                    "rate" to job.rate
                ),
                meta = mapOf("source" to source)
            )
        )

        if (!_engineStatus.value.isSpeaking && !_engineStatus.value.isPaused) {
            engineDataSource.playJob(job)
        } else {
            queueDataSource.enqueue(job)
        }

        return job
    }

    override suspend fun pause() {
        engineDataSource.pause()
        rtkStore.dispatch(
            RtkAction(type = "tts/paused", payload = emptyMap<String, String>())
        )
    }

    override suspend fun resume() {
        engineDataSource.resume()
        rtkStore.dispatch(
            RtkAction(type = "tts/resumed", payload = emptyMap<String, String>())
        )
    }

    override suspend fun stop() {
        engineDataSource.stop()
        rtkStore.dispatch(
            RtkAction(type = "tts/stopped", payload = emptyMap<String, String>())
        )
    }

    override suspend fun clearQueue() {
        queueDataSource.clearQueue()
        rtkStore.dispatch(
            RtkAction(type = "tts/queue_cleared", payload = emptyMap<String, String>())
        )
    }

    override suspend fun cancelJob(jobId: String) {
        queueDataSource.remove(jobId)
    }
}
