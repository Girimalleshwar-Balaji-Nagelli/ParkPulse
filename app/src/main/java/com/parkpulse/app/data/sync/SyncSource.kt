package com.parkpulse.app.data.sync

import kotlinx.coroutines.flow.Flow

/**
 * One syncable record type. Parks implement it today; surveys and photo records plug in by
 * adding another implementation bound @IntoSet in SyncModule. SyncManager needs no changes.
 */
interface SyncSource {
    val label: String
    fun observePendingCount(): Flow<Int>
    suspend fun pendingCountNow(): Int

    /**
     * Pushes pending records one by one, marking each SYNCED only after the backend accepted it.
     * Stops at the first failure. Never deletes or modifies local data other than the status flag.
     */
    suspend fun pushPending(onRecordSynced: () -> Unit): SyncOutcome
}