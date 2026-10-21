/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pao.isla.lab.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import com.pao.isla.lab.di.IslaRepositories
import com.pao.isla.lab.model.IslandMode
import com.pao.isla.lab.model.IslandNotification
import com.pao.isla.lab.model.IslandNotificationAction
import com.pao.isla.lab.model.TimerState
import com.pao.isla.lab.receiver.TimerActionReceiver

/**
 * A countdown the island runs itself, shown as the normal Timer card. Its buttons are broadcasts to
 * [TimerActionReceiver].
 */
object InAppTimer {
    const val KEY = "isla.timer"

    const val ACTION_TOGGLE = "com.pao.isla.lab.timer.TOGGLE"
    const val ACTION_ADD_MINUTE = "com.pao.isla.lab.timer.ADD_MINUTE"
    const val ACTION_STOP = "com.pao.isla.lab.timer.STOP"
    const val ACTION_FINISH = "com.pao.isla.lab.timer.FINISH"
    const val ACTION_NOOP = "com.pao.isla.lab.timer.NOOP"

    private const val PREFS = "in_app_timer"
    private const val PREF_STATE = "state"

    private const val FINISHED_LINGER_MS = 60_000L
    private const val RING_MS = 8_000L

    // The labels stay English on purpose: the expanded card tells Pause, Resume, +1 min and Stop apart by these words.
    private const val LABEL_PAUSE = "Pause"
    private const val LABEL_RESUME = "Resume"
    private const val LABEL_ADD_MINUTE = "+1 min"
    private const val LABEL_STOP = "Stop"

    private val main = Handler(Looper.getMainLooper())
    private var ringtone: Ringtone? = null

    @Volatile var state: TimerState = TimerState()
        private set

    /** True while a timer exists (running, paused or finished and not yet stopped): the Timer tile lights up. */
    val isActive: Boolean get() = state.active

    /** Safe to call repeatedly. */
    fun restore(context: Context) {
        val app = context.applicationContext
        val saved = TimerState.decode(prefs(app).getString(PREF_STATE, null))
        if (!saved.active) return
        state = saved
        val now = System.currentTimeMillis()
        if (state.isDue(now)) {
            // Ended while the app was away: ring only if that was just now, otherwise the timer is long over.
            if (now - state.endMs > FINISHED_LINGER_MS) stop(app) else finish(app)
            return
        }
        if (state.finished) {
            // A finished timer that outlived a restart is cleared.
            stop(app)
            return
        }
        post(app)
        schedule(app)
    }

    /** Replaces any timer that exists. */
    fun start(context: Context, seconds: Int) {
        val app = context.applicationContext
        silence()
        state = TimerState().start(System.currentTimeMillis(), seconds * 1000L)
        save(app)
        post(app)
        schedule(app)
    }

    fun toggle(context: Context) = change(context) { it.toggle(System.currentTimeMillis()) }

    fun addMinute(context: Context) = change(context) { it.addMinute(System.currentTimeMillis()) }

    fun stop(context: Context) {
        val app = context.applicationContext
        silence()
        cancelSchedule(app)
        state = TimerState()
        save(app)
        runCatching { IslaRepositories.notificationRepository(app).removeNotification(KEY) }
    }

    fun handle(context: Context, action: String?) {
        when (action) {
            ACTION_TOGGLE -> toggle(context)
            ACTION_ADD_MINUTE -> addMinute(context)
            ACTION_STOP -> stop(context)
            ACTION_FINISH -> if (state.isDue(System.currentTimeMillis())) finish(context.applicationContext)
            else -> Unit
        }
    }

    private fun change(context: Context, transform: (TimerState) -> TimerState) {
        val app = context.applicationContext
        val next = transform(state)
        if (next == state) return
        state = next
        save(app)
        post(app)
        schedule(app)
    }

    private fun finish(app: Context) {
        state = state.finish()
        save(app)
        cancelSchedule(app)
        post(app)
        ring(app)
        main.removeCallbacks(clearRunnable)
        main.postDelayed(clearRunnable, FINISHED_LINGER_MS)
    }

    private val clearRunnable = Runnable { appContext?.let { if (state.finished) stop(it) } }
    private val finishRunnable = Runnable { appContext?.let { if (state.isDue(System.currentTimeMillis())) finish(it) } }
    @Volatile private var appContext: Context? = null

    private fun schedule(app: Context) {
        appContext = app
        main.removeCallbacks(finishRunnable)
        main.removeCallbacks(clearRunnable)
        cancelAlarm(app)
        val s = state
        if (!s.active || !s.running || s.finished) return
        val delay = (s.endMs - System.currentTimeMillis()).coerceAtLeast(0L)
        main.postDelayed(finishRunnable, delay)
        // The backup: wakes the app if the process is gone by then (no special permission; may run a little late in doze).
        runCatching {
            val am = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, s.endMs, broadcast(app, ACTION_FINISH))
        }
    }

    private fun cancelSchedule(app: Context) {
        main.removeCallbacks(finishRunnable)
        main.removeCallbacks(clearRunnable)
        cancelAlarm(app)
    }

    private fun cancelAlarm(app: Context) {
        runCatching { (app.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(broadcast(app, ACTION_FINISH)) }
    }

    private fun ring(app: Context) {
        silence()
        runCatching {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(app, uri)?.also { it.play() }
            main.postDelayed({ silence() }, RING_MS)
        }
        runCatching {
            @Suppress("DEPRECATION")
            val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 250, 400, 250, 400), -1))
        }
    }

    private fun silence() {
        runCatching { ringtone?.stop() }
        ringtone = null
    }

    private fun post(app: Context) {
        val s = state
        if (!s.active) return
        val now = System.currentTimeMillis()
        val actions = buildList {
            if (!s.finished) {
                add(IslandNotificationAction(if (s.running) LABEL_PAUSE else LABEL_RESUME, broadcast(app, ACTION_TOGGLE)))
                add(IslandNotificationAction(LABEL_ADD_MINUTE, broadcast(app, ACTION_ADD_MINUTE)))
            }
            add(IslandNotificationAction(LABEL_STOP, broadcast(app, ACTION_STOP)))
        }
        val remaining = s.remainingAt(now)
        val card = IslandNotification(
            key = KEY,
            packageName = app.packageName,
            appName = "Timer",
            title = "Timer",
            text = when {
                s.finished -> "Time's up"
                s.running -> "Running"
                else -> "Paused"
            },
            // A running timer counts down to its end; a paused or finished one does not move.
            timeMillis = if (s.running) s.endMs else now + remaining,
            actionIntents = actions,
            // A tap on the card does nothing; without this it would open this app.
            contentIntent = broadcast(app, ACTION_NOOP),
            mode = IslandMode.Timer,
            recordingFrozenMs = if (s.running) null else remaining,
            recordingPaused = !s.running && !s.finished
        )
        runCatching { IslaRepositories.notificationRepository(app).postNotification(card, autoExpand = false) }
    }

    private fun broadcast(app: Context, action: String): PendingIntent {
        val intent = Intent(app, TimerActionReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(app, action.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun save(app: Context) {
        prefs(app).edit().putString(PREF_STATE, state.encode()).apply()
    }

    private fun prefs(app: Context) = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
