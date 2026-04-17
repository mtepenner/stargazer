package com.example.stargazer

import org.junit.Assert.*
import org.junit.Test

class StarDataTest {

    @Test
    fun `getVisibleStars returns a non-empty list`() {
        val stars = StarData.getVisibleStars()
        assertTrue(stars.isNotEmpty())
    }

    @Test
    fun `getVisibleStars returns the expected number of entries`() {
        assertEquals(5, StarData.getVisibleStars().size)
    }

    @Test
    fun `Polaris is positioned due North`() {
        val polaris = StarData.getVisibleStars().first { it.name.contains("Polaris") }
        assertEquals(0f, polaris.azimuth, 0.001f)
    }

    @Test
    fun `all star azimuths are in the range 0 to 360 degrees`() {
        StarData.getVisibleStars().forEach { star ->
            assertTrue(
                "${star.name} azimuth ${star.azimuth} is out of range",
                star.azimuth in 0f..360f
            )
        }
    }

    @Test
    fun `all star altitudes are in the range 0 to 90 degrees`() {
        StarData.getVisibleStars().forEach { star ->
            assertTrue(
                "${star.name} altitude ${star.altitude} is out of range",
                star.altitude in 0f..90f
            )
        }
    }

    @Test
    fun `CelestialBody equality is based on all fields`() {
        val a = CelestialBody("Sirius", 100f, 35f)
        val b = CelestialBody("Sirius", 100f, 35f)
        val c = CelestialBody("Sirius", 101f, 35f)
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    @Test
    fun `CelestialBody copy produces an independent object`() {
        val original = CelestialBody("Vega", 200f, 50f)
        val copy = original.copy(altitude = 55f)
        assertEquals(50f, original.altitude, 0.001f)
        assertEquals(55f, copy.altitude, 0.001f)
    }

    @Test
    fun `CelestialBody toString includes the name`() {
        val star = CelestialBody("Canopus", 180f, 10f)
        assertTrue(star.toString().contains("Canopus"))
    }
}
