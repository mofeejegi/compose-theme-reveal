package com.mofeejegi.themereveal

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.takeOrElse
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.mofeejegi.themereveal.api.front.FrontShape
import com.mofeejegi.themereveal.api.tiles.TileTurn
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.withSign

/**
 * Dual-composition clip reveal. [content] is ONE tree, composed once with the
 * committed value and — only while a reveal is in flight — a second time with
 * the incoming value, clipped to the animated mask. A tile front draws one
 * world back as cards instead — the committed world flipping away over the
 * incoming one, or the incoming world unfolding over the committed one — from
 * a recording that is redone whenever that world redraws. Both worlds stay
 * live. Never snapshots.
 *
 * All reveal drama (edge stroke, glow, shake) draws inside this host's own
 * layer: a sibling composed after the host (a floating identity object, say)
 * always stacks above the entire spectacle, no API needed.
 *
 * Anything ambient-animated that the mask will cross must be driven by state
 * hoisted above this host — state remembered inside [content] is duplicated
 * across the two compositions and will desync.
 */
@Composable
fun <T> ThemeRevealHost(
    controller: RevealController<T>,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val maskPath = remember { Path() } // allocated once, rebuilt per frame
    // Tile reveals draw a world back as cards, from these recordings.
    val committedLayer = rememberGraphicsLayer()
    val incomingLayer = rememberGraphicsLayer()
    val board = remember { TileBoard() }
    Box(modifier = modifier.offset { shakeOffset(controller) }) {
        Box(Modifier.committedWorld(controller, committedLayer)) {
            content(controller.current)
        }
        val incoming = controller.incoming
        if (incoming != null) {
            Box(
                Modifier
                    .matchParentSize()
                    .revealIncoming(controller, maskPath, committedLayer, incomingLayer, board)
            ) {
                content(incoming)
            }
            if (controller.inputPolicy == InputPolicy.Block) {
                Box(Modifier.matchParentSize().blockAndAccelerate(controller))
            }
        }
    }
}

/**
 * World shake. Lives in the offset placement lambda, so a running reveal
 * invalidates placement only — zero recomposition.
 */
private fun androidx.compose.ui.unit.Density.shakeOffset(
    controller: RevealController<*>,
): IntOffset {
    if (!controller.isRevealing) return IntOffset.Zero
    val style = controller.style
    if (style.shake.value <= 0f) return IntOffset.Zero
    // Clock reads happen only here and in draw — never in composition.
    val progress = controller.progress.floatValue
    val time = controller.timeSeconds.floatValue
    val intensity = style.envelope.intensity(progress) * style.shake.toPx()
    return IntOffset(
        (sin(time * 47f) * intensity).roundToInt(),
        (cos(time * 61f) * intensity).roundToInt(),
    )
}

/**
 * The committed world. While its cards flip away it is recorded into [layer]
 * rather than drawn, and the incoming side draws it back as those cards; the
 * read of [RevealController.incoming] redraws it when a reveal starts or ends.
 */
private fun Modifier.committedWorld(
    controller: RevealController<*>,
    layer: GraphicsLayer,
): Modifier = drawWithContent {
    val front = controller.style.front
    if (controller.incoming != null && front is FrontShape.Tiles && front.turn == TileTurn.Flip) {
        layer.record { this@drawWithContent.drawContent() }
    } else {
        drawContent()
    }
}

/** Draws the incoming world through the current front: a mask, or a board of cards. */
private fun Modifier.revealIncoming(
    controller: RevealController<*>,
    maskPath: Path,
    committedLayer: GraphicsLayer,
    incomingLayer: GraphicsLayer,
    board: TileBoard,
): Modifier = drawWithContent {
    val style = controller.style
    when (val front = style.front) {
        // The mask path is idle during a tile reveal; it clips the still cards instead.
        is FrontShape.Tiles -> drawTileReveal(
            style = style,
            tiles = front,
            progress = controller.progress.floatValue,
            origin = controller.origin.takeOrElse { size.center },
            outgoing = committedLayer,
            incoming = incomingLayer,
            board = board,
            clip = maskPath,
        )
        else -> drawMaskReveal(controller, maskPath)
    }
}

