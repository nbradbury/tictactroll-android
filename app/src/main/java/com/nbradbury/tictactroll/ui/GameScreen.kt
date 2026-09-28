package com.nbradbury.tictactroll.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.nbradbury.tictactroll.R
import com.nbradbury.tictactroll.game.COLUMNS
import com.nbradbury.tictactroll.game.GameState
import com.nbradbury.tictactroll.game.Gaze
import com.nbradbury.tictactroll.game.Mode
import com.nbradbury.tictactroll.game.Team
import com.nbradbury.tictactroll.ui.theme.Cream
import com.nbradbury.tictactroll.ui.theme.DirtBottom
import com.nbradbury.tictactroll.ui.theme.DirtTop
import com.nbradbury.tictactroll.ui.theme.DrawLabel
import com.nbradbury.tictactroll.ui.theme.Grass
import com.nbradbury.tictactroll.ui.theme.Muted
import com.nbradbury.tictactroll.ui.theme.Outline
import com.nbradbury.tictactroll.ui.theme.Scrim
import com.nbradbury.tictactroll.ui.theme.SheetBackground
import com.nbradbury.tictactroll.ui.theme.dirt
import com.nbradbury.tictactroll.ui.theme.lilita
import com.nbradbury.tictactroll.ui.theme.mono
import com.nbradbury.tictactroll.ui.theme.sans
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlinx.coroutines.delay

private const val CELL = 108
private const val STEP = 118 // cell + gap
private const val BOARD = 344
private val TOP_BAR_BUTTON_BAND = 96.dp
private val TOP_SIDES = WindowInsetsSides.Top + WindowInsetsSides.Horizontal
private val PIECE_PADDING = PaddingValues(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 8.dp)
// A bored troll swells like a bubble and vanishes as the burst goes off.
private val BORED_EXIT = scaleOut(tween(POP_SWELL_MS, easing = FastOutLinearInEasing), targetScale = 1.25f) +
    fadeOut(tween(90, delayMillis = POP_SWELL_MS - 40))
private const val POP_SWELL_MS = 140
private const val POP_BURST_MS = 380
private const val POP_DROPLETS = 8
private const val POP_AFTER_LANDING_MS = 250L

@Composable
fun GameScreen(
    state: GameState,
    onCell: (Int) -> Unit,
    onFallenLanded: (Int) -> Unit,
    onMenu: () -> Unit,
    onRestart: () -> Unit,
    onRematch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val result = state.result
    Box(modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                // With the bars hidden, this keeps the top bar clear of the camera cutout.
                .windowInsetsPadding(WindowInsets.safeDrawing.only(TOP_SIDES))
                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        ) {
            TopBar(onMenu, onRestart)
            ScoreChips(state, Modifier.padding(top = 14.dp))
            val status = statusText(state)
            val announcement = listOfNotNull(lastMoveAnnouncement(state), status).joinToString(" ")
            BasicText(
                status,
                style = sans(17).copy(textAlign = TextAlign.Center),
                modifier = Modifier
                    .padding(top = 22.dp)
                    .heightIn(min = 28.dp)
                    // Read aloud whenever it changes, so TalkBack users hear every move, including the CPU's.
                    .semantics {
                        contentDescription = announcement
                        liveRegion = LiveRegionMode.Polite
                    },
            )
            Board(state, onCell, onFallenLanded, Modifier.padding(top = 26.dp))
        }

        AnimatedVisibility(
            visible = state.showSheet && result != null,
            enter = slideInVertically(tween(400, easing = CubicBezierEasing(0.2f, 1.2f, 0.4f, 1f))) { it / 8 } +
                fadeIn(tween(250)),
            exit = ExitTransition.None,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            if (result != null) ResultSheet(result.winner, onMenu, onRematch)
        }
    }
}

@Composable
private fun statusText(state: GameState): String {
    val result = state.result
    return when {
        result?.winner != null -> stringResource(R.string.result_wins, result.winner.displayName())
        result != null -> stringResource(R.string.result_draw)
        state.isCpuTurn -> stringResource(R.string.status_cpu_thinking)
        else -> stringResource(R.string.status_move, state.turn.displayName())
    }
}

