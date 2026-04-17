package com.example.stargazer

/**
 * Pure math functions for projecting celestial positions onto a 2D screen.
 * Keeping these free of Android/Compose dependencies makes them easy to unit test.
 */
object AstroMath {

    /**
     * Calculates the shortest angular difference between a star's azimuth and the
     * phone's heading, handling the 360°→0° wrap-around at North.
     * @return A value in the range [-180, 180], where positive means the star is
     * clockwise (to the right) from the phone heading.
     */
    fun calculateDeltaAzimuth(starAzimuth: Float, phoneAzimuth: Float): Float {
        var delta = starAzimuth - phoneAzimuth
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        return delta
    }

    /**
     * Projects a celestial body's angular sky position into 2D screen pixel coordinates.
     * @return A [Pair] of (screenX, screenY) pixel positions.
     */
    fun calculateScreenPosition(
        star: CelestialBody,
        phoneAzimuth: Float,
        phonePitch: Float,
        pixelsPerDegree: Float,
        canvasWidth: Float,
        canvasHeight: Float
    ): Pair<Float, Float> {
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f

        // Positive deltaAzimuth → star is to the right on screen.
        val deltaAzimuth = calculateDeltaAzimuth(star.azimuth, phoneAzimuth)

        // Positive deltaPitch → star is below the phone's tilt → further down on screen.
        val deltaPitch = phonePitch - star.altitude

        val screenX = centerX + deltaAzimuth * pixelsPerDegree
        val screenY = centerY + deltaPitch * pixelsPerDegree
        return Pair(screenX, screenY)
    }

    /**
     * Returns true when a screen position is close enough to the visible canvas to be
     * worth drawing. A [padding] margin is allowed so that labels near the edge are not
     * clipped mid-render.
     */
    fun isWithinRenderBounds(
        screenX: Float,
        screenY: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        padding: Float = 300f
    ): Boolean {
        return screenX in -padding..(canvasWidth + padding) &&
            screenY in -padding..(canvasHeight + padding)
    }
}
