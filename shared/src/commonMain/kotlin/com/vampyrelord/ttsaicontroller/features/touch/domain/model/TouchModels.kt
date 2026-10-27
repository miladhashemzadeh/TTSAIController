package com.vampyrelord.ttsaicontroller.features.touch.domain.model

import com.vampyrelord.ttsaicontroller.core.util.currentTimeMillis
import kotlin.math.cos
import kotlin.math.sin

/**
 * Coordinate representation mode.
 */
enum class CoordinateType {
    NORMALIZED,      // Values 0.0f to 1.0f relative to screen width/height
    SCREEN_PIXELS    // Absolute pixel values on screen
}

/**
 * Action to perform at a given touch point.
 */
enum class TouchActionType {
    TAP,
    DOUBLE_TAP,
    LONG_PRESS,
    SWIPE_UP,
    SWIPE_DOWN
}

/**
 * Scroll or swipe direction.
 */
enum class ScrollDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

/**
 * A single touch point within an automation sequence.
 */
data class TouchPoint(
    val id: String,
    val order: Int,
    val x: Float,
    val y: Float,
    val coordinateType: CoordinateType = CoordinateType.NORMALIZED,
    val actionType: TouchActionType = TouchActionType.TAP,
    val delayMillis: Long = 100L,
    val durationMillis: Long = 50L
) {
    /**
     * Converts to absolute pixel coordinates given screen bounds.
     */
    fun toPixelPosition(screenWidth: Float, screenHeight: Float): Pair<Float, Float> {
        return when (coordinateType) {
            CoordinateType.NORMALIZED -> Pair(
                (x * screenWidth).coerceIn(0f, screenWidth),
                (y * screenHeight).coerceIn(0f, screenHeight)
            )
            CoordinateType.SCREEN_PIXELS -> Pair(
                x.coerceIn(0f, screenWidth),
                y.coerceIn(0f, screenHeight)
            )
        }
    }

    /**
     * Converts to normalized coordinates (0.0 to 1.0) given screen bounds.
     */
    fun toNormalizedPosition(screenWidth: Float, screenHeight: Float): Pair<Float, Float> {
        if (screenWidth <= 0f || screenHeight <= 0f) return Pair(0.5f, 0.5f)
        return when (coordinateType) {
            CoordinateType.NORMALIZED -> Pair(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))
            CoordinateType.SCREEN_PIXELS -> Pair(
                (x / screenWidth).coerceIn(0f, 1f),
                (y / screenHeight).coerceIn(0f, 1f)
            )
        }
    }
}

/**
 * Strategies for automatic placement of touch points.
 */
enum class AutomaticPlacementStrategy {
    GRID,
    CIRCULAR,
    HORIZONTAL_ROW,
    VERTICAL_COLUMN,
    BALANCED_SPREAD
}

/**
 * Placement configuration mode.
 */
enum class PlacementMode {
    MANUAL,
    AUTOMATIC
}

/**
 * Execution state of the touch automation engine.
 */
enum class TouchExecutionStatus {
    IDLE,
    PREPARING,
    RUNNING,
    PAUSED,
    COMPLETED,
    CANCELLED,
    ERROR
}

/**
 * Detailed execution state containing current step progress.
 */
data class TouchExecutionState(
    val status: TouchExecutionStatus = TouchExecutionStatus.IDLE,
    val activeStepIndex: Int = -1,
    val completedSteps: Int = 0,
    val totalSteps: Int = 0,
    val currentIteration: Int = 0,
    val totalIterations: Int = 1,
    val errorMessage: String? = null
)

/**
 * Single operation result.
 */
sealed interface TouchResult {
    data class Success(val message: String = "Operation completed successfully") : TouchResult
    data class Failure(val error: String, val recoverable: Boolean = true) : TouchResult
}

/**
 * Full execution sequence report.
 */
