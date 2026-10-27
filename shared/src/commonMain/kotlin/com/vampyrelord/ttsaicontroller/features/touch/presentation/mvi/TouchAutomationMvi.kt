package com.vampyrelord.ttsaicontroller.features.touch.presentation.mvi

import com.vampyrelord.ttsaicontroller.core.architecture.MviEffect
import com.vampyrelord.ttsaicontroller.core.architecture.MviIntent
import com.vampyrelord.ttsaicontroller.core.architecture.MviState
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.AutomaticPlacementStrategy
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlacementMode
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlatformTouchCapability
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.RushwulfSettings
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.SavedTouchConfig
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchActionType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchExecutionReport
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchExecutionState
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPoint

data class TouchAutomationState(
    val touchCount: Int = 3,
    val touchPoints: List<TouchPoint> = emptyList(),
    val selectedPointIndex: Int? = null,
    val placementMode: PlacementMode = PlacementMode.AUTOMATIC,
    val automaticStrategy: AutomaticPlacementStrategy = AutomaticPlacementStrategy.GRID,
    val rushwulfSettings: RushwulfSettings = RushwulfSettings(),
    val savedConfigs: List<SavedTouchConfig> = emptyList(),
    val selectedConfigId: String? = null,
    val configNameInput: String = "My Touch Routine",
    val executionState: TouchExecutionState = TouchExecutionState(),
    val executionReport: TouchExecutionReport? = null,
    val validationError: String? = null,
    val platformCapability: PlatformTouchCapability = PlatformTouchCapability("Unknown", true),
    val isSettingsDialogOpen: Boolean = false,
    val isSaveDialogOpen: Boolean = false
) : MviState

sealed interface TouchAutomationIntent : MviIntent {
    data class SetTouchCount(val count: Int) : TouchAutomationIntent
    data class SetPlacementMode(val mode: PlacementMode) : TouchAutomationIntent
    data class SetAutomaticStrategy(val strategy: AutomaticPlacementStrategy) : TouchAutomationIntent
    data object RegeneratePositions : TouchAutomationIntent
    data class UpdatePointPosition(val index: Int, val x: Float, val y: Float) : TouchAutomationIntent
    data class UpdatePointAction(
        val index: Int,
        val action: TouchActionType,
        val durationMs: Long,
        val delayMs: Long
    ) : TouchAutomationIntent
    data class AddPoint(val x: Float = 0.5f, val y: Float = 0.5f) : TouchAutomationIntent
    data class RemovePoint(val index: Int) : TouchAutomationIntent
    data class ReorderPoints(val fromIndex: Int, val toIndex: Int) : TouchAutomationIntent
    data class SelectPoint(val index: Int?) : TouchAutomationIntent
    data class LoadConfig(val config: SavedTouchConfig) : TouchAutomationIntent
    data class UpdateConfigNameInput(val name: String) : TouchAutomationIntent
    data object SaveCurrentConfig : TouchAutomationIntent
    data class DeleteConfig(val id: String) : TouchAutomationIntent
    data class UpdateRushwulfSettings(val transform: (RushwulfSettings) -> RushwulfSettings) : TouchAutomationIntent
    data object OpenSettingsDialog : TouchAutomationIntent
    data object CloseSettingsDialog : TouchAutomationIntent
    data object OpenSaveDialog : TouchAutomationIntent
    data object CloseSaveDialog : TouchAutomationIntent
    data object ConfirmAndStart : TouchAutomationIntent
    data object StopAutomation : TouchAutomationIntent
    data object DismissError : TouchAutomationIntent
    data object RefreshCapabilities : TouchAutomationIntent
}

sealed interface TouchAutomationEffect : MviEffect {
    data class ShowMessage(val message: String) : TouchAutomationEffect
    data class RequestAccessibilityPermission(val message: String) : TouchAutomationEffect
    data object AutomationStarted : TouchAutomationEffect
    data class AutomationFinished(val report: TouchExecutionReport) : TouchAutomationEffect
}
