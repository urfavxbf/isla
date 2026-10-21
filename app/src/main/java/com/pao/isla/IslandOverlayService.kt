package com.pao.isla

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
import android.widget.LinearLayout
import android.widget.TextView

/**
 * The small pill remains configurable around circular camera cutouts.
 * Music information is shown only when the user explicitly enables notification access.
 */
class IslandOverlayService : Service() {
    private lateinit var wm: WindowManager
    private var island: LinearLayout? = null
    private var expanded = false

    private val musicObserver: (TrackInfo?) -> Unit = {
        if (island != null) renderIsland()
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
                // Settings changes must not start an overlay the user stopped.
                if (island != null && Settings.canDrawOverlays(this)) {
                    removeIsland()
                    showIsland()
                }
            }
            else -> {
                if (Settings.canDrawOverlays(this)) showIsland()
                else stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun showIsland() {
        if (island != null) return
        expanded = false
        val c = IslandConfig.load(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            elevation = dp(10).toFloat()
            contentDescription = "Isla Dynamic Island, tap to expand"
            isClickable = true
            isFocusable = true
            setOnClickListener { expanded = !expanded; renderIsland() }
        }

        val params = WindowManager.LayoutParams(
            dp(c.widthDp), dp(c.heightDp),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(c.positionYDp)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        try {
            wm.addView(root, params)
            island = root
            MusicState.observe(musicObserver)
            renderIsland()
        } catch (_: SecurityException) {
            island = null
        } catch (_: WindowManager.BadTokenException) {
            island = null
        }
    }

    private fun renderIsland() {
        val root = island ?: return
        val c = IslandConfig.load(this)
        val track = MusicState.track.takeIf { c.showMedia }
        val playing = track?.isPlaying == true
        val autoMusicPill = playing && c.expandOnPlay && !expanded

        val width = when {
            expanded -> c.widthDp.coerceAtLeast(288)
            autoMusicPill -> c.widthDp.coerceAtLeast(160)
            else -> c.widthDp
        }
        val height = when {
            expanded && track != null -> 116
            expanded -> 74
            else -> c.heightDp
        }

        root.alpha = c.transparency
        root.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(AndroidColor.BLACK)
            cornerRadius = dp(if (expanded) 26 else c.radiusDp).toFloat()
            setStroke(dp(1).coerceAtLeast(1), c.accentHex.toInt())
        }
        root.removeAllViews()
        root.setPadding(dp(if (expanded) 12 else 4), 0, dp(if (expanded) 12 else 4), 0)

        if (expanded) {
            renderExpanded(root, track, c)
        } else {
            val label = when {
                autoMusicPill -> "♫  ${track?.title.orEmpty()}"
                c.isHoleSize -> ""
                c.isCompact -> if (playing) "♫" else "●"
                track != null -> "♫  ${track.title}"
                else -> "●     Isla     ●"
            }
            root.addView(text(label, 11f, AndroidColor.WHITE).apply {
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                gravity = Gravity.CENTER
                setPadding(dp(3), 0, dp(3), 0)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            })
        }

        val params = root.layoutParams as? WindowManager.LayoutParams ?: return
        if (params.width != dp(width) || params.height != dp(height)) {
            params.width = dp(width)
            params.height = dp(height)
            try {
                wm.updateViewLayout(root, params)
            } catch (_: IllegalArgumentException) {
                // View detached while processing a session change.
            }
        }
    }

    private fun renderExpanded(root: LinearLayout, track: TrackInfo?, c: IslandConfig) {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        header.addView(text(track?.title ?: "Isla is active", 13f, AndroidColor.WHITE).apply {
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, dp(26), 1f)
            gravity = Gravity.CENTER_VERTICAL
        })
        header.addView(text("×", 21f, AndroidColor.LTGRAY).apply {
            gravity = Gravity.CENTER
            contentDescription = "Close music controls"
            setOnClickListener {
                expanded = false
                renderIsland()
            }
            layoutParams = LinearLayout.LayoutParams(dp(32), dp(30))
        })
        root.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(text(
            track?.artist?.ifBlank { track.packageName } ?: "Tap to minimize",
            10f, AndroidColor.LTGRAY
        ).apply {
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(18)
            )
        })

        if (track == null) {
            root.addView(text("Play a song to show its controls", 11f, c.accentHex.toInt()).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(25)
                )
            })
            return
        }

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
            LinearLayout.LayoutParams.MATCH_PARENT, dp(50)
        ))
    }

    private fun control(
        label: String,
        description: String,
        enabled: Boolean,
        accent: Int,
        click: () -> Unit
    ): TextView = text(label, 23f, if (enabled) accent else AndroidColor.DKGRAY).apply {
        gravity = Gravity.CENTER
        isEnabled = enabled
        isClickable = enabled
        contentDescription = description
        layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f)
        if (enabled) setOnClickListener { click() }
    }

    private fun text(value: String, size: Float, color: Int) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        includeFontPadding = false
    }

    private fun removeIsland() {
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

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

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
