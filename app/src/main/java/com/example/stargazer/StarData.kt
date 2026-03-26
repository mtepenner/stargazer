package com.example.stargazer

/**
 * Represents a celestial body in the sky using a Horizontal Coordinate System.
 * * @param name The display name of the star or planet.
 * @param azimuth The compass direction in degrees (0 = North, 90 = East, 180 = South, 270 = West).
 * @param altitude The tilt angle in degrees (0 = Horizon, 90 = Zenith/Straight Up).
 */
data class CelestialBody(
    val name: String, 
    val azimuth: Float, 
    val altitude: Float
)

object StarData {
    
    /**
     * Returns a list of mock stars to test the AR projection math.
     * When you spin in a circle or tilt your phone, these should stay anchored 
     * to their respective compass directions and heights.
     */
    fun getVisibleStars(): List<CelestialBody> {
        return listOf(
            CelestialBody("North Star (Polaris)", 0f, 45f), // Due North, halfway up the sky
            CelestialBody("East Marker", 90f, 30f),         // Due East, lower in the sky
            CelestialBody("South Marker", 180f, 60f),       // Due South, high in the sky
            CelestialBody("West Marker", 270f, 20f),        // Due West, near the horizon
            CelestialBody("Zenith (Straight Up)", 0f, 89f)  // Directly overhead
        )
    }
}
