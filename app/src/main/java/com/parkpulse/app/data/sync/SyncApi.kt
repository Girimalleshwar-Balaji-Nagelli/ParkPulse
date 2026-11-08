package com.parkpulse.app.data.sync

/**
 * Backend contract. Every call MUST be idempotent: sending the same record twice
 * (e.g. after a timeout or crash) must not create a second copy.
 * Throws java.io.IOException on network/server failure.
 */
interface SyncApi {
    suspend fun upsertPark(park: ParkSyncDto)
    suspend fun upsertSurvey(survey: SurveySyncDto)
    suspend fun upsertPhoto(photo: PhotoSyncDto)
}
