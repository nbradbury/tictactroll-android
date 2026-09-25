package com.nbradbury.tic_tac_troll.game

import kotlin.random.Random

enum class Team {
    A, B;

    val other: Team get() = if (this == A) B else A
}

enum class Difficulty { EASY, MEDIUM, HARD }

typealias Board = List<Team?>

/** [winner] is null for a draw. */
data class GameResult(val winner: Team?, val line: List<Int>) {
    val isDraw: Boolean get() = winner == null
}

val LINES = listOf(
    listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
    listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
    listOf(0, 4, 8), listOf(2, 4, 6),
)

const val COLUMNS = 3
const val CELLS = COLUMNS * COLUMNS
private const val CENTER = 4
private const val WIN_SCORE = 10
private const val MEDIUM_RANDOM_MOVE_CHANCE = 0.2f

val EMPTY_BOARD: Board = List(CELLS) { null }

fun judge(board: Board): GameResult? {
    for (line in LINES) {
        val (x, y, z) = line
        val t = board[x]
        if (t != null && t == board[y] && t == board[z]) return GameResult(t, line)
    }
    return if (board.all { it != null }) GameResult(null, emptyList()) else null
}

/** Returns the empty cell that completes a line for [who], or -1. */
fun findLine(board: Board, who: Team): Int {
    for (line in LINES) {
        val cells = line.map { board[it] }
        if (cells.count { it == who } == 2 && cells.contains(null)) return line[cells.indexOf(null)]
    }
    return -1
}

private fun minimax(board: MutableList<Team?>, turn: Team, me: Team, depth: Int): Int {
    judge(board)?.let { r ->
        return when (r.winner) {
            me -> WIN_SCORE - depth
            null -> 0
            else -> depth - WIN_SCORE
        }
    }
    var best = if (turn == me) Int.MIN_VALUE else Int.MAX_VALUE
    for (i in board.indices) {
        if (board[i] != null) continue
        board[i] = turn
        val score = minimax(board, turn.other, me, depth + 1)
        board[i] = null
        best = if (turn == me) maxOf(best, score) else minOf(best, score)
    }
    return best
}

/** Picks a move for the CPU, which always plays [me] (Bramble / B in the game). */
fun cpuPick(board: Board, difficulty: Difficulty, random: Random = Random, me: Team = Team.B): Int {
    val free = board.indices.filter { board[it] == null }
    return when (difficulty) {
        Difficulty.EASY -> free.random(random)
        Difficulty.MEDIUM -> {
            var move = findLine(board, me)
            if (move < 0) move = findLine(board, me.other)
            if (move < 0 || random.nextFloat() < MEDIUM_RANDOM_MOVE_CHANCE) {
                move = if (board[CENTER] != null) free.random(random) else CENTER
            }
            move
        }
        Difficulty.HARD -> {
            val b = board.toMutableList()
            free.maxBy { i ->
                b[i] = me
                val score = minimax(b, me.other, me, 0)
                b[i] = null
                score
            }
        }
    }
}
