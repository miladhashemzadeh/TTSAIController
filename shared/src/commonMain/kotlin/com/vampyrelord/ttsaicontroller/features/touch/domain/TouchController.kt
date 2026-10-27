package com.vampyrelord.ttsaicontroller.features.touch.domain

import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlatformTouchCapability
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.ScrollDirection
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchExecutionReport
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchExecutionState
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPoint
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchResult
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchSequence
import kotlinx.coroutines.flow.StateFlow

/**
 * Universal cross-platform Touch Controller interface.
 * Implemented natively on Android (via AccessibilityService gesture injection)
 * and Desktop (via java.awt.Robot).
 */
interface TouchController {

    /**
     * Observable reactive execution state.
     */
    val executionState: StateFlow<TouchExecutionState>

    /**
     * Inspects current platform automation capability, service connection, and permissions.
     */
    fun checkCapabilities(): PlatformTouchCapability

    /**
     * Performs a single tap at the given touch position.
     */
    suspend fun tap(position: TouchPoint): TouchResult

    /**
     * Performs a double tap at the given touch position.
     */
    suspend fun doubleTap(
        position: TouchPoint,
        intervalMillis: Long = 150L
    ): TouchResult

    /**
     * Performs a long press at the given touch position.
     */
    suspend fun longPress(
        position: TouchPoint,
        durationMillis: Long = 600L
    ): TouchResult

    /**
     * Scrolls in a given direction by [distance] pixels over [durationMillis].
     */
    suspend fun scroll(
        direction: ScrollDirection,
        distance: Float = 400f,
        durationMillis: Long = 300L
    ): TouchResult

    /**
     * Scrolls upwards.
     */
    suspend fun scrollUp(
        distance: Float = 400f,
        durationMillis: Long = 300L
    ): TouchResult = scroll(ScrollDirection.UP, distance, durationMillis)

    /**
     * Scrolls downwards.
     */
    suspend fun scrollDown(
        distance: Float = 400f,
        durationMillis: Long = 300L
    ): TouchResult = scroll(ScrollDirection.DOWN, distance, durationMillis)

    /**
     * Executes an entire [TouchSequence], running through all configured points,
     * delays, iterations, and operations sequentially.
     */
    suspend fun execute(sequence: TouchSequence): TouchExecutionReport

    /**
     * Cancels any active automation sequence immediately and halts touch execution.
     */
    fun cancel()
}
