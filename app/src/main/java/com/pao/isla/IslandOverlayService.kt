package com.pao.isla

import android.animation.ValueAnimator
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView

/**
 * A tiny, tappable music indicator when collapsed; song information and actual
 * media-session transport controls only after an explicit tap.
 *
 * Android system UI may still take precedence for touch input over the status bar.
 * If a phone intercepts the punch-hole tap, adjust the vertical position in Isla.
 */
class IslandOverlayService : Service() {
    private lateinit var wm: WindowManager
    private var island: LinearLayout? = null
    private var expanded = false
    private var sizeAnimator: ValueAnimator? = null

    private val musicObserver: (TrackInfo?) -> Unit = {
        if (island != null) renderIsland(animate = false)
    }

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
                // Settings updates may redraw an active widget, but must not start a stopped one.
                if (island != null && Settings.canDrawOverlays(this)) {
                    val keepExpanded = expanded
                    removeIsland()
                    showIsland()
                    if (keepExpanded) {
                        expanded = true
                        renderIsland(animate = true)
                    }
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

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            elevation = dp(8).toFloat()
            isClickable = true
            contentDescription = "Isla music island. Tap to open music details"
            setOnClickListener {
                expanded = !expanded
                renderIsland(animate = true)
            }
        }

        val params = WindowManager.LayoutParams(
            dp(collapsedWidth(c, MusicState.track.takeIf { c.showMedia })),
            dp(c.heightDp),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(c.positionYDp)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        try {
            wm.addView(root, params)
            island = root
            MusicState.observe(musicObserver)
            renderIsland(animate = false)
        } catch (_: SecurityException) {
            island = null
        } catch (_: WindowManager.BadTokenException) {
            island = null
        }
    }

    private fun renderIsland(animate: Boolean) {
        val root = island ?: return
        val c = IslandConfig.load(this)
        val track = MusicState.track.takeIf { c.showMedia }

        val targetWidth = if (expanded) {
            // The expanded panel is wide enough for readable titles, but not off-screen.
            minOf(dp(304), resources.displayMetrics.widthPixels - dp(24))
        } else {
            dp(collapsedWidth(c, track))
        }
        val targetHeight = dp(if (expanded && track != null) 156
            else if (expanded) 94 else c.heightDp)

        // Keep the expanded card BELOW the status-bar/camera area.
        val statusBarHeight = resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        ).takeIf { it != 0 }?.let { resources.getDimensionPixelSize(it) } ?: dp(30)
        val desiredTop = dp(c.positionYDp) + dp(c.heightDp + 8)
        val targetY = if (expanded) maxOf(statusBarHeight + dp(8), desiredTop)
            else dp(c.positionYDp)