data class TouchExecutionReport(
    val sequenceId: String,
    val startTime: Long = currentTimeMillis(),
    val durationMs: Long = 0L,
    val completedSteps: Int = 0,
    val totalSteps: Int = 0,
    val iterationsCompleted: Int = 0,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Executable touch sequence.
 */
data class TouchSequence(
    val id: String,
    val name: String,
    val points: List<TouchPoint>,
    val repeatCount: Int = 1,
    val delayBetweenIterationsMs: Long = 500L,
    val screenWidth: Float = 1080f,
    val screenHeight: Float = 1920f
)

/**
 * Persistent Rushwulf configuration settings for touch automation.
 */
data class RushwulfSettings(
    val profileName: String = "Default Rushwulf Profile",
    val defaultTapDurationMs: Long = 50L,
    val defaultDoubleTapIntervalMs: Long = 200L,
    val defaultLongPressDurationMs: Long = 600L,
    val defaultStepDelayMs: Long = 150L,
    val scrollStepDistancePixels: Float = 400f,
    val scrollDurationMs: Long = 250L,
    val repeatCount: Int = 1,
    val stopOnError: Boolean = true,
    val showVisualOverlay: Boolean = true,
    val autoRestartOnFail: Boolean = false
)

/**
 * Saved touch configuration profile.
 */
data class SavedTouchConfig(
    val id: String,
    val name: String,
    val points: List<TouchPoint>,
    val placementMode: PlacementMode = PlacementMode.MANUAL,
    val strategy: AutomaticPlacementStrategy = AutomaticPlacementStrategy.GRID,
    val rushwulfSettings: RushwulfSettings = RushwulfSettings(),
    val createdAt: Long = currentTimeMillis()
)

/**
 * Platform touch automation capability descriptor.
 */
data class PlatformTouchCapability(
    val platformName: String,
    val isAutomationSupported: Boolean,
    val requiresAccessibilityService: Boolean = false,
    val isAccessibilityServiceEnabled: Boolean = false,
    val requiresOverlayPermission: Boolean = false,
    val isOverlayPermissionGranted: Boolean = false,
    val guidanceMessage: String = ""
)

/**
 * Utility for generating and validating touch positions.
 */
object TouchPositionGenerator {

    /**
     * Generates a list of [TouchPoint]s according to the specified [strategy].
     * Coordinates are returned in NORMALIZED [0.0..1.0] coordinate space.
     */
    fun generatePositions(
        count: Int,
        strategy: AutomaticPlacementStrategy,
        defaultDelayMs: Long = 150L,
        defaultDurationMs: Long = 50L
    ): List<TouchPoint> {
        val safeCount = count.coerceIn(1, 20)
        return when (strategy) {
            AutomaticPlacementStrategy.GRID -> generateGrid(safeCount, defaultDelayMs, defaultDurationMs)
            AutomaticPlacementStrategy.CIRCULAR -> generateCircular(safeCount, defaultDelayMs, defaultDurationMs)
            AutomaticPlacementStrategy.HORIZONTAL_ROW -> generateHorizontalRow(safeCount, defaultDelayMs, defaultDurationMs)
            AutomaticPlacementStrategy.VERTICAL_COLUMN -> generateVerticalColumn(safeCount, defaultDelayMs, defaultDurationMs)
            AutomaticPlacementStrategy.BALANCED_SPREAD -> generateBalancedSpread(safeCount, defaultDelayMs, defaultDurationMs)
        }
    }

    private fun generateGrid(count: Int, delayMs: Long, durationMs: Long): List<TouchPoint> {
        val cols = if (count <= 3) count else if (count <= 8) 3 else 4
        val rows = (count + cols - 1) / cols

        val points = mutableListOf<TouchPoint>()
        for (i in 0 until count) {
            val r = i / cols
            val c = i % cols
            val x = (c + 1f) / (cols + 1f)
            val y = (r + 1f) / (rows + 1f)
            points.add(
                TouchPoint(
                    id = "pt-grid-${i + 1}",
                    order = i,
                    x = x,
                    y = y,
                    coordinateType = CoordinateType.NORMALIZED,
                    delayMillis = delayMs,
                    durationMillis = durationMs
                )
            )
        }
        return points
    }

    private fun generateCircular(count: Int, delayMs: Long, durationMs: Long): List<TouchPoint> {
        val points = mutableListOf<TouchPoint>()
        val centerX = 0.5f
        val centerY = 0.5f
        val radius = 0.3f
        val angleStep = (2 * kotlin.math.PI) / count

        for (i in 0 until count) {
            val angle = i * angleStep - (kotlin.math.PI / 2) // Start from top
            val x = (centerX + radius * cos(angle)).toFloat().coerceIn(0.05f, 0.95f)
            val y = (centerY + radius * sin(angle)).toFloat().coerceIn(0.05f, 0.95f)
            points.add(
                TouchPoint(
                    id = "pt-circ-${i + 1}",
                    order = i,
                    x = x,
                    y = y,
                    coordinateType = CoordinateType.NORMALIZED,
                    delayMillis = delayMs,
                    durationMillis = durationMs
                )
            )
        }
        return points
    }

    private fun generateHorizontalRow(count: Int, delayMs: Long, durationMs: Long): List<TouchPoint> {
        val points = mutableListOf<TouchPoint>()
        val y = 0.5f
        for (i in 0 until count) {
            val x = (i + 1f) / (count + 1f)
            points.add(
                TouchPoint(
                    id = "pt-hrow-${i + 1}",
                    order = i,
                    x = x,
                    y = y,
                    coordinateType = CoordinateType.NORMALIZED,
                    delayMillis = delayMs,
                    durationMillis = durationMs
                )
            )
        }
        return points
    }

    private fun generateVerticalColumn(count: Int, delayMs: Long, durationMs: Long): List<TouchPoint> {
        val points = mutableListOf<TouchPoint>()
        val x = 0.5f
        for (i in 0 until count) {
            val y = (i + 1f) / (count + 1f)
            points.add(
                TouchPoint(
                    id = "pt-vcol-${i + 1}",
                    order = i,
                    x = x,
                    y = y,
                    coordinateType = CoordinateType.NORMALIZED,
                    delayMillis = delayMs,
                    durationMillis = durationMs
                )
            )
        }
        return points
    }

    private fun generateBalancedSpread(count: Int, delayMs: Long, durationMs: Long): List<TouchPoint> {
        // Generates an even diamond/spread pattern
        val points = mutableListOf<TouchPoint>()
        for (i in 0 until count) {
            val step = if (count > 1) i.toFloat() / (count - 1) else 0.5f
            val x = 0.2f + 0.6f * step
            val y = 0.25f + 0.5f * (if (i % 2 == 0) step else 1f - step)
            points.add(
                TouchPoint(
                    id = "pt-spread-${i + 1}",
                    order = i,
                    x = x.coerceIn(0.05f, 0.95f),
                    y = y.coerceIn(0.05f, 0.95f),
                    coordinateType = CoordinateType.NORMALIZED,
                    delayMillis = delayMs,
                    durationMillis = durationMs
                )
            )
        }
        return points
    }

    /**
     * Validates a list of [TouchPoint]s.
     * Returns null if valid, or a descriptive error message if invalid.
     */
    fun validatePoints(points: List<TouchPoint>): String? {
        if (points.isEmpty()) {
            return "At least one touch position must be configured."
        }
        for ((index, point) in points.withIndex()) {
            when (point.coordinateType) {
                CoordinateType.NORMALIZED -> {
                    if (point.x !in 0.0f..1.0f || point.y !in 0.0f..1.0f) {
                        return "Touch point #${index + 1} has out-of-bounds coordinates (${point.x}, ${point.y}). Normalized coordinates must be between 0.0 and 1.0."
                    }
                }
                CoordinateType.SCREEN_PIXELS -> {
                    if (point.x < 0f || point.y < 0f) {
                        return "Touch point #${index + 1} has negative pixel coordinates (${point.x}, ${point.y})."
                    }
                }
            }
            if (point.delayMillis < 0) {
                return "Touch point #${index + 1} has negative delay (${point.delayMillis}ms)."
            }
            if (point.durationMillis < 0) {
                return "Touch point #${index + 1} has negative duration (${point.durationMillis}ms)."
            }
        }
        return null
    }
}
