package com.mofeejegi.themereveal.styles.edge

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp

/**
 * One sinusoidal component of the reveal front's edge. The edge radius is
 * modulated by the sum of a style's bands — most of an arrival personality
 * lives in these parameters (none = hard clean edge; a few high-frequency
 * bands = nervous crackle; one slow wide band = liquid swell; sharpened
 * bands = shards).
 *
 * @param amplitude radial displacement at full intensity
 * @param frequency cycles around the full edge circumference; 0 collapses the
 *   band to a uniform radial pulse — the whole front breathes in and out
 * @param speed phase drift in radians per second of raw time — this is what
 *   keeps the edge alive, driven by the continuous clock, not eased progress.
 *   The drift rotates the band's lobes around the edge, and the sign sets the
 *   direction: bands running opposite signs counter-rotate, which is what
 *   makes an edge swirl rather than merely wobble
 * @param sharpness spike shaping: 1 leaves the pure sine; higher values raise
 *   the wave to this power (sign kept), pinching each swell into a narrow
 *   spike — and since the path renders with straight segments, high values
 *   read as jagged shards rather than smooth bumps
 */
@Immutable
data class WobbleBand(
    val amplitude: Dp,
    val frequency: Float,
    val speed: Float,
    val sharpness: Float = 1f,
)
