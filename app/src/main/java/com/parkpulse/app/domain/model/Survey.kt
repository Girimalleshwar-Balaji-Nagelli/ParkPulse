package com.parkpulse.app.domain.model

enum class Cleanliness(val label: String) {
    CLEAN("Good"), MODERATE("Fair"), DIRTY("Poor") // enum names are stored in Room/sync: do not rename
}

data class Survey(
    val id: String,
    val parkId: String,
    val checkInTimeMillis: Long,
    val checkInLatitude: Double,
    val checkInLongitude: Double,
    val checkInAccuracyM: Float,
    val treeCount: Int,
    val lightCount: Int,
    val cleanliness: Cleanliness,
    val notes: String,
    val createdAtMillis: Long,
    val syncStatus: SyncStatus,
)
