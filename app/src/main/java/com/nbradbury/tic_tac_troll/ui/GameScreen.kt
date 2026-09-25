package com.nbradbury.tic_tac_troll.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.nbradbury.tic_tac_troll.R
import com.nbradbury.tic_tac_troll.game.COLUMNS
import com.nbradbury.tic_tac_troll.game.GameState
import com.nbradbury.tic_tac_troll.game.Mode
import com.nbradbury.tic_tac_troll.game.Team
import com.nbradbury.tic_tac_troll.ui.theme.DirtBottom
import com.nbradbury.tic_tac_troll.ui.theme.DirtTop
import com.nbradbury.tic_tac_troll.ui.theme.DrawLabel
import com.nbradbury.tic_tac_troll.ui.theme.Grass
import com.nbradbury.tic_tac_troll.ui.theme.Muted
import com.nbradbury.tic_tac_troll.ui.theme.Outline
import com.nbradbury.tic_tac_troll.ui.theme.Scrim
import com.nbradbury.tic_tac_troll.ui.theme.SheetBackground
import com.nbradbury.tic_tac_troll.ui.theme.dirt
import com.nbradbury.tic_tac_troll.ui.theme.lilita
import com.nbradbury.tic_tac_troll.ui.theme.mono
import com.nbradbury.tic_tac_troll.ui.theme.sans
import kotlin.math.roundToInt

private const val CELL = 108
private const val STEP = 118 // cell + gap
private const val BOARD = 344
private val TOP_BAR_BUTTON_BAND = 96.dp
private val PIECE_PADDING = PaddingValues(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 8.dp)

@Composable
fun GameScreen(
    state: GameState,
    onCell: (Int) -> Unit,
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
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        ) {
            TopBar(onMenu, onRestart)
            ScoreChips(state, Modifier.padding(top = 14.dp))
            BasicText(
                statusText(state),
                style = sans(17).copy(textAlign = TextAlign.Center),
                modifier = Modifier.padding(top = 22.dp).heightIn(min = 28.dp),
            )
            Board(state, onCell, Modifier.padding(top = 26.dp))
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

@Composable
private fun TopBar(onMenu: () -> Unit, onRestart: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Scrim)
                .clickable(onClickLabel = stringResource(R.string.back), onClick = onMenu),
        ) {
            BasicText("‹", style = sans(22, FontWeight.Bold))
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
            Image(
                painterResource(team.image),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomCenter,
                modifier = Modifier.size(34.dp, 40.dp),
            )
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
private fun Board(state: GameState, onCell: (Int) -> Unit, modifier: Modifier = Modifier) {
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
                    .clickable(interactionSource = interaction, indication = null, enabled = clickable) { onCell(i) },
            ) {
                if (team != null) {
                    Piece(state, team, i, fallen)
                } else if (pressed && clickable) {
                    // A ghost of the troll about to land; sliding off the crate cancels the move.
                    Image(
                        painterResource(state.turn.image),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.BottomCenter,
                        modifier = Modifier.matchParentSize().padding(PIECE_PADDING).alpha(0.35f),
                    )
                }
            }
        }
    }
}

/** A troll on the board, which topples off its crate onto the dirt when its team loses. */
@Composable
private fun BoxScope.Piece(state: GameState, team: Team, index: Int, fallen: Boolean) {
    val row = index / COLUMNS
    val col = index % COLUMNS
    val result = state.result
    val mood = when {
        result?.winner == team -> Mood.HOP
        result?.isDraw == true && state.stare -> Mood.SHRUG
        else -> Mood.IDLE
    }
    val fall = remember { Animatable(0f) }
    LaunchedEffect(fallen) {
        if (fallen) {
            fall.animateTo(
                1f,
                tween(800, delayMillis = index % 5 * 120 + 250, easing = CubicBezierEasing(0.55f, 0f, 0.75f, 1.2f)),
            )
        } else {
            fall.animateTo(0f, tween(300))
        }
    }
    val direction = when (col) {
        0 -> -1
        2 -> 1
        else -> if (row % 2 == 1) 1 else -1
    }
    val drop = (2 - row) * STEP + 44
    val density = LocalDensity.current.density
    Troll(
        team = team,
        mood = mood,
        gaze = state.gaze[index] ?: 0,
        index = index,
        shadowHeight = 10.dp,
        grounded = !fallen,
        modifier = Modifier
            .matchParentSize()
            .padding(PIECE_PADDING)
            .graphicsLayer {
                translationY = fall.value * drop * density
                rotationZ = fall.value * direction * 90f
                transformOrigin = TransformOrigin(0.5f, 1f)
            },
    )
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
            .navigationBarsPadding()
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
