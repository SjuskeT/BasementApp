package com.example.basementorganizer

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

// A rounded-rect card shape with a real punched hole near the left edge,
// like a physical inventory tag with a lanyard hole.
class PunchedTagShape(
    private val cornerRadius: Dp = 6.dp,
    private val holeRadius: Dp = 5.dp,
    private val holeCenterX: Dp = 20.dp
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cornerPx = with(density) { cornerRadius.toPx() }
        val holeRadiusPx = with(density) { holeRadius.toPx() }
        val holeCenterXPx = with(density) { holeCenterX.toPx() }
        val holeCenterYPx = size.height / 2f

        val base = Path().apply {
            addRoundRect(RoundRect(0f, 0f, size.width, size.height, cornerPx, cornerPx))
        }
        val hole = Path().apply {
            addOval(
                Rect(
                    holeCenterXPx - holeRadiusPx,
                    holeCenterYPx - holeRadiusPx,
                    holeCenterXPx + holeRadiusPx,
                    holeCenterYPx + holeRadiusPx
                )
            )
        }
        val result = Path()
        result.op(base, hole, PathOperation.Difference)
        return Outline.Generic(result)
    }
}

// A hand-drawn-feeling dashed border, like a "cut here" line on a tag.
fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 1.5.dp,
    cornerRadius: Dp = 6.dp,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 5.dp
) = this.drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength.toPx(), gapLength.toPx()), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    )
}

// A faint repeating dot grid, evoking a pegboard backdrop.
fun Modifier.pegboardTexture(
    dotColor: Color = Color(0x14000000),
    spacing: Dp = 22.dp,
    radius: Dp = 1.2.dp
) = this.drawBehind {
    val spacingPx = spacing.toPx()
    val radiusPx = radius.toPx()
    var y = spacingPx / 2
    while (y < size.height) {
        var x = spacingPx / 2
        while (x < size.width) {
            drawCircle(color = dotColor, radius = radiusPx, center = androidx.compose.ui.geometry.Offset(x, y))
            x += spacingPx
        }
        y += spacingPx
    }
}