/** Clips the incoming world to the mask and draws the edge drama along it. */
private fun ContentDrawScope.drawMaskReveal(
    controller: RevealController<*>,
    maskPath: Path,
) {
    val style = controller.style
    val progress = controller.progress.floatValue
    val time = controller.timeSeconds.floatValue
    val baseOrigin = controller.origin.takeOrElse { size.center }
    val intensity = style.envelope.intensity(progress)

    // Sway drifts the mask's origin, not the world — the front floats side to
    // side while everything beneath it holds still.
    val swayPx = style.sway.toPx()
    val origin = if (swayPx > 0f) {
        baseOrigin.copy(x = baseOrigin.x + sin(time * SWAY_RATE) * swayPx * intensity)
    } else {
        baseOrigin
    }

    maskPath.reset()
    when (val front = style.front) {
        FrontShape.Radial -> radialMask(
            maskPath, style, progress, time, intensity,
            coverFrom = baseOrigin, origin = origin, swayPx = swayPx,
        )
        is FrontShape.Slit -> slitMask(maskPath, style, front.angle, progress, time, intensity, origin)
        is FrontShape.Box -> boxMask(maskPath, style, front.angle, progress, time, intensity, origin)
        is FrontShape.Tiles -> Unit // drawn as cards, never as a mask
    }
    maskPath.close()

    clipPath(maskPath) {
        this@drawMaskReveal.drawContent()
    }

    if (progress > 0f && progress < 1f) {
        if (style.accent.isSpecified && style.edge.glowWidth.value > 0f) {
            // Feathered: nested strokes stepping down in coverage fake a soft
            // falloff — one wide band at flat alpha reads as a stripe, not light.
            val glowPx = style.edge.glowWidth.toPx()
            for (pass in 1..GLOW_PASSES) {
                drawPath(
                    path = maskPath,
                    color = style.accent,
                    alpha = GLOW_PASS_ALPHA * intensity,
                    style = Stroke(width = glowPx * pass / GLOW_PASSES),
                )
            }
        }
        // Char band behind the front line (paper-burn arrivals).
        if (style.edge.charWidth.value > 0f && style.edge.charColor.isSpecified) {
            drawPath(
                path = maskPath,
                color = style.edge.charColor,
                alpha = intensity,
                style = Stroke(width = style.edge.charWidth.toPx()),
            )
        }
        // Zero width must mean no front line — Stroke(0f) draws a hairline.
        if (style.accent.isSpecified && style.edge.strokeWidth.value > 0f) {
            drawPath(
                path = maskPath,
                color = style.accent,
                alpha = intensity,
                style = Stroke(width = style.edge.strokeWidth.toPx()),
            )
        }
    }
}

/**
 * The polar mask around the origin; radius modulated by the summed wobble
 * bands — eased progress grows the front, raw time keeps it alive.
 */
private fun DrawScope.radialMask(
    path: Path,
    style: ArrivalStyle,
    progress: Float,
    time: Float,
    intensity: Float,
    coverFrom: Offset,
    origin: Offset,
    swayPx: Float,
) {
    val maxWobblePx = style.edge.wobble.maxOfOrNull { it.amplitude.toPx() } ?: 0f
    val coverRadius = maxCornerDistance(coverFrom, size) + maxWobblePx + swayPx
    val baseRadius = progress * coverRadius

    var theta = 0f
    var first = true
    while (theta < TWO_PI) {
        val radius =
            (baseRadius + wobbleDisplacement(style, theta, time, intensity)).coerceAtLeast(0f)
        val x = origin.x + radius * cos(theta)
        val y = origin.y + radius * sin(theta)
        if (first) {
            path.moveTo(x, y)
            first = false
        } else {
            path.lineTo(x, y)
        }
        theta += STEP
    }
}

