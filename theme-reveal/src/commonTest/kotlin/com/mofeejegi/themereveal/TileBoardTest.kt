package com.mofeejegi.themereveal

import com.mofeejegi.themereveal.api.tiles.TileOrder
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TileBoardTest {

    private fun board(
        order: TileOrder,
        cardWidth: Float = 168f,
        cardHeight: Float = 168f,
        originX: Float = 540f,
        originY: Float = 1200f,
    ) = TileBoard().apply { layout(1080f, 2400f, cardWidth, cardHeight, order, originX, originY) }

    @Test
    fun fitsWholeCardsWithEdgesOnWholePixels() {
        val board = board(TileOrder.Random())
        assertEquals(6, board.columns)
        assertEquals(14, board.rows)
        assertEquals(0f, board.edgesX.first())
        assertEquals(1080f, board.edgesX.last())
        assertEquals(2400f, board.edgesY.last())
        assertTrue(board.edgesY.all { it == it.toInt().toFloat() })
    }

    @Test
    fun aCardAsWideAsTheHostMakesStrips() {
        val board = board(TileOrder.Sweep(angle = 90f), cardWidth = 1080f)
        assertEquals(1, board.columns)
        assertEquals(14, board.rows)
    }

    @Test
    fun aShuffleGivesEveryCardItsOwnPlace() {
        val board = board(TileOrder.Random(seed = 3))
        val count = board.columns * board.rows
        val expected = FloatArray(count) { it / count.toFloat() }
        assertContentEquals(expected, board.places.sortedArray())
    }

    @Test
    fun theSameSeedPlaysTheSamePattern() {
        assertContentEquals(
            board(TileOrder.Random(seed = 7)).places,
            board(TileOrder.Random(seed = 7)).places,
        )
    }

    @Test
    fun aSweepRunsLeftToRightWithColumnsTogether() {
        val board = board(TileOrder.Sweep(angle = 0f))
        for (row in 0 until board.rows) {
            for (column in 0 until board.columns) {
                val place = board.places[row * board.columns + column]
                assertEquals(board.places[column], place)
                if (column > 0) assertTrue(place > board.places[row * board.columns + column - 1])
            }
        }
        assertEquals(0f, board.places.first())
        assertEquals(1f, board.places[board.columns - 1])
    }

    @Test
    fun aDiagonalSweepStartsTopLeftAndEndsBottomRight() {
        val board = board(TileOrder.Sweep(angle = 45f))
        assertEquals(0f, board.places.first())
        assertEquals(1f, board.places.last())
    }

    @Test
    fun fromTheOriginTheNearestCardGoesFirst() {
        val board = board(TileOrder.FromOrigin, originX = 10f, originY = 10f)
        assertEquals(0f, board.places.first())
        assertEquals(1f, board.places.last())
    }

    @Test
    fun everyCardHasMovedAtFullCoverage() {
        val lastPlace = 1f
        assertEquals(0f, cardTurn(lastPlace, progress = 0f))
        assertEquals(0f, cardTurn(lastPlace, progress = lastPlace * TILE_STAGGER))
        assertEquals(1f, cardTurn(lastPlace, progress = 1f))
        assertEquals(1f, cardTurn(place = 0f, progress = 1f - TILE_STAGGER))
    }
}
