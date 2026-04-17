package com.example.stargazer

import org.junit.Assert.*
import org.junit.Test

class AstroMathTest {

    // -------------------------------------------------------------------------
    // calculateDeltaAzimuth
    // -------------------------------------------------------------------------

    @Test
    fun `delta azimuth is zero when star and phone face the same direction`() {
        assertEquals(0f, AstroMath.calculateDeltaAzimuth(90f, 90f), 0.001f)
    }

    @Test
    fun `delta azimuth is positive when star is clockwise from phone heading`() {
        // Star at East (90°), phone facing North (0°) → star is 90° to the right
        assertEquals(90f, AstroMath.calculateDeltaAzimuth(90f, 0f), 0.001f)
    }

    @Test
    fun `delta azimuth is negative when star is counter-clockwise from phone heading`() {
        // Star at West (270°), phone facing North (360° = 0°) → star is 90° to the left
        assertEquals(-90f, AstroMath.calculateDeltaAzimuth(270f, 360f), 0.001f)
    }

    @Test
    fun `delta azimuth wraps correctly when star is just east of North and phone faces just west of North`() {
        // Star at 10°, phone at 350° → star is 20° to the right (not 340° to the left)
        assertEquals(20f, AstroMath.calculateDeltaAzimuth(10f, 350f), 0.001f)
    }

    @Test
    fun `delta azimuth wraps correctly when star is just west of North and phone faces just east of North`() {
        // Star at 350°, phone at 10° → star is 20° to the left (not 340° to the right)
        assertEquals(-20f, AstroMath.calculateDeltaAzimuth(350f, 10f), 0.001f)
    }

    @Test
    fun `delta azimuth is 180 for directly opposite heading`() {
        assertEquals(180f, AstroMath.calculateDeltaAzimuth(180f, 0f), 0.001f)
    }

