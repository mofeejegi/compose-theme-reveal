package com.mofeejegi.themereveal.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.ArrivalStyle
import com.mofeejegi.themereveal.styles.edge.EdgeCharacter
import com.mofeejegi.themereveal.styles.edge.WobbleBand

/**
 * The rift: the energetic original this library grew from — a broad, living
 * wobble tearing the world open, a glowing [accent] front, and a slight
 * world-shake while it runs. The general-purpose dramatic arrival.
 */
fun ArrivalStyle.Companion.rift(
    accent: Color,
    duration: Int = 800,
): ArrivalStyle = ArrivalStyle(
    edge = EdgeCharacter(
        wobble = listOf(
            WobbleBand(amplitude = 10.dp, frequency = 9f, speed = 4f),
            WobbleBand(amplitude = 5.dp, frequency = 23f, speed = 7f),
            WobbleBand(amplitude = 2.dp, frequency = 41f, speed = 11f),
        ),
        strokeWidth = 2.dp,
        glowWidth = 18.dp,
    ),
    accent = accent,
    shake = 3.dp,
    duration = duration,
)
