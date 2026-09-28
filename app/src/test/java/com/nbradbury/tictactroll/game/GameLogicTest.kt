package com.nbradbury.tictactroll.game

import com.nbradbury.tictactroll.game.Team.A
import com.nbradbury.tictactroll.game.Team.B
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
        // A: 0, 1, 8. B: 3, 4, 6. A's fourth at 5 doesn't win, so 0 leaves.
        val before = rolling(0, 3, 1, 4, 8, 6)
        assertEquals(0, before.leavingNext(A, Rules.ROLLING))
        val after = before.play(5, A, Rules.ROLLING)
        assertEquals(board(".A.BBAB.A"), after.board)
        assertEquals(listOf(3, 1, 4, 8, 6, 5), after.history)
        assertNull(judge(after.board))
    }

    @Test
    fun `rolling line completed with the leaving troll wins and it stays`() {
        // A's fourth at 2 completes 0-1-2 with the oldest troll at 0: the win comes first.
        val after = rolling(0, 3, 1, 4, 8, 6).play(2, A, Rules.ROLLING)
        assertEquals(A, after.board[0])
        assertEquals(GameResult(A, listOf(0, 1, 2)), judge(after.board))
        assertEquals(2, winningMove(rolling(0, 3, 1, 4, 8, 6), A, Rules.ROLLING))
    }

    @Test
    fun `classic never makes a troll leave`() {
        assertNull(rolling(0, 3, 1, 4, 8, 6).leavingNext(A, Rules.CLASSIC))
    }

    @Test
    fun `cpu takes a rolling win that uses its own oldest troll`() {
        // B: 3 (oldest), 4, 8. B at 5 completes 3-4-5 with its oldest troll, which beats blocking A at 2.
        val pos = position("AA.BB.A.B", 3, 0, 4, 1, 8, 6)
        for (difficulty in listOf(Difficulty.MEDIUM, Difficulty.HARD)) {
            assertEquals(5, cpuPick(pos, difficulty, focused, rules = Rules.ROLLING))
        }
    }

    @Test
    fun `cpu blocks a rolling threat that uses the opponent's oldest troll`() {
        // A: 3 (oldest), 4, 8. A at 5 would complete 3-4-5 with its oldest troll, so B must block there.
        val pos = position("B..AA.B.A", 3, 0, 4, 6, 8)
        for (difficulty in listOf(Difficulty.MEDIUM, Difficulty.HARD)) {
            assertEquals(5, cpuPick(pos, difficulty, focused, rules = Rules.ROLLING))
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
