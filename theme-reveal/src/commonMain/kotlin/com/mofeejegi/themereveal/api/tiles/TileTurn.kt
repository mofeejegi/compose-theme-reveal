package com.mofeejegi.themereveal.api.tiles

/** How each card of a tile board moves from the old world to the new. */
sealed interface TileTurn {

    /**
     * Each old card turns on its own centre line, in perspective, until it is
     * edge-on and gone, uncovering the new world beneath. Cards wider than
     * they are tall turn about their long axis, like the slats of a blind. In
     * a shuffled order neighbours turn opposite ways, like the squares of a
     * chessboard.
     */
    data object Flip : TileTurn

    /**
     * Each new card swings in over the old world, from edge-on to flat,
     * hinged on the side the order comes from and landing softly.
     */
    data object Unfold : TileTurn
}
