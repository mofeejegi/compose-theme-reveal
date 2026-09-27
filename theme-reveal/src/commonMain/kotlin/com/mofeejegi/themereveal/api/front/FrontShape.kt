package com.mofeejegi.themereveal.api.front

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.api.tiles.TileOrder
import com.mofeejegi.themereveal.api.tiles.TileTurn

/**
 * The mask's geometry: what shape the reveal front takes as it grows from
 * the origin. The edge treatment decorates whichever front is chosen — a
 * straight front is a topology, not a wobble of zero.
 */
sealed interface FrontShape {

    /** The polar blob — one closed front expanding radially from the origin. */
    data object Radial : FrontShape

    /**
     * A band parting from the origin: two straight fronts moving apart until
     * together they cover the host. [angle] is the fronts' direction in
     * degrees, clockwise from horizontal: 0 parts up and down, 90 parts left
     * and right, anything between is a diagonal. Wobble bands run along the
     * lines (frequency = cycles across the span); leave them empty for
     * dead-straight edges.
     */
    data class Slit(val angle: Float = 0f) : FrontShape

    /**
     * A rectangle growing from the origin, proportioned so every side reaches
     * the host's edge at the same moment. [angle] turns it, in degrees
     * clockwise: 45 is a diamond. Wobble bands run around its perimeter
     * (frequency = cycles around the whole box), and its corners stay square.
     */
    data class Box(val angle: Float = 0f) : FrontShape

    /**
     * The host as a board of cards that turn one at a time from the old world
     * to the new. [width] and [height] are the target card size and must be
     * positive — the board fits whole cards to the host, so they come out
     * close to it, and never under a pixel — and [Dp.Infinity] spans the
     * host: `width = Dp.Infinity` makes full-width strips. [order] decides
     * when each card goes and [turn] how. Of the edge treatment only the
     * accent and stroke width apply, as a rim on each moving card; wobble,
     * glow, char and sway belong to the drawn fronts.
     */
    data class Tiles(
        val width: Dp = 64.dp,
        val height: Dp = 64.dp,
        val order: TileOrder = TileOrder.Random(),
        val turn: TileTurn = TileTurn.Flip,
    ) : FrontShape {
        init {
            // Rejects zero, negative and Dp.Unspecified sizes (an unspecified Dp
            // compares false against everything); Dp.Infinity passes.
            require(width > 0.dp && height > 0.dp) {
                "Tiles needs a positive card size, got $width × $height"
            }
        }
    }
}
