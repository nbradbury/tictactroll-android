package com.nbradbury.tic_tac_troll.game

import com.nbradbury.tic_tac_troll.game.Team.A
import com.nbradbury.tic_tac_troll.game.Team.B
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class GameLogicTest {
    /** Never triggers medium's 20% "random move" roll. */
    private val focused = object : Random() {
        override fun nextBits(bitCount: Int) = (1 shl bitCount) - 1
    }

    private fun board(s: String): Board = s.map { if (it == 'A') A else if (it == 'B') B else null }

    @Test
    fun `judge finds every line`() {
        for (line in LINES) {
            val b = List(CELLS) { if (it in line) B else null }
            assertEquals(GameResult(B, line), judge(b))
        }
    }

    @Test
    fun `judge reports draw on full board without a line`() {
        assertEquals(GameResult(null, emptyList()), judge(board("ABAABBBAA")))
    }

    @Test
    fun `judge returns null for game in progress`() {
        assertNull(judge(board("AB.......")))
    }

    @Test
    fun `medium takes the win over blocking`() {
        // B can win at 5, A threatens at 2.
        assertEquals(5, cpuPick(board("AA.BB...."), Difficulty.MEDIUM, focused))
    }

    @Test
    fun `medium blocks an immediate threat`() {
        assertEquals(2, cpuPick(board("AA..B...."), Difficulty.MEDIUM, focused))
    }

    @Test
    fun `hard never loses`() {
        playAll(EMPTY_BOARD, A)
        playAll(EMPTY_BOARD, B)
    }

    /** Explores every human (A) move sequence against the hard CPU (B). */
    private fun playAll(board: Board, turn: Team) {
        val result = judge(board)
        if (result != null) {
            assertNotEquals("CPU lost on $board", A, result.winner)
            return
        }
        if (turn == B) {
            val move = cpuPick(board, Difficulty.HARD)
            playAll(board.toMutableList().also { it[move] = B }, A)
        } else {
            for (i in board.indices.filter { board[it] == null }) {
                playAll(board.toMutableList().also { it[i] = A }, B)
            }
        }
    }
}
