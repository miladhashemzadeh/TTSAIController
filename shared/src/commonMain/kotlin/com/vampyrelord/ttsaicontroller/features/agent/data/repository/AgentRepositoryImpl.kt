package com.vampyrelord.ttsaicontroller.features.agent.data.repository

import com.vampyrelord.ttsaicontroller.core.rtk.AgentActionDispatcher
import com.vampyrelord.ttsaicontroller.core.rtk.AgentAuditLog
import com.vampyrelord.ttsaicontroller.core.rtk.AgentAuditRecord
import com.vampyrelord.ttsaicontroller.core.rtk.AgentToolRegistry
import com.vampyrelord.ttsaicontroller.core.rtk.ToolDefinition
import com.vampyrelord.ttsaicontroller.core.rtk.ToolResult
import com.vampyrelord.ttsaicontroller.core.rtk.currentTimeMillis
import com.vampyrelord.ttsaicontroller.features.agent.domain.model.AgentCommandResult
import com.vampyrelord.ttsaicontroller.features.agent.domain.model.AgentProfile
import com.vampyrelord.ttsaicontroller.features.agent.domain.repository.AgentRepository
import kotlinx.coroutines.flow.StateFlow

class AgentRepositoryImpl(
    private val toolRegistry: AgentToolRegistry,
    private val dispatcher: AgentActionDispatcher,
    private val auditLog: AgentAuditLog
) : AgentRepository {

    override val auditLogs: StateFlow<List<AgentAuditRecord>> = auditLog.records

    override val agentProfiles: List<AgentProfile> = listOf(
        AgentProfile(
            id = "agent-orchestrator",
            name = "Autonomous Orchestrator",
            role = "SYSTEM_CONTROLLER",
            description = "High-autonomy agent capable of dispatching speech commands, monitoring playback, and reacting to state changes."
        ),
        AgentProfile(
            id = "agent-announcer",
            name = "Audio Announcer",
            role = "ANNOUNCER",
            description = "Focused on queueing priority notifications, alerts, and system broadcasts."
        ),
        AgentProfile(
            id = "agent-tester",
            name = "RTK Verification Agent",
            role = "TEST_RUNNER",
            description = "Specialized in exercising tools, checking engine health, and verifying state slice transitions."
        )
    )

    override suspend fun getAvailableTools(): List<ToolDefinition> {
        return toolRegistry.getDefinitions()
    }

    override suspend fun executeCommand(prompt: String, caller: String): AgentCommandResult {
        val startTime = currentTimeMillis()
        val result = dispatcher.executeNaturalCommand(prompt, caller)
        val duration = currentTimeMillis() - startTime

        return when (result) {
            is ToolResult.Success -> {
                AgentCommandResult(
                    commandText = prompt,
                    toolName = "auto_routed",
                    summary = result.summary,
                    isSuccess = true,
                    durationMs = duration
                )
            }
            is ToolResult.Failure -> {
                AgentCommandResult(
                    commandText = prompt,
                    toolName = "auto_routed",
                    summary = result.error,
                    isSuccess = false,
                    durationMs = duration
                )
            }
        }
    }

    override suspend fun executeTool(
        toolName: String,
        params: Map<String, Any?>,
        caller: String
    ): ToolResult {
        return dispatcher.executeTool(toolName, params, caller)
    }

    override suspend fun clearAuditLogs() {
        auditLog.clear()
    }
}
