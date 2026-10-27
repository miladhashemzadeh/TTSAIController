package com.vampyrelord.ttsaicontroller.features.agent.domain.usecase

import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.core.rtk.ToolDefinition
import com.vampyrelord.ttsaicontroller.core.rtk.ToolResult
import com.vampyrelord.ttsaicontroller.features.agent.domain.model.AgentCommandResult
import com.vampyrelord.ttsaicontroller.features.agent.domain.repository.AgentRepository

class ExecuteAgentCommandUseCase(private val repository: AgentRepository) {
    suspend operator fun invoke(prompt: String, caller: String = "AI_AGENT"): AppResult<AgentCommandResult> {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) {
            return AppResult.Error(message = "Agent command prompt cannot be empty")
        }
        return try {
            val result = repository.executeCommand(trimmed, caller)
            AppResult.Success(result)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}

class ExecuteDirectToolUseCase(private val repository: AgentRepository) {
    suspend operator fun invoke(
        toolName: String,
        params: Map<String, Any?>,
        caller: String = "AI_AGENT"
    ): AppResult<ToolResult> {
        return try {
            val result = repository.executeTool(toolName, params, caller)
            AppResult.Success(result)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}

class GetAgentToolsUseCase(private val repository: AgentRepository) {
    suspend operator fun invoke(): AppResult<List<ToolDefinition>> {
        return try {
            val tools = repository.getAvailableTools()
            AppResult.Success(tools)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}

class ClearAgentLogsUseCase(private val repository: AgentRepository) {
    suspend operator fun invoke(): AppResult<Unit> {
        return try {
            repository.clearAuditLogs()
            AppResult.Success(Unit)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}
