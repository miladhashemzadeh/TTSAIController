package com.vampyrelord.ttsaicontroller.touch

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import com.vampyrelord.ttsaicontroller.features.touch.data.AndroidAccessibilityRegistry
import com.vampyrelord.ttsaicontroller.features.touch.data.AndroidGestureDispatcher

/**
 * Android AccessibilityService responsible for executing system-wide touch gestures
 * on behalf of the cross-platform [TouchController].
 */
class TouchAutomationAccessibilityService : AccessibilityService(), AndroidGestureDispatcher {

    override fun onServiceConnected() {
        super.onServiceConnected()
        AndroidAccessibilityRegistry.dispatcher = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No action required on event notifications
    }

    override fun onInterrupt() {
        // Interrupt callback
    }

    override fun onDestroy() {
        if (AndroidAccessibilityRegistry.dispatcher === this) {
            AndroidAccessibilityRegistry.dispatcher = null
        }
        super.onDestroy()
    }

    override fun isServiceActive(): Boolean = true

    override fun dispatchGesturePath(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long,
        onComplete: (Boolean) -> Unit
    ) {
        val path = Path().apply {
            moveTo(startX, startY)
            if (startX != endX || startY != endY) {
                lineTo(endX, endY)
            }
        }

        val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs.coerceAtLeast(10L))
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(
            gesture,
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    onComplete(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    onComplete(false)
                }
            },
            null
        )
    }
}
