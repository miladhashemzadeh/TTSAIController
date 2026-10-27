package com.vampyrelord.ttsaicontroller.features.touch.data

import com.vampyrelord.ttsaicontroller.core.rtk.RtkAction
import com.vampyrelord.ttsaicontroller.core.rtk.RtkStore
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchConfigRepository
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.AutomaticPlacementStrategy
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlacementMode
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.RushwulfSettings
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.SavedTouchConfig
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPoint
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPositionGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryTouchConfigRepository(
    private val rtkStore: RtkStore
) : TouchConfigRepository {

    private val defaultRushwulfSettings = RushwulfSettings(
        profileName = "Rushwulf Pro Macro",
        defaultTapDurationMs = 50L,
        defaultDoubleTapIntervalMs = 180L,
        defaultLongPressDurationMs = 500L,
        defaultStepDelayMs = 150L,
        scrollStepDistancePixels = 450f,
        repeatCount = 1,
        stopOnError = true,
        showVisualOverlay = true
    )

    private val defaultPresetConfigs: List<SavedTouchConfig> = listOf(
        SavedTouchConfig(
            id = "cfg-preset-3pt-grid",
            name = "3-Point Grid Routine",
            points = TouchPositionGenerator.generatePositions(3, AutomaticPlacementStrategy.GRID),
            placementMode = PlacementMode.AUTOMATIC,
            strategy = AutomaticPlacementStrategy.GRID,
            rushwulfSettings = defaultRushwulfSettings
        ),
        SavedTouchConfig(
            id = "cfg-preset-5pt-circle",
            name = "5-Point Circular Sequence",
            points = TouchPositionGenerator.generatePositions(5, AutomaticPlacementStrategy.CIRCULAR),
            placementMode = PlacementMode.AUTOMATIC,
            strategy = AutomaticPlacementStrategy.CIRCULAR,
            rushwulfSettings = defaultRushwulfSettings
        ),
        SavedTouchConfig(
            id = "cfg-preset-2pt-vertical",
            name = "2-Point Vertical Scroll Sequence",
            points = TouchPositionGenerator.generatePositions(2, AutomaticPlacementStrategy.VERTICAL_COLUMN),
            placementMode = PlacementMode.AUTOMATIC,
            strategy = AutomaticPlacementStrategy.VERTICAL_COLUMN,
            rushwulfSettings = defaultRushwulfSettings
        )
    )

    private val _savedConfigs = MutableStateFlow<List<SavedTouchConfig>>(defaultPresetConfigs)
    override val savedConfigurations: StateFlow<List<SavedTouchConfig>> = _savedConfigs.asStateFlow()

    private val _activeConfig = MutableStateFlow<SavedTouchConfig?>(defaultPresetConfigs.firstOrNull())
    override val activeConfiguration: StateFlow<SavedTouchConfig?> = _activeConfig.asStateFlow()

    private val _rushwulfSettings = MutableStateFlow(defaultRushwulfSettings)
    override val rushwulfSettings: StateFlow<RushwulfSettings> = _rushwulfSettings.asStateFlow()

    override suspend fun saveConfiguration(config: SavedTouchConfig) {
        _savedConfigs.update { current ->
            val existingIndex = current.indexOfFirst { it.id == config.id }
            if (existingIndex >= 0) {
                current.toMutableList().apply { set(existingIndex, config) }
            } else {
                listOf(config) + current
            }
        }
        rtkStore.dispatch(
            RtkAction(
                type = "touch/config_saved",
                payload = mapOf(
                    "id" to config.id,
                    "name" to config.name,
                    "pointCount" to config.points.size
                )
            )
        )
    }

    override suspend fun loadConfiguration(configId: String): SavedTouchConfig? {
        val found = _savedConfigs.value.find { it.id == configId }
        if (found != null) {
            _activeConfig.value = found
            _rushwulfSettings.value = found.rushwulfSettings
        }
        return found
    }

    override suspend fun deleteConfiguration(configId: String) {
        _savedConfigs.update { current -> current.filterNot { it.id == configId } }
        if (_activeConfig.value?.id == configId) {
            _activeConfig.value = _savedConfigs.value.firstOrNull()
        }
        rtkStore.dispatch(
            RtkAction(type = "touch/config_deleted", payload = mapOf("id" to configId))
        )
    }

    override suspend fun setActiveConfiguration(config: SavedTouchConfig?) {
        _activeConfig.value = config
        if (config != null) {
            _rushwulfSettings.value = config.rushwulfSettings
        }
    }

    override suspend fun updateRushwulfSettings(transform: (RushwulfSettings) -> RushwulfSettings) {
        _rushwulfSettings.update(transform)
        val updated = _rushwulfSettings.value
        rtkStore.dispatch(
            RtkAction(
                type = "touch/rushwulf_settings_updated",
                payload = mapOf(
                    "profileName" to updated.profileName,
                    "stepDelay" to updated.defaultStepDelayMs,
                    "repeatCount" to updated.repeatCount
                )
            )
        )
    }
}
