package com.vampyrelord.ttsaicontroller.core.rtk

data class TtsSliceState(
    val lastAction: String = "IDLE",
    val isSpeaking: Boolean = false,
    val isPaused: Boolean = false,
    val activeJobId: String? = null
)

class TtsRtkSlice : RtkSlice<TtsSliceState> {
    override val name: String = "tts"
    override val initialState: TtsSliceState = TtsSliceState()

    override fun reduce(state: TtsSliceState, action: RtkAction<*>): TtsSliceState {
        return when (action.type) {
            "tts/speak_requested" -> {
                val payloadMap = action.payload as? Map<*, *>
                val id = payloadMap?.get("id")?.toString()
                state.copy(lastAction = action.type, isSpeaking = true, isPaused = false, activeJobId = id)
            }
            "tts/job_completed" -> state.copy(lastAction = action.type, isSpeaking = false, isPaused = false, activeJobId = null)
            "tts/paused" -> state.copy(lastAction = action.type, isPaused = true)
            "tts/resumed" -> state.copy(lastAction = action.type, isPaused = false)
            "tts/stopped" -> state.copy(lastAction = action.type, isSpeaking = false, isPaused = false, activeJobId = null)
            else -> state
        }
    }
}

data class AgentSliceState(
    val executedActionsCount: Int = 0,
    val lastToolExecuted: String? = null
)

class AgentRtkSlice : RtkSlice<AgentSliceState> {
    override val name: String = "agent"
    override val initialState: AgentSliceState = AgentSliceState()

    override fun reduce(state: AgentSliceState, action: RtkAction<*>): AgentSliceState {
        return when (action.type) {
            "agent/tool_executed" -> {
                val payloadMap = action.payload as? Map<*, *>
                val toolName = payloadMap?.get("tool")?.toString()
                state.copy(
                    executedActionsCount = state.executedActionsCount + 1,
                    lastToolExecuted = toolName
                )
            }
            else -> state
        }
    }
}

data class TouchSliceState(
    val lastAction: String = "IDLE",
    val isRunning: Boolean = false,
    val savedConfigsCount: Int = 3
)

class TouchRtkSlice : RtkSlice<TouchSliceState> {
    override val name: String = "touch"
    override val initialState: TouchSliceState = TouchSliceState()

    override fun reduce(state: TouchSliceState, action: RtkAction<*>): TouchSliceState {
        return when (action.type) {
            "touch/config_saved" -> state.copy(lastAction = action.type, savedConfigsCount = state.savedConfigsCount + 1)
            "touch/config_deleted" -> state.copy(lastAction = action.type, savedConfigsCount = (state.savedConfigsCount - 1).coerceAtLeast(0))
            "touch/rushwulf_settings_updated" -> state.copy(lastAction = action.type)
            else -> state
        }
    }
}
