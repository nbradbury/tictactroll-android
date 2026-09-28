package com.nbradbury.tictactroll.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.nbradbury.tictactroll.ui.theme.CrateBorder
import com.nbradbury.tictactroll.ui.theme.CrateBrace
import com.nbradbury.tictactroll.ui.theme.CratePlank
import com.nbradbury.tictactroll.ui.theme.CrateSeam
import com.nbradbury.tictactroll.ui.theme.CrateShadow

/** Draws a wooden crate (planks, diagonal brace, frame and shadows) with an optional [glow] outline. */
fun Modifier.crate(glow: Color) = drawBehind {
    val radius = CornerRadius(4.dp.toPx())
    val border = 6.dp.toPx()

    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            color = Color.Black.copy(alpha = 0.4f).toArgb()
            maskFilter = android.graphics.BlurMaskFilter(9.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        val y = 12.dp.toPx()
        canvas.nativeCanvas.drawRoundRect(0f, y, size.width, size.height + y, radius.x, radius.y, paint)
    }
    translate(top = 5.dp.toPx()) { drawRoundRect(CrateShadow, cornerRadius = radius) }
    drawRoundRect(CrateBorder, cornerRadius = radius)

    clipRect(border, border, size.width - border, size.height - border) {
        drawRect(CratePlank)
        // A 2dp seam every 33dp, measured up from the bottom.
        val plank = 31.dp.toPx()
        val seam = 2.dp.toPx()
        var y = size.height - border - plank - seam
        while (y > border - seam) {
            drawRect(CrateSeam, Offset(0f, y), Size(size.width, seam))
            y -= plank + seam
        }
        val inner = size.width - 2 * border
        drawLine(
            CrateBrace,
            Offset(border, border),
            Offset(size.width - border, size.height - border),
            strokeWidth = inner * 0.1f * 1.414f,
        )
        drawRect(Color(0x14FFE6BE), Offset(border, border), Size(inner, 3.dp.toPx()))
        drawRect(
            Color.Black.copy(alpha = 0.25f),
            Offset(border, border),
            Size(inner, inner),
            style = Stroke(1.dp.toPx()),
        )
    }

    if (glow.alpha > 0f) {
        val w = 3.dp.toPx()
        val o = w / 2 - 1.dp.toPx()
        drawRoundRect(
            glow,
            topLeft = Offset(-o, -o),
            size = Size(size.width + 2 * o, size.height + 2 * o),
            cornerRadius = CornerRadius(radius.x + o),
            style = Stroke(w),
        )
    }
}
