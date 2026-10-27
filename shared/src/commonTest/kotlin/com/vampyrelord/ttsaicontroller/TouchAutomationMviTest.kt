package com.vampyrelord.ttsaicontroller

import com.vampyrelord.ttsaicontroller.core.di.AppContainer
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.AutomaticPlacementStrategy
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlacementMode
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.SavedTouchConfig
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchActionType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPoint
import com.vampyrelord.ttsaicontroller.features.touch.presentation.TouchAutomationViewModel
import com.vampyrelord.ttsaicontroller.features.touch.presentation.mvi.TouchAutomationIntent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TouchAutomationMviTest {

    private fun createTestViewModel(controller: FakeTouchController = FakeTouchController()): Pair<AppContainer, TouchAutomationViewModel> {
        val container = AppContainer()
        val vm = TouchAutomationViewModel(
            repository = container.touchConfigRepository,
            controller = controller,
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
    fun testInitialState() {
        val (_, vm) = createTestViewModel()
        val state = vm.currentState

        assertTrue(state.touchPoints.isNotEmpty())
        assertEquals(PlacementMode.AUTOMATIC, state.placementMode)
        assertNotNull(state.rushwulfSettings)
        assertEquals("Rushwulf Pro Macro", state.rushwulfSettings.profileName)
    }

    @Test
    fun testSetTouchCount() = runTest {
        val (_, vm) = createTestViewModel()

        vm.handleIntent(TouchAutomationIntent.SetTouchCount(5))
        assertEquals(5, vm.currentState.touchCount)
        assertEquals(5, vm.currentState.touchPoints.size)

        vm.handleIntent(TouchAutomationIntent.SetTouchCount(2))
        assertEquals(2, vm.currentState.touchCount)
        assertEquals(2, vm.currentState.touchPoints.size)
    }

    @Test
    fun testSwitchPlacementModeAndStrategy() = runTest {
        val (_, vm) = createTestViewModel()

        vm.handleIntent(TouchAutomationIntent.SetPlacementMode(PlacementMode.MANUAL))
        assertEquals(PlacementMode.MANUAL, vm.currentState.placementMode)

        vm.handleIntent(TouchAutomationIntent.SetAutomaticStrategy(AutomaticPlacementStrategy.CIRCULAR))
        assertEquals(AutomaticPlacementStrategy.CIRCULAR, vm.currentState.automaticStrategy)
    }

    @Test
    fun testAddEditRemoveReorderPoints() = runTest {
        val (_, vm) = createTestViewModel()
        val initialSize = vm.currentState.touchPoints.size

        // Add a point
        vm.handleIntent(TouchAutomationIntent.AddPoint(0.3f, 0.7f))
        assertEquals(initialSize + 1, vm.currentState.touchPoints.size)
        val added = vm.currentState.touchPoints.last()
        assertEquals(0.3f, added.x)
        assertEquals(0.7f, added.y)

        // Edit point action
        vm.handleIntent(
            TouchAutomationIntent.UpdatePointAction(
                index = vm.currentState.touchPoints.lastIndex,
                action = TouchActionType.DOUBLE_TAP,
                durationMs = 60L,
                delayMs = 200L
            )
        )
        assertEquals(TouchActionType.DOUBLE_TAP, vm.currentState.touchPoints.last().actionType)
        assertEquals(200L, vm.currentState.touchPoints.last().delayMillis)

        // Reorder points
        val firstId = vm.currentState.touchPoints[0].id
        val secondId = vm.currentState.touchPoints[1].id
        vm.handleIntent(TouchAutomationIntent.ReorderPoints(0, 1))
        assertEquals(secondId, vm.currentState.touchPoints[0].id)
        assertEquals(firstId, vm.currentState.touchPoints[1].id)

        // Remove point
        val countBeforeRemove = vm.currentState.touchPoints.size
        vm.handleIntent(TouchAutomationIntent.RemovePoint(0))
        assertEquals(countBeforeRemove - 1, vm.currentState.touchPoints.size)
    }

    @Test
    fun testUpdateRushwulfSettings() = runTest {
        val (_, vm) = createTestViewModel()

        vm.handleIntent(
            TouchAutomationIntent.UpdateRushwulfSettings {
                it.copy(
                    profileName = "Fast Rushwulf Mode",
                    defaultStepDelayMs = 50L,
                    repeatCount = 5
                )
            }
        )

        assertEquals("Fast Rushwulf Mode", vm.currentState.rushwulfSettings.profileName)
        assertEquals(50L, vm.currentState.rushwulfSettings.defaultStepDelayMs)
        assertEquals(5, vm.currentState.rushwulfSettings.repeatCount)
    }

    @Test
    fun testSaveAndLoadConfiguration() = runTest {
        val (_, vm) = createTestViewModel()

        val testConfig = SavedTouchConfig(
            id = "cfg-unit-test",
            name = "Unit Test Routine",
            points = listOf(
                TouchPoint("pt-1", 0, 0.4f, 0.4f),
                TouchPoint("pt-2", 1, 0.6f, 0.6f)
            )
        )

        vm.handleIntent(TouchAutomationIntent.LoadConfig(testConfig))
        assertEquals("Unit Test Routine", vm.currentState.configNameInput)
        assertEquals(2, vm.currentState.touchPoints.size)
    }

    @Test
    fun testPermissionDialogAndCapabilities() = runTest {
        val (_, vm) = createTestViewModel()

        vm.handleIntent(TouchAutomationIntent.OpenPermissionDialog)
        assertTrue(vm.currentState.isPermissionDialogOpen)

        vm.handleIntent(TouchAutomationIntent.ClosePermissionDialog)
        kotlin.test.assertFalse(vm.currentState.isPermissionDialogOpen)

        vm.handleIntent(TouchAutomationIntent.UpdateOverlayPermission(false))
        kotlin.test.assertFalse(vm.currentState.isOverlayPermissionGranted)

        vm.handleIntent(TouchAutomationIntent.UpdateOverlayPermission(true))
        assertTrue(vm.currentState.isOverlayPermissionGranted)
    }
}