/** "Bramble played row 1, column 3." plus, under rolling rules, which troll left. Null before the first move. */
@Composable
private fun lastMoveAnnouncement(state: GameState): String? {
    val last = state.history.lastOrNull()
    val mover = last?.let { state.board[it] } ?: return null
    val played = stringResource(R.string.announce_played, mover.displayName(), last / COLUMNS + 1, last % COLUMNS + 1)
    val left = state.departed?.let {
        stringResource(R.string.announce_left, mover.displayName(), it / COLUMNS + 1, it % COLUMNS + 1)
    }
    return listOfNotNull(played, left).joinToString(" ")
}

@Composable
private fun TopBar(onMenu: () -> Unit, onRestart: () -> Unit) {
    val back = stringResource(R.string.back)
    Box(Modifier.fillMaxWidth()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Scrim)
                .clickable(onClickLabel = back, onClick = onMenu)
                .semantics { contentDescription = back },
        ) {
            BasicText("‹", style = sans(22, FontWeight.Bold), modifier = Modifier.clearAndSetSemantics {})
        }
        // Reserve a band for the buttons on each side so the centered title can't run into them.
        TitleText(
            dirt(21),
            Modifier.align(Alignment.Center).padding(horizontal = TOP_BAR_BUTTON_BAND + 8.dp),
            autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = 21.sp),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(48.dp)
                .widthIn(max = TOP_BAR_BUTTON_BAND)
                .clip(CircleShape)
                .background(Scrim)
                .clickable(onClick = onRestart)
                .padding(horizontal = 14.dp),
        ) {
            BasicText(
                stringResource(R.string.restart),
                style = sans(13),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = 13.sp),
            )
        }
    }
}

@Composable
private fun ScoreChips(state: GameState, modifier: Modifier = Modifier) {
    val playing = state.result == null
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        ScoreChip(Team.A, stringResource(R.string.gorp), state.scores.a, playing && state.turn == Team.A)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight().padding(horizontal = 4.dp),
        ) {
            BasicText(stringResource(R.string.draw_label), style = mono(11, DrawLabel))
            BasicText(state.scores.draws.toString(), style = lilita(20, Muted))
        }
        val nameB = stringResource(if (state.mode == Mode.CPU) R.string.bramble_cpu else R.string.bramble)
        ScoreChip(Team.B, nameB, state.scores.b, playing && state.turn == Team.B)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ScoreChip(
    team: Team,
    name: String,
    score: Int,
    active: Boolean,
) {
    val shape = RoundedCornerShape(16.dp)
    val border by animateColorAsState(if (active) team.color else Color.Transparent, tween(250))
    val mirrored = team == Team.B
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, if (mirrored) Alignment.End else Alignment.Start),
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(Scrim, shape)
            .border(2.dp, border, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        val avatar = @Composable {
            TrollArt(team, Modifier.size(34.dp, 40.dp))
        }
        if (!mirrored) avatar()
        Column(
            horizontalAlignment = if (mirrored) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false),
        ) {
            BasicText(
                name,
                style = sans(12, color = Muted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = 12.sp),
            )
            BasicText(score.toString(), style = lilita(26))
        }
        if (mirrored) avatar()
    }
}

