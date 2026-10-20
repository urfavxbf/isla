package com.pao.isla

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Color as AndroidColor
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
            ACTION_STOP -> { removeIsland(); stopSelf() }
            ACTION_UPDATE -> { removeIsland(); if (Settings.canDrawOverlays(this)) showIsland() }
            else -> if (Settings.canDrawOverlays(this)) showIsland()
        }
        return START_NOT_STICKY
    }

    private fun showIsland() {
        if (island != null) return
        val c = IslandConfig.load(this)
        val view = TextView(this).apply {
            text = "●     Isla     ●"
            setTextColor(AndroidColor.WHITE); textSize = 11f; gravity = Gravity.CENTER
            alpha = c.transparency
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(AndroidColor.BLACK); cornerRadius = dp(c.radiusDp).toFloat()
                setStroke(dp(1), c.accentHex.toInt())
            }
            setPadding(dp(12), 0, dp(12), 0); elevation = dp(10).toFloat()
            contentDescription = "Isla Dynamic Island. Tap to expand or collapse."
            setOnClickListener {
                expanded = !expanded
                text = if (expanded) "●   Isla is active   •   Tap to close" else "●     Isla     ●"
                (layoutParams as? WindowManager.LayoutParams)?.let { p ->
                    p.width = dp(if (expanded) c.widthDp.coerceAtLeast(240) else c.widthDp)
                    p.height = dp(if (expanded) (c.heightDp + 24).coerceAtMost(90) else c.heightDp)
                    try { wm.updateViewLayout(this, p) } catch (_: Exception) { }
                }
            }
        }
        val p = WindowManager.LayoutParams(
            dp(c.widthDp), dp(c.heightDp),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = dp(8) }
        try { wm.addView(view, p); island = view }
        catch (_: SecurityException) { island = null }
        catch (_: WindowManager.BadTokenException) { island = null }
    }

    private fun removeIsland() {
        island?.let { try { wm.removeView(it) } catch (_: Exception) { } }
        island = null
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    override fun onDestroy() { removeIsland(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.pao.isla.START"
        const val ACTION_STOP = "com.pao.isla.STOP"
        const val ACTION_UPDATE = "com.pao.isla.UPDATE"
    }
}
