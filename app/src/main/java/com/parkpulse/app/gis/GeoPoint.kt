package com.parkpulse.app.gis

data class GeoPoint(val latitude: Double, val longitude: Double) {
    /** False for NaN or out-of-range values. */
    val isValid: Boolean
        get() = latitude in -90.0..90.0 && longitude in -180.0..180.0
}
