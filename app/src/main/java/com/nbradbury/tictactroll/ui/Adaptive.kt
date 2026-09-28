package com.nbradbury.tictactroll.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.min

/** True when the window is wide enough that the screens lay out side by side. */
val LocalWideLayout = staticCompositionLocalOf { false }

/** The space each arrangement is designed for: a phone in portrait, and a side-by-side landscape layout. */
private val TALL_DESIGN = DpSize(412.dp, 860.dp)
private val WIDE_DESIGN = DpSize(860.dp, 520.dp)

/** Wider than this many times its height, a window gets the side-by-side layout. */
private const val WIDE_ASPECT = 1.25f

/** Scales near 1 are left alone, so phones keep the exact design size. */
private const val SNAP_MIN = 0.95f
private const val SNAP_MAX = 1.15f
private const val MIN_SCALE = 0.6f
private const val MAX_SCALE = 2.4f

/**
 * Scales [content] so the design fits the window: bigger on tablets, smaller in a small split-screen window. It does
 * this by scaling the density, so every dp and sp grows together and touch targets stay aligned with what's drawn.
 */
@Composable
fun FitToWindow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    BoxWithConstraints(modifier) {
        val wide = maxWidth > maxHeight * WIDE_ASPECT
        val design = if (wide) WIDE_DESIGN else TALL_DESIGN
        val fit = min(maxWidth / design.width, maxHeight / design.height)
        val scale = if (fit in SNAP_MIN..SNAP_MAX) 1f else fit.coerceIn(MIN_SCALE, MAX_SCALE)
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(density.density * scale, density.fontScale),
            LocalWideLayout provides wide,
            content = content,
        )
    }
}