/**
 * The slit mask: a band parting from the origin — two straight fronts moving
 * apart across their own direction ([angle] degrees clockwise from
 * horizontal). Built as one closed loop whose ends overshoot the canvas, so
 * only the two fronts — and their stroke and glow — ever render.
 */
private fun DrawScope.slitMask(
    path: Path,
    style: ArrivalStyle,
    angle: Float,
    progress: Float,
    time: Float,
    intensity: Float,
    origin: Offset,
) {
    // The band's own frame: "along" runs down the fronts, "across" is the way they part.
    val radians = angle * DEGREES_TO_RADIANS
    val alongX = cos(radians)
    val alongY = sin(radians)
    val acrossX = -alongY
    val acrossY = alongX

    // The canvas's reach from the origin, measured in that frame.
    var alongMin = Float.MAX_VALUE
    var alongMax = -Float.MAX_VALUE
    var acrossReach = 0f
    forEachCorner { cornerX, cornerY ->
        val dx = cornerX - origin.x
        val dy = cornerY - origin.y
        val along = dx * alongX + dy * alongY
        alongMin = min(alongMin, along)
        alongMax = max(alongMax, along)
        acrossReach = max(acrossReach, abs(dx * acrossX + dy * acrossY))
    }

    val maxWobblePx = style.edge.wobble.maxOfOrNull { it.amplitude.toPx() } ?: 0f
    val half = progress * (acrossReach + maxWobblePx)
    val overshoot = style.edge.glowWidth.toPx() + style.edge.strokeWidth.toPx() +
        style.shake.toPx() + maxWobblePx
    val start = alongMin - overshoot
    val span = alongMax - alongMin + 2 * overshoot

    // One loop, the perimeter parameter carried through both edges so their
    // wobble phases differ instead of mirroring.
    var i = 0
    while (i <= SLIT_STEPS) {
        val t = i / SLIT_STEPS.toFloat()
        val along = start + span * t
        val across = -half + wobbleDisplacement(style, t * TWO_PI, time, intensity)
        val x = origin.x + along * alongX + across * acrossX
        val y = origin.y + along * alongY + across * acrossY
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        i++
    }
    i = 0
    while (i <= SLIT_STEPS) {
        val t = i / SLIT_STEPS.toFloat()
        val along = start + span * (1f - t)
        val across = half + wobbleDisplacement(style, (1f + t) * TWO_PI, time, intensity)
        path.lineTo(
            origin.x + along * alongX + across * acrossX,
            origin.y + along * alongY + across * acrossY,
        )
        i++
    }
}

/**
 * The box mask: a rectangle growing from the origin, turned [angle] degrees
 * clockwise and proportioned so every side reaches the canvas edge at the
 * same moment. Wobble pushes each side out along its own normal and each
 * corner along both of its sides' normals, so the corners stay square.
 */
