package com.vampyrelord.ttsaicontroller.features.tts.data.datasource

import com.vampyrelord.ttsaicontroller.features.tts.domain.model.SpeechJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryTtsQueueDataSource(private val maxHistory: Int = 50) {
    private val _queue = MutableStateFlow<List<SpeechJob>>(emptyList())
    val queue: StateFlow<List<SpeechJob>> = _queue.asStateFlow()

    private val _history = MutableStateFlow<List<SpeechJob>>(emptyList())
    val history: StateFlow<List<SpeechJob>> = _history.asStateFlow()

    fun enqueue(job: SpeechJob) {
        _queue.update { it + job }
    }

    fun poll(): SpeechJob? {
        var polled: SpeechJob? = null
        _queue.update { current ->
            if (current.isNotEmpty()) {
                polled = current.first()
                current.drop(1)
            } else {
                current
            }
        }
        return polled
    }

    fun remove(jobId: String) {
        _queue.update { current -> current.filterNot { it.id == jobId } }
    }

    fun clearQueue() {
        _queue.value = emptyList()
    }

    fun addToHistory(job: SpeechJob) {
        _history.update { current ->
            (listOf(job) + current).take(maxHistory)
        }
    }
}
