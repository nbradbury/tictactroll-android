package com.nbradbury.tic_tac_troll.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.nbradbury.tic_tac_troll.R
import com.nbradbury.tic_tac_troll.ui.theme.Accent
import com.nbradbury.tic_tac_troll.ui.theme.AccentShadow
import com.nbradbury.tic_tac_troll.ui.theme.BackgroundBottom
import com.nbradbury.tic_tac_troll.ui.theme.BackgroundTop
import com.nbradbury.tic_tac_troll.ui.theme.OnAccent

/** The mossy radial glow behind every screen: an ellipse 130% wide and 55% tall, centered at the top. */
fun Modifier.backdrop() = drawBehind {
    drawRect(BackgroundBottom)
    val center = Offset(size.width / 2, 0f)
    val rx = size.width * 1.3f
    scale(1f, size.height * 0.55f / rx, pivot = center) {
        drawCircle(
            Brush.radialGradient(0f to BackgroundTop, 0.7f to BackgroundBottom, center = center, radius = rx),
            radius = rx,
            center = center,
        )
    }
}

/** "Tic Tac Troll" with "Troll" in the accent color. */
@Composable
fun TitleText(
    style: TextStyle,
    modifier: Modifier = Modifier,
    separator: String = " ",
    autoSize: TextAutoSize? = null,
) {
    val text = buildAnnotatedString {
        append(stringResource(R.string.title_tic_tac))
        append(separator)
        withStyle(SpanStyle(color = Accent)) { append(stringResource(R.string.title_troll)) }
    }
    BasicText(text, modifier, style, maxLines = if (autoSize != null) 1 else Int.MAX_VALUE, autoSize = autoSize)
}

/** The accent button with a hard bottom shadow that sinks when pressed. */
@Composable
fun ChunkyButton(
    text: String,
    style: TextStyle,
    height: Dp,
    radius: Dp,
    shadow: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(radius)
    val sink = if (pressed) shadow - 2.dp else 0.dp
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .offset(y = sink)
            .dropShadow(shape, Shadow(radius = 0.dp, color = AccentShadow, offset = DpOffset(0.dp, shadow - sink)))
            .background(Accent, shape)
            .clickable(interaction, indication = null, onClick = onClick),
    ) {
        BasicText(text, style = style.copy(color = OnAccent))
    }
}