private fun DrawScope.boxMask(
    path: Path,
    style: ArrivalStyle,
    angle: Float,
    progress: Float,
    time: Float,
    intensity: Float,
    origin: Offset,
) {
    // The box's own axes.
    val radians = angle * DEGREES_TO_RADIANS
    val axisXx = cos(radians)
    val axisXy = sin(radians)
    val axisYx = -axisXy
    val axisYy = axisXx

    // How far the box must reach along each axis to cover the canvas.
    var reachX = 0f
    var reachY = 0f
    forEachCorner { cornerX, cornerY ->
        val dx = cornerX - origin.x
        val dy = cornerY - origin.y
        reachX = max(reachX, abs(dx * axisXx + dy * axisXy))
        reachY = max(reachY, abs(dx * axisYx + dy * axisYy))
    }

    val maxWobblePx = style.edge.wobble.maxOfOrNull { it.amplitude.toPx() } ?: 0f
    val halfX = progress * (reachX + maxWobblePx)
    val halfY = progress * (reachY + maxWobblePx)
    val perimeter = max(4f * (halfX + halfY), 1f)

    // Clockwise from the top-left corner: top, right, bottom, left sides.
    var walked = 0f
    for (side in 0 until 4) {
        val fromX = if (side == 0 || side == 3) -halfX else halfX
        val fromY = if (side <= 1) -halfY else halfY
        val toX = if (side <= 1) halfX else -halfX
        val toY = if (side == 0 || side == 3) -halfY else halfY
        val sideLength = if (side % 2 == 0) 2f * halfX else 2f * halfY
        // The side's outward normal.
        val normalX = if (side == 1) 1f else if (side == 3) -1f else 0f
        val normalY = if (side == 0) -1f else if (side == 2) 1f else 0f
        for (step in 0 until BOX_STEPS) {
            val t = step / BOX_STEPS.toFloat()
            val offset = wobbleDisplacement(
                style, (walked + t * sideLength) / perimeter * TWO_PI, time, intensity,
            )
            var localX = fromX + (toX - fromX) * t
            var localY = fromY + (toY - fromY) * t
            if (step == 0) {
                // A corner moves out along both of its sides, keeping it square.
                localX += offset * (if (fromX < 0f) -1f else 1f)
                localY += offset * (if (fromY < 0f) -1f else 1f)
            } else {
                localX += offset * normalX
                localY += offset * normalY
            }
            val x = origin.x + localX * axisXx + localY * axisYx
            val y = origin.y + localX * axisXy + localY * axisYy
            if (side == 0 && step == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        walked += sideLength
    }
}

/** Runs [block] with each corner of the canvas. */
private inline fun DrawScope.forEachCorner(block: (x: Float, y: Float) -> Unit) {
    block(0f, 0f)
    block(size.width, 0f)
    block(0f, size.height)
    block(size.width, size.height)
}

/** The summed wobble bands at [param] (radians along the front's perimeter). */
private fun DrawScope.wobbleDisplacement(
    style: ArrivalStyle,
    param: Float,
    time: Float,
    intensity: Float,
): Float {
    var displacement = 0f
    for (band in style.edge.wobble) {
        var wave = sin(band.frequency * param + band.speed * time)
        if (band.sharpness != 1f) {
            wave = abs(wave).pow(band.sharpness).withSign(wave)
        }
        displacement += band.amplitude.toPx() * intensity * wave
    }
    return displacement
}

private fun maxCornerDistance(origin: Offset, size: Size): Float {
    val corners = listOf(
        Offset.Zero,
        Offset(size.width, 0f),
        Offset(0f, size.height),
        Offset(size.width, size.height),
    )
    return corners.maxOf { (it - origin).getDistance() }
}

/**
 * Consumes all pointer input inside the host; the first down accelerates the
 * reveal to completion (never cancels — the input law).
 */
private fun Modifier.blockAndAccelerate(controller: RevealController<*>): Modifier =
    pointerInput(controller) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            down.consume()
            controller.accelerate()
            while (true) {
                val event = awaitPointerEvent()
                event.changes.forEach { it.consume() }
                if (event.changes.none { it.pressed }) break
            }
        }
    }

private const val TWO_PI = (2 * PI).toFloat()
private const val DEGREES_TO_RADIANS = (PI / 180).toFloat()
private const val STEP = TWO_PI / 128f
private const val SLIT_STEPS = 64
private const val BOX_STEPS = 32

// Composite where all passes overlap (the front line) ≈ 1 − (1 − α)³ ≈ 0.34,
// matching the single flat band this replaced; each step outward drops one pass.
private const val GLOW_PASSES = 3
private const val GLOW_PASS_ALPHA = 0.13f

// Slow enough to read as floating, not vibrating — one full side-to-side
// period every ~2.7 seconds of raw time.
private const val SWAY_RATE = 2.3f
