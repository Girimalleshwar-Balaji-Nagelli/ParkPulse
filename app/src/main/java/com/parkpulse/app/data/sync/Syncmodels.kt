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

sealed interface SyncState {
    object Idle : SyncState
    data class Running(val done: Int, val total: Int) : SyncState
    data class Success(val synced: Int, val atMillis: Long) : SyncState
    data class Failed(val message: String, val remaining: Int) : SyncState
}

data class SyncOutcome(val synced: Int, val failed: Int, val error: String?)