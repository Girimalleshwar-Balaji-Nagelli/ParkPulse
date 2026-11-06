package com.parkpulse.app.gis

import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {
    const val EARTH_RADIUS_M = 6_371_008.8

    /** Great-circle (haversine) distance in metres. */
    fun meters(a: GeoPoint, b: GeoPoint): Double {
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = lat2 - lat1
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
        return 2 * EARTH_RADIUS_M * asin(min(1.0, sqrt(h)))
    }

    fun format(meters: Double): String =
        if (meters < 1000) "${meters.roundToInt()} m"
        else String.format(Locale.getDefault(), "%.1f km", meters / 1000.0)
}
