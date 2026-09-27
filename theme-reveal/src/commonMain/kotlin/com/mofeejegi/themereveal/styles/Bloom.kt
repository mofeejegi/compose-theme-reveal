package com.mofeejegi.themereveal.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.ArrivalStyle
import com.mofeejegi.themereveal.api.edge.EdgeCharacter
import com.mofeejegi.themereveal.api.edge.WobbleBand

/**
 * A bloom arrival: no front line at all — a broad soft halo of [accent] over
 * petal lobes churning in opposite directions, the whole front breathing as
 * it grows and swaying gently side to side. The new world reads as a flower
 * of light opening through the page, not an edge crossing it.
 */
fun ArrivalStyle.Companion.bloom(
    accent: Color,
    duration: Int = 950,
): ArrivalStyle = ArrivalStyle(
    edge = EdgeCharacter(
        wobble = listOf(
            // Counter-rotating petal lobes — opposite speed signs are the swirl.
            WobbleBand(amplitude = 16.dp, frequency = 5f, speed = 2.2f),
            WobbleBand(amplitude = 12.dp, frequency = 3f, speed = -1.4f),
            // Frequency 0: a uniform radial pulse — the bloom breathes.
            WobbleBand(amplitude = 6.dp, frequency = 0f, speed = 3.4f),
        ),
        strokeWidth = 0.dp,
        glowWidth = 44.dp,
    ),
    accent = accent,
    sway = 10.dp,
    duration = duration,
)
