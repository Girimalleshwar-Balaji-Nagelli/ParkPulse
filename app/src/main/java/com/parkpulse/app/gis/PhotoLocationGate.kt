package com.parkpulse.app.gis

sealed interface PhotoGateResult {
    object Ready : PhotoGateResult
    object WaitingForGps : PhotoGateResult
    object MockLocation : PhotoGateResult
}

/**
 * A photo may only be stamped with a trustworthy position: present, fresh, real (not mock) and
 * with a reported accuracy. Unlike check-in it does NOT require 25 m accuracy: the stamp
 * simply prints the accuracy that was actually measured.
 */
object PhotoLocationGate {
    fun check(fix: LocationFix?): PhotoGateResult = when {
        fix == null -> PhotoGateResult.WaitingForGps
        fix.isMock -> PhotoGateResult.MockLocation
        !fix.point.isValid || fix.ageMs > CheckInValidator.MAX_FIX_AGE_MS || fix.accuracyM == null ->
            PhotoGateResult.WaitingForGps
        else -> PhotoGateResult.Ready
    }
}
