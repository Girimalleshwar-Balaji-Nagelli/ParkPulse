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
 * the client-generated park id, so a retried request overwrites instead of duplicating.
 */
@Singleton
class MockSyncApi @Inject constructor() : SyncApi {

    private val store = ConcurrentHashMap<String, ParkSyncDto>()
    private val _injectedFailures = MutableStateFlow(0)

    /** Number of upcoming requests that will fail (test hook). */
    val injectedFailures: StateFlow<Int> = _injectedFailures

    val storedParkCount: Int get() = store.size

    fun failNext(count: Int = 1) {
        _injectedFailures.value = count
    }

    override suspend fun upsertPark(park: ParkSyncDto) {
        delay(LATENCY_MS)
        if (_injectedFailures.value > 0) {
            _injectedFailures.update { (it - 1).coerceAtLeast(0) }
            throw IOException("Mock server unavailable (HTTP 503)")
        }
        store[park.id] = park // idempotent upsert
    }

    private companion object {
        const val LATENCY_MS = 500L
    }
}