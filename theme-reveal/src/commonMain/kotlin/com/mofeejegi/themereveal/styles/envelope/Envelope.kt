package com.mofeejegi.themereveal.styles.envelope

import androidx.compose.runtime.Immutable

/**
 * Intensity envelope over reveal progress: ramp in below [rampIn], hold at
 * full, ramp out above [rampOut]. Applied to the transient drama (wobble,
 * shake, edge glow) so a reveal starts from stillness and ends clean — at
 * full coverage the mask must be a plain full-bleed cover with no residue.
 */
@Immutable
data class Envelope(
    val rampIn: Float = 0.3f,
    val rampOut: Float = 0.7f,
) {
    init {
        require(rampIn in 0f..1f && rampOut in 0f..1f && rampIn <= rampOut) {
            "Envelope requires 0 <= rampIn <= rampOut <= 1"
        }
    }

    fun intensity(progress: Float): Float = when {
        progress <= 0f || progress >= 1f -> 0f
        progress < rampIn -> progress / rampIn
        progress > rampOut -> (1f - progress) / (1f - rampOut)
        else -> 1f
    }
}
