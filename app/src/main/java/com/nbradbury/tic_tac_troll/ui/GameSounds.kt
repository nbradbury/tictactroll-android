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
import com.nbradbury.tic_tac_troll.game.Board
import com.nbradbury.tic_tac_troll.game.GameState
import com.nbradbury.tic_tac_troll.game.MENU_A
import com.nbradbury.tic_tac_troll.game.MENU_B
import com.nbradbury.tic_tac_troll.game.Team
import kotlin.random.Random

/** Plays a thunk as each troll lands on the board and voices each new speech bubble, while [enabled]. */
@Composable
fun GameSounds(state: GameState, enabled: Boolean) {
    val context = LocalContext.current
    val sounds = remember { SoundPlayer(context) }
    DisposableEffect(sounds) { onDispose { sounds.release() } }
    LaunchedEffect(state.board) { sounds.onBoard(state.board, enabled) }
    LaunchedEffect(state.bubbles) {
        sounds.onBubbles(state.bubbles, enabled) { key ->
            when (key) {
                MENU_A -> Team.A
                MENU_B -> Team.B
                else -> state.board.getOrNull(key)
            }
        }
    }
}

/** Tracks what's already been heard even while muted, so unmuting doesn't replay anything already on screen. */
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
    private val voices = mapOf(
        (Team.A to "meh") to pool.load(context, R.raw.meh_gorp, 1),
        (Team.A to "bleh") to pool.load(context, R.raw.bleh_gorp, 1),
        (Team.B to "meh") to pool.load(context, R.raw.meh_bramble, 1),
        (Team.B to "bleh") to pool.load(context, R.raw.bleh_bramble, 1),
    )
    private var board: Board = emptyList()
    private var heard = emptyMap<Int, String>()

    fun onBoard(newBoard: Board, enabled: Boolean) {
        if (enabled) {
            newBoard.forEachIndexed { i, team ->
                if (team != null && board.getOrNull(i) == null) {
                    // Gorp lands a little lighter than Bramble.
                    play(thunk, if (team == Team.A) 1.12f else 0.88f)
                }
            }
        }
        board = newBoard
    }

    /** Plays bubbles that weren't showing last time, once per team and word so the draw chorus doesn't stack 9 deep. */
    fun onBubbles(bubbles: Map<Int, String>, enabled: Boolean, teamOf: (Int) -> Team?) {
        if (enabled) {
            bubbles
                .filter { (key, text) -> heard[key] != text }
                .mapNotNullTo(mutableSetOf()) { (key, text) -> teamOf(key)?.let { it to text } }
                .forEach { voice -> voices[voice]?.let { play(it) } }
        }
        heard = bubbles
    }

    /** Plays [sound] with a slight random pitch so repeats don't sound canned. */
    private fun play(sound: Int, rate: Float = 1f) {
        pool.play(sound, 1f, 1f, 1, 0, rate * Random.nextDouble(0.95, 1.05).toFloat())
    }

    fun release() = pool.release()
}
