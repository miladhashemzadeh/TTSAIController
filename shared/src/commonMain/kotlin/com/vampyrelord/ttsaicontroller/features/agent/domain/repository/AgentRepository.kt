package com.vampyrelord.ttsaicontroller.features.agent.domain.repository

import com.vampyrelord.ttsaicontroller.core.rtk.AgentAuditRecord
import com.vampyrelord.ttsaicontroller.core.rtk.ToolDefinition
import com.vampyrelord.ttsaicontroller.core.rtk.ToolResult
import com.vampyrelord.ttsaicontroller.features.agent.domain.model.AgentCommandResult
import com.vampyrelord.ttsaicontroller.features.agent.domain.model.AgentProfile
import kotlinx.coroutines.flow.StateFlow

interface AgentRepository {
    val auditLogs: StateFlow<List<AgentAuditRecord>>
    val agentProfiles: List<AgentProfile>

    suspend fun getAvailableTools(): List<ToolDefinition>
    suspend fun executeCommand(prompt: String, caller: String = "AI_AGENT"): AgentCommandResult
    suspend fun executeTool(toolName: String, params: Map<String, Any?>, caller: String = "AI_AGENT"): ToolResult
    suspend fun clearAuditLogs()
}
