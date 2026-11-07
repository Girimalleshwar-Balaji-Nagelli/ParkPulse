package com.parkpulse.app.data.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val sources: Set<@JvmSuppressWildcards SyncSource>,
    network: NetworkMonitor,
) {
    private enum class RunResult { SUCCESS, FAILED, BUSY }

    // Own scope: a sync keeps running if the user leaves the screen that started it.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val runLock = Mutex()

    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    val state: StateFlow<SyncState> = _state.asStateFlow()

    val isOnline: StateFlow<Boolean> =
        network.isOnline.distinctUntilChanged().stateIn(scope, SharingStarted.Eagerly, false)

    val pendingCount: Flow<Int> =
        combine(sources.map { it.observePendingCount() }) { counts -> counts.sum() }

    init {
        // Connectivity (re)gained -> sync automatically, with limited backoff retries.
        scope.launch {
            isOnline.filter { it }.collect { autoSync() }
        }
    }

    /** Manual "Sync Now". Fire-and-forget; observe [state]. */
    fun requestSync() {
        scope.launch { runSync() }
    }

    private suspend fun autoSync() {
        if (pendingNow() == 0) return
        var backoffMs = FIRST_BACKOFF_MS
        for (attempt in 1..MAX_AUTO_ATTEMPTS) {
            if (!isOnline.value) return // wait for the next connectivity event
            if (runSync() != RunResult.FAILED) return
            if (attempt < MAX_AUTO_ATTEMPTS) {
                delay(backoffMs)
                backoffMs *= 2
            }
        }
    }

    private suspend fun pendingNow(): Int = sources.sumOf { it.pendingCountNow() }

    private suspend fun runSync(): RunResult {
        if (!runLock.tryLock()) return RunResult.BUSY // never two syncs at once
        try {
            val total = pendingNow()
            if (total == 0) {
                _state.value = SyncState.Success(0, System.currentTimeMillis())
                return RunResult.SUCCESS
            }
            if (!isOnline.value) {
                _state.value = SyncState.Failed(
                    "No internet. Your data is saved on this device and will sync when you are back online.",
                    total,
                )
                return RunResult.FAILED
            }

            _state.value = SyncState.Running(0, total)
            var done = 0
            var remaining = 0
            var error: String? = null
            for (source in sources) {
                val outcome = source.pushPending {
                    done++
                    _state.value = SyncState.Running(done, total)
                }
                if (outcome.error != null) {
                    remaining += outcome.failed
                    if (error == null) error = "${source.label}: ${outcome.error}"
                }
            }

            return if (error == null) {
                _state.value = SyncState.Success(done, System.currentTimeMillis())
                RunResult.SUCCESS
            } else {
                _state.value = SyncState.Failed(error, remaining)
                RunResult.FAILED
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.value = SyncState.Failed(e.message ?: "Unexpected sync error", pendingNow())
            return RunResult.FAILED
        } finally {
            runLock.unlock()
        }
    }

    private companion object {
        const val MAX_AUTO_ATTEMPTS = 3
        const val FIRST_BACKOFF_MS = 2_000L
    }
}