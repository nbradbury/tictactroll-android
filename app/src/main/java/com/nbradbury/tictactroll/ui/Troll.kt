package com.nbradbury.tictactroll.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.nbradbury.tictactroll.R
import com.nbradbury.tictactroll.game.Gaze
import com.nbradbury.tictactroll.game.Team
import com.nbradbury.tictactroll.ui.theme.Bramble
import com.nbradbury.tictactroll.ui.theme.BubbleCream
import com.nbradbury.tictactroll.ui.theme.BubbleInk
import com.nbradbury.tictactroll.ui.theme.Gorp
import com.nbradbury.tictactroll.ui.theme.lilita
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val Team.color: Color get() = if (this == Team.A) Gorp else Bramble

val Team.image: Int get() = if (this == Team.A) R.drawable.troll_blue else R.drawable.troll_red

@Composable
fun Team.displayName(): String = stringResource(if (this == Team.A) R.string.gorp else R.string.bramble)

enum class Mood { IDLE, HOP, SHRUG, YAWN }

/**
 * A startle away from a troll that just landed nearby: [away] is the side to lean (-1, 0 or 1), and a new [key]
 * retriggers it.
 */
data class Flinch(val away: Int, val key: Any)

private val Bottom = TransformOrigin(0.5f, 1f)

/** How long a troll takes to drop onto its crate; the landing thunk and haptic wait this long to hit with it. */
const val LANDING_MS = 150

/**
 * A troll that drops in when first composed, breathes, blinks, looks and tilts its head toward [gaze], and hops,
 * shrugs or yawns per [mood]. [index] staggers the animations so trolls don't move in sync; the shadow fades when not
 * [grounded]; [flinch] makes it startle away from a new neighbor.
 */
@Composable
fun Troll(
    team: Team,
    mood: Mood,
    gaze: Gaze,
    index: Int,
    shadowHeight: Dp,
    modifier: Modifier = Modifier,
    grounded: Boolean = true,
    flinch: Flinch? = null,
) {
    val landing = rememberLanding()
    val body = rememberBodyMotion(mood, index)
    val lids = rememberBlink(mood)
    val jolt = rememberJolt(flinch)
    val away = flinch?.away ?: 0

    val head by animateFloatAsState(
        targetValue = gaze.dx.toFloat(),
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
    )
    // Eyes dart ahead of the slower head turn.
    val eyeX by animateFloatAsState(gaze.dx.toFloat(), spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium))
    val eyeY by animateFloatAsState(gaze.dy.toFloat(), spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium))
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
                .background(Color.Black.copy(alpha = shadowAlpha * landing.drop.value), RoundedCornerShape(50))
        )
        // Decorative: the board cell or the name under a menu troll describes it.
        TrollArt(
            team = team,
            lookX = eyeX,
            lookY = eyeY,
            blink = lids.value,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val drop = landing.drop.value
                    val squash = landing.squash.value
                    val stretch = body.stretch.value
                    alpha = (drop * 3f).coerceIn(0f, 1f)
                    // Squashes wide on impact, stretches tall in a yawn.
                    scaleX = (1f + 0.12f * squash) * (1f - 0.4f * stretch)
                    scaleY = (1f - 0.16f * squash) * (1f + stretch) * breathe
                    translationY = (body.y.value - (1f - drop) * DROP_HEIGHT - 5f * jolt.value) * density
                    translationX = (head * 3f + away * 3f * jolt.value) * density
                    rotationZ = body.rotation.value + head * 8f + away * 7f * jolt.value
                    transformOrigin = Bottom
                },
        )
    }
}

/** Falls from above onto the crate, then squashes on impact and springs back. */
private class Landing {
    val drop = Animatable(0f)
    val squash = Animatable(0f)
}

private const val DROP_HEIGHT = 70f

@Composable
private fun rememberLanding(): Landing {
    val landing = remember { Landing() }
    LaunchedEffect(Unit) {
        landing.drop.animateTo(1f, tween(LANDING_MS, easing = EaseIn))
        landing.squash.snapTo(1f)
        landing.squash.animateTo(0f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium))
    }
    return landing
}