        root.alpha = c.transparency
        root.gravity = if (expanded) Gravity.CENTER_VERTICAL else Gravity.CENTER
        root.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(AndroidColor.BLACK)
            cornerRadius = dp(if (expanded) 25 else c.radiusDp).toFloat()
            setStroke(dp(1).coerceAtLeast(1), c.accentHex.toInt())
        }
        root.setPadding(
            dp(if (expanded) 14 else 2),
            dp(if (expanded) 9 else 0),
            dp(if (expanded) 14 else 2),
            dp(if (expanded) 9 else 0)
        )
        root.removeAllViews()

        if (expanded) {
            renderExpanded(root, track, c)
            root.contentDescription = "Isla expanded music controls"
        } else {
            renderCollapsed(root, track, c)
            root.contentDescription = "Isla. Tap to show song information and controls"
        }

        resizeWindow(targetWidth, targetHeight, targetY, animate)
    }

    /**
     * Never auto-widen a collapsed island when playback starts.
     * A normal 190dp preset becomes a discreet 64dp music indicator.
     * The camera-hole preset keeps the user's exact 28-55dp width.
     */
    private fun collapsedWidth(c: IslandConfig, track: TrackInfo?): Int =
        if (track != null) minOf(c.widthDp, 64) else c.widthDp

    private fun renderCollapsed(root: LinearLayout, track: TrackInfo?, c: IslandConfig) {
        val label = when {
            track != null -> "♫"
            c.isHoleSize -> ""
            c.isCompact -> "●"
            else -> "●     Isla     ●"
        }
        root.addView(text(label, 14f,
            if (track != null) c.accentHex.toInt() else AndroidColor.WHITE
        ).apply {
            gravity = Gravity.CENTER
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        })
    }

    private fun renderExpanded(root: LinearLayout, track: TrackInfo?, c: IslandConfig) {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(text(track?.title ?: "No music playing", 15f, AndroidColor.WHITE).apply {
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, dp(43), 1f)
        })
        header.addView(text("×", 23f, AndroidColor.LTGRAY).apply {
            gravity = Gravity.CENTER
            contentDescription = "Collapse island"
            isClickable = true
            setOnClickListener {
                expanded = false
                renderIsland(animate = true)
            }
            layoutParams = LinearLayout.LayoutParams(dp(36), dp(43))
        })
        root.addView(header, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        val artist = track?.artist?.takeIf { it.isNotBlank() }
            ?: if (track != null) "Unknown artist" else "Open a music app to start"
        root.addView(text(artist, 12f, AndroidColor.LTGRAY).apply {
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(19)
            )
        })

        if (track != null) {
            val source = try {
                val label = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(track.packageName, 0)
                ).toString()
                "Playing from " + label
            } catch (_: Exception) {
                "Playing from " + track.packageName
            }
            root.addView(text(source, 10f, c.accentHex.toInt()).apply {
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(19)
                )
            })
            val controls = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            controls.addView(control("⏮", "Previous track", track.canPrevious, c.accentHex.toInt()) {
                MusicState.previous()
            })
            controls.addView(control(
                if (track.isPlaying) "Ⅱ" else "▶",
                if (track.isPlaying) "Pause music" else "Play music",
                true,
                c.accentHex.toInt()
            ) {
                MusicState.playPause()
            })
            controls.addView(control("⏭", "Next track", track.canNext, c.accentHex.toInt()) {
                MusicState.next()
            })
            root.addView(controls, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(45)
            ))
        } else {
            root.addView(text("Tap × to minimize", 12f, c.accentHex.toInt()).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(20)
                )
            })
        }
    }

    private fun control(
        label: String,
        description: String,
        enabled: Boolean,
        accent: Int,
        onTap: () -> Unit
    ): TextView = text(label, 24f, if (enabled) accent else AndroidColor.DKGRAY).apply {
        gravity = Gravity.CENTER
        isEnabled = enabled
        isClickable = enabled
        contentDescription = description
        layoutParams = LinearLayout.LayoutParams(0, dp(44), 1f)
        if (enabled) setOnClickListener { onTap() }
    }

    private fun text(value: String, size: Float, color: Int): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            includeFontPadding = false
        }

    private fun resizeWindow(width: Int, height: Int, top: Int, animate: Boolean) {
        val root = island ?: return
        val current = root.layoutParams as? WindowManager.LayoutParams ?: return
        sizeAnimator?.cancel()
        sizeAnimator = null

        val startWidth = current.width
        val startHeight = current.height
        val startTop = current.y
        if (!animate || (startWidth == width && startHeight == height && startTop == top)) {
            updateWindow(root, current, width, height, top)
            return
        }

        sizeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 220L
            interpolator = DecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                if (island !== root) return@addUpdateListener
                val progress = valueAnimator.animatedValue as Float
                updateWindow(
                    root, current,
                    startWidth + ((width - startWidth) * progress).toInt(),
                    startHeight + ((height - startHeight) * progress).toInt(),
                    startTop + ((top - startTop) * progress).toInt()
                )
            }
            start()
        }
    }

    private fun updateWindow(
        root: LinearLayout,
        params: WindowManager.LayoutParams,
        width: Int,
        height: Int,
        top: Int
    ) {
        if (root.layoutParams !is WindowManager.LayoutParams) return
        if (params.width == width && params.height == height && params.y == top) return
        params.width = width
        params.height = height
        params.y = top
        try {
            wm.updateViewLayout(root, params)
        } catch (_: IllegalArgumentException) {
            // Overlay may have been detached by Android.
        }
    }

    private fun removeIsland() {
        sizeAnimator?.cancel()
        sizeAnimator = null
        MusicState.removeObserver(musicObserver)
        island?.let {
            try {
                wm.removeView(it)
            } catch (_: IllegalArgumentException) {
                // Already removed.
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
