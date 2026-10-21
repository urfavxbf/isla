/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pao.isla.lab.util

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.DisplayCutout
import android.view.WindowManager
import com.pao.isla.lab.data.IslaSettings

data class DetectedCutoutInfo(
    val xOffsetDp: Float,
    val yOffsetDp: Float,
    val widthDp: Float,
    val heightDp: Float,
    val hasHardwareCutout: Boolean
)

object CameraCutoutDetector {

    /**
     * The camera hole as best known, portrait only: the manual area when set, else the detected one (without the
     * padding the pill size adds), else null. Notch mode has no camera hole of its own (null).
     */
    fun areaFor(
        settings: IslaSettings,
        detected: DetectedCutoutInfo,
        isLandscape: Boolean
    ): com.pao.isla.lab.model.CutoutArea? = when {
        isLandscape || settings.enableNotchMode -> null
        settings.cameraCutoutManual -> com.pao.isla.lab.model.CutoutArea(
            settings.cameraCutoutX, settings.cameraCutoutY, settings.cameraCutoutWidth, settings.cameraCutoutHeight
        )
        detected.hasHardwareCutout -> com.pao.isla.lab.model.CutoutArea(
            detected.xOffsetDp,
            detected.yOffsetDp,
            (detected.widthDp - PADDING_W_DP).coerceAtLeast(1f),
            (detected.heightDp - PADDING_H_DP).coerceAtLeast(1f)
        )
        else -> null
    }

    /** Added around the real hole when the pill size is derived from it (see [calculateCutoutOffset]). */
    const val PADDING_W_DP = 24f
    const val PADDING_H_DP = 10f

    fun calculateCutoutOffset(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        screenWidthPx: Int,
        density: Float
    ): DetectedCutoutInfo {
        if (density <= 0f) {
            return DetectedCutoutInfo(0f, 12f, 112f, 34f, false)
        }
        val cutoutCenterX = (left + right) / 2f
        val screenCenterX = screenWidthPx / 2f
        val xOffsetPx = cutoutCenterX - screenCenterX
        val xOffsetDp = (xOffsetPx / density).coerceIn(
            IslaSettings.MIN_X_OFFSET,
            IslaSettings.MAX_X_OFFSET
        )

        val yOffsetDp = (top.toFloat() / density).coerceIn(
            IslaSettings.MIN_Y_OFFSET,
            IslaSettings.MAX_Y_OFFSET
        )

        val paddingW = PADDING_W_DP
        val paddingH = PADDING_H_DP
        val widthDp = ((right - left).toFloat() / density + paddingW).coerceIn(
            IslaSettings.MIN_WIDTH,
            IslaSettings.MAX_WIDTH
        )
        val heightDp = ((bottom - top).toFloat() / density + paddingH).coerceIn(
            IslaSettings.MIN_HEIGHT,
            IslaSettings.MAX_HEIGHT
        )

        return DetectedCutoutInfo(
            xOffsetDp = xOffsetDp,
            yOffsetDp = yOffsetDp,
            widthDp = widthDp,
            heightDp = heightDp,
            hasHardwareCutout = true
        )
    }

    fun calculateCutoutOffset(
        rect: Rect,
        screenWidthPx: Int,
        density: Float
    ): DetectedCutoutInfo = calculateCutoutOffset(
        left = rect.left,
        top = rect.top,
        right = rect.right,
        bottom = rect.bottom,
        screenWidthPx = screenWidthPx,
        density = density
    )

    fun detect(context: Context): DetectedCutoutInfo {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            ?: return fallbackInfo(context)

        val metrics = context.resources.displayMetrics
        val screenWidthPx = metrics.widthPixels
        val density = metrics.density

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val display = runCatching { windowManager.defaultDisplay }.getOrNull()
            val cutout: DisplayCutout? = try {
                display?.cutout
            } catch (e: Throwable) {
                null
            }

            val boundingRects = cutout?.boundingRects
            if (!boundingRects.isNullOrEmpty()) {
                val topCutout = boundingRects.minByOrNull { it.top }
                if (topCutout != null && topCutout.height() > 0) {
                    return calculateCutoutOffset(topCutout, screenWidthPx, density)
                }
            }
        }

        return fallbackInfo(context)
    }

    private fun fallbackInfo(context: Context): DetectedCutoutInfo {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        val heightPx = if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
        val density = context.resources.displayMetrics.density
        val heightDp = if (density > 0f) heightPx / density else 24f
        val yOffsetDp = (heightDp * 0.3f).coerceIn(0f, 30f)

        return DetectedCutoutInfo(
            xOffsetDp = 0f,
            yOffsetDp = yOffsetDp,
            widthDp = 112f,
            heightDp = 34f,
            hasHardwareCutout = false
        )
    }
}
