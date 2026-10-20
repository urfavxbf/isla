package com.pao.isla

import android.content.Context
import androidx.compose.ui.graphics.Color

data class IslandConfig(
    val widthDp: Int = 190,
    val heightDp: Int = 36,
    val radiusDp: Int = 24,
    val transparency: Float = 1f,
    val accentHex: Long = 0xFF8B5CF6
) {
    val accent: Color get() = Color(accentHex)
    fun save(context: Context) {
        context.getSharedPreferences("isla_config", Context.MODE_PRIVATE).edit()
            .putInt("width", widthDp).putInt("height", heightDp).putInt("radius", radiusDp)
            .putFloat("transparency", transparency).putLong("accent", accentHex).apply()
    }
    companion object {
        fun load(context: Context): IslandConfig {
            val p = context.getSharedPreferences("isla_config", Context.MODE_PRIVATE)
            return IslandConfig(p.getInt("width", 190), p.getInt("height", 36), p.getInt("radius", 24),
                p.getFloat("transparency", 1f), p.getLong("accent", 0xFF8B5CF6))
        }
    }
}
