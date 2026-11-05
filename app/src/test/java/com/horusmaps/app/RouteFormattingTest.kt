package com.horusmaps.app

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteFormattingTest {
    @Test
    fun distanceUsesMetersBelowOneKilometer() {
        assertEquals("850 م", formatRouteDistance(850.0))
    }

    @Test
    fun distanceUsesTwoDecimalKilometersAboveOneKilometer() {
        assertEquals("2.50 كم", formatRouteDistance(2500.0))
    }

    @Test
    fun durationRoundsUpToTheNextMinute() {
        assertEquals("2 د", formatRouteDuration(90.0))
    }

    @Test
    fun durationFormatsHoursAndHandlesInvalidValues() {
        assertEquals("1 س 0 د", formatRouteDuration(3600.0))
        assertEquals("—", formatRouteDuration(0.0))
        assertEquals("—", formatRouteDuration(Double.NaN))
    }
}
