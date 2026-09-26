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

    private fun position(board: String, vararg history: Int) = Position(board(board), history.toList())

    /** Plays [moves] from an empty board under rolling rules, A first. */
    private fun rolling(vararg moves: Int): Position =
        moves.foldIndexed(Position()) { i, pos, cell -> pos.play(cell, if (i % 2 == 0) A else B, Rules.ROLLING) }

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
        assertEquals(5, cpuPick(Position(board("AA.BB....")), Difficulty.MEDIUM, focused))
    }

    @Test
    fun `medium blocks an immediate threat`() {
        assertEquals(2, cpuPick(Position(board("AA..B....")), Difficulty.MEDIUM, focused))
    }

    @Test
    fun `rolling fourth troll makes the oldest leave`() {
        // A: 0, 1, 8. B: 3, 4, 6. A's fourth at 2 would complete 0-1-2, but 0 leaves first.
        val before = rolling(0, 3, 1, 4, 8, 6)
        assertEquals(0, before.leavingNext(A, Rules.ROLLING))
        val after = before.play(2, A, Rules.ROLLING)
        assertEquals(board(".AABB.B.A"), after.board)
        assertEquals(listOf(3, 1, 4, 8, 6, 2), after.history)
        assertNull(judge(after.board))
    }

    @Test
    fun `rolling win detection allows for the leaving troll`() {
        val pos = rolling(0, 3, 1, 4, 8, 6)
        assertEquals(2, winningMove(pos, A, Rules.CLASSIC))
        assertNull(winningMove(pos, A, Rules.ROLLING))
    }

    @Test
    fun `classic never makes a troll leave`() {
        assertNull(rolling(0, 3, 1, 4, 8, 6).leavingNext(A, Rules.CLASSIC))
    }

    @Test
    fun `cpu takes a rolling win that needs its oldest to leave`() {
        // B: 6 (oldest), 3, 4. Playing 5 makes 6 leave and completes 3-4-5; it also blocks A's 2-5-8.
        val pos = rolling(0, 6, 1, 3, 8, 4, 2)
        for (difficulty in listOf(Difficulty.MEDIUM, Difficulty.HARD)) {
            assertEquals(5, cpuPick(pos, difficulty, focused, rules = Rules.ROLLING))
        }
    }

    @Test
    fun `cpu blocks the real rolling threat, not the one that needs a leaving troll`() {
        // A: 3 (oldest), 4, 7. A at 5 would lose 3 first, so only 1-4-7 is a real threat.
        val pos = position("B..AA.BA.", 3, 0, 4, 6, 7)
        for (difficulty in listOf(Difficulty.MEDIUM, Difficulty.HARD)) {
            assertEquals(1, cpuPick(pos, difficulty, focused, rules = Rules.ROLLING))
        }
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
            val move = cpuPick(Position(board), Difficulty.HARD)
            playAll(board.toMutableList().also { it[move] = B }, A)
        } else {
            for (i in board.indices.filter { board[it] == null }) {
                playAll(board.toMutableList().also { it[i] = A }, B)
            }
        }
    }
}
