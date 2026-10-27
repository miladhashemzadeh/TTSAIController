package com.vampyrelord.ttsaicontroller.features.agent.data.datasource

import com.vampyrelord.ttsaicontroller.core.rtk.AgentTool
import com.vampyrelord.ttsaicontroller.core.rtk.RtkAction
import com.vampyrelord.ttsaicontroller.core.rtk.RtkStore
import com.vampyrelord.ttsaicontroller.core.rtk.ToolDefinition
import com.vampyrelord.ttsaicontroller.core.rtk.ToolParameter
import com.vampyrelord.ttsaicontroller.core.rtk.ToolResult
import com.vampyrelord.ttsaicontroller.features.tts.domain.repository.TtsRepository

class SpeakTextTool(
    private val ttsRepository: TtsRepository
) : AgentTool {
    override val definition = ToolDefinition(
        name = "speak_text",
        description = "Queue and speak specified text using the TTS audio engine.",
        category = "TTS_CONTROL",
        parameters = listOf(
            ToolParameter("text", "string", "Text string to synthesize and speak", required = true),
            ToolParameter("voiceId", "string", "Optional ID of the voice to use", required = false),
            ToolParameter("pitch", "number", "Voice pitch multiplier (0.5 to 2.0)", required = false, defaultValue = 1.0),
            ToolParameter("rate", "number", "Voice speed multiplier (0.5 to 2.5)", required = false, defaultValue = 1.0)
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val text = params["text"]?.toString() ?: return ToolResult.Failure("Missing 'text' parameter")
        val voices = ttsRepository.getAvailableVoices()
        val voiceId = params["voiceId"]?.toString()
        val voice = (if (voiceId != null) voices.find { it.id == voiceId } else null) ?: voices.firstOrNull()
            ?: return ToolResult.Failure("No available voice found in system")

        val pitch = (params["pitch"] as? Number)?.toFloat() ?: 1.0f
        val rate = (params["rate"] as? Number)?.toFloat() ?: 1.0f

        val job = ttsRepository.speak(
            text = text,
            voice = voice,
            pitch = pitch,
            rate = rate,
            volume = 1.0f,
            source = "AGENT"
        )
        return ToolResult.Success(
            summary = "Queued speech job '${job.id}' for agent with voice ${voice.name}",
            data = mapOf(
                "jobId" to job.id,
                "text" to job.text,
                "voice" to voice.name,
                "status" to job.status.name
            )
        )
    }
}

class StopSpeechTool(
    private val ttsRepository: TtsRepository
) : AgentTool {
    override val definition = ToolDefinition(
        name = "stop_speech",
        description = "Immediately cancel active speech playback and halt audio engine.",
        category = "TTS_CONTROL",
        parameters = emptyList()
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        ttsRepository.stop()
        return ToolResult.Success(summary = "Speech playback stopped by agent")
    }
}

class PauseSpeechTool(
    private val ttsRepository: TtsRepository
) : AgentTool {
    override val definition = ToolDefinition(
        name = "pause_speech",
        description = "Pause active speech audio playback.",
        category = "TTS_CONTROL",
        parameters = emptyList()
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        ttsRepository.pause()
        return ToolResult.Success(summary = "Speech playback paused by agent")
    }
}

class ResumeSpeechTool(
    private val ttsRepository: TtsRepository
) : AgentTool {
    override val definition = ToolDefinition(
        name = "resume_speech",
        description = "Resume paused speech audio playback.",
        category = "TTS_CONTROL",
        parameters = emptyList()
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        ttsRepository.resume()
        return ToolResult.Success(summary = "Speech playback resumed by agent")
    }
}

class GetPlaybackStatusTool(
    private val ttsRepository: TtsRepository
) : AgentTool {
    override val definition = ToolDefinition(
        name = "get_playback_status",
        description = "Retrieve the current status of the TTS audio engine, active job, and queue size.",
        category = "SYSTEM_INSPECTION",
        parameters = emptyList()
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val status = ttsRepository.engineStatus.value
        return ToolResult.Success(
            summary = "Engine is ${if (status.isSpeaking) "SPEAKING" else "IDLE"} (Queue: ${status.queueSize})",
            data = mapOf(
                "isSpeaking" to status.isSpeaking,
                "isPaused" to status.isPaused,
                "activeJobText" to (status.activeJob?.text ?: ""),
                "queueSize" to status.queueSize,
                "totalProcessed" to status.totalProcessedJobs
            )
        )
    }
}

class ListVoicesTool(
    private val ttsRepository: TtsRepository
) : AgentTool {
    override val definition = ToolDefinition(
        name = "list_voices",
        description = "List all available TTS neural and standard voice models.",
        category = "SYSTEM_INSPECTION",
        parameters = emptyList()
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val voices = ttsRepository.getAvailableVoices()
        return ToolResult.Success(
            summary = "Found ${voices.size} available voices",
            data = mapOf(
                "voices" to voices.map { "${it.id} (${it.name}, ${it.language}, ${it.gender})" }
            )
        )
    }
}

class QueryStateSliceTool(
    private val rtkStore: RtkStore
) : AgentTool {
    override val definition = ToolDefinition(
        name = "query_state_slice",
        description = "Query an RTK state slice by name for inspection by an agent.",
        category = "RTK_STORE",
        parameters = listOf(
            ToolParameter("sliceName", "string", "Name of the slice to read", required = true)
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val sliceName = params["sliceName"]?.toString() ?: return ToolResult.Failure("Missing 'sliceName' parameter")
        val sliceData = rtkStore.getSliceState<Any>(sliceName)
        return ToolResult.Success(
            summary = "Retrieved state slice '$sliceName'",
            data = mapOf("slice" to sliceName, "content" to (sliceData?.toString() ?: "empty/null"))
        )
    }
}

class DispatchRtkActionTool(
    private val rtkStore: RtkStore
) : AgentTool {
    override val definition = ToolDefinition(
        name = "dispatch_rtk_action",
        description = "Dispatch a raw Redux Toolkit action into the centralized store.",
        category = "RTK_STORE",
        parameters = listOf(
            ToolParameter("type", "string", "RTK action type identifier", required = true),
            ToolParameter("payload", "string", "Action payload description", required = false)
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val type = params["type"]?.toString() ?: return ToolResult.Failure("Missing 'type' parameter")
        val payload = params["payload"]?.toString() ?: ""
        rtkStore.dispatch(
            RtkAction(
                type = type,
                payload = payload,
                meta = mapOf("source" to "AGENT_TOOL")
            )
        )
        return ToolResult.Success(summary = "Dispatched RTK action '$type'")
    }
}
