package com.nbradbury.tic_tac_troll.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.nbradbury.tic_tac_troll.R
import com.nbradbury.tic_tac_troll.game.Team
import com.nbradbury.tic_tac_troll.ui.theme.BubbleCream
import com.nbradbury.tic_tac_troll.ui.theme.BubbleInk
import com.nbradbury.tic_tac_troll.ui.theme.Bramble
import com.nbradbury.tic_tac_troll.ui.theme.Gorp
import com.nbradbury.tic_tac_troll.ui.theme.lilita
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val Team.color: Color get() = if (this == Team.A) Gorp else Bramble

val Team.image: Int get() = if (this == Team.A) R.drawable.troll_blue else R.drawable.troll_red

@Composable
fun Team.displayName(): String = stringResource(if (this == Team.A) R.string.gorp else R.string.bramble)

enum class Mood { IDLE, HOP, SHRUG }

private val Bottom = TransformOrigin(0.5f, 1f)

/**
 * A troll that pops in when first composed, breathes, tilts its head toward [gaze] (-1, 0 or 1), and hops or shrugs
 * per [mood]. [index] staggers the animations so trolls don't move in sync; the shadow fades when not [grounded].
 */
@Composable
fun Troll(
    team: Team,
    mood: Mood,
    gaze: Int,
    index: Int,
    shadowHeight: Dp,
    modifier: Modifier = Modifier,
    grounded: Boolean = true,
) {
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow))
    }

    val bodyY = remember { Animatable(0f) }
    val bodyRotation = remember { Animatable(0f) }
    LaunchedEffect(mood) {
        bodyY.snapTo(0f)
        bodyRotation.snapTo(0f)
        when (mood) {
            Mood.HOP -> {
                delay(index % 3 * 100L)
                while (true) {
                    bodyY.animateTo(0f, keyframes {
                        durationMillis = 600
                        0f at 0 using EaseInOut
                        -12f at 300 using EaseInOut
                    })
                }
            }
            Mood.SHRUG -> repeat(3) {
                launch {
                    bodyY.animateTo(0f, keyframes {
                        durationMillis = 500
                        0f at 0 using EaseInOut
                        -7f at 200 using EaseInOut
                        -4f at 350 using EaseInOut
                    })
                }
                bodyRotation.animateTo(0f, keyframes {
                    durationMillis = 500
                    0f at 0 using EaseInOut
                    -3f at 200 using EaseInOut
                    3f at 350 using EaseInOut
                })
            }
            Mood.IDLE -> Unit
        }
    }

    val head by animateFloatAsState(
        targetValue = gaze.toFloat(),
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
    )
    val breathe by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset((index * 370) % 2000),
        ),
    )
    val shadowAlpha by animateFloatAsState(if (grounded) 0.35f else 0f, tween(200))
    val density = LocalDensity.current.density

    Box(modifier) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.76f)
                .offset(y = shadowHeight / 3)
                .height(shadowHeight)
                .blur(3.dp, BlurredEdgeTreatment.Unbounded)
                .background(Color.Black.copy(alpha = shadowAlpha), RoundedCornerShape(50))
        )
        Image(
            painter = painterResource(team.image),
            contentDescription = team.displayName(),
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val p = pop.value
                    alpha = p.coerceIn(0f, 1f)
                    val scale = 0.2f + 0.8f * p
                    scaleX = scale
                    scaleY = scale * breathe
                    translationY = (bodyY.value + (1f - p) * 30f) * density
                    translationX = head * 3f * density
                    rotationZ = bodyRotation.value + head * 8f
                    transformOrigin = Bottom
                },
        )
    }
}

/** A speech bubble centered horizontally at the top of its parent, [lift] above the top edge. */
@Composable
fun BoxScope.SpeechBubble(text: String, fontSize: Int, lift: Dp) {
    val scale = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)) }
    BasicText(
        text = text,
        style = lilita(fontSize, BubbleInk),
        softWrap = false,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = -lift)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                alpha = ((scale.value - 0.4f) / 0.6f).coerceIn(0f, 1f)
            }
            .dropShadow(
                RoundedCornerShape(50),
                Shadow(radius = 0.dp, color = Color.Black.copy(alpha = 0.25f), offset = DpOffset(0.dp, 3.dp)),
            )
            .background(BubbleCream, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}
