package com.example.fitness_tracker

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness_tracker.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal val CardShape = RoundedCornerShape(22.dp)
internal val RingColors = listOf(MovePink, ExerciseGreen, StandCyan)

// Every tap uses the same quick compression. The platform ripple and semantics remain available.
@Composable
internal fun Modifier.press(
    role: Role = Role.Button,
    selected: Boolean? = null,
    onClick: () -> Unit,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val down by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (down) 0.965f else 1f, tween(if (down) 85 else 180), label = "Press")
    val animated = graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
    val indication = androidx.compose.foundation.LocalIndication.current
    return when {
        selected == null -> animated.clickable(source, indication, role = role, onClick = onClick)
        role == Role.Checkbox -> animated.toggleable(selected, source, indication, role = role) { onClick() }
        else -> animated.selectable(selected, source, indication, role = role, onClick = onClick)
    }
}

@Composable
internal fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 560.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            content = content
        )
    }
}

@Composable
internal fun PageTitle(title: String, eyebrow: String? = null, action: @Composable (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            eyebrow?.let { Eyebrow(it) }
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineLarge)
        }
        action?.invoke()
    }
}

@Composable
internal fun Eyebrow(text: String, color: Color = TextMuted) {
    Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp, lineHeight = 16.sp)
}

@Composable
internal fun SectionTitle(title: String, detail: String? = null) {
    Row(
        Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
        detail?.let { Text(it, color = TextMuted, style = MaterialTheme.typography.labelMedium) }
    }
}

