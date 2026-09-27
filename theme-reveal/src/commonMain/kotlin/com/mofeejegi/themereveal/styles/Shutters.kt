package com.mofeejegi.themereveal.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.ArrivalStyle
import com.mofeejegi.themereveal.api.edge.EdgeCharacter
import com.mofeejegi.themereveal.api.front.FrontShape
import com.mofeejegi.themereveal.api.tiles.TileOrder
import com.mofeejegi.themereveal.api.tiles.TileTurn

/**
 * A shutters arrival: the old world as full-width slats that flip away one
 * after another from top to bottom, each turning on its long axis like the
 * slat of a blind to let the new world through. A thin [accent] rim on each
 * turning slat.
 */
fun ArrivalStyle.Companion.shutters(
    accent: Color,
    duration: Int = 1100,
): ArrivalStyle = ArrivalStyle(
    front = FrontShape.Tiles(
        width = Dp.Infinity,
        height = 56.dp,
        order = TileOrder.Sweep(angle = 90f),
        turn = TileTurn.Flip,
    ),
    edge = EdgeCharacter(strokeWidth = 1.dp),
    accent = accent,
    duration = duration,
)
