package com.vampyrelord.ttsaicontroller.core.rtk

/**
 * Standard Redux Toolkit (RTK) Action structure.
 * Represents an immutable action dispatched either by human UI or an autonomous AI Agent.
 *
 * @param type The action identifier, conventionally "slice/actionName".
 * @param payload The strongly typed payload data.
 * @param meta Metadata such as sender ("USER", "AGENT", "SYSTEM"), trace IDs, or priority.
 * @param timestamp Epoch millisecond timestamp when the action was created.
 */
data class RtkAction<T>(
    val type: String,
    val payload: T,
    val meta: Map<String, String> = emptyMap(),
    val timestamp: Long = currentTimeMillis()
)

expect fun currentTimeMillis(): Long