private class BodyMotion {
    val y = Animatable(0f)
    val rotation = Animatable(0f)
    val stretch = Animatable(0f)
}

@Composable
private fun rememberBodyMotion(mood: Mood, index: Int): BodyMotion {
    val body = remember { BodyMotion() }
    LaunchedEffect(mood) {
        body.y.snapTo(0f)
        body.rotation.snapTo(0f)
        body.stretch.snapTo(0f)
        when (mood) {
            Mood.HOP -> {
                delay(index % 3 * 100L)
                while (true) {
                    body.y.animateTo(0f, keyframes {
                        durationMillis = 600
                        0f at 0 using EaseInOut
                        -12f at 300 using EaseInOut
                    })
                }
            }
            Mood.SHRUG -> repeat(3) {
                launch {
                    body.y.animateTo(0f, keyframes {
                        durationMillis = 500
                        0f at 0 using EaseInOut
                        -7f at 200 using EaseInOut
                        -4f at 350 using EaseInOut
                    })
                }
                body.rotation.animateTo(0f, keyframes {
                    durationMillis = 500
                    0f at 0 using EaseInOut
                    -3f at 200 using EaseInOut
                    3f at 350 using EaseInOut
                })
            }
            // A slow stretch up and back, then a slump.
            Mood.YAWN -> {
                launch {
                    body.rotation.animateTo(0f, keyframes {
                        durationMillis = YAWN_MS
                        0f at 0 using EaseOut
                        -2.5f at 550 using EaseInOut
                        -2.5f at 800 using EaseInOut
                    })
                }
                body.stretch.animateTo(0f, keyframes {
                    durationMillis = YAWN_MS
                    0f at 0 using EaseOut
                    0.07f at 550 using EaseInOut
                    0.07f at 800 using EaseInOut
                    -0.04f at 1050 using EaseInOut
                })
            }
            Mood.IDLE -> Unit
        }
    }
    return body
}

private const val YAWN_MS = 1300

/**
 * How closed the eyelids are (0 open, 1 shut): quick random blinks, sometimes doubled; sleepy while yawning; and one
 * slow, disapproving blink during a draw's stare.
 */
@Composable
private fun rememberBlink(mood: Mood): Animatable<Float, *> {
    val lids = remember { Animatable(0f) }
    LaunchedEffect(mood) {
        lids.snapTo(0f)
        when (mood) {
            Mood.SHRUG -> {
                delay(700)
                lids.animateTo(1f, tween(260))
                delay(180)
                lids.animateTo(0f, tween(320))
            }
            Mood.YAWN -> {
                lids.animateTo(0.65f, tween(400))
                delay(500)
                lids.animateTo(0f, tween(300))
            }
            else -> while (true) {
                delay(Random.nextLong(2200, 6000))
                repeat(if (Random.nextFloat() < 0.15f) 2 else 1) {
                    lids.animateTo(1f, tween(70))
                    lids.animateTo(0f, tween(110))
                }
            }
        }
    }
    return lids
}

/** A quick jolt (0 to 1 and back) timed to a new neighbor hitting its crate. */
@Composable
private fun rememberJolt(flinch: Flinch?): Animatable<Float, *> {
    val jolt = remember { Animatable(0f) }
    LaunchedEffect(flinch) {
        if (flinch == null) return@LaunchedEffect
        delay(LANDING_MS.toLong())
        jolt.animateTo(1f, tween(90, easing = FastOutSlowInEasing))
        jolt.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow))
    }
    return jolt
}

/** A speech bubble centered horizontally at the top of its parent, [lift] above the top edge. */
@Composable
fun BoxScope.SpeechBubble(text: String, fontSize: Int, lift: Dp, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)) }
    BasicText(
        text = text,
        style = lilita(fontSize, BubbleInk),
        softWrap = false,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = -lift)
            .then(modifier)
            // Chatter isn't read aloud; it would interrupt TalkBack every few seconds.
            .clearAndSetSemantics {}
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
