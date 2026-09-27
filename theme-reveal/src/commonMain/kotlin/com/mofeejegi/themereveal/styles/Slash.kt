package com.mofeejegi.themereveal.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.ArrivalStyle
import com.mofeejegi.themereveal.api.edge.EdgeCharacter
import com.mofeejegi.themereveal.api.envelope.Envelope
import com.mofeejegi.themereveal.api.front.FrontShape

/**
 * A slash arrival: one clean diagonal cut, and the page falls open along it.
 * Two dead-straight fronts part behind a razor-thin [accent] line in a tight
 * glow; fast, with a jolt of shake at the moment of the cut that dies away
 * as the halves separate.
 */
fun ArrivalStyle.Companion.slash(
    accent: Color,
    duration: Int = 600,
): ArrivalStyle = ArrivalStyle(
    front = FrontShape.Slit(angle = -30f),
    edge = EdgeCharacter(
        strokeWidth = 1.dp,
        glowWidth = 10.dp,
    ),
    accent = accent,
    shake = 5.dp,
    duration = duration,
    // The jolt lands with the cut, then fades as the halves part.
    envelope = Envelope(rampIn = 0.04f, rampOut = 0.3f),
)
