package com.nbradbury.tictactroll.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect

/** Loops [res] while the UI is started and [enabled], pausing otherwise. */
@Composable
fun BackgroundMusic(@RawRes res: Int, enabled: Boolean, volume: Float = 0.6f) {
    val context = LocalContext.current
    val player = remember(res) {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        MediaPlayer.create(context, res, attributes, 0).apply {
            isLooping = true
            setVolume(volume, volume)
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    LifecycleStartEffect(player, enabled) {
        if (enabled) player.start()
        // pause() on a never-started player is an illegal state and breaks later start() calls.
        onStopOrDispose { if (player.isPlaying) player.pause() }
    }
}
