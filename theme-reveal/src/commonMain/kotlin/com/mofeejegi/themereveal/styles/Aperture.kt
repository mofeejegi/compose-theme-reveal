package com.mofeejegi.themereveal.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.ArrivalStyle
import com.mofeejegi.themereveal.api.edge.EdgeCharacter
import com.mofeejegi.themereveal.api.edge.WobbleBand
import com.mofeejegi.themereveal.api.front.FrontShape

/**
 * An aperture arrival: a rectangular frame opening from the origin, like a
 * lens gate widening until the new world fills the view. A crisp [accent]
 * frame line in a narrow glow, square corners and a faint focus pulse; no
 * shake.
 */
fun ArrivalStyle.Companion.aperture(
    accent: Color,
    duration: Int = 800,
): ArrivalStyle = ArrivalStyle(
    front = FrontShape.Box(),
    edge = EdgeCharacter(
        wobble = listOf(
            // Frequency 0: the whole frame pulses in and out, like focusing.
            WobbleBand(amplitude = 2.dp, frequency = 0f, speed = 10f),
        ),
        strokeWidth = 2.5.dp,
        glowWidth = 8.dp,
    ),
    accent = accent,
    duration = duration,
)
