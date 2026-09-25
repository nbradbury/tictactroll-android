package com.nbradbury.tic_tac_troll.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.nbradbury.tic_tac_troll.ui.theme.Scrim
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.nbradbury.tic_tac_troll.R
import com.nbradbury.tic_tac_troll.game.Difficulty
import com.nbradbury.tic_tac_troll.game.GameState
import com.nbradbury.tic_tac_troll.game.MENU_A
import com.nbradbury.tic_tac_troll.game.MENU_B
import com.nbradbury.tic_tac_troll.game.Mode
import com.nbradbury.tic_tac_troll.game.Team
import com.nbradbury.tic_tac_troll.ui.theme.Cream
import com.nbradbury.tic_tac_troll.ui.theme.Eyebrow
import com.nbradbury.tic_tac_troll.ui.theme.Ink
import com.nbradbury.tic_tac_troll.ui.theme.Muted
import com.nbradbury.tic_tac_troll.ui.theme.MutedLabel
import com.nbradbury.tic_tac_troll.ui.theme.OnAccent
import com.nbradbury.tic_tac_troll.ui.theme.Outline
import com.nbradbury.tic_tac_troll.ui.theme.TitleShadow
import com.nbradbury.tic_tac_troll.ui.theme.dirt
import com.nbradbury.tic_tac_troll.ui.theme.lilita
import com.nbradbury.tic_tac_troll.ui.theme.mono
import com.nbradbury.tic_tac_troll.ui.theme.sans
import androidx.compose.ui.graphics.Shadow as TextShadow

@Composable
fun MenuScreen(
    state: GameState,
    onMode: (Mode) -> Unit,
    onDifficulty: (Difficulty) -> Unit,
    onStart: () -> Unit,
    soundOn: Boolean,
    onSoundChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        MenuContent(state, onMode, onDifficulty, onStart)
        SoundToggle(
            on = soundOn,
            onChange = onSoundChange,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 12.dp),
        )
    }
}

@Composable
private fun MenuContent(
    state: GameState,
    onMode: (Mode) -> Unit,
    onDifficulty: (Difficulty) -> Unit,
    onStart: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 36.dp, bottom = 28.dp),
    ) {
        BasicText(
            stringResource(R.string.tagline).uppercase(),
            style = mono(12, Eyebrow).copy(letterSpacing = 0.2.em),
        )
        val shadowOffset = with(LocalDensity.current) { 5.dp.toPx() }
        TitleText(
            style = dirt(66).copy(
                lineHeight = 60.sp,
                textAlign = TextAlign.Center,
                shadow = TextShadow(TitleShadow, Offset(0f, shadowOffset), 0f),
            ),
            separator = "\n",
        )
        BasicText(stringResource(R.string.byline), style = sans(14, FontWeight.Normal, Muted))

        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 30.dp).height(200.dp),
        ) {
            MenuTroll(state, Team.A, MENU_A, 0)
            MenuTroll(state, Team.B, MENU_B, 1)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            listOf(Team.A, Team.B).forEach { team ->
                BasicText(
                    team.displayName(),
                    style = sans(13, color = Muted).copy(textAlign = TextAlign.Center),
                    modifier = Modifier.width(140.dp),
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                    .padding(5.dp),
            ) {
                listOf(Mode.PVP to R.string.mode_pvp, Mode.CPU to R.string.mode_cpu).forEach { (mode, label) ->
                    SelectableButton(
                        text = stringResource(label),
                        selected = state.mode == mode,
                        onClick = { onMode(mode) },
                        height = 48.dp,
                        radius = 12.dp,
                        weight = FontWeight.Bold,
                        fontSize = 15,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (state.mode == Mode.CPU) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BasicText(
                        stringResource(R.string.cpu_skill),
                        style = sans(13, color = MutedLabel),
                        modifier = Modifier.width(72.dp),
                    )
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            Difficulty.EASY to R.string.difficulty_easy,
                            Difficulty.MEDIUM to R.string.difficulty_medium,
                            Difficulty.HARD to R.string.difficulty_hard,
                        ).forEach { (difficulty, label) ->
                            SelectableButton(
                                text = stringResource(label),
                                selected = state.difficulty == difficulty,
                                onClick = { onDifficulty(difficulty) },
                                height = 44.dp,
                                radius = 22.dp,
                                weight = FontWeight.Medium,
                                fontSize = 14,
                                outlined = true,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            ChunkyButton(
                text = stringResource(R.string.start),
                style = lilita(24).copy(letterSpacing = 0.02.em),
                height = 60.dp,
                radius = 18.dp,
                shadow = 5.dp,
                onClick = onStart,
            )
        }
    }
}

@Composable
private fun MenuTroll(state: GameState, team: Team, key: Int, index: Int) {
    Box(Modifier.size(140.dp, 170.dp)) {
        Troll(
            team = team,
            mood = Mood.IDLE,
            gaze = state.gaze[key] ?: 0,
            index = index,
            shadowHeight = 14.dp,
            modifier = Modifier.fillMaxSize(),
        )
        state.bubbles[key]?.let { SpeechBubble(it, fontSize = 20, lift = 34.dp) }
    }
}

@Composable
private fun SelectableButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    height: Dp,
    radius: Dp,
    weight: FontWeight,
    fontSize: Int,
    modifier: Modifier = Modifier,
    outlined: Boolean = false,
) {
    val shape = RoundedCornerShape(radius)
    val background by animateColorAsState(if (selected) Cream else Color.Transparent, tween(200))
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(background, shape)
            .then(if (outlined) Modifier.border(1.5.dp, if (selected) Cream else Outline, shape) else Modifier)
            .clickable(onClick = onClick),
    ) {
        BasicText(text, style = sans(fontSize, weight, if (selected) OnAccent else Ink))
    }
}

/** A round speaker button: sound waves when [on], a cross when muted. */
@Composable
private fun SoundToggle(on: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.sound)
    Canvas(
        modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Scrim)
            .toggleable(value = on, role = Role.Switch, onValueChange = onChange)
            .semantics { contentDescription = label }
            .padding(12.dp),
    ) {
        // Drawn on a 24-unit grid.
        val u = size.width / 24
        val speaker = Path().apply {
            moveTo(3 * u, 9 * u)
            lineTo(8 * u, 9 * u)
            lineTo(13 * u, 4 * u)
            lineTo(13 * u, 20 * u)
            lineTo(8 * u, 15 * u)
            lineTo(3 * u, 15 * u)
            close()
        }
        drawPath(speaker, Ink)
        val stroke = Stroke(width = 2 * u, cap = StrokeCap.Round)
        if (on) {
            for (radius in listOf(4f, 8f)) {
                drawArc(
                    Ink,
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset((13 - radius) * u, (12 - radius) * u),
                    size = Size(2 * radius * u, 2 * radius * u),
                    style = stroke,
                )
            }
        } else {
            drawLine(Ink, Offset(16 * u, 9 * u), Offset(22 * u, 15 * u), 2 * u, StrokeCap.Round)
            drawLine(Ink, Offset(22 * u, 9 * u), Offset(16 * u, 15 * u), 2 * u, StrokeCap.Round)
        }
    }
}
