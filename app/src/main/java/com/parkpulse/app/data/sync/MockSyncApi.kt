package com.parkpulse.app.data.sync

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simulated server. Records live in memory (lost when the app process dies) and are keyed by
 * the client-generated id, so a retried request overwrites instead of duplicating.
 */
@Singleton
class MockSyncApi @Inject constructor() : SyncApi {

    private val parks = ConcurrentHashMap<String, ParkSyncDto>()
    private val surveys = ConcurrentHashMap<String, SurveySyncDto>()
    private val photos = ConcurrentHashMap<String, PhotoSyncDto>()
    private val _injectedFailures = MutableStateFlow(0)

    /** Number of upcoming requests that will fail (test hook). */
    val injectedFailures: StateFlow<Int> = _injectedFailures

    val storedParkCount: Int get() = parks.size
    val storedSurveyCount: Int get() = surveys.size
    val storedPhotoCount: Int get() = photos.size

    fun failNext(count: Int = 1) {
        _injectedFailures.value = count
    }

    override suspend fun upsertPark(park: ParkSyncDto) {
        simulateRequest()
        parks[park.id] = park // idempotent upsert
    }

    override suspend fun upsertSurvey(survey: SurveySyncDto) {
        simulateRequest()
        surveys[survey.id] = survey // idempotent upsert
    }

    override suspend fun upsertPhoto(photo: PhotoSyncDto) {
        simulateRequest()
        photos[photo.id] = photo // idempotent upsert (metadata only; the mock does not keep file bytes)
    }

    private suspend fun simulateRequest() {
        delay(LATENCY_MS)
        if (_injectedFailures.value > 0) {
            _injectedFailures.update { (it - 1).coerceAtLeast(0) }
            throw IOException("Mock server unavailable (HTTP 503)")
        }
    }

    private companion object {
        const val LATENCY_MS = 500L
    }
}
