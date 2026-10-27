package com.vampyrelord.ttsaicontroller

import com.vampyrelord.ttsaicontroller.features.touch.data.BaseTouchController
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.CoordinateType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlatformTouchCapability
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.ScrollDirection
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchActionType
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchExecutionStatus
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchPoint
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchResult
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchSequence
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeTouchController(
    val shouldFailOnStep: Int = -1
) : BaseTouchController() {

    val executedOperations = mutableListOf<String>()

    override fun checkCapabilities(): PlatformTouchCapability {
        return PlatformTouchCapability("FakeController", isAutomationSupported = true)
    }

    override suspend fun performTap(pixelX: Float, pixelY: Float, durationMs: Long): TouchResult {
        if (executedOperations.size == shouldFailOnStep) {
            return TouchResult.Failure("Simulated tap failure at step $shouldFailOnStep")
        }
        executedOperations.add("TAP($pixelX, $pixelY, ${durationMs}ms)")
        return TouchResult.Success()
    }

    override suspend fun performDoubleTap(pixelX: Float, pixelY: Float, intervalMs: Long): TouchResult {
        executedOperations.add("DOUBLE_TAP($pixelX, $pixelY)")
        return TouchResult.Success()
    }

    override suspend fun performLongPress(pixelX: Float, pixelY: Float, durationMs: Long): TouchResult {
        executedOperations.add("LONG_PRESS($pixelX, $pixelY, ${durationMs}ms)")
        return TouchResult.Success()
    }

    override suspend fun performScroll(
        direction: ScrollDirection,
        distance: Float,
        durationMs: Long
    ): TouchResult {
        executedOperations.add("SCROLL($direction, $distance)")
        return TouchResult.Success()
    }
}

class TouchControllerExecutionTest {

    @Test
    fun testSuccessfulSequenceExecution() = runTest {
        val fake = FakeTouchController()
        val points = listOf(
            TouchPoint("pt-1", 0, 0.2f, 0.4f, CoordinateType.NORMALIZED, TouchActionType.TAP, delayMillis = 10L, durationMillis = 20L),
            TouchPoint("pt-2", 1, 0.5f, 0.6f, CoordinateType.NORMALIZED, TouchActionType.DOUBLE_TAP, delayMillis = 10L),
            TouchPoint("pt-3", 2, 0.8f, 0.9f, CoordinateType.NORMALIZED, TouchActionType.LONG_PRESS, delayMillis = 10L, durationMillis = 400L)
        )

        val sequence = TouchSequence(
            id = "seq-test-1",
            name = "Test Sequence",
            points = points,
            repeatCount = 1,
            screenWidth = 1000f,
            screenHeight = 2000f
        )

        val report = fake.execute(sequence)

        assertTrue(report.isSuccess)
        assertEquals(3, report.completedSteps)
        assertEquals(TouchExecutionStatus.COMPLETED, fake.executionState.value.status)

        // Verify correct operation calls and coordinate translations
        assertEquals(3, fake.executedOperations.size)
        assertEquals("TAP(200.0, 800.0, 20ms)", fake.executedOperations[0])
        assertEquals("DOUBLE_TAP(500.0, 1200.0)", fake.executedOperations[1])
        assertEquals("LONG_PRESS(800.0, 1800.0, 400ms)", fake.executedOperations[2])
    }

    @Test
    fun testMultiIterationExecution() = runTest {
        val fake = FakeTouchController()
        val points = listOf(
            TouchPoint("pt-1", 0, 0.1f, 0.1f, CoordinateType.NORMALIZED, TouchActionType.TAP, delayMillis = 5L)
        )

        val sequence = TouchSequence(
            id = "seq-multi",
            name = "Multi Iteration",
            points = points,
            repeatCount = 3,
            delayBetweenIterationsMs = 10L
        )

        val report = fake.execute(sequence)
        assertTrue(report.isSuccess)
        assertEquals(3, report.completedSteps)
        assertEquals(3, report.iterationsCompleted)
        assertEquals(3, fake.executedOperations.size)
    }

    @Test
    fun testCancellationHaltsSubsequentOperations() = runTest {
        val fake = FakeTouchController()
        val points = listOf(
            TouchPoint("pt-1", 0, 0.2f, 0.2f, delayMillis = 50L),
            TouchPoint("pt-2", 1, 0.4f, 0.4f, delayMillis = 50L),
            TouchPoint("pt-3", 2, 0.6f, 0.6f, delayMillis = 50L)
        )

        val sequence = TouchSequence("seq-cancel", "Cancel Test", points, repeatCount = 1)

        // Cancel immediately
        fake.cancel()
        val report = fake.execute(sequence)

        assertFalse(report.isSuccess)
        assertEquals(TouchExecutionStatus.CANCELLED, fake.executionState.value.status)
        // No operations should have completed
        assertEquals(0, fake.executedOperations.size)
    }

    @Test
    fun testStepFailureAbortsExecution() = runTest {
        // Step 1 fails
        val fake = FakeTouchController(shouldFailOnStep = 1)
        val points = listOf(
            TouchPoint("pt-1", 0, 0.1f, 0.1f, delayMillis = 5L),
            TouchPoint("pt-2", 1, 0.2f, 0.2f, delayMillis = 5L),
            TouchPoint("pt-3", 2, 0.3f, 0.3f, delayMillis = 5L)
        )

        val sequence = TouchSequence("seq-fail", "Fail Test", points)
        val report = fake.execute(sequence)

        assertFalse(report.isSuccess)
        assertEquals(TouchExecutionStatus.ERROR, fake.executionState.value.status)
        // Only step 0 succeeded, step 1 failed, step 2 was not executed
        assertEquals(1, fake.executedOperations.size)
    }
}
