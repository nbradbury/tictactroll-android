package com.nbradbury.tic_tac_troll.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.nbradbury.tic_tac_troll.game.GameState
import com.nbradbury.tic_tac_troll.game.Mode
import com.nbradbury.tic_tac_troll.game.Team
import kotlinx.coroutines.delay

/**
 * A light tick as each troll lands, then a confirm for a win or a reject for losing to the CPU, timed with the ending
 * sound. Independent of the sound toggle; the system haptics setting still applies.
 */
@Composable
fun GameHaptics(state: GameState) {
    val haptics = LocalHapticFeedback.current
    val last = remember { LastState() }
    LaunchedEffect(state.board, state.result) {
        val previous = last.state
        last.state = state
        // The first state seen is recorded silently, so recreating the activity doesn't buzz.
        if (previous == null) return@LaunchedEffect
        if (state.board.indices.any { state.board[it] != null && previous.board[it] == null }) {
            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
        }
        val winner = state.result?.winner
        if (previous.result == null && winner != null) {
            delay(ENDING_LEAD_IN_MS)
            val playerLost = state.mode == Mode.CPU && winner == Team.B
            haptics.performHapticFeedback(if (playerLost) HapticFeedbackType.Reject else HapticFeedbackType.Confirm)
        }
    }
}

private class LastState {
    var state: GameState? = null
}

/** Matches the silent lead-in on the win and loss sounds. */
private const val ENDING_LEAD_IN_MS = 250L
