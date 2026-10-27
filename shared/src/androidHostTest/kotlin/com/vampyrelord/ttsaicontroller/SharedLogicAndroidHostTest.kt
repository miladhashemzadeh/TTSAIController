package com.vampyrelord.ttsaicontroller

import com.vampyrelord.ttsaicontroller.features.touch.data.AndroidAccessibilityRegistry
import com.vampyrelord.ttsaicontroller.features.touch.data.AndroidAccessibilityTouchController
import com.vampyrelord.ttsaicontroller.features.touch.data.AndroidGestureDispatcher
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPoint
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchResult
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedLogicAndroidHostTest {

    @BeforeTest
    @AfterTest
    fun cleanup() {
        AndroidAccessibilityRegistry.dispatcher = null
    }

    @Test
    fun testAccessibilityServiceDisabledReportsCapabilities() {
        val controller = AndroidAccessibilityTouchController()
        val capabilities = controller.checkCapabilities()

        assertEquals("Android (AccessibilityService Gesture API)", capabilities.platformName)
        assertTrue(capabilities.requiresAccessibilityService)
        assertFalse(capabilities.isAccessibilityServiceEnabled)
        assertFalse(capabilities.isAutomationSupported)
    }

    @Test
    fun testAccessibilityServiceEnabledExecutesGestures() = runTest {
        val mockDispatcher = object : AndroidGestureDispatcher {
            val dispatches = mutableListOf<String>()

            override fun isServiceActive(): Boolean = true

            override fun dispatchGesturePath(
                startX: Float,
                startY: Float,
                endX: Float,
                endY: Float,
                durationMs: Long,
                onComplete: (Boolean) -> Unit
            ) {
                dispatches.add("GESTURE($startX, $startY -> $endX, $endY)")
                onComplete(true)
            }
        }

        AndroidAccessibilityRegistry.dispatcher = mockDispatcher
        val controller = AndroidAccessibilityTouchController()

        val capabilities = controller.checkCapabilities()
        assertTrue(capabilities.isAccessibilityServiceEnabled)
        assertTrue(capabilities.isAutomationSupported)

        val result = controller.tap(TouchPoint("pt-1", 0, 0.5f, 0.5f))
        assertTrue(result is TouchResult.Success)
        assertEquals(1, mockDispatcher.dispatches.size)
    }
}
