package com.mofeejegi.themereveal.api.front

/**
 * The mask's geometry: what shape the reveal front takes as it grows from
 * the origin. The edge treatment decorates whichever front is chosen — a
 * straight front is a topology, not a wobble of zero.
 */
sealed interface FrontShape {

    /** The polar blob — one closed front expanding radially from the origin. */
    data object Radial : FrontShape

    /**
     * A horizontal band parting from the origin: two straight fronts, one
     * rising and one falling, until together they cover the host. Wobble
     * bands run along the lines (frequency = cycles across the span); leave
     * them empty for dead-straight edges.
     */
    data object Slit : FrontShape
}
