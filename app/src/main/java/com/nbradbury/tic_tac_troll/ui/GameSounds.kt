package com.nbradbury.tic_tac_troll.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nbradbury.tic_tac_troll.R
import com.nbradbury.tic_tac_troll.game.GameState
import com.nbradbury.tic_tac_troll.game.MENU_A
import com.nbradbury.tic_tac_troll.game.MENU_B
import com.nbradbury.tic_tac_troll.game.Mode
import com.nbradbury.tic_tac_troll.game.Team
import kotlin.random.Random

/**
 * Plays a thunk as each troll lands, voices each new speech bubble, and plays a stinger when a game is won, while
 * [enabled]. Against the CPU a Bramble win is a loss for the player, so it gets the sad trombone instead.
 */
@Composable
fun GameSounds(state: GameState, enabled: Boolean) {
    val context = LocalContext.current
    val sounds = remember { SoundPlayer(context) }
    DisposableEffect(sounds) { onDispose { sounds.release() } }
    LaunchedEffect(state.board, state.bubbles, state.result) { sounds.onState(state, enabled) }
}

/**
 * Plays whatever changed since the last [GameState]. Keeps tracking while muted, and records the first state it sees
 * without playing, so unmuting or recreating the activity doesn't replay anything already on screen.
 */
private class SoundPlayer(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val thunk = pool.load(context, R.raw.thunk, 1)
    private val fanfare = pool.load(context, R.raw.fanfare, 1)
    private val wompWomp = pool.load(context, R.raw.womp_womp, 1)
    private val voices = mapOf(
        (Team.A to "meh") to pool.load(context, R.raw.meh_gorp, 1),
        (Team.A to "bleh") to pool.load(context, R.raw.bleh_gorp, 1),
        (Team.B to "meh") to pool.load(context, R.raw.meh_bramble, 1),
        (Team.B to "bleh") to pool.load(context, R.raw.bleh_bramble, 1),
    )
    private var last: GameState? = null

    fun onState(state: GameState, enabled: Boolean) {
        val previous = last
        last = state
        if (previous == null || !enabled) return

        state.board.forEachIndexed { i, team ->
            if (team != null && previous.board[i] == null) {
                // Gorp lands a little lighter than Bramble.
                play(thunk, if (team == Team.A) 1.12f else 0.88f)
            }
        }

        // Once per team and word, so the draw chorus doesn't stack 9 deep.
        state.bubbles
            .filter { (key, text) -> previous.bubbles[key] != text }
            .mapNotNullTo(mutableSetOf()) { (key, text) -> teamOf(state, key)?.let { it to text } }
            .forEach { voice -> voices[voice]?.let { play(it) } }

        val winner = state.result?.winner
        if (previous.result == null && winner != null) {
            val playerLost = state.mode == Mode.CPU && winner == Team.B
            play(if (playerLost) wompWomp else fanfare, varyPitch = false)
        }
    }

    private fun teamOf(state: GameState, key: Int): Team? = when (key) {
        MENU_A -> Team.A
        MENU_B -> Team.B
        else -> state.board.getOrNull(key)
    }

    /** Plays [sound], by default with a slight random pitch so repeats don't sound canned. */
    private fun play(sound: Int, rate: Float = 1f, varyPitch: Boolean = true) {
        val jitter = if (varyPitch) Random.nextDouble(0.95, 1.05).toFloat() else 1f
        pool.play(sound, 1f, 1f, 1, 0, rate * jitter)
    }

    fun release() = pool.release()
}
