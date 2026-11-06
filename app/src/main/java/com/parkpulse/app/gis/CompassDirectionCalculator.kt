package com.parkpulse.app.gis

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class CompassDirection(val label: String) {
    NORTH("North"),
    NORTH_EAST("North-East"),
    EAST("East"),
    SOUTH_EAST("South-East"),
    SOUTH("South"),
    SOUTH_WEST("South-West"),
    WEST("West"),
    NORTH_WEST("North-West"),
}

object CompassDirectionCalculator {

    /** Initial great-circle bearing from [from] to [to], in degrees clockwise from true north (0..360). */
    fun bearingDegrees(from: GeoPoint, to: GeoPoint): Double {
        val phi1 = Math.toRadians(from.latitude)
        val phi2 = Math.toRadians(to.latitude)
        val dLambda = Math.toRadians(to.longitude - from.longitude)
        val y = sin(dLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLambda)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    fun fromBearing(bearing: Double): CompassDirection {
        val normalized = ((bearing % 360.0) + 360.0) % 360.0
        val index = ((normalized + 22.5) / 45.0).toInt() % 8
        return CompassDirection.values()[index]
    }
}