@Composable
internal fun Panel(
    modifier: Modifier = Modifier,
    color: Color = CardGrey,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = CardShape
    val body = modifier.clip(shape)
    val action = if (onClick != null) body.press(onClick = onClick) else body
    Column(
        action.background(color).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
internal fun ActionButton(label: String, onClick: () -> Unit, primary: Boolean = false, icon: Glyph? = null) {
    val tint = if (primary) ScreenBlack else FocusGreen
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).press(onClick = onClick)
            .background(if (primary) FocusGreen else CardGrey).heightIn(min = 52.dp).padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let { FitnessIcon(it, tint, 20.dp) }
        Text(label, color = tint, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

@Composable
internal fun IconButton(icon: Glyph, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(48.dp).clip(CircleShape).press(onClick = onClick).background(CardGrey)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) { FitnessIcon(icon, FocusGreen, 22.dp) }
}

@Composable
internal fun Avatar(initials: String, modifier: Modifier = Modifier, palette: Int = 0) {
    val (top, bottom) = when (palette) {
        1 -> Color(0xFF547A9E) to Color(0xFF273A52)
        2 -> Color(0xFFA46744) to Color(0xFF4D2E29)
        3 -> Color(0xFF465063) to Color(0xFF292E3A)
        else -> Color(0xFF84659E) to Color(0xFF40324E)
    }
    Box(
        modifier.size(48.dp).clip(CircleShape).background(Brush.verticalGradient(listOf(top, bottom)))
            .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape),
        contentAlignment = Alignment.Center
    ) { Text(initials, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
}

@Composable
internal fun IconDisc(icon: Glyph, tint: Color = FocusGreen, size: Dp = 42.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) { FitnessIcon(icon, tint, size * 0.52f) }
}

@Composable
internal fun ActivityRings(
    progress: List<Float>,
    modifier: Modifier,
    description: String? = null,
    animate: Boolean = true,
) {
    val fractions = if (animate) {
        listOf(
            animateFloatAsState(progress[0], tween(420), label = "Move ring").value,
            animateFloatAsState(progress[1], tween(420), label = "Exercise ring").value,
            animateFloatAsState(progress[2], tween(420), label = "Stand ring").value,
        )
    } else progress
    Canvas(if (description == null) modifier else modifier.semantics { contentDescription = description }) {
        val width = size.minDimension
        if (width <= 0f) return@Canvas
        val stroke = width * 0.105f
        val capRadius = stroke / 2f
        val startAngle = -90f
        RingColors.forEachIndexed { index, tint ->
            val radius = width * (0.435f - index * 0.126f)
            val diameter = radius * 2
            val origin = center - Offset(radius, radius)
            val bounds = Size(diameter, diameter)
            val style = Stroke(stroke, cap = StrokeCap.Round)
            val fraction = fractions[index]
            drawCircle(tint.copy(alpha = 0.19f), radius, style = style)
            if (!fraction.isFinite() || fraction <= 0f) return@forEachIndexed

            val gradient = Brush.sweepGradient(
                0f to tint, 1f to tint, 1f to lerp(tint, Color.Black, 1f), 1f to tint,
                center = center
            )
            val lapSweep = (fraction % 1f) * 360f
            if (fraction >= 1f) {
                drawCircle(gradient, radius, style = style)
            } else {
                drawArc(gradient, startAngle, lapSweep, false, origin, bounds, style = style)
            }

            val endRadians = (startAngle + lapSweep) * PI / 180
            val radial = Offset(cos(endRadians).toFloat(), sin(endRadians).toFloat())
            val tail = center + radial * radius
            // Mask the shadow to this band's inner and outer edges before layering the tail over it.
            val lane = Path().apply {
                fillType = PathFillType.EvenOdd
                addOval(Rect(center, radius + capRadius))
                addOval(Rect(center, radius - capRadius))
            }
            val shadowRadius = capRadius * 2f
            // Keep the painted arc continuous by casting the shadow only beyond the tail.
            val side = radial * shadowRadius
            val forward = Offset(-radial.y, radial.x) * shadowRadius
            val exposedEdge = Path().apply {
                moveTo(tail.x - side.x, tail.y - side.y)
                lineTo(tail.x + side.x, tail.y + side.y)
                lineTo(tail.x + side.x + forward.x, tail.y + side.y + forward.y)
                lineTo(tail.x - side.x + forward.x, tail.y - side.y + forward.y)
                close()
            }
            clipPath(lane) {
                clipPath(exposedEdge) {
                    drawCircle(
                        Brush.radialGradient(
                            0f to Color.Black.copy(alpha = 0.5f),
                            0.5f to Color.Black.copy(alpha = 0.5f),
                            1f to Color.Transparent,
                            center = tail,
                            radius = shadowRadius
                        ),
                        radius = shadowRadius,
                        center = tail
                    )
                }
            }
            drawCircle(gradient, radius = capRadius, center = tail)
        }
    }
}

internal enum class Glyph {
    Rings, Run, Walk, Cycle, Strength, Core, Bolt, People, Chevron, Back, Check, Plus, Award,
    Steps, Route, Trend, Clock, Play, Pause, Stop, Edit, Grid, Close
}

// These monochrome pictograms share a 24 point grid, so they stay crisp at every density.
@Composable
internal fun FitnessIcon(glyph: Glyph, tint: Color, size: Dp = 24.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val unit = this.size.minDimension / 24
        fun point(x: Number, y: Number) = Offset(x.toFloat() * unit, y.toFloat() * unit)
        fun line(vararg xy: Number) {
            val path = Path().apply {
                moveTo(xy[0].toFloat() * unit, xy[1].toFloat() * unit)
                for (i in 2 until xy.size step 2) lineTo(xy[i].toFloat() * unit, xy[i + 1].toFloat() * unit)
            }
            drawPath(path, tint, style = Stroke(1.85f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        fun dot(x: Number, y: Number, r: Number) = drawCircle(tint, r.toFloat() * unit, point(x, y))
        fun circle(x: Number, y: Number, r: Number) =
            drawCircle(tint, r.toFloat() * unit, point(x, y), style = Stroke(1.75f * unit))
        fun fill(vararg xy: Number) {
            val path = Path().apply {
                moveTo(xy[0].toFloat() * unit, xy[1].toFloat() * unit)
                for (i in 2 until xy.size step 2) lineTo(xy[i].toFloat() * unit, xy[i + 1].toFloat() * unit)
                close()
            }
            drawPath(path, tint)
        }
        when (glyph) {
            Glyph.Rings -> {
                for (radius in listOf(9.4f, 6.1f, 2.8f)) {
                    drawArc(tint, -88f, 314f - (9.4f - radius) * 8, false,
                        point(12 - radius, 12 - radius), Size(radius * 2 * unit, radius * 2 * unit),
                        style = Stroke(1.7f * unit, cap = StrokeCap.Round))
                }
            }
            Glyph.Run -> {
                dot(15.5, 4, 2)
                line(5, 11, 9, 7.5, 13, 8, 16, 12, 20, 12)
                line(13, 8, 11, 13, 15, 16, 15, 21)
                line(11, 13, 8, 17, 3.5, 18)
            }
            Glyph.Walk -> {
                dot(13, 4, 2)
                line(12, 8, 11, 14, 7, 21)
                line(11, 14, 15, 17, 16, 21)
                line(7, 13, 8, 10, 12, 8, 15, 12, 18, 12)
            }
            Glyph.Cycle -> {
                circle(5.5, 16.5, 3.7)
                circle(18.5, 16.5, 3.7)
                dot(15.3, 4, 1.8)
                line(12, 20, 12, 15, 8.5, 12, 12.5, 7.5, 15.3, 11, 19, 11)
            }
            Glyph.Strength -> {
                line(2, 12, 22, 12)
                line(5, 8, 5, 16)
                line(8, 5, 8, 19)
                line(16, 5, 16, 19)
                line(19, 8, 19, 16)
            }
            Glyph.Core -> {
                dot(18.5, 7, 2)
                line(3, 18, 6, 18, 11, 12, 17, 12, 20, 18, 22, 18)
                line(9, 16, 14, 18)
            }
            Glyph.Bolt -> fill(13, 2, 4, 14, 11, 14, 10, 22, 20, 10, 13, 10)
            Glyph.People -> {
                dot(9, 7, 3)
                dot(17.5, 8, 2.4)
                drawArc(tint, 180f, 180f, true, point(2, 12), Size(14 * unit, 14 * unit))
                drawArc(tint, 198f, 162f, true, point(13.5, 13), Size(9.5f * unit, 12 * unit))
            }
            Glyph.Chevron -> line(9, 5, 16, 12, 9, 19)
            Glyph.Back -> line(14.5, 5, 7.5, 12, 14.5, 19)
            Glyph.Check -> line(5, 12, 10, 17, 19, 7)
            Glyph.Plus -> { line(12, 5, 12, 19); line(5, 12, 19, 12) }
            Glyph.Award -> {
                circle(12, 9.5, 6)
                line(8, 14, 5.5, 21, 10, 19, 12, 22, 14, 19, 18.5, 21, 16, 14)
                fill(12, 6, 13, 8.4, 15.5, 8.7, 13.7, 10.4, 14.2, 13, 12, 11.8, 9.8, 13, 10.3, 10.4, 8.5, 8.7, 11, 8.4)
            }
            Glyph.Steps -> {
                drawOval(tint, point(4.5, 10), Size(5.5f * unit, 9 * unit))
                dot(7, 8, 1.6)
                drawOval(tint, point(14, 6), Size(5.5f * unit, 9 * unit))
                dot(16.5, 4, 1.6)
            }
            Glyph.Route -> {
                circle(6, 18, 2)
                circle(18, 6, 2)
                line(8, 18, 16, 18, 19, 16, 19, 13, 16, 11, 8, 11, 6, 9, 6, 6, 8, 4, 13, 4)
            }
            Glyph.Trend -> { line(4, 18, 10, 12, 14, 15, 20, 7); line(15, 7, 20, 7, 20, 12) }
            Glyph.Clock -> { circle(12, 12, 9); line(12, 7, 12, 12, 16, 14) }
            Glyph.Play -> fill(8, 4, 20, 12, 8, 20)
            Glyph.Pause -> { line(9, 5, 9, 19); line(15, 5, 15, 19) }
            Glyph.Stop -> fill(6, 6, 18, 6, 18, 18, 6, 18)
            Glyph.Edit -> line(5, 19, 6, 14, 16, 4, 20, 8, 10, 18, 5, 19)
            Glyph.Grid -> for (x in listOf(7, 17)) for (y in listOf(7, 17)) circle(x, y, 3)
            Glyph.Close -> { line(6, 6, 18, 18); line(6, 18, 18, 6) }
        }
    }
}