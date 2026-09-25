package com.mofeejegi.themereveal.styles.envelope

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnvelopeTest {

    @Test
    fun silentAtRestAndAtFullCoverage() {
        val envelope = Envelope()
        assertEquals(0f, envelope.intensity(0f))
        assertEquals(0f, envelope.intensity(1f))
    }

    @Test
    fun rampsInHoldsAndRampsOut() {
        val envelope = Envelope(rampIn = 0.2f, rampOut = 0.8f)
        assertEquals(0.5f, envelope.intensity(0.1f), absoluteTolerance = 1e-4f)
        assertEquals(1f, envelope.intensity(0.5f))
        assertEquals(0.5f, envelope.intensity(0.9f), absoluteTolerance = 1e-4f)
    }

    @Test
    fun rejectsInvertedRamps() {
        assertFailsWith<IllegalArgumentException> { Envelope(rampIn = 0.8f, rampOut = 0.2f) }
    }
}
