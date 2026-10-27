package com.vampyrelord.ttsaicontroller.features.touch.data

import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchController
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlatformTouchCapability
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.ScrollDirection
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchResult
import kotlinx.coroutines.delay
import java.awt.AWTException
import java.awt.Robot
import java.awt.Toolkit
import java.awt.event.InputEvent
import kotlin.math.roundToInt

class DesktopRobotTouchController : BaseTouchController() {

    private val robot: Robot? by lazy {
        try {
            Robot().apply { isAutoWaitForIdle = true }
        } catch (e: Throwable) {
            null
        }
    }

    override fun checkCapabilities(): PlatformTouchCapability {
        val isAwtSupported = robot != null
        return PlatformTouchCapability(
            platformName = "Desktop JVM (java.awt.Robot)",
            isAutomationSupported = isAwtSupported,
            requiresAccessibilityService = false,
            guidanceMessage = if (isAwtSupported) {
                "Desktop native mouse and touch emulation ready via java.awt.Robot."
            } else {
                "AWT Robot is unavailable on headless environment."
            }
        )
    }

    override suspend fun performTap(pixelX: Float, pixelY: Float, durationMs: Long): TouchResult {
        val r = robot ?: return TouchResult.Failure("Robot not initialized on desktop")
        return try {
            val screenSize = Toolkit.getDefaultToolkit().screenSize
            val targetX = pixelX.coerceIn(0f, screenSize.width.toFloat()).toInt()
            val targetY = pixelY.coerceIn(0f, screenSize.height.toFloat()).toInt()

            r.mouseMove(targetX, targetY)
            r.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            delay(durationMs.coerceAtLeast(10L))
            r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            TouchResult.Success("Tapped at ($targetX, $targetY)")
        } catch (e: Throwable) {
            TouchResult.Failure("Desktop tap failed: ${e.message}")
        }
    }

    override suspend fun performDoubleTap(pixelX: Float, pixelY: Float, intervalMs: Long): TouchResult {
        val r = robot ?: return TouchResult.Failure("Robot not initialized on desktop")
        return try {
            performTap(pixelX, pixelY, 30L)
            delay(intervalMs.coerceAtLeast(50L))
            performTap(pixelX, pixelY, 30L)
            TouchResult.Success("Double tapped at ($pixelX, $pixelY)")
        } catch (e: Throwable) {
            TouchResult.Failure("Desktop double tap failed: ${e.message}")
        }
    }

    override suspend fun performLongPress(pixelX: Float, pixelY: Float, durationMs: Long): TouchResult {
        val r = robot ?: return TouchResult.Failure("Robot not initialized on desktop")
        return try {
            val screenSize = Toolkit.getDefaultToolkit().screenSize
            val targetX = pixelX.coerceIn(0f, screenSize.width.toFloat()).toInt()
            val targetY = pixelY.coerceIn(0f, screenSize.height.toFloat()).toInt()

            r.mouseMove(targetX, targetY)
            r.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            delay(durationMs.coerceAtLeast(400L))
            r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            TouchResult.Success("Long pressed at ($targetX, $targetY)")
        } catch (e: Throwable) {
            TouchResult.Failure("Desktop long press failed: ${e.message}")
        }
    }

    override suspend fun performScroll(
        direction: ScrollDirection,
        distance: Float,
        durationMs: Long
    ): TouchResult {
        val r = robot ?: return TouchResult.Failure("Robot not initialized on desktop")
        return try {
            // In AWT Robot: mouseWheel negative is scrolling up/away, positive is scrolling down/towards user
            val notches = (distance / 100f).roundToInt().coerceAtLeast(1)
            val wheelDirection = when (direction) {
                ScrollDirection.UP -> -notches
                ScrollDirection.DOWN -> notches
                ScrollDirection.LEFT, ScrollDirection.RIGHT -> notches // Horizontal fallback
            }
            r.mouseWheel(wheelDirection)
            delay(durationMs.coerceAtLeast(50L))
            TouchResult.Success("Scrolled $direction by $distance px")
        } catch (e: Throwable) {
            TouchResult.Failure("Desktop scroll failed: ${e.message}")
        }
    }
}

actual fun createPlatformTouchController(): TouchController = DesktopRobotTouchController()
