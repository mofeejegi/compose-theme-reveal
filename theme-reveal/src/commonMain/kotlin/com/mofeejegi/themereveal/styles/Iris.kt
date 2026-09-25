package com.mofeejegi.themereveal.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.ArrivalStyle
import com.mofeejegi.themereveal.styles.edge.EdgeCharacter
import com.mofeejegi.themereveal.styles.edge.WobbleBand

/**
 * An iris arrival: a luminous ring of [accent] sweeping the new world in —
 * geometrically clean, but burnished: a crisp front line inside a tight
 * halo, a fine fast shimmer keeping the ring alive, and a trace of shake
 * putting weight behind it. Still the most composed register in the set;
 * no longer a bare one.
 */
fun ArrivalStyle.Companion.iris(
    accent: Color,
    duration: Int = 850,
): ArrivalStyle = ArrivalStyle(
    edge = EdgeCharacter(
        wobble = listOf(
            WobbleBand(amplitude = 1.5.dp, frequency = 36f, speed = 12f),
        ),
        strokeWidth = 2.dp,
        glowWidth = 16.dp,
    ),
    accent = accent,
    shake = 1.5.dp,
    duration = duration,
)