@Composable
private fun Board(
    state: GameState,
    onCell: (Int) -> Unit,
    onFallenLanded: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val result = state.result
    Box(modifier.size(BOARD.dp)) {
        // The dirt strip the losing trolls topple onto.
        Box(
            Modifier
                .offset(y = (BOARD - 6).dp)
                .align(Alignment.TopCenter)
                .requiredWidth((BOARD + 80).dp)
                .height(120.dp)
                .background(Brush.verticalGradient(listOf(DirtTop, DirtBottom)))
                .drawBehind { drawRect(Grass, size = size.copy(height = 3.dp.toPx())) }
        )
        state.board.forEachIndexed { i, team ->
            val row = i / COLUMNS
            val col = i % COLUMNS
            val fallen = team != null && state.isFallen(team)
            val clickable = team == null && state.acceptsMove
            val interaction = remember { MutableInteractionSource() }
            val pressed by interaction.collectIsPressedAsState()
            // Keeps a departing troll's team for its exit animation; a plain holder so writing it doesn't recompose.
            val lastTeam = remember { arrayOfNulls<Team>(1) }
            if (team != null) lastTeam[0] = team
            val description = cellDescription(i, team, leaving = state.leavingNext == i)
            val placeLabel = stringResource(R.string.place_troll, state.turn.displayName())
            val glow by animateColorAsState(
                if (result?.winner != null && i in result.line) result.winner.color else Color.Transparent,
                tween(300),
            )
            Box(
                Modifier
                    .offset((col * STEP).dp, (row * STEP).dp)
                    .size(CELL.dp)
                    .zIndex(if (fallen) 10f + (2 - row) else 1f)
                    .crate(glow)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        enabled = clickable,
                        onClickLabel = placeLabel,
                    ) { onCell(i) }
                    .semantics { contentDescription = description },
            ) {
                AnimatedVisibility(
                    visible = team != null,
                    enter = EnterTransition.None, // The troll pops itself in.
                    // Only a troll that got bored and left sulks off; a new round clears the board instantly.
                    exit = if (state.departed == i) BORED_EXIT else ExitTransition.None,
                    modifier = Modifier.matchParentSize(),
                ) {
                    Box(Modifier.fillMaxSize()) {
                        lastTeam[0]?.let { Piece(state, it, i, fallen, onLanded = { onFallenLanded(i) }) }
                    }
                }
                if (state.departed == i) {
                    // Keyed on the move, so each new departure bursts once.
                    lastTeam[0]?.let { PopBurst(it.color, trigger = state.history) }
                }
                if (team == null && pressed && clickable) {
                    // A ghost of the troll about to land; sliding off the crate cancels the move.
                    TrollArt(state.turn, Modifier.matchParentSize().padding(PIECE_PADDING).alpha(0.35f))
                }
            }
        }
    }
}

/** "Row 1, column 2: Gorp, leaves next", 1-based for TalkBack. */
@Composable
private fun cellDescription(index: Int, team: Team?, leaving: Boolean): String {
    val occupant = when {
        team == null -> stringResource(R.string.cell_empty)
        leaving -> stringResource(R.string.cell_leaving, team.displayName())
        else -> team.displayName()
    }
    return stringResource(R.string.cell_description, index / COLUMNS + 1, index % COLUMNS + 1, occupant)
}

/** The bubble burst where a bored troll was: a ring snapping outward and a spray of [color] droplets, fading. */
@Composable
private fun BoxScope.PopBurst(color: Color, trigger: Any, offset: DpOffset = DpOffset.Zero) {
    val progress = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) {
        delay(POP_SWELL_MS - 30L)
        progress.animateTo(1f, tween(POP_BURST_MS, easing = LinearOutSlowInEasing))
    }
    Canvas(Modifier.matchParentSize()) {
        val p = progress.value
        if (p == 0f || p == 1f) return@Canvas
        val fade = 1f - p
        val center = Offset(size.width / 2 + offset.x.toPx(), size.height * 0.55f + offset.y.toPx())
        val reach = size.minDimension
        drawCircle(
            Cream.copy(alpha = 0.85f * fade),
            radius = reach * (0.26f + 0.28f * p),
            center = center,
            style = Stroke(width = (6.dp.toPx() * fade).coerceAtLeast(1f)),
        )
        repeat(POP_DROPLETS) { k ->
            val angle = 2 * PI * k / POP_DROPLETS + 0.4
            val distance = reach * (0.28f + 0.3f * p)
            drawCircle(
                color.copy(alpha = fade),
                radius = 4.5.dp.toPx() * (1f - 0.5f * p),
                center = center + Offset((cos(angle) * distance).toFloat(), (sin(angle) * distance).toFloat()),
            )
        }
    }
}

