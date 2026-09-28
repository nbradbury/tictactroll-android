package com.nbradbury.tictactroll.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.nbradbury.tictactroll.R
import com.nbradbury.tictactroll.game.Difficulty
import com.nbradbury.tictactroll.game.Gaze
import com.nbradbury.tictactroll.game.GameState
import com.nbradbury.tictactroll.game.MENU_A
import com.nbradbury.tictactroll.game.MENU_B
import com.nbradbury.tictactroll.game.Mode
import com.nbradbury.tictactroll.game.Rules
import com.nbradbury.tictactroll.game.Team
import com.nbradbury.tictactroll.ui.theme.Accent
import com.nbradbury.tictactroll.ui.theme.Cream
import com.nbradbury.tictactroll.ui.theme.Eyebrow
import com.nbradbury.tictactroll.ui.theme.Ink
import com.nbradbury.tictactroll.ui.theme.Muted
import com.nbradbury.tictactroll.ui.theme.MutedLabel
import com.nbradbury.tictactroll.ui.theme.OnAccent
import com.nbradbury.tictactroll.ui.theme.Outline
import com.nbradbury.tictactroll.ui.theme.Scrim
import com.nbradbury.tictactroll.ui.theme.TitleShadow
import com.nbradbury.tictactroll.ui.theme.dirt
import com.nbradbury.tictactroll.ui.theme.lilita
import com.nbradbury.tictactroll.ui.theme.mono
import com.nbradbury.tictactroll.ui.theme.sans
import androidx.compose.ui.graphics.Shadow as TextShadow

/** Bubble headroom, troll and name at their design size. */
private val MENU_TROLLS_HEIGHT = 227.dp

@Composable
fun MenuScreen(
    state: GameState,
    onMode: (Mode) -> Unit,
    onDifficulty: (Difficulty) -> Unit,
    onRules: (Rules) -> Unit,
    onStart: () -> Unit,
    soundOn: Boolean,
    onSoundChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        MenuContent(state, onMode, onDifficulty, onRules, onStart)
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
    onRules: (Rules) -> Unit,
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
            style = mono(12, Eyebrow).copy(letterSpacing = 0.2.em, textAlign = TextAlign.Center),
            // Clears the sound toggle in the corner; large text wraps instead of running under it.
            modifier = Modifier.padding(horizontal = 48.dp),
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

        // The trolls take the height left over, up to their design size, so the controls below always fit.
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.padding(top = 30.dp).heightIn(max = MENU_TROLLS_HEIGHT),
            ) {
                MenuTroll(state, Team.A, MENU_A, 0)
                MenuTroll(state, Team.B, MENU_B, 1)
            }
        }

        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SettingsCard(state, onMode, onDifficulty, onRules)
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
    Column(Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Headroom for the speech bubble, then the troll at its 140 x 170 design proportions.
        Spacer(Modifier.weight(30f))
        Box(Modifier.weight(170f).aspectRatio(140f / 170f, matchHeightConstraintsFirst = true)) {
            Troll(
                team = team,
                mood = if (state.excitedTroll == key) Mood.HOP else Mood.IDLE,
                gaze = state.gaze[key] ?: Gaze(),
                index = index,
                shadowHeight = 14.dp,
                modifier = Modifier.fillMaxSize(),
            )
            state.bubbles[key]?.let { SpeechBubble(it, fontSize = 20, lift = 34.dp) }
        }
        BasicText(
            team.displayName(),
            style = sans(13, color = Muted).copy(textAlign = TextAlign.Center),
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

/** All game setup in one panel: who you play, how hard the CPU is, and whether trolls get bored. */
@Composable
private fun SettingsCard(
    state: GameState,
    onMode: (Mode) -> Unit,
    onDifficulty: (Difficulty) -> Unit,
    onRules: (Rules) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .padding(5.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(Mode.PVP to R.string.mode_pvp, Mode.CPU to R.string.mode_cpu).forEach { (mode, label) ->
                SelectableButton(
                    text = stringResource(label),
                    selected = state.mode == mode,
                    onClick = { onMode(mode) },
                    height = 48.dp,
                    weight = FontWeight.Bold,
                    fontSize = 15,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        // Difficulty only matters against the CPU, and sits right under that choice in a quieter style.
        if (state.mode == Mode.CPU) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    Difficulty.EASY to R.string.difficulty_easy,
                    Difficulty.MEDIUM to R.string.difficulty_medium,
                    Difficulty.HARD to R.string.difficulty_hard,
                ).forEach { (difficulty, label) ->
                    SelectableButton(
                        text = stringResource(label),
                        selected = state.difficulty == difficulty,
                        onClick = { onDifficulty(difficulty) },
                        height = 40.dp,
                        weight = FontWeight.Medium,
                        fontSize = 14,
                        prominent = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        BoredTrollsSwitch(
            on = state.rules == Rules.ROLLING,
            onChange = { onRules(if (it) Rules.ROLLING else Rules.CLASSIC) },
        )
    }
}

@Composable
private fun BoredTrollsSwitch(on: Boolean, onChange: (Boolean) -> Unit) {
    val track by animateColorAsState(if (on) Accent else Outline, tween(200))
    val thumbX by animateDpAsState(if (on) 21.dp else 3.dp, tween(200))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .toggleable(value = on, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column(Modifier.weight(1f)) {
            BasicText(stringResource(R.string.rules_rolling), style = sans(15, FontWeight.Bold))
            BasicText(stringResource(R.string.rules_rolling_hint), style = sans(12, FontWeight.Normal, MutedLabel))
        }
        Box(Modifier.size(44.dp, 26.dp).background(track, CircleShape)) {
            Box(
                Modifier
                    .offset(x = thumbX, y = 3.dp)
                    .size(20.dp)
                    .background(Cream, CircleShape),
            )
        }
    }
}

@Composable
private fun SelectableButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    height: Dp,
    weight: FontWeight,
    fontSize: Int,
    modifier: Modifier = Modifier,
    prominent: Boolean = true,
) {
    val shape = RoundedCornerShape(12.dp)
    val highlight = if (prominent) Cream else Color.White.copy(alpha = 0.14f)
    val background by animateColorAsState(if (selected) highlight else Color.Transparent, tween(200))
    val textColor = when {
        selected && prominent -> OnAccent
        selected || prominent -> Ink
        else -> MutedLabel
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(background, shape)
            .clickable(onClick = onClick),
    ) {
        BasicText(
            text,
            style = sans(fontSize, weight, textColor),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = fontSize.sp),
            modifier = Modifier.padding(horizontal = 6.dp),
        )
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
