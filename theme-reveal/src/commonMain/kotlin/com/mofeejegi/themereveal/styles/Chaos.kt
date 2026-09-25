package com.mofeejegi.themereveal.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.ArrivalStyle
import com.mofeejegi.themereveal.styles.edge.EdgeCharacter
import com.mofeejegi.themereveal.styles.edge.WobbleBand
import com.mofeejegi.themereveal.styles.envelope.Envelope

/**
 * A chaos arrival: the new world detonates through the old. Sharpened bands
 * tear the front into jagged shards, a thick [accent] blast line with a wide
 * glow, and the hardest world-shake in the set. The envelope hits almost
 * immediately — an explosion does not ramp up politely.
 */
fun ArrivalStyle.Companion.chaos(
    accent: Color,
    duration: Int = 850,
): ArrivalStyle = ArrivalStyle(
    edge = EdgeCharacter(
        wobble = listOf(
            WobbleBand(amplitude = 16.dp, frequency = 11f, speed = 9f, sharpness = 3f),
            WobbleBand(amplitude = 10.dp, frequency = 29f, speed = 17f, sharpness = 5f),
            WobbleBand(amplitude = 5.dp, frequency = 47f, speed = 27f),
        ),
        strokeWidth = 2.5.dp,
        glowWidth = 26.dp,
    ),
    accent = accent,
    shake = 7.dp,
    duration = duration,
    envelope = Envelope(rampIn = 0.15f, rampOut = 0.75f),
)
