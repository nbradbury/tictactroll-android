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
import com.nbradbury.tic_tac_troll.game.Team
import kotlin.random.Random

/** Voices each new speech bubble in its troll's voice. */
@Composable
fun TrollVoices(state: GameState) {
    val context = LocalContext.current
    val voices = remember { VoicePlayer(context) }
    DisposableEffect(voices) { onDispose { voices.release() } }
    LaunchedEffect(state.bubbles) {
        voices.onBubbles(state.bubbles) { key ->
            when (key) {
                MENU_A -> Team.A
                MENU_B -> Team.B
                else -> state.board.getOrNull(key)
            }
        }
    }
}

private class VoicePlayer(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        .build()
    private val sounds = mapOf(
        (Team.A to "meh") to pool.load(context, R.raw.meh_gorp, 1),
        (Team.A to "bleh") to pool.load(context, R.raw.bleh_gorp, 1),
        (Team.B to "meh") to pool.load(context, R.raw.meh_bramble, 1),
        (Team.B to "bleh") to pool.load(context, R.raw.bleh_bramble, 1),
    )
    private var heard = emptyMap<Int, String>()

    /** Plays bubbles that weren't showing last time, once per team and word so the draw chorus doesn't stack 9 deep. */
    fun onBubbles(bubbles: Map<Int, String>, teamOf: (Int) -> Team?) {
        bubbles
            .filter { (key, text) -> heard[key] != text }
            .mapNotNullTo(mutableSetOf()) { (key, text) -> teamOf(key)?.let { it to text } }
            .forEach { voice ->
                // A slight random pitch so repeated grumbles don't sound canned.
                sounds[voice]?.let { pool.play(it, 1f, 1f, 1, 0, Random.nextDouble(0.94, 1.06).toFloat()) }
            }
        heard = bubbles
    }

    fun release() = pool.release()
}
