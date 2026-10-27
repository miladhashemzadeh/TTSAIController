package com.vampyrelord.ttsaicontroller.core.rtk

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AgentExecutionStatus {
    PENDING,
    SUCCESS,
    FAILED
}

/**
 * Audit record tracking an individual Agent interaction, tool invocation, or RTK action dispatch.
 */
data class AgentAuditRecord(
    val id: String,
    val timestamp: Long = currentTimeMillis(),
    val caller: String,
    val toolName: String,
    val inputParams: Map<String, Any?>,
    val status: AgentExecutionStatus,
    val outputSummary: String,
    val durationMs: Long
)

/**
 * In-memory thread-safe audit store for Agent activities.
 */
class AgentAuditLog(private val maxEntries: Int = 100) {
    private val _records = MutableStateFlow<List<AgentAuditRecord>>(emptyList())
    val records: StateFlow<List<AgentAuditRecord>> = _records.asStateFlow()

    fun record(entry: AgentAuditRecord) {
        _records.update { current ->
            (listOf(entry) + current).take(maxEntries)
        }
    }

    fun clear() {
        _records.value = emptyList()
    }
}
