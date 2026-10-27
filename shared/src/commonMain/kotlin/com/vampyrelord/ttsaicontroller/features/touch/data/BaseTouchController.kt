package com.vampyrelord.ttsaicontroller.features.touch.data

import com.vampyrelord.ttsaicontroller.core.rtk.currentTimeMillis
import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchController
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.ScrollDirection
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchActionType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchExecutionReport
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchExecutionState
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchExecutionStatus
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPoint
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPositionGenerator
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchResult
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchSequence
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Common base implementation of [TouchController] that coordinates
 * sequential execution, iteration loops, delays, cancellation tokens,
 * and state notifications across platforms.
 */
abstract class BaseTouchController : TouchController {

    private val _executionState = MutableStateFlow(TouchExecutionState())
    override val executionState: StateFlow<TouchExecutionState> = _executionState.asStateFlow()

    @Volatile
    private var isCancelled = false
    private val executionMutex = Mutex()

    protected abstract suspend fun performTap(pixelX: Float, pixelY: Float, durationMs: Long): TouchResult
    protected abstract suspend fun performDoubleTap(pixelX: Float, pixelY: Float, intervalMs: Long): TouchResult
    protected abstract suspend fun performLongPress(pixelX: Float, pixelY: Float, durationMs: Long): TouchResult
    protected abstract suspend fun performScroll(direction: ScrollDirection, distance: Float, durationMs: Long): TouchResult

    override suspend fun tap(position: TouchPoint): TouchResult {
        val (px, py) = position.toPixelPosition(1080f, 1920f)
        return performTap(px, py, position.durationMillis)
    }

    override suspend fun doubleTap(position: TouchPoint, intervalMillis: Long): TouchResult {
        val (px, py) = position.toPixelPosition(1080f, 1920f)
        return performDoubleTap(px, py, intervalMillis)
    }

    override suspend fun longPress(position: TouchPoint, durationMillis: Long): TouchResult {
        val (px, py) = position.toPixelPosition(1080f, 1920f)
        return performLongPress(px, py, durationMillis)
    }

    override suspend fun scroll(direction: ScrollDirection, distance: Float, durationMillis: Long): TouchResult {
        return performScroll(direction, distance, durationMillis)
    }

    override suspend fun execute(sequence: TouchSequence): TouchExecutionReport = executionMutex.withLock {
        if (isCancelled) {
            isCancelled = false
            _executionState.value = TouchExecutionState(
                status = TouchExecutionStatus.CANCELLED
            )
            return TouchExecutionReport(
                sequenceId = sequence.id,
                startTime = currentTimeMillis(),
                durationMs = 0L,
                isSuccess = false,
                errorMessage = "Execution cancelled by user or agent."
            )
        }
        isCancelled = false
        val startTime = currentTimeMillis()

        val validationError = TouchPositionGenerator.validatePoints(sequence.points)
        if (validationError != null) {
            _executionState.value = TouchExecutionState(
                status = TouchExecutionStatus.ERROR,
                errorMessage = validationError
            )
            return TouchExecutionReport(
                sequenceId = sequence.id,
                startTime = startTime,
                durationMs = 0L,
                isSuccess = false,
                errorMessage = validationError
            )
        }

        val capabilities = checkCapabilities()
        if (!capabilities.isAutomationSupported) {
            _executionState.value = TouchExecutionState(
                status = TouchExecutionStatus.ERROR,
                errorMessage = capabilities.guidanceMessage
            )
            return TouchExecutionReport(
                sequenceId = sequence.id,
                startTime = startTime,
                durationMs = 0L,
                isSuccess = false,
                errorMessage = capabilities.guidanceMessage
            )
        }

        var completedSteps = 0
        val totalStepsPerIteration = sequence.points.size
        var completedIterations = 0

        _executionState.value = TouchExecutionState(
            status = TouchExecutionStatus.RUNNING,
            activeStepIndex = 0,
            totalSteps = totalStepsPerIteration,
            currentIteration = 1,
            totalIterations = sequence.repeatCount
        )

        for (iter in 1..sequence.repeatCount) {
            if (isCancelled) break

            for ((stepIndex, point) in sequence.points.withIndex()) {
                if (isCancelled) break

                _executionState.value = TouchExecutionState(
                    status = TouchExecutionStatus.RUNNING,
                    activeStepIndex = stepIndex,
                    totalSteps = totalStepsPerIteration,
                    currentIteration = iter,
                    totalIterations = sequence.repeatCount
                )

                // Apply pre-step delay
                if (point.delayMillis > 0) {
                    delay(point.delayMillis)
                }
                if (isCancelled) break

                val (px, py) = point.toPixelPosition(sequence.screenWidth, sequence.screenHeight)
                val stepResult = when (point.actionType) {
                    TouchActionType.TAP -> performTap(px, py, point.durationMillis)
                    TouchActionType.DOUBLE_TAP -> performDoubleTap(px, py, 150L)
                    TouchActionType.LONG_PRESS -> performLongPress(px, py, point.durationMillis)
                    TouchActionType.SWIPE_UP -> performScroll(ScrollDirection.UP, 400f, point.durationMillis)
                    TouchActionType.SWIPE_DOWN -> performScroll(ScrollDirection.DOWN, 400f, point.durationMillis)
                }

                if (stepResult is TouchResult.Failure) {
                    val duration = currentTimeMillis() - startTime
                    _executionState.value = TouchExecutionState(
                        status = TouchExecutionStatus.ERROR,
                        errorMessage = stepResult.error
                    )
                    return TouchExecutionReport(
                        sequenceId = sequence.id,
                        startTime = startTime,
                        durationMs = duration,
                        completedSteps = completedSteps,
                        totalSteps = totalStepsPerIteration * sequence.repeatCount,
                        iterationsCompleted = completedIterations,
                        isSuccess = false,
                        errorMessage = stepResult.error
                    )
                }

                completedSteps++
            }

            completedIterations++
            if (iter < sequence.repeatCount && sequence.delayBetweenIterationsMs > 0 && !isCancelled) {
                delay(sequence.delayBetweenIterationsMs)
            }
        }

        val totalDuration = currentTimeMillis() - startTime
        return if (isCancelled) {
            _executionState.value = TouchExecutionState(
                status = TouchExecutionStatus.CANCELLED,
                completedSteps = completedSteps
            )
            TouchExecutionReport(
                sequenceId = sequence.id,
                startTime = startTime,
                durationMs = totalDuration,
                completedSteps = completedSteps,
                totalSteps = totalStepsPerIteration * sequence.repeatCount,
                iterationsCompleted = completedIterations,
                isSuccess = false,
                errorMessage = "Execution cancelled by user or agent."
            )
        } else {
            _executionState.value = TouchExecutionState(
                status = TouchExecutionStatus.COMPLETED,
                completedSteps = completedSteps,
                totalSteps = totalStepsPerIteration,
                currentIteration = sequence.repeatCount,
                totalIterations = sequence.repeatCount
            )
            TouchExecutionReport(
                sequenceId = sequence.id,
                startTime = startTime,
                durationMs = totalDuration,
                completedSteps = completedSteps,
                totalSteps = totalStepsPerIteration * sequence.repeatCount,
                iterationsCompleted = completedIterations,
                isSuccess = true
            )
        }
    }

    override fun cancel() {
        isCancelled = true
        _executionState.value = _executionState.value.copy(
            status = TouchExecutionStatus.CANCELLED
        )
    }
}
