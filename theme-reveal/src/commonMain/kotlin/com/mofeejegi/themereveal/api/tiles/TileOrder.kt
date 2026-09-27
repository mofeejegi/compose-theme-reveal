package com.mofeejegi.themereveal.api.tiles

/** When each card of a tile board goes. */
sealed interface TileOrder {

    /** A shuffled order. The same [seed] always plays the same pattern. */
    data class Random(val seed: Int = 0) : TileOrder

    /**
     * A wave crossing the board toward [angle], in degrees clockwise from
     * rightward: 0 runs left to right, 90 top to bottom, 45 from the top-left
     * corner to the bottom-right. Cards level with each other go together.
     */
    data class Sweep(val angle: Float = 0f) : TileOrder

    /** Outward from the reveal's origin: the cards nearest it go first. */
    data object FromOrigin : TileOrder
}