/** A troll on the board, which topples off its crate onto the dirt when its team loses, then pops. */
@Composable
private fun BoxScope.Piece(state: GameState, team: Team, index: Int, fallen: Boolean, onLanded: () -> Unit) {
    val row = index / COLUMNS
    val col = index % COLUMNS
    val fall = remember { Animatable(0f) }
    LaunchedEffect(fallen) {
        if (fallen) {
            fall.animateTo(
                1f,
                tween(800, delayMillis = index % 5 * 120 + 250, easing = CubicBezierEasing(0.55f, 0f, 0.75f, 1.2f)),
            )
            delay(POP_AFTER_LANDING_MS)
            onLanded()
        } else {
            fall.animateTo(0f, tween(300))
        }
    }
    val direction = fallDirection(row, col)
    val drop = (2 - row) * STEP + 44
    // Under rolling rules, the troll about to get bored is dimmed as a warning.
    val presence by animateFloatAsState(if (state.leavingNext == index) 0.5f else 1f, tween(250))
    val popped = index in state.popped
    val pop = remember { Animatable(0f) }
    LaunchedEffect(popped) {
        if (popped) pop.animateTo(1f, tween(POP_SWELL_MS, easing = FastOutLinearInEasing)) else pop.snapTo(0f)
    }
    val density = LocalDensity.current.density
    Troll(
        team = team,
        mood = moodOf(state, team, index),
        gaze = state.gaze[index] ?: Gaze(),
        index = index,
        shadowHeight = 10.dp,
        grounded = !fallen,
        flinch = flinchOf(state, index),
        modifier = Modifier
            .matchParentSize()
            .padding(PIECE_PADDING)
            .graphicsLayer {
                // Swells like a bubble, fading out over the last stretch as the burst goes off.
                alpha = presence * (1f - ((pop.value - 0.7f) / 0.3f).coerceIn(0f, 1f))
                scaleX = 1f + 0.25f * pop.value
                scaleY = 1f + 0.25f * pop.value
                translationY = fall.value * drop * density
                rotationZ = fall.value * direction * 90f
                transformOrigin = TransformOrigin(0.5f, 1f)
            },
    )
    if (popped) {
        // Centered on the troll lying in the dirt: half its height to the side of its feet, level with them.
        PopBurst(team.color, trigger = true, offset = DpOffset((direction * 48).dp, (drop + 41).dp))
    }
    state.bubbles[index]?.let { text ->
        // Follows a fallen troll down: sideways toward where its head lies, and down into the dirt.
        SpeechBubble(
            text,
            fontSize = 18,
            lift = 22.dp,
            Modifier.offset {
                IntOffset(
                    (fall.value * direction * 40.dp.toPx()).roundToInt(),
                    (fall.value * (drop + 50).dp.toPx()).roundToInt(),
                )
            },
        )
    }
}

/** Winners hop; during a draw's stare, everyone shrugs; the bored troll yawns. */
private fun moodOf(state: GameState, team: Team, index: Int): Mood {
    val result = state.result
    return when {
        result?.winner == team -> Mood.HOP
        result?.isDraw == true && state.stare -> Mood.SHRUG
        state.yawning == index -> Mood.YAWN
        else -> Mood.IDLE
    }
}

/** Trolls next to the one that just landed startle away from it. */
private fun flinchOf(state: GameState, index: Int): Flinch? {
    val last = state.history.lastOrNull()
    val nearby = last != null && last != index &&
        abs(last / COLUMNS - index / COLUMNS) <= 1 && abs(last % COLUMNS - index % COLUMNS) <= 1
    return if (nearby) Flinch(away = (index % COLUMNS - last!! % COLUMNS).sign, key = state.history) else null
}

/** Which way a losing troll topples: off the nearest side, alternating by row in the middle column. */
private fun fallDirection(row: Int, col: Int): Int = when (col) {
    0 -> -1
    COLUMNS - 1 -> 1
    else -> if (row % 2 == 1) 1 else -1
}

@Composable
private fun ResultSheet(winner: Team?, onMenu: () -> Unit, onRematch: () -> Unit) {
    val shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .dropShadow(
                shape,
                Shadow(radius = 30.dp, color = Color.Black.copy(alpha = 0.4f), offset = DpOffset(0.dp, (-10).dp)),
            )
            .background(SheetBackground, shape)
            // Swallow taps so they don't reach the board underneath.
            .clickable(remember { MutableInteractionSource() }, indication = null) {}
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 20.dp),
    ) {
        BasicText(
            if (winner != null) stringResource(R.string.result_wins, winner.displayName())
            else stringResource(R.string.result_draw),
            style = dirt(34),
        )
        BasicText(
            if (winner != null) stringResource(R.string.result_fallen, winner.other.displayName())
            else stringResource(R.string.result_draw_sub),
            style = sans(15, FontWeight.Normal, Muted),
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val menuShape = RoundedCornerShape(16.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(menuShape)
                    .border(1.5.dp, Outline, menuShape)
                    .clickable(onClick = onMenu),
            ) {
                BasicText(stringResource(R.string.menu), style = sans(15, FontWeight.Bold))
            }
            ChunkyButton(
                text = stringResource(R.string.rematch),
                style = lilita(22),
                height = 54.dp,
                radius = 16.dp,
                shadow = 4.dp,
                onClick = onRematch,
                modifier = Modifier.weight(1.4f),
            )
        }
    }
}
