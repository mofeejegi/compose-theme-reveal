package com.mofeejegi.themereveal.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.ArrivalStyle
import com.mofeejegi.themereveal.api.edge.EdgeCharacter
import com.mofeejegi.themereveal.api.front.FrontShape
import com.mofeejegi.themereveal.api.tiles.TileOrder
import com.mofeejegi.themereveal.api.tiles.TileTurn

/**
 * A cascade arrival: the new world unfolds over the old in a diagonal wave
 * from the top-left corner, card after card swinging open from its left edge
 * and settling flat, like a hand of cards laid out across a table. A thin
 * [accent] rim on each card in motion.
 */
fun ArrivalStyle.Companion.cascade(
    accent: Color,
    duration: Int = 1300,
): ArrivalStyle = ArrivalStyle(
    front = FrontShape.Tiles(
        width = 72.dp,
        height = 72.dp,
        order = TileOrder.Sweep(angle = 45f),
        turn = TileTurn.Unfold,
    ),
    edge = EdgeCharacter(strokeWidth = 1.dp),
    accent = accent,
    duration = duration,
)
