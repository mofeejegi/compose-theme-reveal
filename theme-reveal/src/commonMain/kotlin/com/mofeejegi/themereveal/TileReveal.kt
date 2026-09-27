package com.mofeejegi.themereveal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Dp
import com.mofeejegi.themereveal.api.front.FrontShape
import com.mofeejegi.themereveal.api.tiles.TileOrder
import com.mofeejegi.themereveal.api.tiles.TileTurn
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * The board a tile reveal plays on, kept across frames: card edges snapped to
 * whole pixels, and each card's place in the order. Rebuilt only when the
 * host's size, the card size, the order or the origin changes, so a running
 * reveal allocates nothing for it.
 */
internal class TileBoard {
    var columns = 0
        private set
    var rows = 0
        private set

    /** Card edges: `columns + 1` xs and `rows + 1` ys, from 0 to the host's size. */
    var edgesX = FloatArray(0)
        private set
    var edgesY = FloatArray(0)
        private set

    /** Each card's place in the order, from 0 (first) to 1 (last), row by row. */
    var places = FloatArray(0)
        private set

    /** Reused every frame for a moving card's transform. */
    val matrix = Matrix()

    private var laidOutWidth = -1f
    private var laidOutHeight = -1f
    private var laidOutCardWidth = -1f
    private var laidOutCardHeight = -1f
    private var laidOutOrder: TileOrder? = null
    private var laidOutOriginX = Float.NaN
    private var laidOutOriginY = Float.NaN

    fun layout(
        width: Float,
        height: Float,
        cardWidth: Float,
        cardHeight: Float,
        order: TileOrder,
        originX: Float,
        originY: Float,
    ) {
        if (width == laidOutWidth && height == laidOutHeight &&
            cardWidth == laidOutCardWidth && cardHeight == laidOutCardHeight &&
            order == laidOutOrder && originX == laidOutOriginX && originY == laidOutOriginY
        ) {
            return
        }
        laidOutWidth = width
        laidOutHeight = height
        laidOutCardWidth = cardWidth
        laidOutCardHeight = cardHeight
        laidOutOrder = order
        laidOutOriginX = originX
        laidOutOriginY = originY

        columns = max(1, (width / cardWidth).roundToInt())
        rows = max(1, (height / cardHeight).roundToInt())
        edgesX = FloatArray(columns + 1) { (it * width / columns).roundToInt().toFloat() }
        edgesY = FloatArray(rows + 1) { (it * height / rows).roundToInt().toFloat() }
        places = when (order) {
            is TileOrder.Random -> shuffledPlaces(columns * rows, order.seed)
            is TileOrder.Sweep -> {
                val radians = order.angle * DEGREES_TO_RADIANS
                val towardX = cos(radians)
                val towardY = sin(radians)
                spreadPlaces { x, y -> x * towardX + y * towardY }
            }
            TileOrder.FromOrigin -> spreadPlaces { x, y -> hypot(x - originX, y - originY) }
        }
    }

    /** Each card gets a distinct place, in a seeded shuffle. */
    private fun shuffledPlaces(count: Int, seed: Int): FloatArray {
        val sequence = IntArray(count) { it }
        val random = Random(seed)
        for (i in count - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val swap = sequence[i]
            sequence[i] = sequence[j]
            sequence[j] = swap
        }
        val places = FloatArray(count)
        for (position in 0 until count) places[sequence[position]] = position / count.toFloat()
        return places
    }

    /** Places from a measure of each card's centre, spread from 0 (smallest) to 1 (largest). */
    private inline fun spreadPlaces(measure: (x: Float, y: Float) -> Float): FloatArray {
        val places = FloatArray(columns * rows)
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                places[row * columns + column] = measure(
                    (edgesX[column] + edgesX[column + 1]) / 2f,
                    (edgesY[row] + edgesY[row + 1]) / 2f,
                )
            }
        }
        // Take the bounds from the stored values: on Kotlin/JS a Float local keeps
        // double precision while FloatArray holds 32 bits, so the ends would miss 0 and 1.
        val smallest = places.min()
        val largest = places.max()
        val span = largest - smallest
        for (i in places.indices) places[i] = if (span > 0f) (places[i] - smallest) / span else 0f
        return places
    }
}

