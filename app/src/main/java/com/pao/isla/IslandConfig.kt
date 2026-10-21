package com.pao.isla

import android.content.Context
import androidx.compose.ui.graphics.Color

/**
 * Saved dimensions are density-independent pixels (dp).
 * positionYDp is measured from the top edge of the display:
 * negative = higher, positive = lower.
 */
data class IslandConfig(
    val widthDp: Int = 190,
    val heightDp: Int = 36,
    val radiusDp: Int = 24,
    val transparency: Float = 1f,
    val accentHex: Long = 0xFF8B5CF6,
    val positionYDp: Int = 0,
    val showMedia: Boolean = true
) {
    val accent: Color get() = Color(accentHex.toInt())
    val isHoleSize: Boolean get() = widthDp < 56
    val isCompact: Boolean get() = widthDp < 120

    fun save(context: Context) {
        context.getSharedPreferences("isla_config", Context.MODE_PRIVATE).edit()
            .putInt("width", widthDp)
            .putInt("height", heightDp)
            .putInt("radius", radiusDp)
            .putFloat("transparency", transparency)
            .putLong("accent", accentHex)
            .putInt("position_y", positionYDp)
            .putBoolean("show_media", showMedia)
            .apply()
    }

    companion object {
        fun load(context: Context): IslandConfig {
            val p = context.getSharedPreferences("isla_config", Context.MODE_PRIVATE)
            return IslandConfig(
                widthDp = p.getInt("width", 190).coerceIn(28, 300),
                heightDp = p.getInt("height", 36).coerceIn(24, 64),
                radiusDp = p.getInt("radius", 24).coerceIn(12, 36),
                transparency = p.getFloat("transparency", 1f).coerceIn(0.45f, 1f),
                accentHex = p.getLong("accent", 0xFF8B5CF6),
                positionYDp = p.getInt("position_y", 0).coerceIn(-72, 80),
                showMedia = p.getBoolean("show_media", true)
            )
        }
    }
}
