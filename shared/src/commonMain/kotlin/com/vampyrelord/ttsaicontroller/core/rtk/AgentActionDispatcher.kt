package com.vampyrelord.ttsaicontroller.core.rtk

import kotlin.random.Random

/**
 * Orchestrator and Dispatcher for Agent actions.
 * Intercepts tool calls, invokes them, synchronizes with the [RtkStore],
 * and writes to [AgentAuditLog].
 */
class AgentActionDispatcher(
    private val toolRegistry: AgentToolRegistry,
    private val rtkStore: RtkStore,
    private val auditLog: AgentAuditLog
) {

    suspend fun executeTool(
        toolName: String,
        params: Map<String, Any?>,
        caller: String = "AI_AGENT"
    ): ToolResult {
        val tool = toolRegistry.getTool(toolName)
            ?: return ToolResult.Failure("Tool '$toolName' is not registered in AgentToolRegistry.")

        val startTime = currentTimeMillis()
        val recordId = "act-${startTime}-${Random.nextInt(1000, 9999)}"

        return try {
            val result = tool.execute(params)
            val duration = currentTimeMillis() - startTime

            when (result) {
                is ToolResult.Success -> {
                    // Dispatch an action to RTK store
                    rtkStore.dispatch(
                        RtkAction(
                            type = "agent/tool_executed",
                            payload = mapOf(
                                "tool" to toolName,
                                "summary" to result.summary,
                                "data" to result.data
                            ),
                            meta = mapOf("caller" to caller, "status" to "SUCCESS")
                        )
                    )

                    auditLog.record(
                        AgentAuditRecord(
                            id = recordId,
                            timestamp = startTime,
                            caller = caller,
                            toolName = toolName,
                            inputParams = params,
                            status = AgentExecutionStatus.SUCCESS,
                            outputSummary = result.summary,
                            durationMs = duration
                        )
                    )
                }
                is ToolResult.Failure -> {
                    auditLog.record(
                        AgentAuditRecord(
                            id = recordId,
                            timestamp = startTime,
                            caller = caller,
                            toolName = toolName,
                            inputParams = params,
                            status = AgentExecutionStatus.FAILED,
                            outputSummary = "Failed: ${result.error}",
                            durationMs = duration
                        )
                    )
                }
            }
            result
        } catch (e: Throwable) {
            val duration = currentTimeMillis() - startTime
            val errorMsg = e.message ?: "Unexpected error during tool execution"
            auditLog.record(
                AgentAuditRecord(
                    id = recordId,
                    timestamp = startTime,
                    caller = caller,
                    toolName = toolName,
                    inputParams = params,
                    status = AgentExecutionStatus.FAILED,
                    outputSummary = "Exception: $errorMsg",
                    durationMs = duration
                )
            )
            ToolResult.Failure(errorMsg)
        }
    }

    /**
     * Interprets a basic agent natural-command string, routes to appropriate tool, and runs it.
     */
    suspend fun executeNaturalCommand(command: String, caller: String = "AI_AGENT"): ToolResult {
        val trimmed = command.trim()
        val lower = trimmed.lowercase()

        return when {
            lower.startsWith("speak ") || lower.startsWith("say ") -> {
                val textToSay = trimmed.substring(trimmed.indexOf(' ') + 1).trim()
                executeTool(
                    toolName = "speak_text",
                    params = mapOf("text" to textToSay),
                    caller = caller
                )
            }
            lower == "stop" || lower.startsWith("stop speech") || lower.startsWith("shut up") -> {
                executeTool(
                    toolName = "stop_speech",
                    params = emptyMap(),
                    caller = caller
                )
            }
            lower == "pause" || lower.startsWith("pause speech") -> {
                executeTool(
                    toolName = "pause_speech",
                    params = emptyMap(),
                    caller = caller
                )
            }
            lower == "resume" || lower.startsWith("resume speech") -> {
                executeTool(
                    toolName = "resume_speech",
                    params = emptyMap(),
                    caller = caller
                )
            }
            lower.startsWith("status") || lower.contains("playback status") -> {
                executeTool(
                    toolName = "get_playback_status",
                    params = emptyMap(),
                    caller = caller
                )
            }
            lower.startsWith("list voices") || lower.contains("voices") -> {
                executeTool(
                    toolName = "list_voices",
                    params = emptyMap(),
                    caller = caller
                )
            }
            else -> {
                // Fallback: If not recognized as a direct command, synthesize the text directly or report status
                executeTool(
                    toolName = "speak_text",
                    params = mapOf("text" to trimmed),
                    caller = caller
                )
            }
        }
    }
}