/**
 * How far a card at [place] has moved when the reveal is at [progress]: the
 * starts are spread over the first [TILE_STAGGER] of the reveal and every move
 * takes the rest, so the last card lands exactly at full coverage.
 */
internal fun cardTurn(place: Float, progress: Float): Float =
    ((progress - place * TILE_STAGGER) / (1f - TILE_STAGGER)).coerceIn(0f, 1f)

/**
 * A tile reveal, drawn by the incoming world's host.
 *
 * [TileTurn.Flip]: the new world drawn whole, and the old one drawn back over
 * it from [outgoing] as cards turning away. [TileTurn.Unfold]: the old world
 * draws itself beneath, and the new one is recorded into [incoming] and drawn
 * as cards swinging in over it. Either way, still cards go in one draw clipped
 * to all of them through [clip]; each moving card is drawn on its own, in
 * perspective, shaded by its angle, with an accent rim.
 */
internal fun ContentDrawScope.drawTileReveal(
    style: ArrivalStyle,
    tiles: FrontShape.Tiles,
    progress: Float,
    origin: Offset,
    outgoing: GraphicsLayer,
    incoming: GraphicsLayer,
    board: TileBoard,
    clip: Path,
) {
    val cardWidth = if (tiles.width == Dp.Infinity) size.width else tiles.width.toPx()
    val cardHeight = if (tiles.height == Dp.Infinity) size.height else tiles.height.toPx()
    board.layout(size.width, size.height, cardWidth, cardHeight, tiles.order, origin.x, origin.y)

    val arriving = tiles.turn == TileTurn.Unfold
    val cards = if (arriving) {
        incoming.record { this@drawTileReveal.drawContent() }
        incoming
    } else {
        drawContent()
        outgoing
    }

    val places = board.places
    val shuffled = tiles.order is TileOrder.Random
    val hinge = hingeFor(tiles.order)
    val rim = if (style.accent.isSpecified && style.edge.strokeWidth.value > 0f) {
        Stroke(width = style.edge.strokeWidth.toPx())
    } else {
        null
    }
    val intensity = style.envelope.intensity(progress)

    // Perspective must not carry a card past the host.
    clipRect(0f, 0f, size.width, size.height) {
        // Still cards: old ones yet to leave, or new ones already landed.
        clip.rewind()
        board.forEachCard { index, _, _, left, top, right, bottom ->
            val turn = cardTurn(places[index], progress)
            val still = if (arriving) turn >= 1f else turn <= 0f
            if (still) {
                clip.moveTo(left, top)
                clip.lineTo(right, top)
                clip.lineTo(right, bottom)
                clip.lineTo(left, bottom)
                clip.close()
            }
        }
        if (!clip.isEmpty) {
            clipPath(clip) { drawLayer(cards) }
        }

        board.forEachCard { index, column, row, left, top, right, bottom ->
            val turn = cardTurn(places[index], progress)
            if (turn <= 0f || turn >= 1f) return@forEachCard
            val centerX = (left + right) / 2f
            val centerY = (top + bottom) / 2f
            val depth = TILE_DEPTH * sqrt((right - left) * (bottom - top))
            val angle: Float
            if (arriving) {
                // Landing: quick off the hinge, settling as it lies flat.
                val rest = 1f - turn
                angle = rest * rest * HALF_PI
                val k = sin(angle) / depth
                when (hinge) {
                    Hinge.Left -> board.matrix.setTurnAboutVertical(left, centerY, angle, k)
                    Hinge.Right -> board.matrix.setTurnAboutVertical(right, centerY, angle, -k)
                    Hinge.Top -> board.matrix.setTurnAboutHorizontal(top, centerX, angle, k)
                    Hinge.Bottom -> board.matrix.setTurnAboutHorizontal(bottom, centerX, angle, -k)
                }
            } else {
                // Tipping over: slow to leave, gaining speed as it goes.
                angle = turn * turn * HALF_PI
                val direction = if (shuffled && (column + row) % 2 != 0) -1f else 1f
                val k = direction * sin(angle) / depth
                if (right - left > bottom - top) {
                    board.matrix.setTurnAboutHorizontal(centerY, centerX, angle, k)
                } else {
                    board.matrix.setTurnAboutVertical(centerX, centerY, angle, k)
                }
            }
            val topLeft = Offset(left, top)
            val cardSize = Size(right - left, bottom - top)
            withTransform({ transform(board.matrix) }) {
                clipRect(left, top, right, bottom) {
                    drawLayer(cards)
                    drawRect(Color.Black, topLeft, cardSize, alpha = TILE_SHADE * sin(angle))
                }
                if (rim != null) {
                    drawRect(style.accent, topLeft, cardSize, alpha = intensity, style = rim)
                }
            }
        }
    }
}

