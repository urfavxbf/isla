package com.pao.isla

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService

/**
 * Notification access is optional and explicitly granted in Android Settings.
 * Only published media sessions are inspected. Other notifications are ignored.
 */
class IslaMusicListener : NotificationListenerService() {
    private var manager: MediaSessionManager? = null
    private var connected = false
    private val handler = Handler(Looper.getMainLooper())
    private val callbacks = mutableMapOf<MediaController, MediaController.Callback>()
    private var sessions: List<MediaController> = emptyList()

    private val sessionsChanged = MediaSessionManager.OnActiveSessionsChangedListener { active ->
        if (connected) setSessions(active ?: emptyList())
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        if (connected) return
        connected = true
        manager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val component = ComponentName(this, IslaMusicListener::class.java)
        try {
            manager?.addOnActiveSessionsChangedListener(sessionsChanged, component, handler)
            refresh()
        } catch (_: SecurityException) {
            clearSessions()
        }
    }

    override fun onListenerDisconnected() {
        shutdown()
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        shutdown()
        super.onDestroy()
    }

    private fun shutdown() {
        if (!connected) return
        connected = false
        manager?.removeOnActiveSessionsChangedListener(sessionsChanged)
        manager = null
        clearSessions()
    }

    private fun refresh() {
        if (!connected) return
        try {
            val component = ComponentName(this, IslaMusicListener::class.java)
            setSessions(manager?.getActiveSessions(component) ?: emptyList())
        } catch (_: SecurityException) {
            clearSessions()
        }
    }

    private fun setSessions(active: List<MediaController>) {
        callbacks.forEach { (controller, callback) ->
            controller.unregisterCallback(callback)
        }
        callbacks.clear()
        sessions = active

        sessions.forEach { controller ->
            val callback = object : MediaController.Callback() {
                override fun onMetadataChanged(metadata: MediaMetadata?) {
                    chooseSession()
                }

                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    chooseSession()
                }

                override fun onSessionDestroyed() {
                    refresh()
                }
            }
            controller.registerCallback(callback, handler)
            callbacks[controller] = callback
        }
        chooseSession()
    }

    private fun chooseSession() {
        val best = sessions.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: sessions.firstOrNull {
            !it.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).isNullOrBlank()
        } ?: sessions.firstOrNull()
        MusicState.publish(best)
    }

    private fun clearSessions() {
        callbacks.forEach { (controller, callback) ->
            controller.unregisterCallback(callback)
        }
        callbacks.clear()
        sessions = emptyList()
        MusicState.publish(null)
    }
}
