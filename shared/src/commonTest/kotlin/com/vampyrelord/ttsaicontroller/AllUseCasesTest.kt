package com.vampyrelord.ttsaicontroller

import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.core.di.AppContainer
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppThemeMode
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AudioOutputRouting
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.AudioConfig
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.AutomaticPlacementStrategy
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.CoordinateType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlacementMode
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.RushwulfSettings
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.SavedTouchConfig
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchActionType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchExecutionStatus
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPoint
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchSequence
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AllUseCasesTest {

    // ==========================================
    // Touch Automation Use Cases
    // ==========================================

    @Test
    fun testExecuteAndCancelTouchSequenceUseCases() = runTest {
        val fake = FakeTouchController()
        val executeUseCase = com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.ExecuteTouchSequenceUseCase(fake)
        val cancelUseCase = com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.CancelTouchSequenceUseCase(fake)

        val point = TouchPoint(
            id = "pt-test-1",
            order = 0,
            x = 0.5f,
            y = 0.5f,
            coordinateType = CoordinateType.NORMALIZED,
            actionType = TouchActionType.TAP,
            delayMillis = 5L
        )
        val sequence = TouchSequence(
            id = "seq-1",
            name = "Test Sequence",
            points = listOf(point),
            repeatCount = 1,
            screenWidth = 1080f,
            screenHeight = 1920f
        )

        // Execute via platform controller
        val reportResult = executeUseCase(sequence)
        assertTrue(reportResult is AppResult.Success)
        assertTrue(reportResult.data.completedSteps >= 0)

        // Cancel
        cancelUseCase()
        assertEquals(TouchExecutionStatus.CANCELLED, fake.executionState.value.status)
    }

    @Test
    fun testGenerateTouchPositionsUseCaseAllStrategies() {
        val container = AppContainer()
        val generateUseCase = container.generateTouchPositionsUseCase

        for (strategy in AutomaticPlacementStrategy.entries) {
            val count = 4
            val points = generateUseCase(count, strategy)
            assertEquals(count, points.size)

            for (i in points.indices) {
                val pt = points[i]
                assertEquals(i, pt.order)
                assertTrue(pt.x in 0.0f..1.0f, "Point $i x coordinate must be in [0, 1] for $strategy")
                assertTrue(pt.y in 0.0f..1.0f, "Point $i y coordinate must be in [0, 1] for $strategy")
                assertEquals(CoordinateType.NORMALIZED, pt.coordinateType)
            }
        }
    }

    @Test
    fun testSaveAndDeleteTouchConfigUseCases() = runTest {
        val container = AppContainer()
        val saveUseCase = container.saveTouchConfigUseCase
        val deleteUseCase = container.deleteTouchConfigUseCase
        val repo = container.touchConfigRepository

        val initialCount = repo.savedConfigurations.value.size

        val customConfig = SavedTouchConfig(
            id = "custom-config-123",
            name = "Custom 4-Point Macro",
            points = listOf(
                TouchPoint("p1", 0, 0.2f, 0.2f, CoordinateType.NORMALIZED),
                TouchPoint("p2", 1, 0.8f, 0.8f, CoordinateType.NORMALIZED)
            ),
            placementMode = PlacementMode.MANUAL,
            strategy = AutomaticPlacementStrategy.GRID,
            rushwulfSettings = RushwulfSettings(profileName = "Custom Rushwulf")
        )

        // Save
        saveUseCase(customConfig)
        assertEquals(initialCount + 1, repo.savedConfigurations.value.size)
        assertEquals("custom-config-123", repo.activeConfiguration.value?.id)

        // Delete
        deleteUseCase("custom-config-123")
        assertEquals(initialCount, repo.savedConfigurations.value.size)
        assertFalse(repo.savedConfigurations.value.any { it.id == "custom-config-123" })
    }

    @Test
    fun testUpdateRushwulfSettingsUseCase() = runTest {
        val container = AppContainer()
        val updateUseCase = container.updateRushwulfSettingsUseCase
        val repo = container.touchConfigRepository

        updateUseCase { current ->
            current.copy(
                profileName = "Ultra Fast Rushwulf",
                defaultTapDurationMs = 25L,
                defaultStepDelayMs = 50L,
                repeatCount = 5,
                stopOnError = false
            )
        }

        val updated = repo.rushwulfSettings.value
        assertEquals("Ultra Fast Rushwulf", updated.profileName)
        assertEquals(25L, updated.defaultTapDurationMs)
        assertEquals(50L, updated.defaultStepDelayMs)
        assertEquals(5, updated.repeatCount)
        assertFalse(updated.stopOnError)
    }

    // ==========================================
    // Speech-To-Text (STT) Use Cases
    // ==========================================

    @Test
    fun testStartStopCancelSpeechToTextUseCases() = runTest {
        val container = AppContainer()
        val startUseCase = container.startSpeechToTextUseCase
        val stopUseCase = container.stopSpeechToTextUseCase
        val cancelUseCase = container.cancelSpeechToTextUseCase
        val repo = container.speechToTextRepository

        // Initial
        assertEquals(TranscriptionStatus.IDLE, repo.transcriptionStatus.value)

        // Start
        val startResult = startUseCase(AudioConfig(language = "es", sampleRate = 44100))
        assertTrue(startResult is AppResult.Success)
        assertEquals(TranscriptionStatus.LISTENING, repo.transcriptionStatus.value)

        // Stop
        val stopResult = stopUseCase()
        assertTrue(stopResult is AppResult.Success)
        assertEquals(TranscriptionStatus.COMPLETED, repo.transcriptionStatus.value)
        val data = stopResult.data
        assertEquals("[Speech-to-Text: AI Model Pending Integration]", data.fullText)

        // Cancel
        cancelUseCase()
        assertEquals(TranscriptionStatus.IDLE, repo.transcriptionStatus.value)
    }

    @Test
    fun testTranscribeAudioAndFileUseCases() = runTest {
        val container = AppContainer()
        val transcribeAudioUseCase = container.transcribeAudioUseCase
        val transcribeFileUseCase = container.transcribeFileUseCase

        // Transcribe Audio Bytes (Pending AI Model placeholder)
        val audioResult = transcribeAudioUseCase(ByteArray(100), AudioConfig())
        assertTrue(audioResult is AppResult.Error)
        assertTrue(audioResult.message.contains("pending integration"))

        // Transcribe File (Pending AI Model placeholder)
        val fileResult = transcribeFileUseCase("/path/to/sample.wav", AudioConfig())
        assertTrue(fileResult is AppResult.Error)
        assertTrue(fileResult.message.contains("pending integration"))
    }

    @Test
    fun testGetSttCapabilityUseCase() {
        val container = AppContainer()
        val getCapabilityUseCase = container.getSttCapabilityUseCase

        val capability = getCapabilityUseCase()
        assertTrue(capability.aiModelRequired)
        assertTrue(capability.supportsLiveStreaming)
        assertTrue(capability.supportedLanguages.contains("en"))
        assertTrue(capability.supportedLanguages.contains("auto"))
        assertNotNull(capability.statusDescription)
    }

    // ==========================================
    // Settings Use Cases
    // ==========================================

    @Test
    fun testSettingsUseCasesCompleteFlow() = runTest {
        val container = AppContainer()
        val getUseCase = container.getSettingsUseCase
        val updateUseCase = container.updateSettingsUseCase
        val resetUseCase = container.resetSettingsUseCase

        // Initial settings
        val initialSettings = getUseCase().value
        assertEquals(AppThemeMode.SYSTEM, initialSettings.themeMode)
        assertTrue(initialSettings.autoPlayOnQueue)

        // Update settings
        val updateResult = updateUseCase { current ->
            current.copy(
                themeMode = AppThemeMode.LIGHT,
                audioRouting = AudioOutputRouting.HEADPHONES,
                autoPlayOnQueue = false
            )
        }
        assertTrue(updateResult is AppResult.Success)

        val modified = getUseCase().value
        assertEquals(AppThemeMode.LIGHT, modified.themeMode)
        assertEquals(AudioOutputRouting.HEADPHONES, modified.audioRouting)
        assertFalse(modified.autoPlayOnQueue)

        // Reset settings
        val resetResult = resetUseCase()
        assertTrue(resetResult is AppResult.Success)

        val reset = getUseCase().value
        assertEquals(AppThemeMode.SYSTEM, reset.themeMode)
        assertTrue(reset.autoPlayOnQueue)
    }

    // ==========================================
    // Dashboard Use Case
    // ==========================================

    @Test
    fun testGetDashboardMetricsUseCase() = runTest {
        val container = AppContainer()
        val getMetricsUseCase = container.getDashboardMetricsUseCase

        val result = getMetricsUseCase()
        assertTrue(result is AppResult.Success)

        val metrics = result.data
        assertEquals(TranscriptionStatus.IDLE, metrics.transcriptionStatus)
        assertFalse(metrics.isListening)
        assertFalse(metrics.isTouchRunning)
        assertTrue(metrics.totalSavedTouchConfigs >= 0)
        assertNotNull(metrics.aiEngineName)
    }
}