/** The card edge an unfolding card swings from: the side the order comes from. */
private enum class Hinge { Left, Right, Top, Bottom }

private fun hingeFor(order: TileOrder): Hinge = when (order) {
    is TileOrder.Sweep -> {
        val radians = order.angle * DEGREES_TO_RADIANS
        val towardX = cos(radians)
        val towardY = sin(radians)
        if (abs(towardX) >= abs(towardY)) {
            if (towardX >= 0f) Hinge.Left else Hinge.Right
        } else {
            if (towardY >= 0f) Hinge.Top else Hinge.Bottom
        }
    }
    // A shuffle, or the origin, has no one side; cards drop open from the top.
    else -> Hinge.Top
}

/** Runs [block] for every card, row by row, with its index, column, row and edges. */
private inline fun TileBoard.forEachCard(
    block: (index: Int, column: Int, row: Int, left: Float, top: Float, right: Float, bottom: Float) -> Unit,
) {
    var index = 0
    for (row in 0 until rows) {
        for (column in 0 until columns) {
            block(index, column, row, edgesX[column], edgesY[row], edgesX[column + 1], edgesY[row + 1])
            index++
        }
    }
}

/**
 * Sets this matrix to turn a card by [angle] radians about the vertical line
 * x = [axisX], in perspective about height [centerY]: w = 1 − k·(x − axisX),
 * so with [k] positive the side right of the axis swings toward the viewer.
 * A 2D projective map, which Android's canvas and Skia both carry through
 * concat, so the turn is real perspective on every platform.
 */
private fun Matrix.setTurnAboutVertical(axisX: Float, centerY: Float, angle: Float, k: Float) {
    val c = cos(angle)
    reset()
    values[Matrix.ScaleX] = c - k * axisX
    values[Matrix.SkewY] = -k * centerY
    values[Matrix.Perspective0] = -k
    values[Matrix.TranslateX] = axisX + k * axisX * axisX - axisX * c
    values[Matrix.TranslateY] = k * axisX * centerY
    values[Matrix.Perspective2] = 1f + k * axisX
}

/**
 * Sets this matrix to turn a card by [angle] radians about the horizontal line
 * y = [axisY], in perspective about [centerX]: w = 1 − k·(y − axisY), so with
 * [k] positive the side below the axis swings toward the viewer.
 */
private fun Matrix.setTurnAboutHorizontal(axisY: Float, centerX: Float, angle: Float, k: Float) {
    val c = cos(angle)
    reset()
    values[Matrix.SkewX] = -k * centerX
    values[Matrix.TranslateX] = k * centerX * axisY
    values[Matrix.ScaleY] = c - k * axisY
    values[Matrix.TranslateY] = axisY + k * axisY * axisY - axisY * c
    values[Matrix.Perspective1] = -k
    values[Matrix.Perspective2] = 1f + k * axisY
}

/** The share of the reveal over which the cards' starts are spread. */
internal const val TILE_STAGGER = 0.6f

// Camera distance, in card sizes (the geometric mean of width and height, so a
// full-width strip gets a real tilt without an absurd one).
private const val TILE_DEPTH = 3f

// How dark a card gets as it turns edge-on.
private const val TILE_SHADE = 0.5f

private const val HALF_PI = (PI / 2).toFloat()
private const val DEGREES_TO_RADIANS = (PI / 180).toFloat()
