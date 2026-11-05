package com.parkpulse.app.domain.model

import java.time.LocalDate

data class LatLng(val latitude: Double, val longitude: Double)

enum class ParkType(val label: String) {
    GARDEN("Garden"), PLAYGROUND("Playground"), FOREST_PARK("Forest Park"), LAKEFRONT("Lakefront")
}

enum class ParkStatus(val label: String) {
    OPEN("Open"), NEEDS_REPAIR("Needs repair"), CLOSED("Closed")
}

enum class SyncStatus { SYNCED, PENDING }

data class Park(
    val id: String,
    val name: String,
    val type: ParkType,
    val areaSqM: Double,
    /** null = verified boundary not available yet. */
    val boundary: List<LatLng>?,
    val lastSurveyDate: LocalDate,
    val status: ParkStatus,
    val syncStatus: SyncStatus
) {
    val isPendingSync: Boolean get() = syncStatus == SyncStatus.PENDING
}