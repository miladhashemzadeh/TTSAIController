package com.vampyrelord.ttsaicontroller.features.touch.data

import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchController
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlatformTouchCapability
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.ScrollDirection
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Interface that the Android Accessibility Service implements to execute gestures.
 */
interface AndroidGestureDispatcher {
    fun isServiceActive(): Boolean
    fun dispatchGesturePath(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long,
        onComplete: (Boolean) -> Unit
    )
}

/**
 * Thread-safe global hook connecting the Android Accessibility Service instance
 * to the shared Kotlin Multiplatform touch controller.
 */
object AndroidAccessibilityRegistry {
    @Volatile
    var dispatcher: AndroidGestureDispatcher? = null
}

class AndroidAccessibilityTouchController : BaseTouchController() {

    override fun checkCapabilities(): PlatformTouchCapability {
        val dispatcher = AndroidAccessibilityRegistry.dispatcher
        val isServiceActive = dispatcher?.isServiceActive() == true

        return PlatformTouchCapability(
            platformName = "Android (AccessibilityService Gesture API)",
            isAutomationSupported = isServiceActive,
            requiresAccessibilityService = true,
            isAccessibilityServiceEnabled = isServiceActive,
            requiresOverlayPermission = true,
            guidanceMessage = if (isServiceActive) {
                "Accessibility Service connected and ready for gesture automation."
            } else {
                "Accessibility Service is not enabled. Please enable 'TTSAI Touch Automation' in Android Accessibility Settings."
            }
        )
    }

    override suspend fun performTap(pixelX: Float, pixelY: Float, durationMs: Long): TouchResult {
        return executeGesture(pixelX, pixelY, pixelX, pixelY, durationMs.coerceIn(20L, 200L))
    }

    override suspend fun performDoubleTap(pixelX: Float, pixelY: Float, intervalMs: Long): TouchResult {
        val tap1 = performTap(pixelX, pixelY, 30L)
        if (tap1 is TouchResult.Failure) return tap1
        delay(intervalMs.coerceAtLeast(60L))
        return performTap(pixelX, pixelY, 30L)
    }

    override suspend fun performLongPress(pixelX: Float, pixelY: Float, durationMs: Long): TouchResult {
        return executeGesture(pixelX, pixelY, pixelX, pixelY, durationMs.coerceAtLeast(500L))
    }

    override suspend fun performScroll(
        direction: ScrollDirection,
        distance: Float,
        durationMs: Long
    ): TouchResult {
        val centerX = 540f
        val centerY = 960f
        val (endX, endY) = when (direction) {
            ScrollDirection.UP -> Pair(centerX, centerY - distance)
            ScrollDirection.DOWN -> Pair(centerX, centerY + distance)
            ScrollDirection.LEFT -> Pair(centerX - distance, centerY)
            ScrollDirection.RIGHT -> Pair(centerX + distance, centerY)
        }
        return executeGesture(centerX, centerY, endX, endY, durationMs.coerceAtLeast(150L))
    }

    private suspend fun executeGesture(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long
    ): TouchResult {
        val dispatcher = AndroidAccessibilityRegistry.dispatcher
        if (dispatcher == null || !dispatcher.isServiceActive()) {
            return TouchResult.Failure(
                "Accessibility Service is not enabled. Go to Android Settings > Accessibility to enable touch automation."
            )
        }

        val deferred = CompletableDeferred<Boolean>()
        dispatcher.dispatchGesturePath(startX, startY, endX, endY, durationMs) { success ->
            deferred.complete(success)
        }

        val completed = withTimeoutOrNull(durationMs + 1000L) {
            deferred.await()
        }

        return if (completed == true) {
            TouchResult.Success("Gesture executed from ($startX, $startY) to ($endX, $endY)")
        } else {
            TouchResult.Failure("Gesture timed out or was rejected by Android system.")
        }
    }
}

actual fun createPlatformTouchController(): TouchController = AndroidAccessibilityTouchController()
