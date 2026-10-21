package com.pao.isla.lab.ui

import kotlin.math.max
import kotlin.math.min

/**
 * Screen-space positioning for the *illustrative* settings preview, not the real
 * WindowManager overlay. All values are validated here to avoid drawing outside
 * the preview card when a device has an unusual cutout.
 */
internal data class IslaPreviewPill(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

/**
 * The virtual screen's origin is (0,0); the physical panel width in dp determines
 * the visual scale so the user's offsets stay proportional to their display.
 */
internal fun islaPreviewPill(
    frameWidthPx: Float,
    frameHeightPx: Float,
    screenWidthDp: Float,
    pillWidthDp: Float,
    pillHeightDp: Float,
    xOffsetDp: Float,
    yOffsetDp: Float,
    anchor: String = "middle",
    docked: Boolean = false
): IslaPreviewPill {
    if (!frameWidthPx.isFinite() || !frameHeightPx.isFinite() ||
        frameWidthPx <= 0f || frameHeightPx <= 0f
    ) return IslaPreviewPill(0f, 0f, 0f, 0f)

    val safeDeviceWidth = screenWidthDp.takeIf { it.isFinite() && it > 0f } ?: 360f
    val scale = frameWidthPx / safeDeviceWidth
    val safeWidth = pillWidthDp.takeIf { it.isFinite() } ?: 112f
    val safeHeight = pillHeightDp.takeIf { it.isFinite() } ?: 34f
    val safeX = xOffsetDp.takeIf { it.isFinite() } ?: 0f
    val safeY = yOffsetDp.takeIf { it.isFinite() } ?: 0f
    val width = (safeWidth.coerceAtLeast(1f) * scale).coerceIn(min(8f, frameWidthPx), frameWidthPx)
    val height = (safeHeight.coerceAtLeast(1f) * scale).coerceIn(min(4f, frameHeightPx), frameHeightPx)

    val desiredCenterX = when (anchor) {
        "left" -> width / 2f + frameWidthPx * 0.06f
        "right" -> frameWidthPx - width / 2f - frameWidthPx * 0.06f
        else -> frameWidthPx / 2f
    }
    val left = (desiredCenterX + safeX * scale - width / 2f)
        .coerceIn(0f, max(0f, frameWidthPx - width))
    val top = ((if (docked) 0f else 12f * scale) + safeY * scale)
        .coerceIn(0f, max(0f, frameHeightPx - height))

    return IslaPreviewPill(left, top, width, height)
}
