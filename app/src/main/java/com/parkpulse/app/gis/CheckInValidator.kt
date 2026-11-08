package com.parkpulse.app.gis

/** A GPS reading reduced to what check-in needs. Kept Android-free so it can be unit tested. */
data class LocationFix(
    val point: GeoPoint,
    /** Horizontal accuracy radius in metres, or null if the provider reported none. */
    val accuracyM: Float?,
    /** How old the fix is right now. */
    val ageMs: Long,
    val isMock: Boolean,
)

sealed interface CheckInResult {
    data class Allowed(val accuracyM: Float) : CheckInResult
    object NoBoundary : CheckInResult
    object MockLocation : CheckInResult
    object StaleFix : CheckInResult
    data class PoorAccuracy(val accuracyM: Float?) : CheckInResult
    /** [distanceM] and [direction] point from the user to the nearest boundary edge. */
    data class Outside(val distanceM: Double, val direction: CompassDirection) : CheckInResult
}

object CheckInValidator {
    const val MAX_ACCURACY_M = 25f
    const val MAX_FIX_AGE_MS = 15_000L

    /**
     * @param ring the park's verified boundary (open ring) or null when the park has none.
     * Order matters: a fix that is untrusted (mock, stale, inaccurate) is rejected before the
     * inside/outside test, because the polygon test is meaningless on a bad fix.
     */
    fun evaluate(ring: List<GeoPoint>?, fix: LocationFix): CheckInResult {
        if (ring == null || ring.size < 3) return CheckInResult.NoBoundary
        if (fix.isMock) return CheckInResult.MockLocation
        if (!fix.point.isValid || fix.ageMs > MAX_FIX_AGE_MS) return CheckInResult.StaleFix
        val accuracy = fix.accuracyM
        if (accuracy == null || accuracy > MAX_ACCURACY_M) return CheckInResult.PoorAccuracy(accuracy)

        if (PolygonUtils.contains(ring, fix.point)) return CheckInResult.Allowed(accuracy)

        val nearest = PolygonUtils.nearestPointOnBoundary(ring, fix.point)
        val distance = DistanceCalculator.meters(fix.point, nearest)
        val bearing = CompassDirectionCalculator.bearingDegrees(fix.point, nearest)
        return CheckInResult.Outside(distance, CompassDirectionCalculator.fromBearing(bearing))
    }
}
