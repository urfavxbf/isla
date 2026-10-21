package com.pao.isla

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper

/**
 * In-process bridge between the user-enabled notification listener and the island.
 * Media metadata is kept only in memory and is not uploaded or saved.
 */
data class TrackInfo(
    val title: String,
    val artist: String,
    val packageName: String,
    val isPlaying: Boolean,
    val canPrevious: Boolean,
    val canNext: Boolean
)

object MusicState {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val observers = mutableSetOf<(TrackInfo?) -> Unit>()

    private var selected: MediaController? = null
    var track: TrackInfo? = null
        private set

    fun observe(observer: (TrackInfo?) -> Unit) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { observe(observer) }
            return
        }
        observers.add(observer)
        observer(track)
    }

    fun removeObserver(observer: (TrackInfo?) -> Unit) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { removeObserver(observer) }
            return
        }
        observers.remove(observer)
    }

    fun publish(controller: MediaController?) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { publish(controller) }
            return
        }

        selected = controller
        val state = controller?.playbackState
        val metadata = controller?.metadata
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?.takeIf { it.isNotBlank() }
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                ?.takeIf { it.isNotBlank() }
            ?: "Media player"
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?.takeIf { it.isNotBlank() }
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE).orEmpty()

        track = if (controller == null) null else TrackInfo(
            title = title,
            artist = artist,
            packageName = controller.packageName,
            isPlaying = state?.state == PlaybackState.STATE_PLAYING,
            canPrevious = state == null || (state.actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0L,
            canNext = state == null || (state.actions and PlaybackState.ACTION_SKIP_TO_NEXT) != 0L
        )
        observers.toList().forEach { it(track) }
    }

    fun playPause() {
        val controller = selected ?: return
        val state = controller.playbackState
        if (state?.state == PlaybackState.STATE_PLAYING) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }
    }

    fun next() {
        selected?.transportControls?.skipToNext()
    }

    fun previous() {
        selected?.transportControls?.skipToPrevious()
    }
}
