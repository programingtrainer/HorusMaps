package com.horusmaps.app

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteFormattingTest {
    private fun withLocale(language: String, block: () -> Unit) {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale(language))
            block()
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun distanceAndDurationUseArabicUnits() = withLocale("ar") {
        assertEquals("850 م", formatRouteDistance(850.0))
        assertEquals("2.50 كم", formatRouteDistance(2500.0))
        assertEquals("2 د", formatRouteDuration(90.0))
        assertEquals("1 س 0 د", formatRouteDuration(3600.0))
    }

    @Test
    fun distanceAndDurationUseEnglishUnits() = withLocale("en") {
        assertEquals("850 m", formatRouteDistance(850.0))
        assertEquals("2.50 km", formatRouteDistance(2500.0))
        assertEquals("2 min", formatRouteDuration(90.0))
        assertEquals("1 h 0 min", formatRouteDuration(3600.0))
    }

    @Test
    fun durationHandlesInvalidValues() = withLocale("en") {
        assertEquals("—", formatRouteDuration(0.0))
        assertEquals("—", formatRouteDuration(Double.NaN))
    }
}
