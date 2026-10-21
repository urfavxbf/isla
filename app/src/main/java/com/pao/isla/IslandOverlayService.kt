package com.pao.isla

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class IslandOverlayService : Service() {
    private lateinit var wm: WindowManager
    private var island: TextView? = null
    private var expanded = false

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                removeIsland()
                stopSelf()
            }
            ACTION_UPDATE -> {
                // Editing settings must not turn the island back on if it was stopped.
                if (island != null) {
                    removeIsland()
                    if (Settings.canDrawOverlays(this)) showIsland()
                } else {
                    stopSelf()
                }
            }
            else -> if (Settings.canDrawOverlays(this)) showIsland() else stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun showIsland() {
        if (island != null) return
        val c = IslandConfig.load(this)
        expanded = false

        val view = TextView(this).apply {
            text = collapsedLabel(c)
            setTextColor(AndroidColor.WHITE)
            textSize = 11f
            gravity = Gravity.CENTER
            alpha = c.transparency
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(AndroidColor.BLACK)
                cornerRadius = dp(c.radiusDp).toFloat()
                setStroke(dp(1).coerceAtLeast(1), c.accentHex.toInt())
            }
            // A 28-48dp island needs minimal padding so the pill can stay circular.
            setPadding(if (c.isCompact) dp(2) else dp(12), 0,
                if (c.isCompact) dp(2) else dp(12), 0)
            elevation = dp(10).toFloat()
            contentDescription = "Isla floating island. Tap to expand or collapse."
            setOnClickListener {
                expanded = !expanded
                text = if (expanded) "●   Isla is active   •   Tap to close" else collapsedLabel(c)
                setPadding(if (expanded) dp(12) else if (c.isCompact) dp(2) else dp(12),
                    0, if (expanded) dp(12) else if (c.isCompact) dp(2) else dp(12), 0)
                (layoutParams as? WindowManager.LayoutParams)?.let { p ->
                    p.width = dp(if (expanded) c.widthDp.coerceAtLeast(240) else c.widthDp)
                    p.height = dp(if (expanded) (c.heightDp + 24).coerceIn(56, 90) else c.heightDp)
                    try {
                        wm.updateViewLayout(this, p)
                    } catch (_: IllegalArgumentException) {
                        // View may have been removed while settings changed.
                    }
                }
            }
        }

        val params = WindowManager.LayoutParams(
            dp(c.widthDp),
            dp(c.heightDp),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            // Use the physical screen top instead of the old +8dp offset.
            // Negative positions can help on OEMs that inset application overlays.
            y = dp(c.positionYDp)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        try {
            wm.addView(view, params)
            island = view
        } catch (_: SecurityException) {
            island = null
        } catch (_: WindowManager.BadTokenException) {
            island = null
        }
    }

    private fun collapsedLabel(config: IslandConfig): String = when {
        config.isHoleSize -> "" // A clean circular mask around the camera punch-hole.
        config.isCompact -> "●"
        else -> "●     Isla     ●"
    }

    private fun removeIsland() {
        island?.let {
            try {
                wm.removeView(it)
            } catch (_: IllegalArgumentException) {
                // Already removed by WindowManager.
            }
        }
        island = null
        expanded = false
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        removeIsland()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.pao.isla.START"
        const val ACTION_STOP = "com.pao.isla.STOP"
        const val ACTION_UPDATE = "com.pao.isla.UPDATE"
    }
}
