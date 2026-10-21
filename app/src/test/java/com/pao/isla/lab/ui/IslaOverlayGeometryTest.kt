package com.pao.isla.lab.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IslaOverlayGeometryTest {
    @Test
    fun centerPresetIsCenteredInPreview() {
        val p = islaPreviewPill(
            frameWidthPx = 240f,
            frameHeightPx = 420f,
            screenWidthDp = 360f,
            pillWidthDp = 112f,
            pillHeightDp = 34f,
            xOffsetDp = 0f,
            yOffsetDp = 10f
        )
        assertEquals((240f - p.width) / 2f, p.left, 0.01f)
        assertTrue(p.top > 0f)
    }

    @Test
    fun leftAndRightLandscapeAnchorsStayInsideScreen() {
        val left = islaPreviewPill(400f, 170f, 800f, 120f, 34f, 0f, 10f, anchor = "left")
        val right = islaPreviewPill(400f, 170f, 800f, 120f, 34f, 0f, 10f, anchor = "right")
        assertTrue(left.left < right.left)
        assertTrue(left.left >= 0f)
        assertTrue(right.left + right.width <= 400f)
    }

    @Test
    fun hugeOffsetsAndDimensionsAreClampedToPreviewFrame() {
        val p = islaPreviewPill(210f, 160f, 360f, 9999f, 9999f, 9000f, 9000f)
        assertTrue(p.left >= 0f && p.left + p.width <= 210f)
        assertTrue(p.top >= 0f && p.top + p.height <= 160f)
        assertTrue(p.width > 0f && p.height > 0f)
    }

    @Test
    fun notchDockingStartsAtTopWhenZeroOffset() {
        val p = islaPreviewPill(300f, 340f, 360f, 175f, 35f, 0f, 0f, docked = true)
        assertEquals(0f, p.top, 0.01f)
    }

    @Test
    fun invalidGeometryNeverProducesNanOrNegativeDimensions() {
        val p = islaPreviewPill(300f, 400f, Float.NaN,
            Float.POSITIVE_INFINITY, Float.NaN, Float.NaN, Float.NEGATIVE_INFINITY)
        assertTrue(p.left.isFinite() && p.top.isFinite())
        assertTrue(p.width > 0f && p.height > 0f)
        assertEquals(IslaPreviewPill(0f, 0f, 0f, 0f),
            islaPreviewPill(-1f, 400f, 360f, 100f, 32f, 0f, 0f))
    }
}
