package com.parkpulse.app.location

import android.location.Location
import android.os.Build
import android.os.SystemClock
import com.parkpulse.app.gis.GeoPoint
import com.parkpulse.app.gis.LocationFix

/** Converts an Android Location into the Android-free LocationFix used by CheckInValidator. */
fun Location.toFix(nowElapsedNanos: Long = SystemClock.elapsedRealtimeNanos()): LocationFix =
    LocationFix(
        point = GeoPoint(latitude, longitude),
        accuracyM = if (hasAccuracy()) accuracy else null,
        ageMs = (nowElapsedNanos - elapsedRealtimeNanos) / 1_000_000L,
        isMock = isMockLocation(),
    )

@Suppress("DEPRECATION")
private fun Location.isMockLocation(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) isMock else isFromMockProvider
