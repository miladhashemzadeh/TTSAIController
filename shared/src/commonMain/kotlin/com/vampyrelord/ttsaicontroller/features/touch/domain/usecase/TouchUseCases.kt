package com.vampyrelord.ttsaicontroller.features.touch.domain.usecase

import com.vampyrelord.ttsaicontroller.core.architecture.AppResult
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchConfigRepository
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchController
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.AutomaticPlacementStrategy
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.RushwulfSettings
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.SavedTouchConfig
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchExecutionReport
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPoint
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPositionGenerator
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchSequence

class ExecuteTouchSequenceUseCase(
    private val controller: TouchController
) {
    suspend operator fun invoke(sequence: TouchSequence): AppResult<TouchExecutionReport> {
        val validationError = TouchPositionGenerator.validatePoints(sequence.points)
        if (validationError != null) {
            return AppResult.Error(message = validationError)
        }
        return try {
            val report = controller.execute(sequence)
            if (report.isSuccess) {
                AppResult.Success(report)
            } else {
                AppResult.Error(message = report.errorMessage ?: "Automation execution failed")
            }
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}

class CancelTouchSequenceUseCase(
    private val controller: TouchController
) {
    operator fun invoke() {
        controller.cancel()
    }
}

class GenerateTouchPositionsUseCase {
    operator fun invoke(
        count: Int,
        strategy: AutomaticPlacementStrategy,
        rushwulfSettings: RushwulfSettings = RushwulfSettings()
    ): List<TouchPoint> {
        return TouchPositionGenerator.generatePositions(
            count = count,
            strategy = strategy,
            defaultDelayMs = rushwulfSettings.defaultStepDelayMs,
            defaultDurationMs = rushwulfSettings.defaultTapDurationMs
        )
    }
}

class SaveTouchConfigUseCase(
    private val repository: TouchConfigRepository
) {
    suspend operator fun invoke(config: SavedTouchConfig): AppResult<Unit> {
        val error = TouchPositionGenerator.validatePoints(config.points)
        if (error != null) {
            return AppResult.Error(message = error)
        }
        return try {
            repository.saveConfiguration(config)
            repository.setActiveConfiguration(config)
            AppResult.Success(Unit)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}

class DeleteTouchConfigUseCase(
    private val repository: TouchConfigRepository
) {
    suspend operator fun invoke(configId: String): AppResult<Unit> {
        return try {
            repository.deleteConfiguration(configId)
            AppResult.Success(Unit)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}

class UpdateRushwulfSettingsUseCase(
    private val repository: TouchConfigRepository
) {
    suspend operator fun invoke(transform: (RushwulfSettings) -> RushwulfSettings): AppResult<RushwulfSettings> {
        return try {
            repository.updateRushwulfSettings(transform)
            AppResult.Success(repository.rushwulfSettings.value)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}
