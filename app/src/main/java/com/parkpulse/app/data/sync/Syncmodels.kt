package com.parkpulse.app.data.sync

/** Wire format sent to the backend. Independent of Room/domain classes. */
data class ParkSyncDto(
    val id: String,
    val name: String,
    val type: String,
    val areaSqM: Double,
    /** [[lat, lng], ...] or null. */
    val boundary: List<List<Double>>?,
    val lastSurveyDate: String,
    val status: String,
)

data class SurveySyncDto(
    val id: String,
    val parkId: String,
    val checkInTimeMillis: Long,
    val checkInLatitude: Double,
    val checkInLongitude: Double,
    val checkInAccuracyM: Float,
    val treeCount: Int,
    val lightCount: Int,
    val cleanliness: String,
    val notes: String,
    val createdAtMillis: Long,
)

data class PhotoSyncDto(
    val id: String,
    val surveyId: String,
    val fileName: String,
    val takenAtMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracyM: Float,
    val sizeBytes: Long,
    val createdAtMillis: Long,
)

sealed interface SyncState {
    object Idle : SyncState
    data class Running(val done: Int, val total: Int) : SyncState
    data class Success(val synced: Int, val atMillis: Long) : SyncState
    data class Failed(val message: String, val remaining: Int) : SyncState
}

data class SyncOutcome(val synced: Int, val failed: Int, val error: String?)
