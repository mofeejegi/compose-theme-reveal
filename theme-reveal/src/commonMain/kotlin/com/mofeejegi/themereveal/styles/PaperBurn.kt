package com.mofeejegi.themereveal.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.ArrivalStyle
import com.mofeejegi.themereveal.api.edge.EdgeCharacter
import com.mofeejegi.themereveal.api.edge.WobbleBand

/**
 * A paper-burn arrival: the new world burns through the old page.
 * Fine, fast crackle on the front; a thin bright ember line ([ember]), a
 * charred rim just behind it ([char]), and a warm halo. No shake — a page
 * burns quietly.
 */
fun ArrivalStyle.Companion.paperBurn(
    ember: Color,
    char: Color,
    duration: Int = 900,
): ArrivalStyle = ArrivalStyle(
    edge = EdgeCharacter(
        wobble = listOf(
            WobbleBand(amplitude = 5.dp, frequency = 13f, speed = 5f),
            WobbleBand(amplitude = 3.dp, frequency = 31f, speed = 9f),
            WobbleBand(amplitude = 1.5.dp, frequency = 67f, speed = 13f),
        ),
        strokeWidth = 1.5.dp,
        glowWidth = 22.dp,
        charWidth = 7.dp,
        charColor = char,
    ),
    accent = ember,
    duration = duration,
)
