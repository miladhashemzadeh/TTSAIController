package com.vampyrelord.ttsaicontroller.core.rtk

/**
 * Registry holding all available tools that AI Agents can discover and execute.
 */
class AgentToolRegistry {
    private val toolMap = mutableMapOf<String, AgentTool>()

    fun register(tool: AgentTool) {
        toolMap[tool.definition.name] = tool
    }

    fun registerAll(tools: List<AgentTool>) {
        tools.forEach { register(it) }
    }

    fun getTool(name: String): AgentTool? = toolMap[name]

    fun getAllTools(): List<AgentTool> = toolMap.values.toList()

    fun getDefinitions(): List<ToolDefinition> = toolMap.values.map { it.definition }

    /**
     * Generates a descriptive agent-friendly text schema of all available tools.
     */
    fun exportSchemaDescription(): String {
        return buildString {
            appendLine("=== AVAILABLE AGENT RTK TOOLS ===")
            toolMap.values.forEach { tool ->
                val def = tool.definition
                appendLine("- Tool: ${def.name} [Category: ${def.category}]")
                appendLine("  Description: ${def.description}")
                appendLine("  Parameters:")
                if (def.parameters.isEmpty()) {
                    appendLine("    (None)")
                } else {
                    def.parameters.forEach { param ->
                        val req = if (param.required) "required" else "optional"
                        val defaultInfo = if (param.defaultValue != null) ", default: ${param.defaultValue}" else ""
                        appendLine("    * ${param.name} (${param.type}, $req$defaultInfo): ${param.description}")
                    }
                }
                appendLine()
            }
        }
    }
}
