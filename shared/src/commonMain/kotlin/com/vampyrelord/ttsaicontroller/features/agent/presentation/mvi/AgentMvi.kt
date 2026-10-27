package com.vampyrelord.ttsaicontroller.features.agent.presentation.mvi

import com.vampyrelord.ttsaicontroller.core.architecture.MviEffect
import com.vampyrelord.ttsaicontroller.core.architecture.MviIntent
import com.vampyrelord.ttsaicontroller.core.architecture.MviState
import com.vampyrelord.ttsaicontroller.core.rtk.AgentAuditRecord
import com.vampyrelord.ttsaicontroller.core.rtk.ToolDefinition
import com.vampyrelord.ttsaicontroller.features.agent.domain.model.AgentCommandResult
import com.vampyrelord.ttsaicontroller.features.agent.domain.model.AgentProfile

data class AgentState(
    val promptInput: String = "speak Welcome agent. RTK command execution pipeline is ready.",
    val selectedProfile: AgentProfile? = null,
    val availableProfiles: List<AgentProfile> = emptyList(),
    val availableTools: List<ToolDefinition> = emptyList(),
    val auditLogs: List<AgentAuditRecord> = emptyList(),
    val isExecuting: Boolean = false,
    val lastResult: AgentCommandResult? = null,
    val rtkStoreSnapshot: Map<String, String> = emptyMap(),
    val activeTab: AgentWorkspaceTab = AgentWorkspaceTab.CONSOLE
) : MviState

enum class AgentWorkspaceTab {
    CONSOLE,
    TOOLS,
    RTK_STATE,
    AUDIT_LOG
}

sealed interface AgentIntent : MviIntent {
    data class UpdatePrompt(val prompt: String) : AgentIntent
    data object ExecutePrompt : AgentIntent
    data class ExecuteToolDirectly(val toolName: String, val params: Map<String, Any?>) : AgentIntent
    data class SelectProfile(val profile: AgentProfile) : AgentIntent
    data class SwitchTab(val tab: AgentWorkspaceTab) : AgentIntent
    data object ClearAuditLogs : AgentIntent
    data object RefreshRtkSnapshot : AgentIntent
}

sealed interface AgentEffect : MviEffect {
    data class ShowToast(val message: String) : AgentEffect
    data class ExecutionFinished(val summary: String, val isSuccess: Boolean) : AgentEffect
}
