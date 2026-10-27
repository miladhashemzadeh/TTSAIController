package com.vampyrelord.ttsaicontroller.features.touch.presentation

import androidx.lifecycle.viewModelScope
import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.core.architecture.BaseMviViewModel
import com.vampyrelord.ttsaicontroller.core.util.currentTimeMillis
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchConfigRepository
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchController
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.AutomaticPlacementStrategy
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.CoordinateType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlacementMode
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.RushwulfSettings
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.SavedTouchConfig
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchExecutionStatus
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPoint
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPositionGenerator
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchSequence
import com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.CancelTouchSequenceUseCase
import com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.DeleteTouchConfigUseCase
import com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.ExecuteTouchSequenceUseCase
import com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.GenerateTouchPositionsUseCase
import com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.SaveTouchConfigUseCase
import com.vampyrelord.ttsaicontroller.features.touch.domain.usecase.UpdateRushwulfSettingsUseCase
import com.vampyrelord.ttsaicontroller.features.touch.presentation.mvi.TouchAutomationEffect
import com.vampyrelord.ttsaicontroller.features.touch.presentation.mvi.TouchAutomationIntent
import com.vampyrelord.ttsaicontroller.features.touch.presentation.mvi.TouchAutomationState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.random.Random

class TouchAutomationViewModel(
    private val repository: TouchConfigRepository,
    private val controller: TouchController,
    private val executeSequenceUseCase: ExecuteTouchSequenceUseCase,
    private val cancelSequenceUseCase: CancelTouchSequenceUseCase,
    private val generatePositionsUseCase: GenerateTouchPositionsUseCase,
    private val saveConfigUseCase: SaveTouchConfigUseCase,
    private val deleteConfigUseCase: DeleteTouchConfigUseCase,
    private val updateRushwulfSettingsUseCase: UpdateRushwulfSettingsUseCase
) : BaseMviViewModel<TouchAutomationState, TouchAutomationIntent, TouchAutomationEffect>(
    TouchAutomationState(
        platformCapability = controller.checkCapabilities(),
        isPermissionDialogOpen = controller.checkCapabilities().requiresAccessibilityService && !controller.checkCapabilities().isAccessibilityServiceEnabled,
        touchPoints = repository.activeConfiguration.value?.points
            ?: generatePositionsUseCase(3, AutomaticPlacementStrategy.GRID, repository.rushwulfSettings.value),
        touchCount = repository.activeConfiguration.value?.points?.size ?: 3,
        placementMode = repository.activeConfiguration.value?.placementMode ?: PlacementMode.AUTOMATIC,
        automaticStrategy = repository.activeConfiguration.value?.strategy ?: AutomaticPlacementStrategy.GRID,
        rushwulfSettings = repository.activeConfiguration.value?.rushwulfSettings ?: repository.rushwulfSettings.value,
        savedConfigs = repository.savedConfigurations.value,
        selectedConfigId = repository.activeConfiguration.value?.id,
        configNameInput = repository.activeConfiguration.value?.name ?: "My Touch Routine"
    )
) {

    init {
        // Observe execution state from controller
        controller.executionState.onEach { execState ->
            setState { copy(executionState = execState) }
        }.launchIn(viewModelScope)

        // Observe saved configurations
        repository.savedConfigurations.onEach { configs ->
            setState { copy(savedConfigs = configs) }
        }.launchIn(viewModelScope)

        // Observe Rushwulf settings
        repository.rushwulfSettings.onEach { settings ->
            setState { copy(rushwulfSettings = settings) }
        }.launchIn(viewModelScope)

        // Observe active configuration
        repository.activeConfiguration.onEach { activeConfig ->
            if (activeConfig != null && activeConfig.id != currentState.selectedConfigId) {
                setState {
                    copy(
                        touchPoints = activeConfig.points,
                        touchCount = activeConfig.points.size,
                        placementMode = activeConfig.placementMode,
                        automaticStrategy = activeConfig.strategy,
                        selectedConfigId = activeConfig.id,
                        configNameInput = activeConfig.name,
                        rushwulfSettings = activeConfig.rushwulfSettings
                    )
                }
            } else if (activeConfig == null && currentState.touchPoints.isEmpty()) {
                // Initialize default positions
                generateDefaultPositions()
            }
        }.launchIn(viewModelScope)
    }

    private fun generateDefaultPositions() {
        val points = generatePositionsUseCase(
            count = currentState.touchCount,
            strategy = currentState.automaticStrategy,
            rushwulfSettings = currentState.rushwulfSettings
        )
        setState { copy(touchPoints = points) }
    }

    public override suspend fun handleIntent(intent: TouchAutomationIntent) {
        when (intent) {
            is TouchAutomationIntent.SetTouchCount -> {
                val newCount = intent.count.coerceIn(1, 20)
                if (currentState.placementMode == PlacementMode.AUTOMATIC) {
                    val points = generatePositionsUseCase(
                        count = newCount,
                        strategy = currentState.automaticStrategy,
                        rushwulfSettings = currentState.rushwulfSettings
                    )
                    setState { copy(touchCount = newCount, touchPoints = points) }
                } else {
                    setState { copy(touchCount = newCount) }
                    adjustManualPointCount(newCount)
                }
            }
            is TouchAutomationIntent.SetPlacementMode -> {
                setState { copy(placementMode = intent.mode) }
                if (intent.mode == PlacementMode.AUTOMATIC) {
                    val points = generatePositionsUseCase(
                        count = currentState.touchCount,
                        strategy = currentState.automaticStrategy,
                        rushwulfSettings = currentState.rushwulfSettings
                    )
                    setState { copy(touchPoints = points) }
                }
            }
            is TouchAutomationIntent.SetAutomaticStrategy -> {
                setState { copy(automaticStrategy = intent.strategy) }
                val points = generatePositionsUseCase(
                    count = currentState.touchCount,
                    strategy = intent.strategy,
                    rushwulfSettings = currentState.rushwulfSettings
                )
                setState { copy(touchPoints = points) }
            }
            is TouchAutomationIntent.RegeneratePositions -> {
                val points = generatePositionsUseCase(
                    count = currentState.touchCount,
                    strategy = currentState.automaticStrategy,
                    rushwulfSettings = currentState.rushwulfSettings
                )
                setState { copy(touchPoints = points) }
            }
            is TouchAutomationIntent.UpdatePointPosition -> {
                val updated = currentState.touchPoints.toMutableList()
                if (intent.index in updated.indices) {
                    val pt = updated[intent.index]
                    updated[intent.index] = pt.copy(
                        x = intent.x.coerceIn(0f, 1f),
                        y = intent.y.coerceIn(0f, 1f)
                    )
                    setState {
                        copy(
                            touchPoints = updated,
                            placementMode = PlacementMode.MANUAL
                        )
                    }
                }
            }
            is TouchAutomationIntent.UpdatePointAction -> {
                val updated = currentState.touchPoints.toMutableList()
                if (intent.index in updated.indices) {
                    val pt = updated[intent.index]
                    updated[intent.index] = pt.copy(
                        actionType = intent.action,
                        durationMillis = intent.durationMs,
                        delayMillis = intent.delayMs
                    )
                    setState { copy(touchPoints = updated) }
                }
            }
            is TouchAutomationIntent.AddPoint -> {
                val newPoint = TouchPoint(
                    id = "pt-manual-${currentTimeMillis()}-${Random.nextInt(100, 999)}",
                    order = currentState.touchPoints.size,
                    x = intent.x.coerceIn(0.05f, 0.95f),
                    y = intent.y.coerceIn(0.05f, 0.95f),
                    coordinateType = CoordinateType.NORMALIZED,
                    delayMillis = currentState.rushwulfSettings.defaultStepDelayMs,
                    durationMillis = currentState.rushwulfSettings.defaultTapDurationMs
                )
                val updated = currentState.touchPoints + newPoint
                setState {
                    copy(
                        touchPoints = updated,
                        touchCount = updated.size,
                        selectedPointIndex = updated.lastIndex
                    )
                }
            }
            is TouchAutomationIntent.RemovePoint -> {
                if (currentState.touchPoints.size > 1 && intent.index in currentState.touchPoints.indices) {
                    val updated = currentState.touchPoints.toMutableList()
                    updated.removeAt(intent.index)
                    val reordered = updated.mapIndexed { idx, pt -> pt.copy(order = idx) }
                    setState {
                        copy(
                            touchPoints = reordered,
                            touchCount = reordered.size,
                            selectedPointIndex = null
                        )
                    }
                } else if (currentState.touchPoints.size <= 1) {
                    sendEffect(TouchAutomationEffect.ShowMessage("At least one touch point must remain."))
                }
            }
            is TouchAutomationIntent.ReorderPoints -> {
                val list = currentState.touchPoints.toMutableList()
                if (intent.fromIndex in list.indices && intent.toIndex in list.indices) {
                    val item = list.removeAt(intent.fromIndex)
                    list.add(intent.toIndex, item)
                    val reordered = list.mapIndexed { idx, pt -> pt.copy(order = idx) }
                    setState { copy(touchPoints = reordered, selectedPointIndex = intent.toIndex) }
                }
            }
            is TouchAutomationIntent.SelectPoint -> {
                setState { copy(selectedPointIndex = intent.index) }
            }
            is TouchAutomationIntent.LoadConfig -> {
                setState {
                    copy(
                        touchPoints = intent.config.points,
                        touchCount = intent.config.points.size,
                        placementMode = intent.config.placementMode,
                        automaticStrategy = intent.config.strategy,
                        selectedConfigId = intent.config.id,
                        configNameInput = intent.config.name,
                        rushwulfSettings = intent.config.rushwulfSettings
                    )
                }
                repository.setActiveConfiguration(intent.config)
                sendEffect(TouchAutomationEffect.ShowMessage("Loaded configuration '${intent.config.name}'"))
            }
            is TouchAutomationIntent.UpdateConfigNameInput -> {
                setState { copy(configNameInput = intent.name) }
            }
            is TouchAutomationIntent.SaveCurrentConfig -> {
                saveCurrentConfig()
            }
            is TouchAutomationIntent.DeleteConfig -> {
                deleteConfigUseCase(intent.id)
                sendEffect(TouchAutomationEffect.ShowMessage("Configuration deleted."))
            }
            is TouchAutomationIntent.UpdateRushwulfSettings -> {
                val updated = intent.transform(currentState.rushwulfSettings)
                setState { copy(rushwulfSettings = updated) }
                updateRushwulfSettingsUseCase { updated }
            }
            is TouchAutomationIntent.OpenSettingsDialog -> {
                setState { copy(isSettingsDialogOpen = true) }
            }
            is TouchAutomationIntent.CloseSettingsDialog -> {
                setState { copy(isSettingsDialogOpen = false) }
            }
            is TouchAutomationIntent.OpenSaveDialog -> {
                setState { copy(isSaveDialogOpen = true) }
            }
            is TouchAutomationIntent.CloseSaveDialog -> {
                setState { copy(isSaveDialogOpen = false) }
            }
            is TouchAutomationIntent.ConfirmAndStart -> {
                startAutomation()
            }
            is TouchAutomationIntent.StopAutomation -> {
                cancelSequenceUseCase()
                sendEffect(TouchAutomationEffect.ShowMessage("Automation stopped."))
            }
            is TouchAutomationIntent.DismissError -> {
                setState { copy(validationError = null) }
            }
            is TouchAutomationIntent.RefreshCapabilities -> {
                val caps = controller.checkCapabilities()
                setState {
                    copy(
                        platformCapability = caps,
                        isPermissionDialogOpen = if (caps.isAutomationSupported) false else isPermissionDialogOpen,
                        validationError = if (caps.isAutomationSupported && validationError == caps.guidanceMessage) null else validationError
                    )
                }
            }
            is TouchAutomationIntent.OpenPermissionDialog -> {
                setState { copy(isPermissionDialogOpen = true) }
            }
            is TouchAutomationIntent.ClosePermissionDialog -> {
                setState { copy(isPermissionDialogOpen = false) }
            }
            is TouchAutomationIntent.UpdateOverlayPermission -> {
                setState { copy(isOverlayPermissionGranted = intent.isGranted) }
            }
        }
    }

    private fun adjustManualPointCount(targetCount: Int) {
        val current = currentState.touchPoints
        if (targetCount > current.size) {
            val toAdd = targetCount - current.size
            val newPoints = (1..toAdd).map { i ->
                TouchPoint(
                    id = "pt-manual-adj-${currentTimeMillis()}-$i",
                    order = current.size + i - 1,
                    x = (0.2f + 0.1f * (current.size + i)).coerceIn(0.1f, 0.9f),
                    y = (0.3f + 0.08f * (current.size + i)).coerceIn(0.1f, 0.9f),
                    delayMillis = currentState.rushwulfSettings.defaultStepDelayMs,
                    durationMillis = currentState.rushwulfSettings.defaultTapDurationMs
                )
            }
            setState { copy(touchPoints = current + newPoints) }
        } else if (targetCount < current.size) {
            val trimmed = current.take(targetCount).mapIndexed { idx, pt -> pt.copy(order = idx) }
            setState { copy(touchPoints = trimmed) }
        }
    }

    private suspend fun saveCurrentConfig() {
        val name = currentState.configNameInput.trim().ifEmpty { "Config ${currentState.savedConfigs.size + 1}" }
        val config = SavedTouchConfig(
            id = currentState.selectedConfigId ?: "cfg-${currentTimeMillis()}",
            name = name,
            points = currentState.touchPoints,
            placementMode = currentState.placementMode,
            strategy = currentState.automaticStrategy,
            rushwulfSettings = currentState.rushwulfSettings
        )
        when (val res = saveConfigUseCase(config)) {
            is AppResult.Success -> {
                setState { copy(isSaveDialogOpen = false, selectedConfigId = config.id) }
                sendEffect(TouchAutomationEffect.ShowMessage("Saved configuration '$name'"))
            }
            is AppResult.Error -> {
                setState { copy(validationError = res.message) }
            }
            is AppResult.Loading -> Unit
        }
    }

    private fun startAutomation() {
        val points = currentState.touchPoints
        val validationErr = TouchPositionGenerator.validatePoints(points)
        if (validationErr != null) {
            setState { copy(validationError = validationErr) }
            return
        }

        val capabilities = controller.checkCapabilities()
        setState { copy(platformCapability = capabilities) }

        if (!capabilities.isAutomationSupported) {
            setState {
                copy(
                    validationError = capabilities.guidanceMessage,
                    isPermissionDialogOpen = true
                )
            }
            if (capabilities.requiresAccessibilityService) {
                sendEffect(TouchAutomationEffect.RequestAccessibilityPermission(capabilities.guidanceMessage))
            }
            return
        }

        val sequence = TouchSequence(
            id = "seq-${currentTimeMillis()}",
            name = currentState.configNameInput,
            points = points,
            repeatCount = currentState.rushwulfSettings.repeatCount,
            delayBetweenIterationsMs = currentState.rushwulfSettings.defaultStepDelayMs * 2
        )

        viewModelScope.launch {
            sendEffect(TouchAutomationEffect.AutomationStarted)
            when (val res = executeSequenceUseCase(sequence)) {
                is AppResult.Success -> {
                    setState { copy(executionReport = res.data, validationError = null) }
                    sendEffect(TouchAutomationEffect.AutomationFinished(res.data))
                }
                is AppResult.Error -> {
                    setState { copy(validationError = res.message) }
                    sendEffect(TouchAutomationEffect.ShowMessage("Automation error: ${res.message}"))
                }
                is AppResult.Loading -> Unit
            }
        }
    }
}
