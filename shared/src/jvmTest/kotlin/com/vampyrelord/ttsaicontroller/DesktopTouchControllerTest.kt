package com.vampyrelord.ttsaicontroller

import com.vampyrelord.ttsaicontroller.features.touch.data.DesktopRobotTouchController
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.CoordinateType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.ScrollDirection
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPoint
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DesktopTouchControllerTest {

    @Test
    fun testDesktopControllerCapabilities() {
        val controller = DesktopRobotTouchController()
        val capabilities = controller.checkCapabilities()

        assertNotNull(capabilities)
        assertEquals("Desktop JVM (java.awt.Robot)", capabilities.platformName)
    }

    @Test
    fun testDesktopCoordinateValidation() = runTest {
        val controller = DesktopRobotTouchController()
        val point = TouchPoint("pt-desk", 0, 0.5f, 0.5f, CoordinateType.NORMALIZED)

        // Coordinates can be translated safely without throwing exceptions
        val (px, py) = point.toPixelPosition(1920f, 1080f)
        assertTrue(px > 0f)
        assertTrue(py > 0f)
    }

    private fun assertEquals(expected: String, actual: String) {
        kotlin.test.assertEquals(expected, actual)
    }
}
