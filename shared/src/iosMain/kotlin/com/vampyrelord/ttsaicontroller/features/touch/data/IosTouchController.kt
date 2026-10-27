package com.vampyrelord.ttsaicontroller.features.touch.data

import com.vampyrelord.ttsaicontroller.features.touch.domain.TouchController
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.PlatformTouchCapability
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.ScrollDirection
import com.vampyrelord.ttsaicontroller.features.touch.domain.model.TouchResult

class IosTouchController : BaseTouchController() {

    override fun checkCapabilities(): PlatformTouchCapability {
        return PlatformTouchCapability(
            platformName = "iOS",
            isAutomationSupported = false,
            guidanceMessage = "iOS sandbox security prevents background touch injection."
        )
    }

    override suspend fun performTap(pixelX: Float, pixelY: Float, durationMs: Long): TouchResult {
        return TouchResult.Failure("Touch injection not permitted by iOS sandbox")
    }

    override suspend fun performDoubleTap(pixelX: Float, pixelY: Float, intervalMs: Long): TouchResult {
        return TouchResult.Failure("Touch injection not permitted by iOS sandbox")
    }

    override suspend fun performLongPress(pixelX: Float, pixelY: Float, durationMs: Long): TouchResult {
        return TouchResult.Failure("Touch injection not permitted by iOS sandbox")
    }

    override suspend fun performScroll(
        direction: ScrollDirection,
        distance: Float,
        durationMs: Long
    ): TouchResult {
        return TouchResult.Failure("Scroll injection not permitted by iOS sandbox")
    }
}

actual fun createPlatformTouchController(): TouchController = IosTouchController()
