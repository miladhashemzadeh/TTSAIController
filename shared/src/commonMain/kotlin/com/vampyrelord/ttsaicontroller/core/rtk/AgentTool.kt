package com.vampyrelord.ttsaicontroller.core.rtk

/**
 * Definition of a parameter accepted by an [AgentTool].
 */
data class ToolParameter(
    val name: String,
    val type: String,
    val description: String,
    val required: Boolean = true,
    val defaultValue: Any? = null
)

/**
 * Metadata and schema describing an agent tool.
 * Can be exported as JSON or schema definitions for LLMs (OpenAI, Gemini, Anthropic function calling).
 */
data class ToolDefinition(
    val name: String,
    val description: String,
    val category: String,
    val parameters: List<ToolParameter>
)

/**
 * Result of an agent tool execution.
 */
sealed interface ToolResult {
    data class Success(
        val summary: String,
        val data: Map<String, Any?> = emptyMap()
    ) : ToolResult

    data class Failure(
        val error: String,
        val recoverable: Boolean = true
    ) : ToolResult
}

/**
 * Interface for all tools executable by AI Agents or automated scripts.
 */
interface AgentTool {
    val definition: ToolDefinition
    suspend fun execute(params: Map<String, Any?>): ToolResult
}
