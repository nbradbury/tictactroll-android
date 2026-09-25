package com.nbradbury.tic_tac_troll.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sign
import kotlin.random.Random

enum class Screen { MENU, GAME }

enum class Mode { PVP, CPU }

data class Scores(val a: Int = 0, val b: Int = 0, val draws: Int = 0) {
    fun add(winner: Team?) = when (winner) {
        Team.A -> copy(a = a + 1)
        Team.B -> copy(b = b + 1)
        null -> copy(draws = draws + 1)
    }
}

/** Troll keys: board cells use their index, the two menu trolls use [MENU_A] and [MENU_B]. */
const val MENU_A = 9
const val MENU_B = 10

data class GameState(
    val screen: Screen = Screen.MENU,
    val mode: Mode = Mode.CPU,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val board: Board = EMPTY_BOARD,
    val turn: Team = Team.A,
    val starter: Team = Team.A,
    val result: GameResult? = null,
    val showSheet: Boolean = false,
    val scores: Scores = Scores(),
    /** Horizontal glance direction (-1, 0, 1) per troll key. */
    val gaze: Map<Int, Int> = emptyMap(),
    val bubbles: Map<Int, String> = emptyMap(),
    /** True during the draw sequence, when every troll stares at the player. */
    val stare: Boolean = false,
    val locked: Boolean = false,
) {
    val isCpuTurn: Boolean get() = result == null && mode == Mode.CPU && turn == Team.B

    fun isFallen(team: Team): Boolean = result?.winner.let { it != null && it != team }
}

private data class Actor(val key: Int, val col: Int, val team: Team)

class GameViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    /** Delayed actions tied to the current round, cancelled when a new round starts or on returning to the menu. */
    private val roundJobs = mutableListOf<Job>()

    init {
        repeatEvery(GAZE_MS) { gazeTick() }
        repeatEvery(CHATTER_MS) { chatter() }
    }

    fun setMode(mode: Mode) = _state.update { it.copy(mode = mode) }

    fun setDifficulty(difficulty: Difficulty) = _state.update { it.copy(difficulty = difficulty) }

    fun start() {
        _state.update { it.copy(screen = Screen.GAME, scores = Scores()) }
        newRound(Team.A)
    }

    fun toMenu() {
        clearLater()
        _state.update {
            it.copy(
                screen = Screen.MENU, board = EMPTY_BOARD, result = null, showSheet = false,
                bubbles = emptyMap(), gaze = emptyMap(), stare = false, locked = false,
            )
        }
    }

    fun restart() = newRound(_state.value.starter)

    fun rematch() = newRound(_state.value.starter.other)

    fun play(index: Int) {
        val s = _state.value
        if (s.result != null || s.board[index] != null || s.locked || s.isCpuTurn) return
        place(index)
    }

    private fun place(index: Int) {
        val s = _state.value
        val board = s.board.toMutableList().also { it[index] = s.turn }
        val result = judge(board)
        val next = s.turn.other
        _state.update { it.copy(board = board, turn = next, result = result) }
        if (result != null) {
            endGame(result)
        } else if (s.mode == Mode.CPU && next == Team.B) {
            cpuMove(board)
        }
    }

    private fun cpuMove(board: Board) {
        _state.update { it.copy(locked = true) }
        later(CPU_DELAY_MS) {
            _state.update { it.copy(locked = false) }
            place(cpuPick(board, _state.value.difficulty))
        }
    }

    private fun endGame(result: GameResult) {
        _state.update { it.copy(scores = it.scores.add(result.winner)) }
        if (result.isDraw) {
            _state.update { s -> s.copy(stare = true, gaze = s.board.indices.associateWith { 0 }) }
            later(350) { _state.update { s -> s.copy(bubbles = s.board.indices.associateWith { "meh" }) } }
            later(2600) { _state.update { it.copy(bubbles = emptyMap(), stare = false) } }
        }
        later(if (result.isDraw) 2000 else 1600) { _state.update { it.copy(showSheet = true) } }
    }

    private fun newRound(starter: Team) {
        clearLater()
        _state.update {
            it.copy(
                board = EMPTY_BOARD, turn = starter, starter = starter, result = null, showSheet = false,
                bubbles = emptyMap(), stare = false, locked = false, gaze = emptyMap(),
            )
        }
        if (_state.value.mode == Mode.CPU && starter == Team.B) cpuMove(EMPTY_BOARD)
    }

    private fun actors(): List<Actor> {
        val s = _state.value
        if (s.screen == Screen.MENU) return listOf(Actor(MENU_A, 0, Team.A), Actor(MENU_B, 1, Team.B))
        return s.board.mapIndexedNotNull { i, team -> team?.let { Actor(i, i % 3, it) } }
    }

    /** Makes one or two trolls glance at a neighbor, glance randomly, or look straight ahead. */
    private fun gazeTick() {
        if (_state.value.stare) return
        val actors = actors()
        if (actors.isEmpty()) return
        val gaze = _state.value.gaze.toMutableMap()
        repeat(1 + Random.nextInt(2)) {
            val me = actors.random()
            val others = actors.filter { it.key != me.key }
            val roll = Random.nextFloat()
            gaze[me.key] = when {
                others.isNotEmpty() && roll < 0.6f -> (others.random().col - me.col).sign
                roll < 0.85f -> Random.nextInt(-1, 2)
                else -> 0
            }
        }
        _state.update { it.copy(gaze = gaze) }
    }

    private fun chatter() {
        val s = _state.value
        if (s.stare) return
        val me = actors().filterNot { s.isFallen(it.team) }.randomOrNull() ?: return
        say(me.key, listOf("bleh", "meh").random(), 1800)
    }

    private fun say(key: Int, text: String, ms: Long) {
        _state.update { it.copy(bubbles = it.bubbles + (key to text)) }
        later(ms) { _state.update { it.copy(bubbles = it.bubbles - key) } }
    }

    private fun later(ms: Long, block: () -> Unit) {
        val job = viewModelScope.launch {
            delay(ms)
            block()
        }
        roundJobs += job
        job.invokeOnCompletion { roundJobs -= job }
    }

    private fun clearLater() {
        roundJobs.toList().forEach { it.cancel() }
        roundJobs.clear()
    }

    private fun repeatEvery(ms: Long, block: () -> Unit) {
        viewModelScope.launch {
            while (isActive) {
                delay(ms)
                block()
            }
        }
    }

    private companion object {
        const val GAZE_MS = 750L
        const val CHATTER_MS = 5000L
        const val CPU_DELAY_MS = 650L
    }
}
