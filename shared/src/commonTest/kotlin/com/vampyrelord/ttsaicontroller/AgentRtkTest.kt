package com.vampyrelord.ttsaicontroller

import com.vampyrelord.ttsaicontroller.core.di.AppContainer
import com.vampyrelord.ttsaicontroller.core.rtk.AgentExecutionStatus
import com.vampyrelord.ttsaicontroller.core.rtk.RtkAction
import com.vampyrelord.ttsaicontroller.core.rtk.ToolResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AgentRtkTest {

    @Test
    fun testRtkStoreActionDispatching() {
        val container = AppContainer()
        val store = container.rtkStore

        // Initial state of TTS slice
        val initialTts = store.getSliceState<com.vampyrelord.ttsaicontroller.core.rtk.TtsSliceState>("tts")
        assertNotNull(initialTts)
        assertEquals(false, initialTts.isSpeaking)

        // Dispatch a speak request action
        store.dispatch(
            RtkAction(
                type = "tts/speak_requested",
                payload = mapOf("id" to "job-test-123")
            )
        )

        val updatedTts = store.getSliceState<com.vampyrelord.ttsaicontroller.core.rtk.TtsSliceState>("tts")
        assertNotNull(updatedTts)
        assertEquals(true, updatedTts.isSpeaking)
        assertEquals("job-test-123", updatedTts.activeJobId)
    }

    @Test
    fun testAgentToolExecutionAndAuditTrail() = runTest {
        val container = AppContainer()
        val dispatcher = container.agentActionDispatcher
        val auditLog = container.agentAuditLog

        // Execute speak_text tool
        val result = dispatcher.executeTool(
            toolName = "speak_text",
            params = mapOf("text" to "Hello from Agent RTK unit test!"),
            caller = "TEST_AGENT"
        )

        assertTrue(result is ToolResult.Success)
        val records = auditLog.records.value
        assertTrue(records.isNotEmpty())
        assertEquals("speak_text", records.first().toolName)
        assertEquals("TEST_AGENT", records.first().caller)
        assertEquals(AgentExecutionStatus.SUCCESS, records.first().status)
    }

    @Test
    fun testAgentNaturalCommandDispatch() = runTest {
        val container = AppContainer()
        val dispatcher = container.agentActionDispatcher

        val result = dispatcher.executeNaturalCommand("speak Automated status check", "ORCHESTRATOR")
        assertTrue(result is ToolResult.Success)

        val stopResult = dispatcher.executeNaturalCommand("stop", "ORCHESTRATOR")
        assertTrue(stopResult is ToolResult.Success)
    }
}
