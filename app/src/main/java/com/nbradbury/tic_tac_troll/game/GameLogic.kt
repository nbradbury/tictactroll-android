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

enum class Rules {
    CLASSIC,

    /** Each team keeps at most [ROLLING_LIMIT] trolls; placing another makes its oldest leave. No draws. */
    ROLLING,
}

const val ROLLING_LIMIT = 3

/** Rolling games can go on forever, so the CPU looks this many moves ahead and calls anything deeper even. */
private const val ROLLING_SEARCH_DEPTH = 6

/** A board plus the order its trolls were placed in, oldest first, which [Rules.ROLLING] needs. */
data class Position(val board: Board = EMPTY_BOARD, val history: List<Int> = emptyList()) {
    /** The troll [team] loses when it next places one under [rules], or null. */
    fun leavingNext(team: Team, rules: Rules): Int? =
        if (rules == Rules.ROLLING && board.count { it == team } >= ROLLING_LIMIT) {
            history.first { board[it] == team }
        } else {
            null
        }

    fun play(cell: Int, team: Team, rules: Rules): Position {
        val leaving = leavingNext(team, rules)
        val next = board.toMutableList()
        if (leaving != null) next[leaving] = null
        next[cell] = team
        return Position(next, history.filter { it != leaving } + cell)
    }
}

/** The first empty cell where [team] wins on its next move, allowing for a troll leaving under [rules], or null. */
fun winningMove(position: Position, team: Team, rules: Rules): Int? =
    position.board.indices.firstOrNull {
        position.board[it] == null && judge(position.play(it, team, rules).board)?.winner == team
    }

private fun minimax(position: Position, turn: Team, me: Team, rules: Rules, depth: Int): Int {
    val result = judge(position.board)
    return when {
        result != null -> when (result.winner) {
            me -> WIN_SCORE - depth
            null -> 0
            else -> depth - WIN_SCORE
        }
        rules == Rules.ROLLING && depth >= ROLLING_SEARCH_DEPTH -> 0
        else -> {
            val scores = position.board.indices
                .filter { position.board[it] == null }
                .map { minimax(position.play(it, turn, rules), turn.other, me, rules, depth + 1) }
            if (turn == me) scores.max() else scores.min()
        }
    }
}

/** Picks a move for the CPU, which always plays [me] (Bramble / B in the game). */
fun cpuPick(
    position: Position,
    difficulty: Difficulty,
    random: Random = Random,
    me: Team = Team.B,
    rules: Rules = Rules.CLASSIC,
): Int {
    val board = position.board
    val free = board.indices.filter { board[it] == null }
    return when (difficulty) {
        Difficulty.EASY -> free.random(random)
        Difficulty.MEDIUM -> {
            var move = winningMove(position, me, rules) ?: winningMove(position, me.other, rules)
            if (move == null || random.nextFloat() < MEDIUM_RANDOM_MOVE_CHANCE) {
                move = if (board[CENTER] != null) free.random(random) else CENTER
            }
            move
        }
        Difficulty.HARD -> free.maxBy { minimax(position.play(it, me, rules), me.other, me, rules, 0) }
    }
}
