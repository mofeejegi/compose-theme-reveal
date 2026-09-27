package com.mofeejegi.themereveal.api.front

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertFailsWith

class FrontShapeTest {

    @Test
    fun tilesRejectCardsWithoutASize() {
        assertFailsWith<IllegalArgumentException> { FrontShape.Tiles(width = 0.dp) }
        assertFailsWith<IllegalArgumentException> { FrontShape.Tiles(height = (-8).dp) }
        assertFailsWith<IllegalArgumentException> { FrontShape.Tiles(width = Dp.Unspecified) }
    }

    @Test
    fun tilesTakeTheHostSpan() {
        FrontShape.Tiles(width = Dp.Infinity, height = 56.dp)
    }
}
