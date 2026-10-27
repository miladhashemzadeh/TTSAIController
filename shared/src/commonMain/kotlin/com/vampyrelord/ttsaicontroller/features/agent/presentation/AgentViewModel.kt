package com.vampyrelord.ttsaicontroller.features.agent.presentation

import androidx.lifecycle.viewModelScope
import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.core.architecture.BaseMviViewModel
import com.vampyrelord.ttsaicontroller.core.rtk.RtkStore
import com.vampyrelord.ttsaicontroller.core.rtk.ToolResult
import com.vampyrelord.ttsaicontroller.features.agent.domain.model.AgentCommandResult
import com.vampyrelord.ttsaicontroller.features.agent.domain.repository.AgentRepository
import com.vampyrelord.ttsaicontroller.features.agent.domain.usecase.ClearAgentLogsUseCase
import com.vampyrelord.ttsaicontroller.features.agent.domain.usecase.ExecuteAgentCommandUseCase
import com.vampyrelord.ttsaicontroller.features.agent.domain.usecase.ExecuteDirectToolUseCase
import com.vampyrelord.ttsaicontroller.features.agent.domain.usecase.GetAgentToolsUseCase
import com.vampyrelord.ttsaicontroller.features.agent.presentation.mvi.AgentEffect
import com.vampyrelord.ttsaicontroller.features.agent.presentation.mvi.AgentIntent
import com.vampyrelord.ttsaicontroller.features.agent.presentation.mvi.AgentState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AgentViewModel(
    private val agentRepository: AgentRepository,
    private val executeCommandUseCase: ExecuteAgentCommandUseCase,
    private val executeDirectToolUseCase: ExecuteDirectToolUseCase,
    private val getAgentToolsUseCase: GetAgentToolsUseCase,
    private val clearLogsUseCase: ClearAgentLogsUseCase,
    private val rtkStore: RtkStore
) : BaseMviViewModel<AgentState, AgentIntent, AgentEffect>(AgentState()) {

    init {
        // Observe audit logs
        agentRepository.auditLogs.onEach { logs ->
            setState { copy(auditLogs = logs) }
        }.launchIn(viewModelScope)

        // Observe RTK store state
        rtkStore.state.onEach { storeState ->
            val snapshot = storeState.mapValues { it.value?.toString() ?: "null" }
            setState { copy(rtkStoreSnapshot = snapshot) }
        }.launchIn(viewModelScope)

        loadInitialData()
    }

    private fun loadInitialData() {
        val profiles = agentRepository.agentProfiles
        setState {
            copy(
                availableProfiles = profiles,
                selectedProfile = profiles.firstOrNull()
            )
        }

        viewModelScope.launch {
            when (val res = getAgentToolsUseCase()) {
                is AppResult.Success -> setState { copy(availableTools = res.data) }
                is AppResult.Error -> sendEffect(AgentEffect.ShowToast("Failed to load tools: ${res.message}"))
                is AppResult.Loading -> Unit
            }
        }
    }

    override suspend fun handleIntent(intent: AgentIntent) {
        when (intent) {
            is AgentIntent.UpdatePrompt -> {
                setState { copy(promptInput = intent.prompt) }
            }
            is AgentIntent.ExecutePrompt -> {
                executePrompt()
            }
            is AgentIntent.ExecuteToolDirectly -> {
                executeTool(intent.toolName, intent.params)
            }
            is AgentIntent.SelectProfile -> {
                setState { copy(selectedProfile = intent.profile) }
            }
            is AgentIntent.SwitchTab -> {
                setState { copy(activeTab = intent.tab) }
            }
            is AgentIntent.ClearAuditLogs -> {
                clearLogsUseCase()
            }
            is AgentIntent.RefreshRtkSnapshot -> {
                val snapshot = rtkStore.state.value.mapValues { it.value?.toString() ?: "null" }
                setState { copy(rtkStoreSnapshot = snapshot) }
            }
        }
    }

    private suspend fun executePrompt() {
        val prompt = currentState.promptInput.trim()
        if (prompt.isEmpty()) {
            sendEffect(AgentEffect.ShowToast("Command cannot be empty"))
            return
        }

        setState { copy(isExecuting = true) }
        val caller = currentState.selectedProfile?.name ?: "AI_AGENT"

        when (val res = executeCommandUseCase(prompt, caller)) {
            is AppResult.Success -> {
                setState {
                    copy(
                        isExecuting = false,
                        lastResult = res.data
                    )
                }
                sendEffect(
                    AgentEffect.ExecutionFinished(
                        summary = res.data.summary,
                        isSuccess = res.data.isSuccess
                    )
                )
            }
            is AppResult.Error -> {
                val failedResult = AgentCommandResult(
                    commandText = prompt,
                    toolName = "auto",
                    summary = res.message,
                    isSuccess = false,
                    durationMs = 0
                )
                setState {
                    copy(
                        isExecuting = false,
                        lastResult = failedResult
                    )
                }
                sendEffect(AgentEffect.ExecutionFinished(summary = res.message, isSuccess = false))
            }
            is AppResult.Loading -> Unit
        }
    }

    private suspend fun executeTool(toolName: String, params: Map<String, Any?>) {
        setState { copy(isExecuting = true) }
        val caller = currentState.selectedProfile?.name ?: "DIRECT_TESTER"

        when (val res = executeDirectToolUseCase(toolName, params, caller)) {
            is AppResult.Success -> {
                val toolResult = res.data
                val isSuccess = toolResult is ToolResult.Success
                val summary = when (toolResult) {
                    is ToolResult.Success -> toolResult.summary
                    is ToolResult.Failure -> toolResult.error
                }
                val cmdResult = AgentCommandResult(
                    commandText = "tool:$toolName(${params})",
                    toolName = toolName,
                    summary = summary,
                    isSuccess = isSuccess,
                    durationMs = 0
                )
                setState {
                    copy(
                        isExecuting = false,
                        lastResult = cmdResult
                    )
                }
                sendEffect(AgentEffect.ExecutionFinished(summary = summary, isSuccess = isSuccess))
            }
            is AppResult.Error -> {
                setState { copy(isExecuting = false) }
                sendEffect(AgentEffect.ShowToast("Error: ${res.message}"))
            }
            is AppResult.Loading -> Unit
        }
    }
}