    @Test
    fun `delta azimuth is always in the range negative 180 to positive 180`() {
        for (starAz in 0..359 step 10) {
            for (phoneAz in 0..359 step 10) {
                val delta = AstroMath.calculateDeltaAzimuth(starAz.toFloat(), phoneAz.toFloat())
                assertTrue(
                    "Expected -180..180 but got $delta (star=$starAz phone=$phoneAz)",
                    delta >= -180f && delta <= 180f
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // calculateScreenPosition
    // -------------------------------------------------------------------------

    @Test
    fun `star directly in view projects to screen center`() {
        val star = CelestialBody("Test", azimuth = 90f, altitude = 45f)
        val (x, y) = AstroMath.calculateScreenPosition(
            star = star,
            phoneAzimuth = 90f,
            phonePitch = 45f,
            pixelsPerDegree = 40f,
            canvasWidth = 1080f,
            canvasHeight = 1920f
        )
        assertEquals(540f, x, 0.001f)
        assertEquals(960f, y, 0.001f)
    }

    @Test
    fun `star to the right of heading projects right of center`() {
        val star = CelestialBody("East", azimuth = 100f, altitude = 0f)
        val (x, _) = AstroMath.calculateScreenPosition(
            star = star,
            phoneAzimuth = 90f,
            phonePitch = 0f,
            pixelsPerDegree = 40f,
            canvasWidth = 1080f,
            canvasHeight = 1920f
        )
        assertTrue("Expected x > center but got $x", x > 540f)
    }

    @Test
    fun `star to the left of heading projects left of center`() {
        val star = CelestialBody("West", azimuth = 80f, altitude = 0f)
        val (x, _) = AstroMath.calculateScreenPosition(
            star = star,
            phoneAzimuth = 90f,
            phonePitch = 0f,
            pixelsPerDegree = 40f,
            canvasWidth = 1080f,
            canvasHeight = 1920f
        )
        assertTrue("Expected x < center but got $x", x < 540f)
    }

    @Test
    fun `star higher than phone pitch projects above center`() {
        val star = CelestialBody("High", azimuth = 0f, altitude = 60f)
        val (_, y) = AstroMath.calculateScreenPosition(
            star = star,
            phoneAzimuth = 0f,
            phonePitch = 45f,
            pixelsPerDegree = 40f,
            canvasWidth = 1080f,
            canvasHeight = 1920f
        )
        // Higher altitude → smaller screenY (above center)
        assertTrue("Expected y < center but got $y", y < 960f)
    }

    @Test
    fun `star lower than phone pitch projects below center`() {
        val star = CelestialBody("Low", azimuth = 0f, altitude = 20f)
        val (_, y) = AstroMath.calculateScreenPosition(
            star = star,
            phoneAzimuth = 0f,
            phonePitch = 45f,
            pixelsPerDegree = 40f,
            canvasWidth = 1080f,
            canvasHeight = 1920f
        )
        assertTrue("Expected y > center but got $y", y > 960f)
    }

    @Test
    fun `screen position scales correctly with pixelsPerDegree`() {
        val star = CelestialBody("Scale", azimuth = 45f, altitude = 0f)
        val ppd = 40f
        val (x, _) = AstroMath.calculateScreenPosition(
            star = star,
            phoneAzimuth = 0f,
            phonePitch = 0f,
            pixelsPerDegree = ppd,
            canvasWidth = 1080f,
            canvasHeight = 1920f
        )
        // centerX + 45 degrees * 40 px/degree = 540 + 1800 = 2340
        assertEquals(540f + 45f * ppd, x, 0.001f)
    }

    @Test
    fun `north wrap-around is handled correctly in screen projection`() {
        // Star just west of North (350°), phone facing just east of North (10°)
        // Should be 20° to the left → screenX < center
        val star = CelestialBody("NearNorth", azimuth = 350f, altitude = 0f)
        val (x, _) = AstroMath.calculateScreenPosition(
            star = star,
            phoneAzimuth = 10f,
            phonePitch = 0f,
            pixelsPerDegree = 40f,
            canvasWidth = 1080f,
            canvasHeight = 1920f
        )
        assertTrue("Expected x < center but got $x", x < 540f)
    }

    // -------------------------------------------------------------------------
    // isWithinRenderBounds
    // -------------------------------------------------------------------------

    @Test
    fun `center of screen is within render bounds`() {
        assertTrue(AstroMath.isWithinRenderBounds(540f, 960f, 1080f, 1920f))
    }

    @Test
    fun `position just inside left padding edge is within bounds`() {
        assertTrue(AstroMath.isWithinRenderBounds(-299f, 960f, 1080f, 1920f))
    }

    @Test
    fun `position just outside left padding edge is outside bounds`() {
        assertFalse(AstroMath.isWithinRenderBounds(-301f, 960f, 1080f, 1920f))
    }

    @Test
    fun `position far to the left is outside bounds`() {
        assertFalse(AstroMath.isWithinRenderBounds(-500f, 960f, 1080f, 1920f))
    }

    @Test
    fun `position far to the right is outside bounds`() {
        assertFalse(AstroMath.isWithinRenderBounds(1700f, 960f, 1080f, 1920f))
    }

    @Test
    fun `position far above is outside bounds`() {
        assertFalse(AstroMath.isWithinRenderBounds(540f, -500f, 1080f, 1920f))
    }

    @Test
    fun `position far below is outside bounds`() {
        assertFalse(AstroMath.isWithinRenderBounds(540f, 2500f, 1080f, 1920f))
    }

    @Test
    fun `custom padding is respected`() {
        // With padding = 100, -101 should be outside
        assertFalse(AstroMath.isWithinRenderBounds(-101f, 960f, 1080f, 1920f, padding = 100f))
        // With padding = 100, -99 should be inside
        assertTrue(AstroMath.isWithinRenderBounds(-99f, 960f, 1080f, 1920f, padding = 100f))
    }
}
