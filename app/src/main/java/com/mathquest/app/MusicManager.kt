package com.mathquest.app

import android.content.Context
import android.media.MediaPlayer

/**
 * Singleton that owns a single looping MediaPlayer for background music.
 *
 * Usage:
 *   MusicManager.play(context, R.raw.starting, musicVolume)  // start / switch track
 *   MusicManager.setVolume(musicVolume)                      // live volume update
 *   MusicManager.pause()  / resume()                        // app lifecycle
 *   MusicManager.stop()                                      // clean up
 *
 * Calling play() with the same track that is already playing is a no-op except
 * for updating the volume — the track is never restarted mid-play.
 */
object MusicManager {

    private var player: MediaPlayer? = null
    private var currentResId: Int = -1

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Start playing [resId] looped at [volume].
     * If [resId] is already playing, only the volume is updated.
     */
    fun play(context: Context, resId: Int, volume: Float) {
        if (currentResId == resId && player?.isPlaying == true) {
            setVolume(volume)
            return
        }
        release()
        currentResId = resId
        player = MediaPlayer.create(context.applicationContext, resId)?.apply {
            isLooping = true
            setVolume(volume, volume)
            start()
        }
    }

    /** Update the volume of the currently playing track (both channels). */
    fun setVolume(volume: Float) {
        player?.setVolume(volume, volume)
    }

    /**
     * Pause playback (e.g. when the app goes to the background).
     * Call [resume] to continue from the same position.
     */
    fun pause() {
        if (player?.isPlaying == true) player?.pause()
    }

    /** Resume after [pause]. */
    fun resume() {
        val p = player ?: return
        if (!p.isPlaying) p.start()
    }

    /**
     * Stop and release the MediaPlayer completely.
     * The next call to [play] will create a fresh player.
     */
    fun stop() {
        release()
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private fun release() {
        try {
            player?.stop()
            player?.release()
        } catch (_: Exception) { /* already released — ignore */ }
        player = null
        currentResId = -1
    }
}
