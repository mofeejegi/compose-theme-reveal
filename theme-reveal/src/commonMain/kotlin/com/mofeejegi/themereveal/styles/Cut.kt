package com.mofeejegi.themereveal.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.ArrivalStyle
import com.mofeejegi.themereveal.api.edge.EdgeCharacter
import com.mofeejegi.themereveal.api.front.FrontShape

/**
 * A cut arrival: the page is sliced and parts — two dead-straight horizontal
 * fronts opening from the origin, each a crisp line of [accent] inside a
 * tight glow. No wobble, no shake; the precision is the personality.
 */
fun ArrivalStyle.Companion.cut(
    accent: Color,
    duration: Int = 700,
): ArrivalStyle = ArrivalStyle(
    front = FrontShape.Slit(),
    edge = EdgeCharacter(
        strokeWidth = 2.dp,
        glowWidth = 14.dp,
    ),
    accent = accent,
    duration = duration,
)
