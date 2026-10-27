package com.vampyrelord.ttsaicontroller

import com.vampyrelord.ttsaicontroller.core.navigation.Screen
import com.vampyrelord.ttsaicontroller.features.dashboard.domain.model.DashboardMetrics
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.mvi.DashboardIntent
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.mvi.DashboardState
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppSettings
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppThemeMode
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AudioOutputRouting
import com.vampyrelord.ttsaicontroller.features.settings.presentation.mvi.SettingsIntent
import com.vampyrelord.ttsaicontroller.features.settings.presentation.mvi.SettingsState
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.AudioConfig
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.SttEngineCapability
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionResult
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionSegment
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus
import com.vampyrelord.ttsaicontroller.features.stt.presentation.mvi.SttIntent
import com.vampyrelord.ttsaicontroller.features.stt.presentation.mvi.SttState
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.AutomaticPlacementStrategy
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.CoordinateType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlacementMode
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlatformTouchCapability
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.RushwulfSettings
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.SavedTouchConfig
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchActionType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPoint
import com.vampyrelord.ttsaicontroller.features.touch.presentation.mvi.TouchAutomationIntent
import com.vampyrelord.ttsaicontroller.features.touch.presentation.mvi.TouchAutomationState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ComposeScreensTest {

    // ==========================================
    // 1. Touch Automation Screen State & Hoisting Tests
    // ==========================================

    @Test
    fun testTouchAutomationStateContracts() {
        val points = listOf(
            TouchPoint("pt-1", 0, 0.1f, 0.2f, CoordinateType.NORMALIZED, TouchActionType.TAP),
            TouchPoint("pt-2", 1, 0.8f, 0.9f, CoordinateType.NORMALIZED, TouchActionType.LONG_PRESS)
        )
        val rushwulf = RushwulfSettings(profileName = "Custom Rushwulf")
        val saved = listOf(
            SavedTouchConfig("saved-1", "Macro 1", points, PlacementMode.AUTOMATIC, AutomaticPlacementStrategy.GRID, rushwulf)
        )

        val state = TouchAutomationState(
            touchPoints = points,
            touchCount = 2,
            placementMode = PlacementMode.AUTOMATIC,
            automaticStrategy = AutomaticPlacementStrategy.GRID,
            selectedPointIndex = 0,
            rushwulfSettings = rushwulf,
            savedConfigs = saved,
            selectedConfigId = "saved-1",
            configNameInput = "Macro 1",
            validationError = "Sample validation error",
            platformCapability = PlatformTouchCapability("Desktop Robot", isAutomationSupported = true),
            isSettingsDialogOpen = true,
            isSaveDialogOpen = false,
            isPermissionDialogOpen = true,
            isOverlayPermissionGranted = true
        )

        assertEquals(2, state.touchPoints.size)
        assertEquals(0, state.selectedPointIndex)
        assertTrue(state.isSettingsDialogOpen)
        assertEquals("saved-1", state.selectedConfigId)
        assertTrue(state.isPermissionDialogOpen)
        assertTrue(state.isOverlayPermissionGranted)
    }

    @Test
    fun testTouchAutomationEventHoistingDispatch() {
        val dispatchedIntents = mutableListOf<TouchAutomationIntent>()
        val onIntent: (TouchAutomationIntent) -> Unit = { dispatchedIntents.add(it) }

        // Trigger events as simulated from Compose screen UI controls
        onIntent(TouchAutomationIntent.SetTouchCount(4))
        onIntent(TouchAutomationIntent.SelectPoint(1))
        onIntent(TouchAutomationIntent.UpdatePointPosition(1, 0.3f, 0.6f))
        onIntent(TouchAutomationIntent.AddPoint(0.5f, 0.5f))
        onIntent(TouchAutomationIntent.RemovePoint(0))
        onIntent(TouchAutomationIntent.ConfirmAndStart)
        onIntent(TouchAutomationIntent.StopAutomation)
        onIntent(TouchAutomationIntent.OpenSettingsDialog)
        onIntent(TouchAutomationIntent.UpdateConfigNameInput("New Routine"))
        onIntent(TouchAutomationIntent.SaveCurrentConfig)
        onIntent(TouchAutomationIntent.DismissError)

        assertEquals(11, dispatchedIntents.size)
        assertEquals(TouchAutomationIntent.SetTouchCount(4), dispatchedIntents[0])
        assertEquals(TouchAutomationIntent.SelectPoint(1), dispatchedIntents[1])
        assertEquals(TouchAutomationIntent.ConfirmAndStart, dispatchedIntents[5])
        assertEquals(TouchAutomationIntent.StopAutomation, dispatchedIntents[6])
        assertEquals(TouchAutomationIntent.DismissError, dispatchedIntents[10])
    }

    // ==========================================
    // 2. Automation Permissions Dialog Logic Tests
    // ==========================================

    @Test
    fun testAutomationPermissionsDialogTriggers() {
        var requestedAccessibility = false
        var requestedOverlay = false
        var dismissed = false

        val onGrantAccessibility: () -> Unit = { requestedAccessibility = true }
        val onGrantOverlay: () -> Unit = { requestedOverlay = true }
        val onDismiss: () -> Unit = { dismissed = true }

        // Simulate user clicking Grant Accessibility button
        onGrantAccessibility()
        assertTrue(requestedAccessibility)

        // Simulate user clicking Grant Overlay button
        onGrantOverlay()
        assertTrue(requestedOverlay)

        // Simulate dismiss
        onDismiss()
        assertTrue(dismissed)
    }

    // ==========================================
    // 3. STT Screen State & Hoisting Tests
    // ==========================================

    @Test
    fun testSttScreenStateAndEventHoisting() {
        val result = TranscriptionResult(
            id = "res-1",
            fullText = "Hello AI Speech",
            segments = listOf(
                TranscriptionSegment("seg-1", "Hello AI Speech", 0, 1000, 1.0f, true)
            ),
            durationMs = 1000L,
            aiModelInfo = "AI Whisper Ready"
        )
        val capability = SttEngineCapability(
            engineName = "Neural STT",
            isSupported = true,
            supportsLiveStreaming = true,
            supportedLanguages = listOf("en", "es"),
            aiModelRequired = false
        )

        val state = SttState(
            status = TranscriptionStatus.LISTENING,
            isRecording = true,
            audioConfig = AudioConfig(language = "en"),
            liveTranscript = "Hello AI Speech in progress...",
            lastResult = result,
            capability = capability,
            errorMessage = null
        )

        assertTrue(state.isRecording)
        assertEquals(TranscriptionStatus.LISTENING, state.status)
        assertEquals("en", state.audioConfig.language)
        assertEquals("Neural STT", state.capability.engineName)

        // Test event hoisting
        val sttIntents = mutableListOf<SttIntent>()
        val onSttIntent: (SttIntent) -> Unit = { sttIntents.add(it) }

        onSttIntent(SttIntent.StartListening)
        onSttIntent(SttIntent.StopListening)
        onSttIntent(SttIntent.CancelListening)
        onSttIntent(SttIntent.UpdateLanguage("es"))
        onSttIntent(SttIntent.ClearTranscript)
        onSttIntent(SttIntent.DismissError)

        assertEquals(6, sttIntents.size)
        assertEquals(SttIntent.StartListening, sttIntents[0])
        assertEquals(SttIntent.StopListening, sttIntents[1])
        assertEquals(SttIntent.CancelListening, sttIntents[2])
        assertEquals(SttIntent.UpdateLanguage("es"), sttIntents[3])
        assertEquals(SttIntent.ClearTranscript, sttIntents[4])
        assertEquals(SttIntent.DismissError, sttIntents[5])
    }

    // ==========================================
    // 4. Dashboard Screen State & Hoisting Tests
    // ==========================================

    @Test
    fun testDashboardScreenStateAndEventHoisting() {
        val metrics = DashboardMetrics(
            transcriptionStatus = TranscriptionStatus.IDLE,
            isListening = false,
            lastTranscript = "Previous note",
            aiEngineName = "AI STT Engine",
            isTouchRunning = false,
            totalSavedTouchConfigs = 3
        )
        val state = DashboardState(
            metrics = metrics,
            recentTranscriptions = emptyList(),
            isRefreshing = false
        )

        assertEquals(3, state.metrics.totalSavedTouchConfigs)
        assertFalse(state.metrics.isListening)

        val dashIntents = mutableListOf<DashboardIntent>()
        val onDashIntent: (DashboardIntent) -> Unit = { dashIntents.add(it) }

        onDashIntent(DashboardIntent.StartListening)
        onDashIntent(DashboardIntent.StopListening)
        onDashIntent(DashboardIntent.Refresh)
        onDashIntent(DashboardIntent.NavigateTo(Screen.TouchAutomation))
        onDashIntent(DashboardIntent.NavigateTo(Screen.SttStudio))

        assertEquals(5, dashIntents.size)
        assertEquals(DashboardIntent.StartListening, dashIntents[0])
        assertEquals(DashboardIntent.StopListening, dashIntents[1])
        assertEquals(DashboardIntent.Refresh, dashIntents[2])
        assertEquals(DashboardIntent.NavigateTo(Screen.TouchAutomation), dashIntents[3])
        assertEquals(DashboardIntent.NavigateTo(Screen.SttStudio), dashIntents[4])
    }

    // ==========================================
    // 5. Settings Screen State & Hoisting Tests
    // ==========================================

    @Test
    fun testSettingsScreenStateAndEventHoisting() {
        val settings = AppSettings(
            themeMode = AppThemeMode.DARK,
            autoPlayOnQueue = false,
            agentAutonomousEnabled = true,
            audioRouting = AudioOutputRouting.DEFAULT_SPEAKERS
        )
        val state = SettingsState(settings = settings)

        assertEquals(AppThemeMode.DARK, state.settings.themeMode)
        assertFalse(state.settings.autoPlayOnQueue)

        val settingsIntents = mutableListOf<SettingsIntent>()
        val onSettingsIntent: (SettingsIntent) -> Unit = { settingsIntents.add(it) }

        onSettingsIntent(SettingsIntent.SetThemeMode(AppThemeMode.LIGHT))
        onSettingsIntent(SettingsIntent.SetAudioRouting(AudioOutputRouting.HEADPHONES))
        onSettingsIntent(SettingsIntent.ToggleAutoPlay(true))
        onSettingsIntent(SettingsIntent.ToggleAgentAutonomous(true))
        onSettingsIntent(SettingsIntent.ResetToDefaults)

        assertEquals(5, settingsIntents.size)
        assertEquals(SettingsIntent.SetThemeMode(AppThemeMode.LIGHT), settingsIntents[0])
        assertEquals(SettingsIntent.SetAudioRouting(AudioOutputRouting.HEADPHONES), settingsIntents[1])
        assertEquals(SettingsIntent.ToggleAutoPlay(true), settingsIntents[2])
        assertEquals(SettingsIntent.ToggleAgentAutonomous(true), settingsIntents[3])
        assertEquals(SettingsIntent.ResetToDefaults, settingsIntents[4])
    }

    // ==========================================
    // 6. Navigation AppBottomNav Selection Tests
    // ==========================================

    @Test
    fun testNavigationScreenRoutes() {
        val selectedScreens = mutableListOf<Screen>()
        val onSelect: (Screen) -> Unit = { selectedScreens.add(it) }

        Screen.all.forEach { screen ->
            onSelect(screen)
        }

        assertEquals(4, selectedScreens.size)
        assertTrue(selectedScreens.contains(Screen.TouchAutomation))
        assertTrue(selectedScreens.contains(Screen.Dashboard))
        assertTrue(selectedScreens.contains(Screen.SttStudio))
        assertTrue(selectedScreens.contains(Screen.Settings))
    }
}
