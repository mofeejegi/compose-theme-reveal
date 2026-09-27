package com.mofeejegi.themereveal.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.ArrivalStyle
import com.mofeejegi.themereveal.api.edge.EdgeCharacter
import com.mofeejegi.themereveal.api.front.FrontShape
import com.mofeejegi.themereveal.api.tiles.TileOrder
import com.mofeejegi.themereveal.api.tiles.TileTurn

/**
 * A flipboard arrival: the old world breaks into a board of cards that flip
 * away one by one in a shuffled order, each turning edge-on in perspective to
 * uncover the new world beneath, neighbours turning opposite ways like the
 * squares of a chessboard. A thin [accent] rim on each turning card; no shake.
 */
fun ArrivalStyle.Companion.flipboard(
    accent: Color,
    duration: Int = 1200,
): ArrivalStyle = ArrivalStyle(
    front = FrontShape.Tiles(
        width = 64.dp,
        height = 64.dp,
        order = TileOrder.Random(),
        turn = TileTurn.Flip,
    ),
    edge = EdgeCharacter(strokeWidth = 1.dp),
    accent = accent,
    duration = duration,
)
