package com.mofeejegi.themereveal.styles.edge

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The reveal front's edge treatment: wobble bands summed into the mask
 * outline, plus the stroke and glow drawn along it.
 *
 * [charWidth]/[charColor] add an optional darkened band just behind the
 * front — the burnt rim of a paper-burn arrival, drawn between the glow and
 * the bright front line. Zero width (the default) disables it.
 */
@Immutable
data class EdgeCharacter(
    val wobble: List<WobbleBand> = emptyList(),
    val strokeWidth: Dp = 2.dp,
    val glowWidth: Dp = 0.dp,
    val charWidth: Dp = 0.dp,
    val charColor: Color = Color.Unspecified,
)
