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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.mofeejegi.themereveal.styles.front.FrontShape
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.withSign

/**
 * Dual-composition clip reveal. [content] is ONE tree, composed once with the
 * committed value and — only while a reveal is in flight — a second time with
 * the incoming value, clipped to the animated mask. Both worlds stay live.
 * Never snapshots.
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
    Box(modifier = modifier.offset { shakeOffset(controller) }) {
        content(controller.current)
        val incoming = controller.incoming
        if (incoming != null) {
            Box(
                Modifier
                    .matchParentSize()
                    .revealClip(controller, maskPath)
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

/** Clips the incoming world to the mask and draws the edge drama along it. */
private fun Modifier.revealClip(
    controller: RevealController<*>,
    maskPath: Path,
): Modifier = drawWithContent {
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
    when (style.front) {
        FrontShape.Radial -> radialMask(
            maskPath, style, progress, time, intensity,
            coverFrom = baseOrigin, origin = origin, swayPx = swayPx,
        )
        FrontShape.Slit -> slitMask(maskPath, style, progress, time, intensity, origin)
    }
    maskPath.close()

    clipPath(maskPath) {
        this@drawWithContent.drawContent()
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
 * The slit mask: a horizontal band parting from the origin — two straight
 * fronts, one rising, one falling. Built as one closed loop whose vertical
 * closures overshoot the canvas, so only the horizontal fronts — and their
 * stroke and glow — ever render.
 */
private fun DrawScope.slitMask(
    path: Path,
    style: ArrivalStyle,
    progress: Float,
    time: Float,
    intensity: Float,
    origin: Offset,
) {
    val maxWobblePx = style.edge.wobble.maxOfOrNull { it.amplitude.toPx() } ?: 0f
    val coverHalf = max(origin.y, size.height - origin.y) + maxWobblePx
    val half = progress * coverHalf
    val overshoot = style.edge.glowWidth.toPx() + style.edge.strokeWidth.toPx() +
        style.shake.toPx() + maxWobblePx
    val left = -overshoot
    val span = size.width + 2 * overshoot
    val topY = origin.y - half
    val bottomY = origin.y + half

    // One loop, the perimeter parameter carried through both edges so their
    // wobble phases differ instead of mirroring.
    var i = 0
    while (i <= SLIT_STEPS) {
        val t = i / SLIT_STEPS.toFloat()
        val y = topY + wobbleDisplacement(style, t * TWO_PI, time, intensity)
        if (i == 0) path.moveTo(left, y) else path.lineTo(left + span * t, y)
        i++
    }
    i = 0
    while (i <= SLIT_STEPS) {
        val t = i / SLIT_STEPS.toFloat()
        val y = bottomY + wobbleDisplacement(style, (1f + t) * TWO_PI, time, intensity)
        path.lineTo(left + span * (1f - t), y)
        i++
    }
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
private const val STEP = TWO_PI / 128f
private const val SLIT_STEPS = 64

// Composite where all passes overlap (the front line) ≈ 1 − (1 − α)³ ≈ 0.34,
// matching the single flat band this replaced; each step outward drops one pass.
private const val GLOW_PASSES = 3
private const val GLOW_PASS_ALPHA = 0.13f

// Slow enough to read as floating, not vibrating — one full side-to-side
// period every ~2.7 seconds of raw time.
private const val SWAY_RATE = 2.3f
