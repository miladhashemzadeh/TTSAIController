package com.vampyrelord.ttsaicontroller

import com.vampyrelord.ttsaicontroller.features.touch.domain.model.AutomaticPlacementStrategy
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.CoordinateType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchActionType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPoint
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPositionGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TouchPositionGeneratorTest {

    @Test
    fun testGenerateGridPositions() {
        val points = TouchPositionGenerator.generatePositions(4, AutomaticPlacementStrategy.GRID)
        assertEquals(4, points.size)

        // All points must be within [0.0..1.0]
        points.forEach { pt ->
            assertTrue(pt.x in 0f..1f, "X coordinate ${pt.x} out of bounds")
            assertTrue(pt.y in 0f..1f, "Y coordinate ${pt.y} out of bounds")
            assertEquals(CoordinateType.NORMALIZED, pt.coordinateType)
        }

        // Validate points validation function
        assertNull(TouchPositionGenerator.validatePoints(points))
    }

    @Test
    fun testGenerateCircularPositions() {
        val count = 6
        val points = TouchPositionGenerator.generatePositions(count, AutomaticPlacementStrategy.CIRCULAR)
        assertEquals(count, points.size)

        points.forEach { pt ->
            assertTrue(pt.x in 0f..1f)
            assertTrue(pt.y in 0f..1f)
        }
        assertNull(TouchPositionGenerator.validatePoints(points))
    }

    @Test
    fun testGenerateRowAndColumnPositions() {
        val rowPoints = TouchPositionGenerator.generatePositions(3, AutomaticPlacementStrategy.HORIZONTAL_ROW)
        assertEquals(3, rowPoints.size)
        // In horizontal row, Y should be constant (centered ~0.5f)
        rowPoints.forEach { assertEquals(0.5f, it.y) }

        val colPoints = TouchPositionGenerator.generatePositions(3, AutomaticPlacementStrategy.VERTICAL_COLUMN)
        assertEquals(3, colPoints.size)
        // In vertical column, X should be constant (centered ~0.5f)
        colPoints.forEach { assertEquals(0.5f, it.x) }
    }

    @Test
    fun testCoordinateValidationFailsOnOutOfBounds() {
        // Empty points
        assertNotNull(TouchPositionGenerator.validatePoints(emptyList()))

        // Negative coordinate
        val invalidX = listOf(
            TouchPoint("pt-1", 0, -0.1f, 0.5f, CoordinateType.NORMALIZED)
        )
        assertNotNull(TouchPositionGenerator.validatePoints(invalidX))

        // Exceeding 1.0f in normalized coordinate
        val invalidY = listOf(
            TouchPoint("pt-2", 0, 0.5f, 1.2f, CoordinateType.NORMALIZED)
        )
        assertNotNull(TouchPositionGenerator.validatePoints(invalidY))

        // Negative delay
        val invalidDelay = listOf(
            TouchPoint("pt-3", 0, 0.5f, 0.5f, delayMillis = -50L)
        )
        assertNotNull(TouchPositionGenerator.validatePoints(invalidDelay))
    }

    @Test
    fun testPixelAndNormalizedCoordinateConversions() {
        val screenWidth = 1080f
        val screenHeight = 2400f

        val normPoint = TouchPoint("pt-1", 0, 0.25f, 0.75f, CoordinateType.NORMALIZED)
        val (pixelX, pixelY) = normPoint.toPixelPosition(screenWidth, screenHeight)

        assertEquals(270f, pixelX)
        assertEquals(1800f, pixelY)

        val pixelPoint = TouchPoint("pt-2", 1, 540f, 1200f, CoordinateType.SCREEN_PIXELS)
        val (normX, normY) = pixelPoint.toNormalizedPosition(screenWidth, screenHeight)

        assertEquals(0.5f, normX)
        assertEquals(0.5f, normY)
    }

    @Test
    fun testOrderSequencing() {
        val points = TouchPositionGenerator.generatePositions(5, AutomaticPlacementStrategy.BALANCED_SPREAD)
        assertEquals(5, points.size)
        for (i in 0 until 5) {
            assertEquals(i, points[i].order)
        }
    }
}
