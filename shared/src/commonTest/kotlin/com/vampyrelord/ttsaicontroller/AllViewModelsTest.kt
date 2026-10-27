package com.vampyrelord.ttsaicontroller

import com.vampyrelord.ttsaicontroller.core.di.AppContainer
import com.vampyrelord.ttsaicontroller.core.navigation.Screen
import com.vampyrelord.ttsaicontroller.features.dashboard.presentation.mvi.DashboardIntent
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AppThemeMode
import com.vampyrelord.ttsaicontroller.features.settings.domain.model.AudioOutputRouting
import com.vampyrelord.ttsaicontroller.features.settings.presentation.mvi.SettingsIntent
import com.vampyrelord.ttsaicontroller.features.stt.domain.model.TranscriptionStatus
import com.vampyrelord.ttsaicontroller.features.stt.presentation.mvi.SttIntent
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.AutomaticPlacementStrategy
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlacementMode
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchActionType
import com.vampyrelord.ttsaicontroller.features.touch.presentation.TouchAutomationViewModel
import com.vampyrelord.ttsaicontroller.features.touch.presentation.mvi.TouchAutomationIntent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AllViewModelsTest {

    // ==========================================
    // TouchAutomationViewModel Complete Tests
    // ==========================================

    private fun createTouchVm(): Pair<AppContainer, TouchAutomationViewModel> {
        val container = AppContainer()
        val fake = FakeTouchController()
        val vm = TouchAutomationViewModel(
            repository = container.touchConfigRepository,
            controller = fake,
            executeSequenceUseCase = container.executeTouchSequenceUseCase,
            cancelSequenceUseCase = container.cancelTouchSequenceUseCase,
            generatePositionsUseCase = container.generateTouchPositionsUseCase,
            saveConfigUseCase = container.saveTouchConfigUseCase,
            deleteConfigUseCase = container.deleteTouchConfigUseCase,
            updateRushwulfSettingsUseCase = container.updateRushwulfSettingsUseCase
        )
        return Pair(container, vm)
    }

    @Test
    fun testTouchAutomationViewModelAllIntents() = runTest {
        val (container, vm) = createTouchVm()

        // 1. Initial State
        assertTrue(vm.currentState.touchPoints.isNotEmpty())
        assertEquals(PlacementMode.AUTOMATIC, vm.currentState.placementMode)

        // 2. Select Point
        vm.handleIntent(TouchAutomationIntent.SelectPoint(0))
        assertEquals(0, vm.currentState.selectedPointIndex)

        // 3. Update Point Position
        vm.handleIntent(TouchAutomationIntent.UpdatePointPosition(0, 0.45f, 0.65f))
        assertEquals(0.45f, vm.currentState.touchPoints[0].x)
        assertEquals(0.65f, vm.currentState.touchPoints[0].y)
        assertEquals(PlacementMode.MANUAL, vm.currentState.placementMode)

        // 4. Set Touch Count
        vm.handleIntent(TouchAutomationIntent.SetTouchCount(4))
        assertEquals(4, vm.currentState.touchCount)
        assertEquals(4, vm.currentState.touchPoints.size)

        // 5. Set Automatic Strategy
        vm.handleIntent(TouchAutomationIntent.SetPlacementMode(PlacementMode.AUTOMATIC))
        vm.handleIntent(TouchAutomationIntent.SetAutomaticStrategy(AutomaticPlacementStrategy.CIRCULAR))
        assertEquals(AutomaticPlacementStrategy.CIRCULAR, vm.currentState.automaticStrategy)
        assertEquals(PlacementMode.AUTOMATIC, vm.currentState.placementMode)

        // 6. Regenerate Positions
        vm.handleIntent(TouchAutomationIntent.RegeneratePositions)
        assertEquals(4, vm.currentState.touchPoints.size)

        // 7. Add Point
        val countBeforeAdd = vm.currentState.touchPoints.size
        vm.handleIntent(TouchAutomationIntent.AddPoint(0.12f, 0.34f))
        assertEquals(countBeforeAdd + 1, vm.currentState.touchPoints.size)

        // 8. Remove Point
        val lastIdx = vm.currentState.touchPoints.lastIndex
        vm.handleIntent(TouchAutomationIntent.RemovePoint(lastIdx))
        assertEquals(countBeforeAdd, vm.currentState.touchPoints.size)

        // 9. Update Point Action
        vm.handleIntent(
            TouchAutomationIntent.UpdatePointAction(
                index = 0,
                action = TouchActionType.LONG_PRESS,
                durationMs = 800L,
                delayMs = 250L
            )
        )
        assertEquals(TouchActionType.LONG_PRESS, vm.currentState.touchPoints[0].actionType)
        assertEquals(800L, vm.currentState.touchPoints[0].durationMillis)
        assertEquals(250L, vm.currentState.touchPoints[0].delayMillis)

        // 10. Reorder Points
        val id0 = vm.currentState.touchPoints[0].id
        val id1 = vm.currentState.touchPoints[1].id
        vm.handleIntent(TouchAutomationIntent.ReorderPoints(0, 1))
        assertEquals(id1, vm.currentState.touchPoints[0].id)
        assertEquals(id0, vm.currentState.touchPoints[1].id)

        // 11. Dialog toggles
        vm.handleIntent(TouchAutomationIntent.OpenSettingsDialog)
        assertTrue(vm.currentState.isSettingsDialogOpen)
        vm.handleIntent(TouchAutomationIntent.CloseSettingsDialog)
        assertFalse(vm.currentState.isSettingsDialogOpen)

        vm.handleIntent(TouchAutomationIntent.OpenSaveDialog)
        assertTrue(vm.currentState.isSaveDialogOpen)
        vm.handleIntent(TouchAutomationIntent.CloseSaveDialog)
        assertFalse(vm.currentState.isSaveDialogOpen)

        vm.handleIntent(TouchAutomationIntent.OpenPermissionDialog)
        assertTrue(vm.currentState.isPermissionDialogOpen)
        vm.handleIntent(TouchAutomationIntent.ClosePermissionDialog)
        assertFalse(vm.currentState.isPermissionDialogOpen)

        // 12. Update Rushwulf Settings
        vm.handleIntent(
            TouchAutomationIntent.UpdateRushwulfSettings { current ->
                current.copy(profileName = "VM Tested Profile", repeatCount = 3)
            }
        )
        assertEquals("VM Tested Profile", vm.currentState.rushwulfSettings.profileName)
        assertEquals(3, vm.currentState.rushwulfSettings.repeatCount)

        // 13. Save Configuration via input + SaveCurrentConfig
        vm.handleIntent(TouchAutomationIntent.UpdateConfigNameInput("My Custom Routine"))
        assertEquals("My Custom Routine", vm.currentState.configNameInput)
        vm.handleIntent(TouchAutomationIntent.SaveCurrentConfig)
        val saved = container.touchConfigRepository.savedConfigurations.value
        assertTrue(saved.any { it.name == "My Custom Routine" })

        // 14. Load Configuration
        val configToLoad = saved.first()
        vm.handleIntent(TouchAutomationIntent.LoadConfig(configToLoad))
        assertEquals(configToLoad.id, vm.currentState.selectedConfigId)

        // 15. Delete Configuration
        vm.handleIntent(TouchAutomationIntent.DeleteConfig(configToLoad.id))
        assertFalse(container.touchConfigRepository.savedConfigurations.value.any { it.id == configToLoad.id })

        // 16. Refresh capabilities & overlay permission
        vm.handleIntent(TouchAutomationIntent.RefreshCapabilities)
        vm.handleIntent(TouchAutomationIntent.UpdateOverlayPermission(true))
        assertTrue(vm.currentState.isOverlayPermissionGranted)

        // 17. Confirm and Start & Stop
        vm.handleIntent(TouchAutomationIntent.ConfirmAndStart)
        vm.handleIntent(TouchAutomationIntent.StopAutomation)

        // 18. Dismiss Error
        vm.handleIntent(TouchAutomationIntent.DismissError)
        assertNull(vm.currentState.validationError)
    }

    // ==========================================
    // SttViewModel Complete Tests
    // ==========================================

    @Test
    fun testSttViewModelAllIntentsAndFlows() = runTest {
        val container = AppContainer()
        val vm = container.sttViewModel

        // Initial State
        assertEquals(TranscriptionStatus.IDLE, vm.currentState.status)
        assertFalse(vm.currentState.isRecording)
        assertNotNull(vm.currentState.capability)

        // 1. Update Language
        vm.handleIntent(SttIntent.UpdateLanguage("fr"))
        assertEquals("fr", vm.currentState.audioConfig.language)

        // 2. Start Listening
        vm.handleIntent(SttIntent.StartListening)
        assertEquals(TranscriptionStatus.LISTENING, vm.currentState.status)
        assertTrue(vm.currentState.isRecording)

        // 3. Stop Listening
        vm.handleIntent(SttIntent.StopListening)
        assertEquals(TranscriptionStatus.COMPLETED, vm.currentState.status)
        assertFalse(vm.currentState.isRecording)
        assertNotNull(vm.currentState.lastResult)
        assertTrue(vm.currentState.liveTranscript.isNotEmpty())

        // 4. Clear Transcript
        vm.handleIntent(SttIntent.ClearTranscript)
        assertEquals("", vm.currentState.liveTranscript)

        // 5. Cancel Listening
        vm.handleIntent(SttIntent.StartListening)
        vm.handleIntent(SttIntent.CancelListening)
        assertEquals(TranscriptionStatus.IDLE, vm.currentState.status)
        assertFalse(vm.currentState.isRecording)

        // 6. Dismiss Error
        vm.handleIntent(SttIntent.DismissError)
        assertNull(vm.currentState.errorMessage)
    }

    // ==========================================
    // SettingsViewModel Complete Tests
    // ==========================================

    @Test
    fun testSettingsViewModelAllIntents() = runTest {
        val container = AppContainer()
        val vm = container.settingsViewModel

        // Initial
        assertEquals(AppThemeMode.SYSTEM, vm.currentState.settings.themeMode)

        // 1. Set Theme Mode
        vm.handleIntent(SettingsIntent.SetThemeMode(AppThemeMode.DARK))
        assertEquals(AppThemeMode.DARK, vm.currentState.settings.themeMode)

        // 2. Set Audio Routing
        vm.handleIntent(SettingsIntent.SetAudioRouting(AudioOutputRouting.VIRTUAL_AUDIO_CABLE))
        assertEquals(AudioOutputRouting.VIRTUAL_AUDIO_CABLE, vm.currentState.settings.audioRouting)

        // 3. Toggle Auto Play
        vm.handleIntent(SettingsIntent.ToggleAutoPlay(false))
        assertFalse(vm.currentState.settings.autoPlayOnQueue)

        // 4. Toggle Autonomous Agent
        vm.handleIntent(SettingsIntent.ToggleAgentAutonomous(false))
        assertFalse(vm.currentState.settings.agentAutonomousEnabled)

        // 5. Toggle Waveform
        vm.handleIntent(SettingsIntent.ToggleWaveform(false))
        assertFalse(vm.currentState.settings.showWaveformAnimation)

        // 6. Reset To Defaults
        vm.handleIntent(SettingsIntent.ResetToDefaults)
        assertEquals(AppThemeMode.SYSTEM, vm.currentState.settings.themeMode)
        assertTrue(vm.currentState.settings.autoPlayOnQueue)
    }

    // ==========================================
    // DashboardViewModel Complete Tests
    // ==========================================

    @Test
    fun testDashboardViewModelAllIntentsAndReactiveUpdates() = runTest {
        val container = AppContainer()
        val vm = container.dashboardViewModel
        val nav = container.navigator

        // 1. Refresh
        vm.handleIntent(DashboardIntent.Refresh)
        assertNotNull(vm.currentState.metrics)

        // 2. Start Listening
        vm.handleIntent(DashboardIntent.StartListening)
        assertEquals(TranscriptionStatus.LISTENING, container.speechToTextRepository.transcriptionStatus.value)

        // 3. Stop Listening
        vm.handleIntent(DashboardIntent.StopListening)
        assertEquals(TranscriptionStatus.COMPLETED, container.speechToTextRepository.transcriptionStatus.value)
        assertTrue(vm.currentState.recentTranscriptions.isNotEmpty())

        // 4. Navigate To Screen
        vm.handleIntent(DashboardIntent.NavigateTo(Screen.SttStudio))
        assertEquals(Screen.SttStudio, nav.currentScreen.value)

        vm.handleIntent(DashboardIntent.NavigateTo(Screen.TouchAutomation))
        assertEquals(Screen.TouchAutomation, nav.currentScreen.value)
    }
}
