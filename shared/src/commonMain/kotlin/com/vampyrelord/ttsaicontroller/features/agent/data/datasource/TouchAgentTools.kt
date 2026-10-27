package com.vampyrelord.ttsaicontroller.features.agent.data.datasource

import com.vampyrelord.ttsaicontroller.core.rtk.AgentTool
import com.vampyrelord.ttsaicontroller.core.rtk.ToolDefinition
import com.vampyrelord.ttsaicontroller.core.rtk.ToolParameter
import com.vampyrelord.ttsaicontroller.core.rtk.ToolResult
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchConfigRepository
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchController
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.AutomaticPlacementStrategy
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPositionGenerator
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchSequence

class ExecuteTouchRoutineTool(
    private val controller: TouchController,
    private val repository: TouchConfigRepository
) : AgentTool {
    override val definition = ToolDefinition(
        name = "execute_touch_routine",
        description = "Executes automated touch gesture routine with specified point count or strategy.",
        category = "TOUCH_AUTOMATION",
        parameters = listOf(
            ToolParameter("pointCount", "number", "Number of touch points to generate/execute", required = false, defaultValue = 3),
            ToolParameter("strategy", "string", "Placement strategy: GRID, CIRCULAR, HORIZONTAL_ROW, VERTICAL_COLUMN", required = false, defaultValue = "GRID"),
            ToolParameter("repeatCount", "number", "Number of loop iterations", required = false, defaultValue = 1)
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val count = (params["pointCount"] as? Number)?.toInt() ?: 3
        val stratName = params["strategy"]?.toString() ?: "GRID"
        val strategy = try {
            AutomaticPlacementStrategy.valueOf(stratName.uppercase())
        } catch (e: Exception) {
            AutomaticPlacementStrategy.GRID
        }
        val repeats = (params["repeatCount"] as? Number)?.toInt() ?: 1
        val rushwulf = repository.rushwulfSettings.value

        val points = TouchPositionGenerator.generatePositions(
            count = count,
            strategy = strategy,
            defaultDelayMs = rushwulf.defaultStepDelayMs,
            defaultDurationMs = rushwulf.defaultTapDurationMs
        )

        val sequence = TouchSequence(
            id = "agent-seq-${params.hashCode()}",
            name = "Agent $stratName Sequence",
            points = points,
            repeatCount = repeats,
            delayBetweenIterationsMs = rushwulf.defaultStepDelayMs * 2
        )

        val report = controller.execute(sequence)
        return if (report.isSuccess) {
            ToolResult.Success(
                summary = "Touch automation completed ($count points, $repeats iterations, ${report.durationMs}ms)",
                data = mapOf(
                    "completedSteps" to report.completedSteps,
                    "durationMs" to report.durationMs
                )
            )
        } else {
            ToolResult.Failure(report.errorMessage ?: "Touch automation failed")
        }
    }
}

class StopTouchAutomationTool(
    private val controller: TouchController
) : AgentTool {
    override val definition = ToolDefinition(
        name = "stop_touch_automation",
        description = "Immediately cancels active touch automation sequence.",
        category = "TOUCH_AUTOMATION",
        parameters = emptyList()
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        controller.cancel()
        return ToolResult.Success("Touch automation stopped by agent.")
    }
}

class GetTouchStatusTool(
    private val controller: TouchController
) : AgentTool {
    override val definition = ToolDefinition(
        name = "get_touch_status",
        description = "Queries touch automation engine state, step progress, and platform capability.",
        category = "SYSTEM_INSPECTION",
        parameters = emptyList()
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val state = controller.executionState.value
        val caps = controller.checkCapabilities()
        return ToolResult.Success(
            summary = "Touch engine is ${state.status.name} (${state.activeStepIndex + 1}/${state.totalSteps})",
            data = mapOf(
                "status" to state.status.name,
                "activeStepIndex" to state.activeStepIndex,
                "totalSteps" to state.totalSteps,
                "platformSupported" to caps.isAutomationSupported,
                "guidance" to caps.guidanceMessage
            )
        )
    }
}
