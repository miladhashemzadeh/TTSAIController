package com.vampyrelord.ttsaicontroller.features.agent.domain.model

data class AgentProfile(
    val id: String,
    val name: String,
    val role: String,
    val description: String,
    val isEnabled: Boolean = true
)

data class AgentCommandResult(
    val commandText: String,
    val toolName: String,
    val summary: String,
    val isSuccess: Boolean,
    val durationMs: Long
)
