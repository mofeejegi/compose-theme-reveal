package com.mofeejegi.themereveal.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.ArrivalStyle
import com.mofeejegi.themereveal.api.edge.EdgeCharacter
import com.mofeejegi.themereveal.api.edge.WobbleBand
import com.mofeejegi.themereveal.api.front.FrontShape

/**
 * A curtain arrival: the old world parts like stage drapes — two vertical
 * fronts drawing apart from the origin, softened by a slow fabric swell and
 * a wide [accent] glow, the opening drifting gently side to side as it
 * widens. Unhurried; the reveal is the performance.
 */
fun ArrivalStyle.Companion.curtain(
    accent: Color,
    duration: Int = 1000,
): ArrivalStyle = ArrivalStyle(
    front = FrontShape.Slit(angle = 90f),
    edge = EdgeCharacter(
        wobble = listOf(
            // A long slow swell and a shorter counter-fold: fabric, not flame.
            WobbleBand(amplitude = 7.dp, frequency = 2f, speed = 1.8f),
            WobbleBand(amplitude = 3.dp, frequency = 5f, speed = -2.6f),
        ),
        strokeWidth = 1.5.dp,
        glowWidth = 30.dp,
    ),
    accent = accent,
    sway = 10.dp,
    duration = duration,
)
