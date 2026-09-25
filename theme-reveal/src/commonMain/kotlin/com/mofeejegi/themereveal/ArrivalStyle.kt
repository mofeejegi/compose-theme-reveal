package com.mofeejegi.themereveal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.styles.edge.EdgeCharacter
import com.mofeejegi.themereveal.styles.envelope.Envelope
import com.mofeejegi.themereveal.styles.front.FrontShape

/**
 * The personality of one reveal: how the incoming world's front looks and
 * behaves. Themes own their arrival — the incoming theme supplies the style.
 *
 * This is the spike subset of the full design (tell, particles, lightning,
 * interior sequencing land as they are implemented, not as dead fields).
 *
 * @param front the mask's geometry — the polar blob by default, or a
 *   horizontal band parting from the origin
 * @param edge the front's outline treatment
 * @param accent stroke/glow color along the front
 * @param shake world-shake displacement at full intensity; 0 is a valid
 *   personality
 * @param sway smooth side-to-side drift of the reveal front while it grows —
 *   the front floats rather than jitters. Moves only the mask, never the
 *   world ([shake] does that), and is envelope-scaled so the reveal still
 *   ends clean
 * @param duration reveal duration in milliseconds at normal speed
 * @param envelope intensity ramp for the transient drama
 */
@Immutable
data class ArrivalStyle(
    val front: FrontShape = FrontShape.Radial,
    val edge: EdgeCharacter = EdgeCharacter(),
    val accent: Color = Color.Unspecified,
    val shake: Dp = 0.dp,
    val sway: Dp = 0.dp,
    val duration: Int = 800,
    val envelope: Envelope = Envelope(),
) {
    /**
     * Anchor for the preset styles: each preset lives in its own file under
     * `com.mofeejegi.themereveal.styles` as an extension on this companion,
     * so call sites read `ArrivalStyle.paperBurn(...)`, `ArrivalStyle.rift(...)`.
     */
    companion object
}
